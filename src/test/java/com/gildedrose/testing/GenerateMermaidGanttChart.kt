package com.gildedrose.testing

import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun List<TestStats>.toMermaidGanttChart(): List<String> = listOf(
    "gantt",
    "title Test Execution Timeline",
    "dateFormat $mermaidDateFormat",
    "axisFormat %S.%L",
    "tickInterval 250ms",
) + this.flatMap { stats ->
    stats.toChartLines()
}

private fun Timed.toChartLines(depth: Int): List<String> = when (this) {
    is TestStats -> toChartLines(depth)
    is TestEvent -> toChartLines()
}

private fun TestStats.toChartLines(depth: Int = 0): List<String> {
    val taskName = test.displayName.replace(":", " ")
    val startTime = javaFormatter.format(start)
    val endTime = javaFormatter.format(end)
    return buildList {
        if (depth == 1)
            add("section $taskName")
        add("$taskName ${duration.toMillis()} :, $startTime, $endTime")
        val childLines = (events + children)
            .sortedBy { it.start }
            .flatMap { thing ->
                thing.toChartLines(depth + 1)
            }.map { "    $it" }
        addAll(childLines)
    }
}

private fun TestEvent.toChartLines() = listOf("$name : milestone, $start,")

private val mermaidDateFormat = "YYYY-MM-DDTHH:mm:ss.SSS"
private val javaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    .withZone(ZoneId.systemDefault())
