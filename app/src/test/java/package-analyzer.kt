import java.io.File
import java.util.regex.Pattern
import kotlin.test.assertEquals

private val packagePattern = Pattern.compile("^package\\s+(.*)$")
private val importPattern = Pattern.compile("^import\\s+(.*)$")

fun main() {
    val outputDot = File.createTempFile("output", ".dot")
    val outputPng = File("packages.png")
    val srcDirPath = File("./src/main")
    val fileTree = walkTree(srcDirPath)

    generateDotFile(outputDot, fileTree)
    createImageOutput(outputDot, outputPng)
    checkDotFile(outputDot)
}

private fun walkTree(srcDirPath: File): Map<String, Set<String>> =
    srcDirPath.walkTopDown()
        .filter { it.isFile && it.path.endsWith(".kt") }
        .map { file -> file.extractPackageAndImports() }
        .groupingBy { it.first }
        .fold(emptySet()) { acc, (_, importedPackages) -> acc union importedPackages }

private fun File.extractPackageAndImports(): Pair<String, Set<String>> {
    val lines = this.readLines()
    val matcher = packagePattern.matcher(lines.firstOrNull() ?: "")
    val packageName = if (matcher.find()) matcher.group(1) else "<root>"

    val importedPackages = lines.drop(1)
        .mapNotNull { line ->
            val importMatcher = importPattern.matcher(line)
            if (importMatcher.find()) {
                val importedPackage = importMatcher.group(1).split('.').dropLast(1).joinToString(".")
                if (importedPackage.startsWith("com.gildedrose")) importedPackage else null
            } else null
        }.toSet()

    return Pair(packageName, importedPackages)
}

private fun checkDotFile(dotFile: File) {
    val dotFileContents = dotFile.readLines()
    val approvedFileContents = File("src/test/resources/package-dotfile.approved").readLines()
    assertEquals(approvedFileContents, dotFileContents)
}

private fun generateDotFile(dotFile: File, fileTree: Map<String, Set<String>>) {
    dotFile.printWriter().use { out ->
        out.println("digraph KotlinPackageDependencies {")
        out.println("rankdir=TB;")
        out.println("node [shape=box];")
        out.println("\"com.gildedrose\" [rank=min];")

        fileTree.forEach { (pkg, imports) ->
            imports.forEach { importedPkg -> out.println("\"$pkg\" -> \"$importedPkg\";") }
        }

        out.println("}")
    }
}

private fun createImageOutput(dotFile: File, outputPngFile: File) {
    Runtime.getRuntime().exec(arrayOf("dot", "-Tpng", dotFile.path, "-o", outputPngFile.path))
}
