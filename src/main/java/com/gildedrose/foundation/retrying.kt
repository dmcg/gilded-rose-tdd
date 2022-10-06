package com.gildedrose.foundation

fun <T, R> retry(
    retries: Int = 1,
    reporter: (Exception) -> Unit = {},
    function: (T) -> R
): (T) -> R {
    return fun(it: T): R {
        var countdown = retries
        while (true) {
            try {
                return function(it)
            } catch (x: Exception) {
                if (countdown-- == 0) throw x
                else reporter(x)
            }
        }
    }
}
