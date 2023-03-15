package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import com.gildedrose.theory.Data

@Data
@JvmInline
value class NonBlankString
private constructor(val value: String) : CharSequence by value {
    companion object {
        @Calculation
        operator fun invoke(value: String): NonBlankString? =
            if (value.isNotBlank()) NonBlankString(value)
            else null
    }

    init {
        require(value.isNotBlank())
    }

    @Calculation
    override fun toString() = value
}
