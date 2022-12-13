package com.gildedrose.foundation

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.streams.toList

fun <T, R> List<T>.parallelMapStream(f: (T) -> R) =
    stream().parallel().map(f).toList()


fun <T, R> List<T>.parallelMapStream(
    pool: ForkJoinPool,
    f: (T) -> R
): List<R> =
    pool.submit(Callable { parallelMapStream(f) }).get()

fun <T, R> Iterable<T>.parallelMapThreads(f: (T) -> R): List<R> =
    this.map {
        val result = AtomicReference<R>()
        thread {
            result.set(f(it))
        } to result
    }.map { (thread, result) ->
        thread.join()
        result.get()
    }

fun <T, R> Iterable<T>.parallelMapThreadPool(threadPool: ExecutorService, f: (T) -> R) =
    this.map {
        threadPool.submit(Callable { f(it) })
    }.map { future ->
        future.get()
    }

suspend fun <T, R> Iterable<T>.parallelMapCoroutines(f: suspend (T) -> R) =
    coroutineScope {
        map {
            async { f(it) }
        }.awaitAll()
    }

