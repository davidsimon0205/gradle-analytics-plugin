package io.github.janbarari.gradle.analytics.domain.usecase

import io.github.janbarari.gradle.analytics.core.usecase.UseCase
import io.github.janbarari.gradle.analytics.domain.metric.BuildMetric
import io.github.janbarari.gradle.analytics.domain.repository.DatabaseRepository

class SaveTemporaryMetricUseCase(private val repo: DatabaseRepository): UseCase<BuildMetric, Boolean>() {
    override fun execute(input: BuildMetric): Boolean {
        return repo.saveTemporaryMetric(input)
    }
}