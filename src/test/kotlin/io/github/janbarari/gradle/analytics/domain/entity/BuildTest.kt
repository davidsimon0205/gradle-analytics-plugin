package io.github.janbarari.gradle.analytics.domain.entity

import io.github.janbarari.gradle.analytics.data.database.table.MetricTable
import org.jetbrains.exposed.sql.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildTest {

    private lateinit var longColumn: Column<Long>
    private lateinit var varcharColumn: Column<String>

    @BeforeAll
    fun setup() {
        longColumn = Column(Table(),"longColumn", LongColumnType())
        varcharColumn = Column(Table(),"varcharColumn", VarCharColumnType())
    }

    @Test
    fun `check the number column`() {
        //Check the column name is `number`
        assertEquals("number", MetricTable.number.name)
        //Check the column type is `Long`
        assert(MetricTable.number::class.java == longColumn::class.java)
        //Check the column is auto increment
        assert(MetricTable.number.autoIncColumnType?.isAutoInc ?: false)
    }

    @Test
    fun `check the table primary-key`() {
        assertEquals("number", MetricTable.primaryKey.columns.first().name)
    }

}