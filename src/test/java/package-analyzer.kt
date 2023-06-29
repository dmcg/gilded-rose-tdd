
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Matcher
import java.util.regex.Pattern

fun main() {
    val fileTree = mutableMapOf<String, MutableSet<String>>()
    val srcDirPath = Paths.get("./src/main")
    walkTree(fileTree, srcDirPath)

    generateDotFile(fileTree)

    createImageOutput()
}

private fun walkTree(fileTree: MutableMap<String, MutableSet<String>>, srcDirPath: Path) {
    Files.walkFileTree(srcDirPath, object : FileVisitor<Path> {
        private val packagePattern = Pattern.compile("^package\\s+(.*)$")
        private val importPattern = Pattern.compile("^import\\s+(.*)$")

        override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?) = FileVisitResult.CONTINUE

        override fun visitFileFailed(file: Path?, exc: IOException?) = FileVisitResult.CONTINUE

        override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            file?.getKotlinFilePackages(fileTree, packagePattern, importPattern)

            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path?, exc: IOException?) = FileVisitResult.CONTINUE
    })
}

private fun Path.getKotlinFilePackages(fileTree: MutableMap<String, MutableSet<String>>, packagePattern: Pattern, importPattern: Pattern) {
    if (this.toString().endsWith(".kt")) {
        var currentPackage: String? = null
        this.toFile().readLines().forEach { line ->
            var matcher = packagePattern.matcher(line)
            currentPackage = currentPackage.processPackage(matcher)

            matcher = importPattern.matcher(line)
            matcher.processImport(currentPackage, fileTree)
        }
    }
}

private fun String?.processPackage(matcher: Matcher): String? {
    return if (matcher.find()) {
        val matchedPkg = matcher.group(1)
        if (matchedPkg.startsWith("com.gildedrose")) matchedPkg else this
    } else this
}

private fun Matcher.processImport(targetPackage: String?, fileTree: MutableMap<String, MutableSet<String>>) {
    if (targetPackage != null && this.find()) {
        val importedPackage = this.group(1).split('.').dropLast(1).joinToString(".")
        if (importedPackage.startsWith("com.gildedrose")) fileTree.getOrPut(targetPackage) { mutableSetOf() }.add(importedPackage)
    }
}

private fun generateDotFile(fileTree: MutableMap<String, MutableSet<String>>) {
    File("output.dot").printWriter().use { out ->
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

private fun createImageOutput() {
    try {
        Runtime.getRuntime().exec("dot -Tpng output.dot -o output.png")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
