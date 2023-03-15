package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import com.gildedrose.theory.Data

@Data
@JvmInline
value class ID<@Suppress("unused") T>(val value: NonBlankString) {

    companion object {
        @Calculation
        operator fun <T> invoke(value: String): ID<T>? =
            NonBlankString(value)?.let { ID<T>(it) }
    }

    @Calculation
    override fun toString() = value.toString()
}
