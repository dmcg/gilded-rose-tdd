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

fun subtract(quality: Quality, rhs: Int): Quality =
    add(quality, -rhs)

fun add(quality: Quality, rhs: Int): Quality {
    val qualityCap = quality.value.value.coerceAtLeast(50)
    return Quality((quality.value + rhs).coerceIn(0, qualityCap))
        ?: error("tried to create a negative int")
}
