package com.gildedrose.foundation

import java.io.PrintStream

fun <T> T.printed(to: PrintStream = System.out) =
    this.also { to.println(it) }
