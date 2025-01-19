package com.gildedrose.foundation

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> Iterable<T>.parallelMapCoroutines(f: suspend (T) -> R) =
    coroutineScope {
        map {
            async { f(it) }
        }.awaitAll()
    }
