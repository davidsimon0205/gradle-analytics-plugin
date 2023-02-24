/**
 * MIT License
 * Copyright (c) 2022 Mehdi Janbarari (@janbarari)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.janbarari.gradle.analytics.metric.executionprocess.report

import io.github.janbarari.gradle.analytics.domain.model.metric.BuildMetric
import io.github.janbarari.gradle.analytics.domain.model.report.ExecutionProcessReport
import io.github.janbarari.gradle.analytics.domain.model.report.Report
import io.github.janbarari.gradle.core.SuspendStage
import io.github.janbarari.gradle.extension.isBiggerEquals
import io.github.janbarari.gradle.extension.isNotNull
import io.github.janbarari.gradle.extension.mapToExecutionMeanTimespanChartPoints
import io.github.janbarari.gradle.extension.mapToExecutionMedianTimespanChartPoints
import io.github.janbarari.gradle.extension.whenEmpty
import io.github.janbarari.gradle.logger.Tower

class CreateExecutionProcessReportStage(
    private val tower: Tower,
    private val metrics: List<BuildMetric>
) : SuspendStage<Report, Report> {

    companion object {
        private const val SKIP_THRESHOLD_IN_MS = 50L
        private val clazz = CreateExecutionProcessReportStage::class.java
    }

    override suspend fun process(input: Report): Report {
        tower.i(clazz, "process()")
        val medianChartPoints = metrics.filter { metric ->
            metric.executionProcessMetric.isNotNull() &&
                    metric.executionProcessMetric?.median?.isBiggerEquals(SKIP_THRESHOLD_IN_MS) ?: false
        }.mapToExecutionMedianTimespanChartPoints()
            .whenEmpty {
                return input
            }

        val meanChartPoints = metrics.filter { metric ->
            metric.executionProcessMetric.isNotNull() &&
                    metric.executionProcessMetric?.mean?.isBiggerEquals(SKIP_THRESHOLD_IN_MS) ?: false
        }.mapToExecutionMeanTimespanChartPoints()
            .whenEmpty {
                return input
            }

        return input.apply {
            executionProcessReport = ExecutionProcessReport(
                medianValues = medianChartPoints,
                meanValues = meanChartPoints,
            )
        }
    }
}