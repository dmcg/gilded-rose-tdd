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

fun add(quality: Quality, value: Int): Quality {
    val qualityCap = quality.value.value.coerceAtLeast(50)
    return Quality((quality.value + value).coerceIn(0, qualityCap))
        ?: error("tried to create a negative int")
}

fun subtract(quality: Quality, value: Int): Quality =
    add(quality, -value)