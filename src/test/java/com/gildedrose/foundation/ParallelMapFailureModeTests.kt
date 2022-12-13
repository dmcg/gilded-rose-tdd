package com.gildedrose.foundation

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.*
import kotlin.random.Random


@TestMethodOrder(MethodOrderer.MethodName::class)
@EnabledIfSystemProperty(named = "run-slow-tests", matches = "true")
class ParallelMapFailureModeTests {

    private val listSize = 100
    private val threadPool = ForkJoinPool(50)
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
        try {
            input.mapFunction {
                Thread.sleep(Random.nextLong(50))
                error("error for $it")
            }
            fail("shouldn't be here")
        } catch (x: Exception) {
            println("${testInfo.displayName} : $x")
        }
    }

}



