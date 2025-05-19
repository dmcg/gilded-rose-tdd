@file:Suppress("unused")

package com.gildedrose.stuff

import com.gildedrose.updating.*
import java.time.LocalDate

fun thisFunction(sellByDate: LocalDate?, name: String): ItemType = when {
    sellByDate == null -> Undated()
    name.contains("Aged Brie", ignoreCase = true) -> Brie()
    name.contains("Backstage Pass", ignoreCase = true) -> Pass()
    name.startsWith("Conjured", ignoreCase = true) -> Conjured()
    else -> Standard()
}

fun thatFunction(sellByDate: LocalDate?, name: String): ItemType = when {
    sellByDate == null -> Undated()
    name.contains("Aged Brie", ignoreCase = true) -> Brie()
    name.contains("Backstage Pass", ignoreCase = true) -> Pass()
    name.startsWith("Conjured", ignoreCase = true) -> Conjured()
    else -> Standard()
}

fun theOtherFunction(sellByDate: LocalDate?, name: String): ItemType = when {
    sellByDate == null -> Undated()
    name.contains("Aged Brie", ignoreCase = true) -> Brie()
    name.contains("Backstage Pass", ignoreCase = true) -> Pass()
    name.startsWith("Conjured", ignoreCase = true) -> Conjured()
    else -> Standard()
}