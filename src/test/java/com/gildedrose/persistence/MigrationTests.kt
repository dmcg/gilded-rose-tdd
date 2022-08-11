package com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.onFailure
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(ApprovalTest::class)
class MigrationTests {

    @Test
    fun `migrate adds ids to items`(approver: Approver) {
        val inputLines = stockList.lines()
        val outputLines: List<String> = migrate(inputLines)
        approver.assertApproved(outputLines.joinToString("\n"))
    }

    @Test
    fun `stockList can read new items`(approver: Approver) {
        val inputLines = stockList.lines()
        val outputLines: List<String> = migrate(inputLines)
        val readStock = outputLines.asSequence().toStockList().onFailure { error(it.reason) }
        approver.assertApproved(readStock.toLines().joinToString("\n"))
    }
}

fun main() {
    val input = File("stock.tsv")
    val output = File("new-stock.tsv")
    input.useLines { lines->
        output.writeText(migrate(lines.toList()).joinToString("\n"))
    }
}

private fun migrate(inputLines: List<String>): List<String> {
    val stockList: StockList = inputLines.asSequence().toStockList().onFailure { error(it.reason) }
    val ids = mutableSetOf<ID<Item>>()
    val newItems = stockList.map { it.withID(ids).also { newItem -> ids.add(newItem.id!!) } }
    val newStockList = stockList.copy(items = newItems)
    return newStockList.toLines().toList()
}

private fun Item.withID(ids: Set<ID<Item>>): Item {
    for (i in 1..100) {
        val candiate = ID<Item>(initialsFrom(name) + i)!!
        if (!ids.contains(candiate))
            return this.copy(id = candiate)
    }
    error("Used all my numbers")
}

fun initialsFrom(name: NonBlankString) = name.split(" ").map { it[0] }.joinToString("").uppercase()

val stockList = """
    # LastModified: 2022-08-11T07:43:47.787586Z
    Aged Brie	2022-08-10	23
    Amulet of Absorbing	2022-08-05	0
    Amulet of Youth	2022-08-31	37
    Chest of Eternal Health	2022-09-09	23
    Conjured Banana	2022-08-27	0
    Conjured Holy Band	2022-08-13	3
    Cursed Rabbits Foot	2022-08-27	15
    Deepstar Polyp	2022-08-06	0
    Eternal Ring	2022-08-09	0
    Eternity Chest	2022-08-25	46
    Eternity Stone	2022-08-14	13
    Guardian Box	2022-08-24	16
    Guardian Box	2022-08-12	14
    Hand of Oblivion	2022-08-26	3
    Impurity Seal	2022-09-02	23
    Inscrutable Quantum Device	2022-08-11	19
    Instrument of Restoration	2022-08-29	6
    Malediction Mirror	2022-08-05	0
    Mask of Virility	2022-08-13	46
    Massacre Pillar	2022-08-08	0
    Pillar of Dreams	2022-08-27	43
    Revelation Key	2022-08-31	45
    Shield of Evils	2022-08-24	31
    Sulfuras, Hand of Ragnaros		80
    Sulfuras, Hand of Ragnaros		70
    Termination Statue	2022-08-12	3
    Thunder Horn	2022-08-11	0
    Tome of Fate	2022-09-04	6
    Urn of Riddles	2022-09-06	30
""".trimIndent()
