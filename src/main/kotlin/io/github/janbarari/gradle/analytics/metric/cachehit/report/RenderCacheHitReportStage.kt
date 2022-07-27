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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.janbarari.gradle.analytics.metric.cachehit.report

import io.github.janbarari.gradle.analytics.domain.model.report.ModuleCacheHitReport
import io.github.janbarari.gradle.analytics.domain.model.report.Report
import io.github.janbarari.gradle.core.Stage
import io.github.janbarari.gradle.extension.ensureNotNull
import io.github.janbarari.gradle.extension.isNull
import io.github.janbarari.gradle.extension.removeLastChar
import io.github.janbarari.gradle.extension.toArrayString
import io.github.janbarari.gradle.extension.toIntList
import io.github.janbarari.gradle.extension.whenEach
import io.github.janbarari.gradle.extension.whenNotNull
import io.github.janbarari.gradle.utils.HtmlUtils

class RenderCacheHitReportStage(
    private val report: Report
) : Stage<String, String> {

    companion object {
        private const val CACHE_HIT_METRIC_TEMPLATE_ID = "%cache-hit-metric%"
        private const val CACHE_HIT_METRIC_TEMPLATE_FILE_NAME = "cache-hit-metric-template"
    }

    override suspend fun process(input: String): String {
        if (report.cacheHitReport.isNull())
            return input.replace(CACHE_HIT_METRIC_TEMPLATE_ID, getEmptyRender())

        return input.replace(CACHE_HIT_METRIC_TEMPLATE_ID, getMetricRender())
    }

    fun getEmptyRender(): String {
        return HtmlUtils.renderMessage("Cache hit metric is not available!")
    }

    @Suppress("LongMethod")
    fun getMetricRender(): String {
        val overallChartValues = ensureNotNull(report.cacheHitReport)
            .overallValues
            .map { it.value }
            .toIntList()
            .toString()

        val overallChartLabels = ensureNotNull(report.cacheHitReport)
            .overallValues
            .map { it.description }
            .toArrayString()

        val tableData = buildString {
            report.cacheHitReport?.modules?.forEachIndexed { index, it ->
                var diffRatioRender = "<td>-</td>"
                it.diffRatio.whenNotNull {
                    diffRatioRender = if (this > 0) {
                        "<td class=\"green\">+${this}%</td>"
                    } else if (this < 0) {
                        "<td class=\"red\">${this}%</td>"
                    } else {
                        "<td>Equals</td>"
                    }
                }
                append(
                    """
                    <tr>
                        <td>${index + 1}</td>
                        <td>${it.path}</td>
                        <td>${it.hitRatio}%</td>
                        $diffRatioRender
                    </tr>
                """.trimIndent()
                )
            }
        }

        val overallCacheHit = ensureNotNull(report.cacheHitReport).overallHit.toString() + "%"

        var overallDiffRatioRender = "<td>-</td>"
        ensureNotNull(report.cacheHitReport).overallDiffRatio.whenNotNull {
            overallDiffRatioRender = if (this > 0) {
                "<td class=\"green\">+${this}%</td>"
            } else if (this < 0) {
                "<td class=\"red\">${this}%</td>"
            } else {
                "<td>Equals</td>"
            }
        }

        val bestModulePath = getBestModulePath(ensureNotNull(report.cacheHitReport).modules)
        val worstModulePath = getWorstModulePath(ensureNotNull(report.cacheHitReport).modules)

        val bestChartValues = ensureNotNull(report.cacheHitReport).modules
            .first { it.path == bestModulePath }
            .values
            .map {
                it.value
            }
            .toIntList()
            .toString()

        val worstChartValues = ensureNotNull(report.cacheHitReport).modules
            .first { it.path == worstModulePath }
            .values
            .map {
                it.value
            }
            .toIntList()
            .toString()

        val bwLabels = ensureNotNull(report.cacheHitReport).modules
            .first { it.path == worstModulePath }
            .values
            .map { it.description }
            .toArrayString()

        var template = HtmlUtils.getTemplate(CACHE_HIT_METRIC_TEMPLATE_FILE_NAME)
        template = template
            .replace("%overall-values%", overallChartValues)
            .replace("%overall-labels%", overallChartLabels)
            .replace("%table-data%", tableData)
            .replace("%overall-cache-hit%", overallCacheHit)
            .replace("%overall-diff-ratio%", overallDiffRatioRender)
            .replace("%best-values%", bestChartValues)
            .replace("%worst-values%", worstChartValues)
            .replace("%bw-labels%", bwLabels.toString())
            .replace("%worst-module-name%", "\"$worstModulePath\"")
            .replace("%best-module-name%", "\"$bestModulePath\"")

        return template
    }

    fun getBestModulePath(modules: List<ModuleCacheHitReport>): String? {
        if (modules.isEmpty()) return null
        return modules.sortedByDescending { module ->
            module.values.sumOf { it.value }
        }.first().path
    }

    fun getWorstModulePath(modules: List<ModuleCacheHitReport>): String? {
        if (modules.isNull()) return null
        return modules.sortedByDescending { module ->
            module.values.sumOf { it.value }
        }.last().path
    }

}