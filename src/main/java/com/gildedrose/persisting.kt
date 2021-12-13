package com.gildedrose

import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val lastModifiedHeader = "# LastModified:"

fun StockList.saveTo(file: File) {
    file.writer().buffered().use { writer ->
        this.toLines().forEach(writer::appendLine)
    }
}

fun StockList.toLines(): Sequence<String> = sequenceOf("$lastModifiedHeader $lastModified") +
    items.map { it.toLine() }


fun File.loadItems(
    defaultLastModified: Instant = Instant.now()
): StockList = useLines { lines ->
    lines.toStockList(defaultLastModified)
}

fun Sequence<String>.toStockList(
    defaultLastModified: Instant
): StockList {
    val (header, body) = partition { it.startsWith("#") }
    return StockList(
        lastModified = lastModifiedFrom(header) ?: defaultLastModified,
        items = body.map { line -> line.toItem() }.toList()
    )
}

private fun Item.toLine() = "$name\t$sellByDate\t$quality"


private fun lastModifiedFrom(
    header: List<String>
) = header
    .lastOrNull { it.startsWith(lastModifiedHeader) }
    ?.substring(lastModifiedHeader.length)
    ?.trim()
    ?.toInstant()

private fun String.toInstant() = try {
    Instant.parse(this)
} catch (x: DateTimeParseException) {
    throw IOException("Could not parse LastModified header: " + x.message)
}

private fun String.toItem(): Item {
    val parts: List<String> = this.split('\t')
    return Item(
        name = parts[0],
        sellByDate = LocalDate.parse(parts[1]),
        quality = parts[2].toUInt()
    )
}
