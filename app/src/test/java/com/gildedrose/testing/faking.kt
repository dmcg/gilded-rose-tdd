package com.gildedrose.testing

import java.lang.reflect.Proxy

inline fun <reified T> fake(): T =
    Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java)
    ) { _, _, _ ->
        TODO("not implemented")
    } as T
