package com.gildedrose.testing

import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun List<TestStats>.toMermaidGanttChart(): Sequence<String> = sequenceOf(
    "gantt",
    "title Test Execution Timeline",
    "dateFormat $mermaidDateFormat",
    "axisFormat %S.%L",
    "tickInterval 250ms",
) + this.flatMap { stats ->
    stats.toChartLines()
}

private fun Timed.toChartLines(depth: Int): Sequence<String> = when (this) {
    is TestStats -> toChartLines(depth)
    is TestEvent -> toChartLines()
}

private fun TestStats.toChartLines(depth: Int = 0): Sequence<String> =
    sequence {
        val taskName = test.displayName.replace(":", " ")
        val startTime = javaFormatter.format(start)
        val endTime = javaFormatter.format(end)
        if (depth == 1)
            yield("section $taskName")
        yield("$taskName ${duration.toMillis()} :, $startTime, $endTime")

        val childLines = (events + children)
            .sortedBy { it.start }
            .asSequence()
            .flatMap { thing ->
                thing.toChartLines(depth + 1)
            }.map { "    $it" }
        yieldAll(childLines)
    }

private fun TestEvent.toChartLines() = sequenceOf("$name : milestone, $start,")

private val mermaidDateFormat = "YYYY-MM-DDTHH:mm:ss.SSS"
private val javaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    .withZone(ZoneId.systemDefault())
