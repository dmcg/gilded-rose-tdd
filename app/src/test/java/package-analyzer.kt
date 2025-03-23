import io.github.classgraph.ClassGraph
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.util.regex.Pattern
import kotlin.test.assertEquals


@Disabled("Run by hand only")
class PackageAnalyzer {
    private val includeExternalDependencies = true

    @Test
    fun main() {
        val outputDot = File.createTempFile("output", ".dot")
        val outputPng = File("packages.png")
        val srcDirPath = File("./src/main")
        val fileTree = walkTree(srcDirPath, includeExternalDependencies = includeExternalDependencies)

        generateDotFile(outputDot, fileTree)
        createImageOutput(outputDot, outputPng)
        checkDotFile(outputDot)
    }
}

private fun walkTree(srcDirPath: File, includeExternalDependencies: Boolean): Map<String, Set<String>> =
    srcDirPath.walkTopDown()
        .filter { it.isFile && it.path.endsWith(".kt") }
        .map { file -> file.extractPackageAndImports(includeExternalDependencies) }
        .groupingBy { it.first }
        .fold(emptySet()) { acc, (_, importedPackages) -> acc union importedPackages }

private val packagePattern = Pattern.compile("^package\\s+(.*)$")
private val importPattern = Pattern.compile("^import\\s+(.*)$")

private fun File.extractPackageAndImports(includeExternal: Boolean): Pair<String, Set<String>> {
    val lines = this.readLines()
    val matcher = packagePattern.matcher(lines.firstOrNull() ?: "")
    val packageName = if (matcher.find()) matcher.group(1) else "<root>"

    val importedPackages = lines.drop(1)
        .mapNotNull { line ->
            val importMatcher = importPattern.matcher(line)
            if (importMatcher.find()) {
                val importedPackage = packageNameFrom(importMatcher.group(1).split('.'))
                importedPackage?.let { nameFor(it, includeExternal = includeExternal) }
            } else null
        }.toSet()

    return Pair(packageName, importedPackages)
}

private fun nameFor(importedPackage: String, includeExternal: Boolean) =
    if (includeExternal) {
        when {
            importedPackage.startsWith("com.fasterxml.jackson") -> "jackson"
            importedPackage.startsWith("org.apache.hc") -> "apache-hc"
            importedPackage.startsWith("org.http4k.core") -> "org.http4k.core"
            importedPackage.startsWith("org.jooq") -> "jooq"
            else -> importedPackage
        }
    } else {
        if (importedPackage.startsWith("com.gildedrose"))
            importedPackage
        else
            null
    }

private fun packageNameFrom(components: List<String>): String? {
    var current = components
    while (current.isNotEmpty()) {
        val packageName = current.joinToString(".")
        if (packageName.isValidPackageName())
            return packageName
        current = current.dropLast(1)
    }
    return null
}

private val allPackages = ClassGraph().enableClassInfo()
    .scan().allClasses.map { it.packageName }.toSet()

private fun String.isValidPackageName(): Boolean = allPackages.contains(this)

private fun checkDotFile(dotFile: File) {
    val dotFileContents = dotFile.readText()
    val approvedFileContents = File("src/test/resources/package-dotfile.approved").readText()
    assertEquals(approvedFileContents, dotFileContents)
}

private fun generateDotFile(dotFile: File, fileTree: Map<String, Set<String>>) {
    dotFile.printWriter().use { out ->
        out.println("digraph KotlinPackageDependencies {")
        out.println("rankdir=TB;")
        out.println("node [shape=box];")
        out.println("\"com.gildedrose\" [rank=min];")

        fileTree.forEach { (pkg, imports) ->
            imports.forEach { importedFull ->
                // For the dot file, we need to use the full import path
                out.println("\"$pkg\" -> \"$importedFull\";")
            }
        }

        out.println("}")
    }
}

private fun createImageOutput(dotFile: File, outputPngFile: File) {
    Runtime.getRuntime().exec(arrayOf("dot", "-Tpng", dotFile.path, "-o", outputPngFile.path))
}
