package com.gildedrose.persistence.exposed

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.item
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDate

class PgItemsTests {

    val item1 = item("id-1", "name", LocalDate.of(2023, 2, 14), 42)
    val item2 = item("id-2", "another name", null, 99)

    val items = PgItems()

    @BeforeEach
    fun resetDB() {
        transaction(testDatabase) {
            SchemaUtils.drop(PgItems.SpikeItems)
            SchemaUtils.createMissingTablesAndColumns(PgItems.SpikeItems)
        }
    }

    @Test
    fun `add item`() {
        val items = PgItems()
        transaction(testDatabase) {
            expectThat(items.all()).isEmpty()
        }

        transaction(testDatabase) {
            items.add(item1)
            items.add(item2)
            expectThat(items.all())
                .isEqualTo(listOf(item1, item2))
        }
    }

    @Test
    fun findById() {
        transaction(testDatabase) {
            items.add(item1)
            items.add(item2)
        }

        transaction(testDatabase) {
            expectThat(items.findById(ID("no-such-id")!!))
                .isNull()
            expectThat(items.findById(ID("id-1")!!))
                .isEqualTo(item1)
        }
    }

    @Test
    fun update() {
        transaction(testDatabase) {
            items.add(item1)
            items.add(item2)
        }

        val revisedItem = item1.copy(name = NonBlankString("new name")!!)
        transaction(testDatabase) {
            items.update(revisedItem)
        }

        transaction(testDatabase) {
            expectThat(items.findById(item1.id))
                .isEqualTo(revisedItem)
        }

        transaction(testDatabase) {
            expectCatching {
                items.update(item1.copy(id = ID("no-such")!!))
            }.isFailure().isA<IllegalStateException>()
        }
    }
}

class PgItems {

    fun all(): List<Item> {
        return SpikeItems.all()
    }

    fun add(item: Item) {
        SpikeItems.insert(item)
    }

    fun findById(id: ID<Item>): Item? {
        val items =  SpikeItems.select { SpikeItems.id eq id.toString() }.map {
            it.toItem()
        }
        if (items.size > 1)
            TODO("Handle duplicate ids")
        else
            return items.firstOrNull()
    }

    fun update(item: Item) {
        val rowsChanged = SpikeItems.update({ SpikeItems.id eq item.id.toString()}) {
            it[id] = item.id.toString()
            it[name] = item.name.toString()
            it[sellByDate] = item.sellByDate
            it[quality] = item.quality.valueInt
        }
        check(rowsChanged == 1)
    }

    object SpikeItems : Table() {
        val id: Column<String> = varchar("id", 100)
        val name: Column<String> = varchar("name", 100)
        val sellByDate: Column<LocalDate?> = date("sellByDate").nullable()
        val quality: Column<Int> = integer("quality")
    }
    fun SpikeItems.insert(item: Item) {
        insert {
            it[id] = item.id.toString()
            it[name] = item.name.toString()
            it[sellByDate] = item.sellByDate
            it[quality] = item.quality.valueInt
        }
    }

    fun SpikeItems.all() = selectAll().map {
        it.toItem()
    }

    private fun ResultRow.toItem() =
        Item(
            ID(this[SpikeItems.id]) ?: error("Could not parse id ${this[SpikeItems.id]}"),
            NonBlankString(this[SpikeItems.name]) ?: error("Invalid name ${this[SpikeItems.name]}"),
            this[SpikeItems.sellByDate],
            Quality(this[SpikeItems.quality]) ?: error("Invalid quality ${this[SpikeItems.quality]}")
        )

}






