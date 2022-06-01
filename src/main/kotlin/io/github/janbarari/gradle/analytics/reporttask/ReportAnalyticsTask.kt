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
package io.github.janbarari.gradle.analytics.reporttask

import io.github.janbarari.gradle.analytics.GradleAnalyticsPluginConfig
import io.github.janbarari.gradle.extension.ExcludeJacocoGenerated
import io.github.janbarari.gradle.extension.envCI
import io.github.janbarari.gradle.extension.registerTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * A Gradle task that generates the report based on `git branch`, `time period` and `task name`.
 *
 * A quick instruction about how to invoke the task:
 * `./gradlew reportAnalytics --branch="{your-branch}" --task="{your-task}" --period="{a-number-between-1-to-12}"`
 */
@ExcludeJacocoGenerated
abstract class ReportAnalyticsTask : DefaultTask() {

    companion object {
        const val TASK_NAME = "reportAnalytics"

        @ExcludeJacocoGenerated
        fun register(project: Project, configuration: GradleAnalyticsPluginConfig) {
            project.registerTask<ReportAnalyticsTask>(TASK_NAME) {
                projectNameProperty.set(project.rootProject.name)
                envCIProperty.set(project.envCI().isPresent)
                outputPathProperty.set(configuration.outputPath)
                trackingTasksProperty.set(configuration.trackingTasks)
                trackingBranchesProperty.set(configuration.trackingBranches)
                databaseConfigProperty.set(configuration.getDatabaseConfig())
            }
        }
    }

    @set:Option(option = "branch", description = "Git branch name")
    @get:Input
    var branchArgument: String = ""

    @set:Option(option = "task", description = "Tracking task name")
    @get:Input
    var requestedTasksArgument: String = ""

    @set:Option(option = "period", description = "Number of months")
    @get:Input
    var periodArgument: String = ""

    @get:Input
    abstract val projectNameProperty: Property<String>

    @get:Input
    abstract val envCIProperty: Property<Boolean>

    @get:Input
    abstract val outputPathProperty: Property<String>

    @get:Input
    abstract val trackingTasksProperty: ListProperty<String>

    @get:Input
    abstract val trackingBranchesProperty: ListProperty<String>

    @get:Input
    abstract val databaseConfigProperty: Property<GradleAnalyticsPluginConfig.DatabaseConfig>

    /**
     * Invokes when the task execution process started.
     */
    @TaskAction
    fun execute() {

        val injector = ReportAnalyticsInjector(
            requestedTasks = requestedTasksArgument,
            isCI = envCIProperty.get(),
            databaseConfig = databaseConfigProperty.get(),
            branch = branchArgument,
            outputPath = outputPathProperty.get(),
            projectName = projectNameProperty.get()
        )

        with(injector.provideReportAnalyticsLogic()) {
            ensureBranchArgumentValid(branchArgument)
            ensurePeriodArgumentValid(periodArgument)
            ensureTaskArgumentValid(requestedTasksArgument)
            saveReport(generateReport(branchArgument, requestedTasksArgument, periodArgument.toLong()))
        }

    }

}