package com.gildedrose.persistence

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.gildedrose.domain.*
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.withException
import com.gildedrose.persistence.StockListLoadingError.*
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val lastModifiedHeader = "# LastModified:"

context(IO, Raise<IOError>)
fun StockList.saveTo(file: File) = withException(::IOError) {
    file.writer().buffered().use { writer ->
        this.toLines().forEach(writer::appendLine)
    }
}

fun StockList.toLines(): Sequence<String> = sequenceOf("$lastModifiedHeader $lastModified") +
    items.map { it.toLine() }

context(IO, Raise<StockListLoadingError>)
fun File.loadItems(): StockList = withException(::IOError) {
    useLines { lines -> lines.toStockList() }
}

context(Raise<StockListLoadingError>)
fun Sequence<String>.toStockList(): StockList {
    val (header, body) = partition { it.startsWith("#") }
    val items: List<Item> = body.map { line -> line.toItem() }
    return StockList(
        lastModified = lastModifiedFrom(header) ?: Instant.EPOCH,
        items = items
    )
}

private fun Item.toLine() = "$id\t$name\t${sellByDate ?: ""}\t$quality"

context(Raise<CouldntParseLastModified>)
private fun lastModifiedFrom(
    header: List<String>
): Instant? = header
    .lastOrNull { it.startsWith(lastModifiedHeader) }
    ?.substring(lastModifiedHeader.length)
    ?.trim()
    ?.toInstant()

context(Raise<CouldntParseLastModified>)
private fun String.toInstant(): Instant =
    withException(::CouldntParseLastModifiedHeader) {
        Instant.parse(this)
    }

context(Raise<StockListLoadingError>)
private fun String.toItem(): Item {
    val parts: List<String> = this.split('\t')
    ensure(parts.size >= 4) { NotEnoughFields(this) }
    return itemWithIdFrom(parts)
}

context(Raise<StockListLoadingError>)
private fun String.itemWithIdFrom(parts: List<String>): Item {
    val id = ID<Item>(parts[0]) ?: raise(BlankID(this))
    val name = NonBlankString(parts[1]) ?: raise(BlankName(this))
    val sellByDate = parts[2].toLocalDate(this)
    val quality = parts[3].toIntOrNull()?.let { Quality(it) } ?: raise(CouldntParseQuality(this))
    return Item(id, name, sellByDate, quality)
}

context(Raise<CouldntParseSellBy>)
private fun String.toLocalDate(line: String): LocalDate? =
    withException({ _: DateTimeParseException -> CouldntParseSellBy(line) }) {
        if (this.isBlank()) null else LocalDate.parse(this)
    }

private fun IOError(exception: IOException): IOError = IOError(exception.message ?: "no message")

private fun CouldntParseLastModifiedHeader(exception: DateTimeParseException): CouldntParseLastModified =
    CouldntParseLastModified("Could not parse LastModified header: " + exception.message)
