import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals


@Disabled("Run by hand only")
class PackageAnalyzer {
    private val includeExternalDependencies = false
    private val outputDot = File.createTempFile("output", ".dot")

    @Test
    fun main() {
        val scanResult = ClassGraph().enableInterClassDependencies().scan()
        val dependencies: Map<ClassInfo, List<ClassInfo>> = scanResult.classDependencyMap
        val root = scanResult.getClassInfo("MainKt")
        val packageDependencies = walkClassTree(root, dependencies, includeExternalDependencies)
        generateDotFile(outputDot, packageDependencies)
        createImageOutput(outputDot, File("packages.png"))
        checkDotFile(outputDot)
    }
}

private fun walkClassTree(
    root: ClassInfo,
    dependencies: Map<ClassInfo, List<ClassInfo>>,
    includeExternalDependencies: Boolean,
    destination: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    visited: MutableSet<ClassInfo> = mutableSetOf(),
): MutableMap<String, MutableSet<String>> {
    if (visited.contains(root))
        return destination
    else
        visited.add(root)
    val rootPackageName = nameFor(root.packageName, includeExternalDependencies)
        ?: return destination

    val classDependencies = dependencies[root] ?: emptyList()
    val packageDependencies = classDependencies
        .mapNotNull {
            nameFor(it.packageName, includeExternalDependencies)
        }.toSet()
    destination.getOrPut(rootPackageName) { mutableSetOf() }.addAll(packageDependencies)
    classDependencies
        .filter { it.packageName.startsWith("com.gildedrose") }
        .forEach { walkClassTree(it, dependencies, includeExternalDependencies, destination, visited) }
    return destination
}

private fun nameFor(importedPackage: String, includeExternal: Boolean): String? =
    when {
        importedPackage.isEmpty() -> "<root>"
        importedPackage.startsWith("com.gildedrose.db") -> "com.gildedrose.db.*"
        includeExternal -> {
            when {
                importedPackage.startsWith("com.fasterxml.jackson") -> "jackson"
                importedPackage.startsWith("org.apache.hc") -> "apache-hc"
                importedPackage.startsWith("org.http4k.core") -> "org.http4k.core"
                importedPackage.startsWith("org.jooq") -> "jooq"
                importedPackage.startsWith("kotlinx.html") -> "kotlinx-html"
                importedPackage.startsWith("kotlinx") -> importedPackage
                importedPackage.startsWith("kotlin") -> null
                importedPackage.startsWith("org.jetbrains.annotations") -> null
                else -> importedPackage
            }
        }

        else -> when {
            importedPackage.startsWith("com.gildedrose") -> importedPackage
            else -> null
        }
    }

private fun checkDotFile(dotFile: File) {
    val dotFileContents = dotFile.readText()
    val approvedFileContents = File("src/test/resources/package-dotfile.approved").readText()
    assertEquals(approvedFileContents, dotFileContents)
}

private fun generateDotFile(dotFile: File, packagesMap: Map<String, Set<String>>) {
    dotFile.printWriter().use { out ->
        out.println("digraph KotlinPackageDependencies {")
        out.println("rankdir=TB;")
        out.println("node [shape=box];")
        out.println("\"com.gildedrose\" [rank=min];")

        packagesMap.forEach { (packageInfo, packageDependencies) ->
            packageDependencies.forEach { dependency ->
                out.println(""""$packageInfo" -> "$dependency";""")
            }
        }

        out.println("}")
    }
}

private fun createImageOutput(dotFile: File, outputPngFile: File) {
    Runtime.getRuntime().exec(arrayOf("dot", "-Tpng", dotFile.path, "-o", outputPngFile.path))
}
