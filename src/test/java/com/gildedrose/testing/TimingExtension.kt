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
import kotlin.jvm.optionals.getOrNull

class TimingExtension : TestExecutionListener {

    private val startTimes = ConcurrentHashMap<TestIdentifier, Instant>()
    private val endTimes = ConcurrentHashMap<TestIdentifier, Instant>()

    override fun executionStarted(testIdentifier: TestIdentifier) {
        startTimes[testIdentifier] = Instant.now()
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        endTimes[testIdentifier] = Instant.now()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        val stats = extractStats()
        stats.forEach { printTree(it) }
        File("testrun.mmd").writeText(generateMermaidGanttChart(stats), Charsets.UTF_8)
    }

    private fun extractStats(): List<TestStats> {
        val groupedByParent: Map<UniqueId?, List<TestIdentifier>> =
            endTimes.keys.groupBy { it.parentIdObject.getOrNull() }
        val roots = groupedByParent[null] ?: emptyList()

        fun createStats(testIdentifier: TestIdentifier): TestStats {
            val childStats = groupedByParent[testIdentifier.uniqueIdObject]
                ?.map { createStats(it) }
                ?.sortedBy { it.start }
                ?: emptyList()
            return TestStats(testIdentifier, startTimes[testIdentifier]!!, endTimes[testIdentifier]!!, childStats)
        }
        return roots.map { createStats(it) }
    }
}

private fun printTree(testStats: TestStats, depth: Int = 0) {
    println("  ".repeat(depth) + testStats.test.displayName + " " + testStats.duration)
    testStats.children.forEach { printTree(it, depth + 1) }
}

data class TestStats(
    val test: TestIdentifier,
    val start: Instant,
    val end: Instant,
    val children: List<TestStats> = emptyList(),
) {
    val duration = Duration.between(start, end)
}
