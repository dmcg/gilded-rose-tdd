package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import com.gildedrose.theory.Data
import java.text.NumberFormat
import java.util.*

@Data
@JvmInline
value class Price
private constructor(val pence: Long)  {
    companion object {
        @Calculation
        operator fun invoke(value: Long): Price? =
            if (value >= 0) Price(value)
            else null

        private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK)
    }

    @Calculation
    override fun toString(): String = numberFormat.format(pence / 100.0)
}
