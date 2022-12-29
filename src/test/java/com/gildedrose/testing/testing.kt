package com.gildedrose.testing

fun <E> Collection<E>.only(): E =
    when {
        this.size != 1 -> error("Expected one item, got $this")
        else -> this.first()
    }
