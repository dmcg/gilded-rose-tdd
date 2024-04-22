
import com.gildedrose.App
import com.gildedrose.config.Features
import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.foundation.IO
import dev.forkhandles.result4k.map
import java.time.LocalDate
import java.util.*

fun main() {
    App(
        dbConfig = dbConfig,
        features = Features(newItemEnabled = true)
    ).apply {
        with (IO) {
            val random = Random()
            loadStockList().map { pricedStockList ->
                deleteItemsWithIds(pricedStockList.map { it.id }.toSet())
            }
            val now = LocalDate.now()
            itemData.forEach { (id, name) ->
                addItem(
                    Item(
                        ID(id)!!,
                        NonBlankString(name)!!,
                        now.plusDays(sellByDaysFor(random, id)),
                        qualityFor(random, id)
                    ))
            }
        }
    }
}

private fun qualityFor(random: Random, id: String) = when {
    id.startsWith("SHO") -> Quality(50)
    else -> Quality(random.nextInt(50))
}!!

private fun sellByDaysFor(random: Random, id: String) = random.nextLong(101)

private val itemData = listOf(
    "AB1" to "Aged Brie",
    "AOA1" to "Amulet of Absorbing",
    "AOY1" to "Amulet of Youth",
    "COEH1" to "Chest of Eternal Health",
    "CB1" to "Conjured Banana",
    "CHB1" to "Conjured Holy Band",
    "CRF1" to "Cursed Rabbits Foot",
    "DP1" to "Deepstar Polyp",
    "ER1" to "Eternal Ring",
    "EC1" to "Eternity Chest",
    "ES1" to "Eternity Stone",
    "GB1" to "Guardian Box",
    "GB2" to "Guardian Box",
    "HOO1" to "Hand of Oblivion",
    "IS1" to "Impurity Seal",
    "IQD1" to "Inscrutable Quantum Device",
    "IOR1" to "Instrument of Restoration",
    "MM1" to "Malediction Mirror",
    "MOV1" to "Mask of Virility",
    "MP1" to "Massacre Pillar",
    "POD1" to "Pillar of Dreams",
    "RK1" to "Revelation Key",
    "SOE1" to "Shield of Evils",
    "SHOR1" to "Sulfuras, Hand of Ragnaros",
    "SHOR2" to "Sulfuras, Hand of Ragnaros",
    "TS1" to "Termination Statue",
    "TH1" to "Thunder Horn",
    "TOF1" to "Tome of Fate",
    "UOR1" to "Urn of Riddles",
)

