package com.gildedrose.domain

import java.text.NumberFormat
import java.util.Locale

@JvmInline
value class Price
private constructor(val pence: Long)  {
    companion object {
        operator fun invoke(value: Long): Price? =
            if (value >= 0) Price(value)
            else null

        private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK)
    }

    override fun toString(): String = numberFormat.format(pence / 100.0)
}
