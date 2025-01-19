package com.gildedrose.foundation

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.ForkJoinPool
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

const val repetitions = 100
const val nonParallelRepetitions = 10

@TestMethodOrder(MethodOrderer.MethodName::class)
@EnabledIfSystemProperty(named = "run-benchmark-tests", matches = "true")
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
        @Suppress("removal", "DEPRECATION")
        System.runFinalization()
    }

    @RepeatedTest(nonParallelRepetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `01 kotlin version`() {
        measureAndCheck {
            map(it)
        }.assertThat { it <= 1.0 }
    }

    @RepeatedTest(repetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `02 parallel stream version`() {
        measureAndCheck {
            parallelMapStream(it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(repetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `03 parallel stream forkJoinPool version`() {
        measureAndCheck {
            parallelMapStream(threadPool, it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(repetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `04 threads version`() {
        measureAndCheck {
            parallelMapThreads(it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(repetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `05 threadPool version`() {
        measureAndCheck {
            parallelMapThreadPool(threadPool, it)
        }.assertThat { it > 1 }
    }

    @RepeatedTest(repetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `06 coroutines version`() {
        measureAndCheck {
            runBlocking(threadPool.asCoroutineDispatcher()) {
                parallelMapCoroutines(it)
            }
        }.assertThat { it > 1 }
    }

    @RepeatedTest(repetitions, name = RepeatedTest.LONG_DISPLAY_NAME)
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
        val concurrency = totalSleepMs / timeMs.toDouble()
        runs.add(testInfo to concurrency)
        return concurrency
    }

    companion object {
        private val runs = mutableListOf<Pair<TestInfo, Double>>()

        @JvmStatic
        @AfterAll
        fun report() {
            runs.toNameAndConcurrency().forEach { (name, concurrency) ->
                println("$name : ${concurrency.first.toOneDP()} ± ${concurrency.second.toOneDP()}")
            }
        }
    }
}

private fun List<Pair<TestInfo, Double>>.toNameAndConcurrency() = map { (testInfo, concurrency) ->
    testInfo.displayName.split("() :: ")[0] to concurrency
}.groupBy(
    keySelector = { it.first },
    valueTransform = { it.second }
).map { (name, concurrencies) ->
    name to concurrencies.culledMeanAndDeviation()
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
