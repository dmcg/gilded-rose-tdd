package com.gildedrose.domain

import java.text.NumberFormat

@JvmInline
value class Price
private constructor(private val value: Long)  {
    companion object {
        operator fun invoke(value: Long): Price? =
            if (value >= 0) Price(value)
            else null

        private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
    }

    override fun toString(): String = numberFormat.format(value / 100.0)
}
