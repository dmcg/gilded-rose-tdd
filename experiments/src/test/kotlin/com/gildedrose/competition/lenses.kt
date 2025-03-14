package com.gildedrose.competition

import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

interface Lens<T, R> : (T) -> R {
    fun get(subject: T): R
    fun inject(subject: T, value: R): T
    fun update(subject: T, f: (R) -> R): T = inject(subject, f(get(subject)))
    override fun invoke(subject: T): R = get(subject)
    operator fun invoke(subject: T, value: R): T = inject(subject, value)
}


infix fun <T1, T2, R> ((T1) -> T2?).andThen(second: (T2) -> R): (T1) -> R? = {
    this.invoke(it)?.let { outer -> second.invoke(outer) }
}

infix fun <T1, T2, R> Lens<T1, T2>.andThen(second: Lens<T2, R>): Lens<T1, R> = LensObject(
    { second.get(get(it)) },
    { subject, value ->
        inject(
            subject,
            second.inject(get(subject), value)
        )
    }
)

@JvmName("andThenMaybe")
infix fun <T1, T2, R> Lens<T1, T2?>.andThen(second: Lens<T2, R?>): Lens<T1, R?> = LensObject(
    getter = { get(it)?.let { outer -> second.get(outer) } },
    injector = { subject, value ->
        val outer = get(subject) ?: error("No parent found to inject into")
        inject(
            subject,
            second.inject(outer, value)
        )
    }
)

operator fun <T: Any, R> T.get(extractor: (T) -> R) = extractor(this)
fun <T: Any, R> T.with(lens: Lens<T, R>, of: R) = lens.inject(this, of)
fun <T: Any, R> T.updatedWith(lens: Lens<T, R>, f: (R) -> R) = lens.update(this, f)

data class LensObject<T, R>(
    val getter: (T) -> R,
    val injector: (T, R) -> T
) : Lens<T, R> {
    override fun get(subject: T) = getter(subject)
    override fun inject(subject: T, value: R) = injector(subject, value)
}

inline fun <reified T: Any, R> KProperty1<T, R>.asLens(): Lens<T, R> = LensObject(
    ::get,
    reflectiveCopy(name)
)

inline fun <reified T : Any, R> reflectiveCopy(propertyName: String): (T, R) -> T {
    val copyFunction = T::class.memberFunctions.firstOrNull { it.name == "copy" }
        ?: error("No copy method found")
    val instanceParam = copyFunction.instanceParameter
        ?: error("No copy method instance parameter found")
    val nameParam = copyFunction.parameters.find { it.name == propertyName }
        ?: error("No copy method parameter named $propertyName found")
    return { subject, value ->
        copyFunction.callBy(
            mapOf(
                instanceParam to subject,
                nameParam to value
            )
        ) as T
    }
}
