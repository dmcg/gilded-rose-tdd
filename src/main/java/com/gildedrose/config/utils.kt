package com.gildedrose.config

import org.http4k.lens.BiDiLensSpec
import org.http4k.lens.BiDiMapping
import org.http4k.lens.map

fun <IN : Any> BiDiLensSpec<IN, String>.URI() = map(BiDiMapping(java.net.URI::create, java.net.URI::toString) )
