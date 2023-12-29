package com.gildedrose.foundation

typealias PropertySet = Map<String, Any?>

inline fun <reified T> PropertySet.valueOf(key: String): T =
    when (val result: Any? = get(key)) {
        is T -> result
        null -> {
            if (this.containsKey(key))
                throw NoSuchElementException("Value for key <$key> is null")
            else
                throw NoSuchElementException("Key <$key> is missing in the map")
        }

        else -> throw NoSuchElementException("Value for key <$key> is not a ${T::class}")
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

