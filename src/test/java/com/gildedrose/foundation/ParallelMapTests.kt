package com.gildedrose.foundation

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.sqrt
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
        measureAndCheck {
            map(it)
        }.assertThat { it <= 1.0 }
    }

    @RepeatedTest(10, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `02 parallel stream version`() {
        measureAndCheck {
            parallelMapStream(it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(20, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `03 parallel stream forkJoinPool version`() {
        measureAndCheck {
            parallelMapStream(threadPool, it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(20, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `04 threads version`() {
        measureAndCheck {
            parallelMapThreads(it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(20, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `05 threadPool version`() {
        measureAndCheck {
            parallelMapThreadPool(threadPool, it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(20, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `06 coroutines version`() {
        measureAndCheck {
            runBlocking(threadPool.asCoroutineDispatcher()) {
                parallelMapCoroutines(it)
            }
        }.assertThat { it > 1 }
    }

    @RepeatedTest(20, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `07 coroutines delay version`() {
        measureAndCheck {
            runBlocking {
                parallelMapCoroutines {
                    delay(sleepMs)
                    it.length
                }
            }
        }.assertThat { it > 1 }
    }

    private fun measureAndCheck(
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
        val throughput = totalSleepMs / timeMs.toDouble()
        runs.add(testInfo to throughput)
        return throughput
    }

    companion object {
        private val runs = mutableListOf<Pair<TestInfo, Double>>()

        @JvmStatic
        @AfterAll
        fun report() {
            runs.toNameAndThroughput().forEach { (name, throughput) ->
                println("$name : ${throughput.first.toOneDP()} [${throughput.second.toOneDP()}]")
            }
        }
    }
}

private fun List<Pair<TestInfo, Double>>.toNameAndThroughput() = map { (testInfo, throughput) ->
    testInfo.displayName.split("() :: ")[0] to throughput
}.groupBy(
    keySelector = { it.first },
    valueTransform = { it.second }
).map { (name, throughputs) ->
    name to throughputs.culledMeanAndDeviation()
}

private fun <T> T.assertThat(f: (T) -> Boolean): T = this.also {
    assertTrue(f(this))
}

private fun List<Double>.culledMeanAndDeviation(): Pair<Double, Double> = when {
    isEmpty() -> Double.NaN to Double.NaN
    size == 1 || size == 2 -> this.meanAndDeviation()
    else -> sorted().subList(1, size - 1).meanAndDeviation()
}

private fun List<Double>.meanAndDeviation(): Pair<Double, Double> {
    val mean = sum() / size
    return mean to sqrt(fold(0.0) { acc, value -> acc + (value - mean).squared() } / size)
}

private fun Double.toOneDP() = "%.1f".format(this)

private fun Double.squared() = this * this

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

