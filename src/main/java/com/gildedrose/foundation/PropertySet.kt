package com.gildedrose.foundation

typealias PropertySet = Map<String, Any?>

inline fun <reified T : Any> PropertySet.required(key: String): T {
    val value: Any = getValue(key)
        ?: error("Key <$key> is null")
    return value as? T
        ?: error("Value for key <$key> is not a ${T::class}")
}

inline fun <reified T> PropertySet.valueOf(key: String): T {
    val result: Any? = get(key)
    return when {
        result is T -> result
        result == null && null !is T -> error("Value for key <$key> is null")
        else -> error("Value for key <$key> is not a ${T::class}")
    }
}

inline fun <reified T : Any> PropertySet.required(
    key0: String,
    vararg otherKeys: String,
): T = required(listOf(key0) + otherKeys.toList())

inline fun <reified T : Any> PropertySet.required(
    keys: List<String>,
): T = when {
    keys.isEmpty() -> error("no keys supplied")
    else -> keys.dropLast(1).fold(this) { acc, key ->
        acc.required<PropertySet>(key)
    }.required(keys.last())
}

object PropertySets {
    @JvmName("lensPropertySet")
    fun lens(propertyName: String) = lens<PropertySet>(propertyName)
    @JvmName("aslensPropertySet")
    fun String.asLens() = lens(this)

    inline fun <reified R> String.asLens() = lens<R>(this)

    inline fun <reified R> lens(propertyName: String) =
        LensObject<PropertySet, R>(
            getter = { it.valueOf<R>(propertyName) },
            injector = { subject, value ->
                subject.toMutableMap().apply {
                    this[propertyName] = value
                }
            }
        )
}

