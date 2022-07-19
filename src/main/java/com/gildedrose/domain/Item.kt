package com.gildedrose.domain

import com.gildedrose.domain.ItemCreationError.*
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.time.LocalDate

@Suppress("DataClassPrivateConstructor") // protected by requires in init
data class Item private constructor(
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Int,
    private val type: ItemType
) {
    companion object {
        operator fun invoke(
            name: NonBlankString,
            sellByDate: LocalDate?,
            quality: Int,
        ): Result4k<Item, ItemCreationError> {
            return try {
                Success(Item(name, sellByDate, quality, typeFor(sellByDate, name)))
            } catch (x: ItemCreationException) {
                Failure(x.error)
            }
        }
    }

    init {
        if (quality < 0) {
            throw ItemCreationException(NegativeQuality(quality))
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }

    fun withQuality(quality: Int): Item {
        val qualityCap = this.quality.coerceAtLeast(50)
        return copy(quality = quality.coerceIn(0, qualityCap))
    }
}

sealed interface ItemCreationError {
    @Suppress("unused")
    val errorName: String get() = this::class.simpleName ?: "Error Name Unknown"

    data class NegativeQuality(val actual: Int) : ItemCreationError

    class ItemCreationException(val error: ItemCreationError) : Exception()
}


