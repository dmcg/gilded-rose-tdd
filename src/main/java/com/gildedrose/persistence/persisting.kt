package com.gildedrose.persistence

import com.gildedrose.domain.*
import com.gildedrose.persistence.StockListLoadingError.*
import com.gildedrose.theory.Action
import com.gildedrose.theory.Calculation
import dev.forkhandles.result4k.*
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val lastModifiedHeader = "# LastModified:"

@Action
@kotlin.jvm.Throws(IOException::class)
fun StockList.saveTo(file: File) {
    file.writer().buffered().use { writer ->
        this.toLines().forEach(writer::appendLine)
    }
}

@Calculation
fun StockList.toLines(): Sequence<String> = sequenceOf("$lastModifiedHeader $lastModified") +
    items.map { it.toLine() }

@Action
fun File.loadItems(): Result4k<StockList, StockListLoadingError> =
    try {
        useLines { lines ->
            lines.toStockList()
        }
    } catch (x: IOException) {
        Failure(IO(x.message ?: "no message"))
    }

@Calculation
fun Sequence<String>.toStockList(): Result4k<StockList, StockListLoadingError> {
    val (header, body) = partition { it.startsWith("#") }
    val items: List<Item> = body.map { line -> line.toItem().onFailure { return it } }
    return lastModifiedFrom(header).map { lastModified ->
        StockList(
            lastModified = lastModified ?: Instant.EPOCH,
            items = items
        )
    }
}

@Calculation
private fun Item.toLine() = "$id\t$name\t${sellByDate ?: ""}\t$quality"

@Calculation
private fun lastModifiedFrom(
    header: List<String>
): Result<Instant?, CouldntParseLastModified> = header
    .lastOrNull { it.startsWith(lastModifiedHeader) }
    ?.substring(lastModifiedHeader.length)
    ?.trim()
    ?.toInstant() ?: Success(null)

@Calculation
private fun String.toInstant(): Result<Instant, CouldntParseLastModified> =
    try {
        Success(Instant.parse(this))
    } catch (x: DateTimeParseException) {
        Failure(CouldntParseLastModified("Could not parse LastModified header: " + x.message))
    }

@Calculation
private fun String.toItem(): Result4k<Item, StockListLoadingError> {
    val parts: List<String> = this.split('\t')
    return when {
        parts.size < 4 -> Failure(NotEnoughFields(this))
        else -> itemWithIdFrom(parts)
    }
}

@Calculation
private fun String.itemWithIdFrom(parts: List<String>): Result<Item, StockListLoadingError> {
    val id = ID<Item>(parts[0]) ?: return Failure(BlankID(this))
    val name = NonBlankString(parts[1]) ?: return Failure(BlankName(this))
    val sellByDate = parts[2].toLocalDate(this).onFailure { return it }
    val quality = parts[3].toIntOrNull()?.let { Quality(it) } ?: return Failure(CouldntParseQuality(this))
    return Success(Item(id, name, sellByDate, quality))
}

@Calculation
private fun String.toLocalDate(line: String): Result<LocalDate?, CouldntParseSellBy> =
    try {
        Success(if (this.isBlank()) null else LocalDate.parse(this))
    } catch (x: DateTimeParseException) {
        Failure(CouldntParseSellBy(line))
    }

