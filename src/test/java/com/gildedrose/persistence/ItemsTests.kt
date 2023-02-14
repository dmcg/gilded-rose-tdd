package com.gildedrose.persistence

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
import org.postgresql.ds.PGSimpleDataSource
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDate

val dataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
}
val database = Database.connect(dataSource)

class ItemsTests {

    val item1 = item("id-1", "name", LocalDate.of(2023, 2, 14), 42)
    val item2 = item("id-2", "another name", null, 99)

    val items = Items()

    @BeforeEach
    fun resetDB() {
        transaction(database) {
            SchemaUtils.drop(ItemsTable)
            SchemaUtils.createMissingTablesAndColumns(ItemsTable)
        }
    }

    @Test
    fun `add item`() {
        val items = Items()
        transaction(database) {
            expectThat(items.all()).isEmpty()
        }

        transaction(database) {
            items.add(item1)
            items.add(item2)
            expectThat(items.all())
                .isEqualTo(listOf(item1, item2))
        }
    }

    @Test
    fun findById() {
        transaction(database) {
            items.add(item1)
            items.add(item2)
        }

        transaction(database) {
            expectThat(items.findById(ID("no-such-id")!!))
                .isNull()
            expectThat(items.findById(ID("id-1")!!))
                .isEqualTo(item1)
        }
    }

    @Test
    fun update() {
        transaction(database) {
            items.add(item1)
            items.add(item2)
        }

        val revisedItem = item1.copy(name = NonBlankString("new name")!!)
        transaction(database) {
            items.update(revisedItem)
        }

        transaction(database) {
            expectThat(items.findById(item1.id))
                .isEqualTo(revisedItem)
        }

        transaction(database) {
            expectCatching {
                items.update(item1.copy(id = ID("no-such")!!))
            }.isFailure().isA<IllegalStateException>()
        }
    }
}

class Items {

    fun all(): List<Item> {
        return ItemsTable.all()
    }

    fun add(item: Item) {
        ItemsTable.insert(item)
    }

    fun findById(id: ID<Item>): Item? {
        val items =  ItemsTable.select { ItemsTable.id eq id.toString() }.map {
            it.toItem()
        }
        if (items.size > 1)
            TODO("Handle duplicate ids")
        else
            return items.firstOrNull()
    }

    fun update(item: Item) {
        val rowsChanged = ItemsTable.update({ItemsTable.id eq item.id.toString()}) {
            it[id] = item.id.toString()
            it[name] = item.name.toString()
            it[sellByDate] = item.sellByDate
            it[quality] = item.quality.valueInt
        }
        check(rowsChanged == 1)
    }
}

object ItemsTable : Table() {
    val id: Column<String> = varchar("id", 100)
    val name: Column<String> = varchar("name", 100)
    val sellByDate: Column<LocalDate?> = date("sellByDate").nullable()
    val quality: Column<Int> = integer("quality")
}

fun ItemsTable.insert(item: Item) {
    insert {
        it[id] = item.id.toString()
        it[name] = item.name.toString()
        it[sellByDate] = item.sellByDate
        it[quality] = item.quality.valueInt
    }
}

fun ItemsTable.all() = selectAll().map {
    it.toItem()
}

private fun ResultRow.toItem() =
    Item(
        ID(this[ItemsTable.id]) ?: error("Could not parse id ${this[ItemsTable.id]}"),
        NonBlankString(this[ItemsTable.name]) ?: error("Invalid name ${this[ItemsTable.name]}"),
        this[ItemsTable.sellByDate],
        Quality(this[ItemsTable.quality]) ?: error("Invalid quality ${this[ItemsTable.quality]}")
    )


