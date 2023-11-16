package com.gildedrose.foundation

typealias PropertySet = Map<String, Any?>

inline fun <reified T : Any> PropertySet.required(key: String): T {
    val value: Any = get(key) ?: error("Cannot find key <$key>")
    return value as? T ?: error("Value for key <$key> is not a ${T::class}")
}

inline fun <reified T : Any> PropertySet.required(key0: String, key1: String): T =
    required<PropertySet>(key0).required<T>(key1)
