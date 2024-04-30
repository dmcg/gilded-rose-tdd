package com.gildedrose.domain

@JvmInline
value class Quality(val value: NonNegativeInt) {
    val valueInt get() = value.value

    companion object {
        val ZERO: Quality = Quality(0)!!

        operator fun invoke(value: Int) =
            NonNegativeInt(value)?.let(::Quality)
    }

    operator fun minus(value: Int): Quality =
        plus(-value)

    operator fun plus(value: Int): Quality {
        val qualityCap = this.value.value.coerceAtLeast(50)
        return Quality((this.value + value).coerceIn(0, qualityCap))
            ?: error("tried to create a negative int")
    }

    override fun toString() = value.toString()
}
