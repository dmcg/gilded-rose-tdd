package com.gildedrose.domain

import java.text.NumberFormat
import java.util.*

@JvmInline
value class Price
private constructor(val pence: Long)  {
    companion object {
        operator fun invoke(value: Long): Price? =
            if (value >= 0) Price(value)
            else null

        // This must be a function as NumberFormat is not thread safe
        private fun getNumberFormat() = NumberFormat.getCurrencyInstance(Locale.UK)
    }

    override fun toString(): String = getNumberFormat().format(pence / 100.0)
}
