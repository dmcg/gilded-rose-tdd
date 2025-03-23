package com.gildedrose.foundation

fun <T, R> retry(
    retries: Int = 1,
    reporter: (Exception) -> Unit = {},
    function: (T) -> R
): (T) -> R = function.wrappedWith(
    retry(retries, reporter)
)

fun <R> retry(
    retries: Int = 1,
    reporter: (Exception) -> Unit = {},
): Wrapper<R> {
    return fun(f: () -> R): R {
        var countdown = retries
        while (true) {
            try {
                return f()
            } catch (x: Exception) {
                if (countdown-- == 0) throw x
                else reporter(x)
            }
        }
    }
}

fun <T, R> succeedAfter(
    exceptionCount: Int,
    raiseError: () -> Nothing,
    f: (T) -> R,
): (T) -> R {
    var countdown = exceptionCount
    return { if (countdown-- == 0) f(it) else raiseError() }
}
