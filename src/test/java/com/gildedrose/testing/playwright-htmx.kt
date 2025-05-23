package com.gildedrose.testing

import com.gildedrose.http.serverOn
import com.microsoft.playwright.*
import com.microsoft.playwright.BrowserType.LaunchOptions
import org.http4k.core.HttpHandler
import kotlin.test.assertEquals

fun runWithPlaywright(
    handler: HttpHandler,
    browserType: Playwright.() -> BrowserType = Playwright::chromium,
    launchOptions: LaunchOptions? = null,
    logic: Page.() -> Unit
) {
    val actualLaunchOptions = launchOptions ?: LaunchOptions()
    val server = handler.serverOn(port = 0)
    server.start().use {
        val port = server.port()
        Playwright.create().use { playwright ->
            val browser = browserType(playwright).launch(actualLaunchOptions)
            val page: Page = browser.newPage()
            page.navigate("http://localhost:$port")
            page.installHtmxSupport()
            logic(page)
        }
    }
}

fun launchOptions(showRunning: Boolean) = if (showRunning)
    LaunchOptions().setHeadless(false).setSlowMo(250.0)
else null

fun Page.checkReloadsTheSame() {
    val renderedPage = content()
    reload()
    assertEquals(
        content().withNoEmptyLines(),
        renderedPage.withNoEmptyClassAttributes().withNoEmptyLines()
    )
}

fun Page.acceptNextDialog() {
    onDialog(Dialog::accept)
}

private val htmxSettled = "htmxHasSettled"

fun Page.installHtmxSupport() {
    evaluate("window.$htmxSettled = false; window.addEventListener('htmx:afterSettle', () => window.$htmxSettled = true);")
}

fun Page.waitingForHtmx(action: Page.() -> Unit) {
    evaluate("window.$htmxSettled = false")
    action()
    waitForFunction("window.$htmxSettled === true")
}

fun Page.inputNamed(name: String): Locator =
    locator("""input[name="$name"]""")

fun Page.submitButtonNamed(name: String): Locator =
    locator("""input[value="$name"][type="submit"]""")

fun Page.checkBoxNamed(name: String): Locator =
    locator("""input[name="$name"][type="checkbox"]""")


private fun String.withNoEmptyClassAttributes() = replace(""" class=""""", "")
private fun String.withNoEmptyLines(): String {
    return this.lines().filter { it.isNotBlank() }.joinToString("\n")
}
