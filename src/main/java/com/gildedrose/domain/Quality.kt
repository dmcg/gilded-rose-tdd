package com.gildedrose.domain

@JvmInline
value class Quality(
    val value: NonNegativeInt
) {
    val valueInt get() = value.value

    companion object {
        val ZERO: Quality = Quality(0)!!

        operator fun invoke(value: Int): Quality? {
            val wrapped = NonNegativeInt(value)
                .let { wrapped ->
                    if (wrapped != null) Quality(wrapped) else null
                }
            return wrapped
        }
    }

    override fun toString() = value.toString()
}
