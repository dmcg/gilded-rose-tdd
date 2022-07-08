package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.domain.Item
import com.gildedrose.domain.Item.Companion.invoke
import dev.forkhandles.result4k.*
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

fun File.loadItems(): Result4k<StockList, Nothing?> = useLines { lines ->
    lines.toStockList()
}

fun Sequence<String>.toStockList(): Result4k<StockList, Nothing?> {
    val (header, body) = partition { it.startsWith("#") }
    val items: List<Item> = body.map { line -> line.toItem().onFailure { return Failure(null) } }
    return Success(
        StockList(
            lastModified = lastModifiedFrom(header) ?: Instant.EPOCH,
            items = items
        )
    )
}

private fun Item.toLine() = "$name\t${sellByDate ?: ""}\t$quality"

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

private fun String.toItem(): Result4k<Item, Nothing?> {
    val parts: List<String> = this.split('\t')
    return Item(
        name = parts[0],
        sellByDate = parts[1].toLocalDate(),
        quality = parts[2].toInt()
    ).mapFailure { null }
}

private fun String.toLocalDate() = if (this.isBlank()) null else LocalDate.parse(this)
