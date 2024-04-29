package com.gildedrose.domain

@JvmInline
value class Quality(
    val value: NonNegativeInt
) {
    val valueInt get() = value.value

    companion object {
        val ZERO: Quality = Quality(0)!!

        operator fun invoke(value: Int) =
            NonNegativeInt(value)?.let(::Quality)
    }

    override fun toString() = value.toString()
}
