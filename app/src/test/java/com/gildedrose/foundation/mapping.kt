package com.gildedrose.foundation

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

fun <T, R> List<T>.parallelMapStream(f: (T) -> R) =
    stream().parallel().map(f).toList()

fun <T, R> List<T>.parallelMapStream(
    pool: ForkJoinPool,
    f: (T) -> R
): List<R> =
    pool.submit(Callable { parallelMapStream(f) }).get()

fun <T, R> Iterable<T>.parallelMapThreads(f: (T) -> R): List<R> =
    this.map {
        val result = AtomicReference<Result4k<R, Throwable>>()
        thread {
            try {
                result.set(Success(f(it)))
            } catch (t: Throwable) {
                result.set(Failure(t))
            }
        } to result
    }.map { (thread, result) ->
        thread.join()
        result.get().orThrow()
    }

fun <T, R> Iterable<T>.parallelMapThreadPool(threadPool: ExecutorService, f: (T) -> R) =
    this.map {
        threadPool.submit(Callable { f(it) })
    }.map { future ->
        future.get()
    }
