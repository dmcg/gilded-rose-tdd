package com.gildedrose.foundation

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.streams.toList
import kotlin.system.measureTimeMillis


@TestMethodOrder(MethodOrderer.MethodName::class)
@EnabledIfSystemProperty(named = "run-slow-tests", matches = "true")
class ParallelMapTests {

    /* Note that this attempts to warm up HotSpot by running the non-parallel
     * version first, and GCs between runs, but the results are subject to a
     * lot of noise nonetheless.
     */

    private val listSize = 100
    private val threadPool = ForkJoinPool(50)
    private val sleepMs: Long = 25
    private lateinit var testInfo: TestInfo

    @BeforeEach
    fun rememberName(testInfo: TestInfo) {
        this.testInfo = testInfo
    }

    @AfterEach
    fun shutDown() {
        threadPool.shutdown()
        System.gc()
        System.runFinalization()
    }

    @Test
    fun `01 kotlin version`() {
        val parallelism = checkAndTime {
            map(it)
        }.also(::printDouble)
        assertTrue(parallelism <= 1.0)
    }

    @Test
    fun `02 parallel stream version`() {
        val parallelism = checkAndTime {
            parallelMapStream(it)
        }.also(::printDouble)
        assertTrue(parallelism > 1)
    }

    @Test
    fun `03 parallel stream forkJoinPool version`() {
        val parallelism = checkAndTime {
            parallelMapStream(threadPool, it)
        }.also(::printDouble)
        assertTrue(parallelism > 1)
    }

    @Test
    fun `04 threads version`() {
        val parallelism = checkAndTime {
            parallelMapThreads(it)
        }.also(::printDouble)
        assertTrue(parallelism > 1)
    }

    @Test
    fun `05 threadPool version`() {
        val parallelism = checkAndTime {
            parallelMapThreadPool(threadPool, it)
        }.also(::printDouble)
        assertTrue(parallelism > 1)
    }

    @Test
    fun `06 coroutines version`() {
        // Coroutines run quicker when warmed up
        runBlocking(threadPool.asCoroutineDispatcher()) {
            coroutineScope {
                for (i in 1..1000) {
                    async { 1 }.await()
                }
            }
        }
        val parallelism = checkAndTime {
            runBlocking(threadPool.asCoroutineDispatcher()) {
                parallelMapCoroutines(it)
            }
        }.also(::printDouble)
        assertTrue(parallelism > 1)
    }

    @Test
    fun `07 coroutines delay version`() {
        val parallelism = checkAndTime {
            runBlocking {
                parallelMapCoroutines {
                    delay(sleepMs)
                    it.length
                }
            }
        }.also(::printDouble)
        assertTrue(parallelism > 1)
    }

    private fun checkAndTime(
        mapFunction: List<String>.((String) -> Int) -> List<Int>
    ): Double {
        val totalSleepMs = listSize * sleepMs
        val input = (1..listSize).map { it.toString() }
        val output: List<Int>
        val timeMs = measureTimeMillis {
            output = input.mapFunction { Thread.sleep(sleepMs); it.length }
        }
        assertEquals(output.size, input.size)
        assertEquals(1, output[0])
        assertEquals(1, output[8])
        assertEquals(2, output[9])
        assertEquals(2, output[98])
        assertEquals(3, output[99])
        return totalSleepMs / timeMs.toDouble()
    }

    private fun printDouble(value: Double) {
        println("${this.testInfo.displayName} : ${"%.1f".format(value)}")
    }
}

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

fun <T, R> List<T>.parallelMapThreadPool(threadPool: ExecutorService, f: (T) -> R) =
    this.map {
        threadPool.submit(Callable { f(it) })
    }.map { future ->
        future.get()
    }

suspend fun <T, R> List<T>.parallelMapCoroutines(f: suspend (T) -> R) =
    coroutineScope {
        map {
            async { f(it) }
        }.awaitAll()
    }

