package com.gildedrose.testing

import com.gildedrose.http.serverFor
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Dialog
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.http4k.core.HttpHandler
import kotlin.test.assertEquals

fun runWithPlaywright(
    handler: HttpHandler,
    browserType: Playwright.() -> BrowserType = Playwright::chromium,
    launchOptions: LaunchOptions? = null,
    logic: Page.() -> Unit
) {
    val actualLaunchOptions = launchOptions ?: LaunchOptions()
    val server = serverFor(port = 0, handler)
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

fun Page.checkReloadsTheSame() {
    val renderedPage = content()
    reload()
    assertEquals(
        content().withNoEmptyLines(),
        renderedPage.withNoEmptyClassAttributes().withNoEmptyLines()
    )
}

private fun String.withNoEmptyClassAttributes() = replace(""" class=""""", "")
private fun String.withNoEmptyLines(): String {
    return this.lines().filter { it.isNotBlank() }.joinToString("\n")
}

fun Page.acceptNextDialog() {
    onDialog { dialog: Dialog ->
        dialog.accept()
    }
}

fun Page.installHtmxSupport() {
    evaluate("""window.htmxHasSettled = false; window.addEventListener('htmx:afterSettle', () => window.htmxHasSettled = true); """)
}

fun Page.waitingForHtmx(action: Page.() -> Unit) {
    evaluate("window.dataLoadedFired == false")
    action()
    waitForFunction("window.htmxHasSettled === true")
}
