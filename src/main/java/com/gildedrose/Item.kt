package com.gildedrose

import java.time.LocalDate

data class Item(
    val name: String,
    val sellByDate: LocalDate,
    val quality: Int
) {
    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }

    fun updatedBy(days: Int) =
        copy(quality = (quality - days).coerceAtLeast(0))

}
