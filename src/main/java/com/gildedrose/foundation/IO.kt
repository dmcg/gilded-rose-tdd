package com.gildedrose.foundation

object IO

inline fun <R> runIO(block: context(IO) () -> R) = block(IO)

context(C) fun <C> magic() : C = this@C
