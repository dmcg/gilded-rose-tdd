package com.gildedrose.testing

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.jvm.optionals.getOrNull

class TimingExtension : TestExecutionListener, TestTiming {

    init {
        TestTiming.init(this)
    }

    private var testPath: ThreadLocal<ConcurrentLinkedDeque<TestIdentifier>> = ThreadLocal.withInitial {
        ConcurrentLinkedDeque<TestIdentifier>()
    }
    private val startTimes = ConcurrentHashMap<TestIdentifier, Instant>()
    private val endTimes = ConcurrentHashMap<TestIdentifier, Instant>()
    private val eventTimes = ConcurrentLinkedQueue<TestEvent>()

    override fun event(name: String, now: Instant) {
        eventTimes.add(TestEvent(testPath.get().peek(), name, now))
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        startTimes[testIdentifier] = Instant.now()
        testPath.get().push(testIdentifier)
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        endTimes[testIdentifier] = Instant.now()
        testPath.get().takeUnless { it.isEmpty() }?.pop()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        val stats = extractStats()
        stats.forEach { printTree(it) }
        File("testrun.mmd").writeText(stats.toMermaidGanttChart().joinToString("\n"), Charsets.UTF_8)
    }

    private fun extractStats(): List<TestStats> {
        val groupedByParent: Map<UniqueId?, List<TestIdentifier>> =
            endTimes.keys.groupBy { it.parentIdObject.getOrNull() }

        val eventGroupedByParent = eventTimes.groupBy { it.currentTestIdentifier }

        val roots = groupedByParent[null] ?: emptyList()

        fun createStats(testIdentifier: TestIdentifier): TestStats {
            val childStats = groupedByParent[testIdentifier.uniqueIdObject]
                ?.map { createStats(it) }
                ?.sortedBy { it.start }
                ?: emptyList()
            val childEvents = eventGroupedByParent[testIdentifier] ?: emptyList()
            return TestStats(testIdentifier, startTimes[testIdentifier]!!, endTimes[testIdentifier]!!, childEvents, childStats)
        }
        return roots.map { createStats(it) }
    }
}

private fun printTree(testStats: TestStats, depth: Int = 0) {
    println("  ".repeat(depth) + testStats.test.displayName + " " + testStats.duration)
    testStats.children.forEach { printTree(it, depth + 1) }
}

sealed interface Timed {
    val start: Instant
}

data class TestStats(
    val test: TestIdentifier,
    override val start: Instant,
    val end: Instant,
    val events: List<TestEvent>,
    val children: List<TestStats> = emptyList(),
): Timed {
    val duration = Duration.between(start, end)
}

data class TestEvent(
    val currentTestIdentifier: TestIdentifier?,
    val name: String,
    override val start: Instant,
) : Timed
