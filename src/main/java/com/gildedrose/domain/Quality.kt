package com.gildedrose.domain

@JvmInline
value class Quality(
    val value: NonNegativeInt
) {
    val valueInt get() = value.value

    companion object {
        val ZERO: Quality = Quality(0)!!

        operator fun invoke(value: Int): Quality? =
            NonNegativeInt(value)?.let { Quality(it) }
    }

    override fun toString() = value.toString()
}
