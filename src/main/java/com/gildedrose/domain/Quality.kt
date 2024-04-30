package com.gildedrose.domain

@JvmInline
value class Quality(val value: NonNegativeInt) {
    val valueInt get() = value.value

    companion object {
        val ZERO: Quality = Quality(0)!!

        operator fun invoke(value: Int): Quality? =
            NonNegativeInt(value)?.let(::Quality)
    }

    operator fun minus(value: Int): Quality =
        plus(-value)

    operator fun plus(value: Int): Quality {
        val qualityCap = valueInt.coerceAtLeast(50)
        return Quality((valueInt + value).coerceIn(0, qualityCap))
            ?: error("tried to create a negative int")
    }

    override fun toString() = value.toString()
}
