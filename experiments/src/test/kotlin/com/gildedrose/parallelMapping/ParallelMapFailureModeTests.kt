package com.gildedrose.parallelMapping

import com.gildedrose.foundation.parallelMapCoroutines
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


@TestMethodOrder(MethodOrderer.MethodName::class)
@EnabledIfSystemProperty(named = "run-benchmark-tests", matches = "true")
class ParallelMapFailureModeTests {

    private val listSize = 100
    private val threadPool = ForkJoinPool(10)
    private lateinit var testInfo: TestInfo

    @BeforeEach
    fun rememberName(testInfo: TestInfo) {
        this.testInfo = testInfo
    }


    @AfterEach
    fun shutDown() {
        threadPool.shutdown()
    }

    @Test
    fun `01 kotlin version`() {
        check {
            map(it)
        }
    }

    @Test
    fun `02 parallel stream version`() {
        check {
            parallelMapStream(it)
        }
    }

    @Test
    fun `03 parallel stream forkJoinPool version`() {
        check {
            parallelMapStream(threadPool, it)
        }
    }

    @Test
    fun `04 threads version`() {
        check {
            parallelMapThreads(it)
        }
    }

    @Test
    fun `05 threadPool version`() {
        check {
            parallelMapThreadPool(threadPool, it)
        }
    }

    @Test
    fun `06 coroutines version`() {
        check {
            runBlocking(threadPool.asCoroutineDispatcher()) {
                parallelMapCoroutines(it)
            }
        }
    }

    @Test
    fun `07 coroutines delay version`() {
        check {
            runBlocking {
                parallelMapCoroutines {
                    delay(Random.nextLong(50))
                    error("error for $it")
                }
            }
        }
    }

    private fun check(
        mapFunction: List<String>.((String) -> Int) -> List<Int>
    ) {
        val input = (1..listSize).map { it.toString() }
        val invocationCount = AtomicInteger()
        try {
            input.mapFunction {
                invocationCount.addAndGet(1)
                Thread.sleep(Random.nextLong(50))
                error("error for $it")
            }
            fail("shouldn't be here")
        } catch (x: Exception) {
            Thread.sleep(1000)
            println("${testInfo.displayName} : $x, invocation count ${invocationCount.get()}")
        }
    }

}



