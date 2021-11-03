package com.gildedrose

import java.io.File
import java.time.LocalDate

fun List<Item>.saveTo(file: File) {
    file.writer().buffered().use { writer ->
        forEach { item ->
            writer.appendLine(item.toLine())
        }
    }
}

private fun Item.toLine() = "$name\t$sellByDate\t$quality"

fun File.loadItems(): List<Item> = useLines { lines ->
    lines.map { line -> line.toItem() }.toList()
}

private fun String.toItem(): Item {
    val parts: List<String> = this.split('\t')
    return Item(
        name = parts[0],
        sellByDate = LocalDate.parse(parts[1]),
        quality = parts[2].toUInt()
    )
}
