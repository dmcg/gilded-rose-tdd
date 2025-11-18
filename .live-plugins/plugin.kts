import com.intellij.ide.ui.UISettingsUtils
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.platform.ide.impl.presentationAssistant.getWinKeyText
import com.intellij.platform.ide.impl.presentationAssistant.getWinModifiersText
import com.intellij.ui.BalloonImpl
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import liveplugin.currentEditor
import liveplugin.registerAction
import liveplugin.show
import java.awt.Point
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

var counter = 0
var balloon: Balloon? = null

registerAction("Increment refactorings counter", keyStroke = "meta alt shift F12") {
    balloon?.let(Disposer::dispose)
    balloon = createCounterBalloon(++counter).showIn(it.project)
}

registerAction("Decrement refactorings counter", keyStroke = "meta alt shift F11") {
    balloon?.let(Disposer::dispose)
    counter = (counter - 1).coerceAtLeast(0)
    balloon = createCounterBalloon(counter).showIn(it.project)
}

registerAction("Toggle refactorings counter", keyStroke = "meta alt shift F9") {
    if (balloon == null || balloon!!.wasFadedOut()) {
        balloon = createCounterBalloon(counter).showIn(it.project)
    } else {
        balloon!!.hide()
    }
}

fun createCounterBalloon(counter: Int) =
    JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(
            /* htmlContent = */ "<span style='font-size: 32pt; font-weight:bold'>Refactorings: $counter</span>",
            /* icon = */ null,
            /* fillColor = */ JBColor.background(),
            /* listener = */ null
        )
        .setBorderColor(JBColor.gray)
        .setFadeoutTime(-1) // We could use this to make popup disappear after a time-out
        .setAnimationCycle(0)
        .setCloseButtonEnabled(false)
        .setHideOnClickOutside(false)
        .setDisposable(pluginDisposable)
        .setHideOnFrameResize(false)
        .setHideOnKeyOutside(false)
        .setBlockClicksThroughBalloon(true)
        .setHideOnAction(false)
        .setShowCallout(false)
        .setCornerRadius(15)
        .createBalloon()

fun Balloon.showIn(project: Project?) = apply {
    val component = project?.currentEditor?.component ?: return@apply
    show(
        RelativePoint(component, Point(component.width, 100)),
        Balloon.Position.above
    )
}

////////////////////////////////////////////////////////////

// See https://gist.github.com/dkandalov/54839566c1de9e6012c93bcb87309306

// Not using built-in Presentation Assistant because:
// - it shows shortcuts from the keymap, not the actual shortcuts
// - it shows shortcuts for actions which were not invoked via keyboard
// - there is no way exclude shortcuts, e.g. up/down in project tree view
// - there is no way include shortcuts, e.g. text editor navigation
// - displayed text cannot be customised, e.g. remove "Move Caret " prefix

ShortcutsPresenter(NotificationBalloon(pluginDisposable), pluginDisposable).init()

class ShortcutsPresenter(
    private val notificationBalloon: NotificationBalloon,
    private val parentDisposable: Disposable,
    private val showActionId: Boolean = false
) {
    private val actionManager = ActionManager.getInstance()!!

    fun init() = apply {
        ApplicationManager.getApplication().messageBus.connect(parentDisposable)
            .subscribe(AnActionListener.TOPIC, object : AnActionListener {
                override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
                    val project = event.project ?: return
                    val lastKeyStroke = (event.inputEvent as? KeyEvent)?.let(KeyStroke::getKeyStrokeForEvent) ?: return
                    val actionId = actionManager.getId(action)
                    val actionDescription = event.presentation.text
                        ?.replace("Move Caret to ", "")
                        ?.replace("Move Caret ", "")
                        ?.replace(" in Text Component", "")
                    if (actionId in excludedActions || actionDescription == null) return

                    if (showActionId) show(actionId)
                    notificationBalloon.showShortcut(lastKeyStroke.toPresentableString(), actionDescription, project)
                }
            })
    }

    // Not using: MacKeymapUtil.getKeyStrokeText(this, "+", false)
    // because Windows key rendering (e.g. "Meta+F12") is easier to read.
    @Suppress("UnstableApiUsage")
    private fun KeyStroke.toPresentableString() =
        listOfNotNull(if (modifiers > 0) getWinModifiersText(modifiers) else null, getWinKeyText(keyCode))
            .filter { it.isNotEmpty() }
            .joinToString("+")
            .trim()
            .replace("Meta", "Cmd")

    private val movingActions = setOf(
//        "EditorLeft", "EditorRight", "EditorDown", "EditorUp",
//        "EditorLineStart", "EditorLineEnd", "EditorPageUp", "EditorPageDown",
//        "EditorPreviousWord", "EditorNextWord",
//        "EditorScrollUp", "EditorScrollDown",
//        "EditorTextStart", "EditorTextEnd",
//        "EditorDownWithSelection", "EditorUpWithSelection",
//        "EditorRightWithSelection", "EditorLeftWithSelection",
//        "EditorLineStartWithSelection", "EditorLineEndWithSelection",
//        "EditorPageDownWithSelection", "EditorPageUpWithSelection",
//        "EditorNextWordWithSelection", "EditorPreviousWordWithSelection",
//        "ijkl.MoveCaretLeftWithSelectionAction",
        "Tree-selectNext", "Tree-selectPrevious", "Tree-selectParent", "Tree-selectChild",
        "List-selectNextColumn", "List-selectPreviousColumn",
        "NavBar-selectLeft", "NavBar-selectRight", "NavBar-selectDown", "NavBar-selectUp",
    )

    private val editorActions = setOf(
        IdeActions.ACTION_EDITOR_BACKSPACE,
        IdeActions.ACTION_EDITOR_ENTER,
        IdeActions.ACTION_EDITOR_NEXT_TEMPLATE_VARIABLE,
        "\$Undo", "\$Redo",
//        "EditorCut", "EditorCopy", "EditorPaste",
//        "EditorDelete",
//        "EditorEscape",
        "EditorChooseLookupItem"
    )

    private val otherActions = setOf(
        "CloseFirstNotification", "CloseAllNotifications",
        "Remove Top Editor Component",
        "Toggle refactorings counter",
        "Increment refactorings counter",
        "Decrement refactorings counter",
        "NextWindow",
//        "ActivateProjectToolWindow",
//        "ActivateVersionControlToolWindow",
//        "ActivateRunToolWindow",
    )

    private val excludedActions = movingActions + editorActions + otherActions
}

class NotificationBalloon(private val parentDisposable: Disposable) {
    private var balloon1: Balloon? = null
    private var balloon2: Balloon? = null
    private var balloonText: String? = null

    fun showShortcut(keyStroke: String, actionDescription: String, project: Project) {
        val text = "$keyStroke - $actionDescription"
        if (balloon1 == null || balloon1!!.wasFadedOut()) {
            balloon1 = createBalloon(actionDescription).showIn(project)
            balloon2 = createBalloon(keyStroke).showIn(project, balloon1)
            balloonText = text
        } else if (balloonText == text) {
            return
        } else {
            balloon1!!.hide()
            balloon2!!.hide()
            balloon1 = createBalloon(actionDescription).showIn(project)
            balloon2 = createBalloon(keyStroke).showIn(project, balloon1)
            balloonText = text
        }
    }

    @Suppress("unused") // The color used by the built-in shortcuts presenter (in case I want to use it)
    private val blue4 = JBColor.namedColor("ColorPalette.Blue4")
    private val backgroundColor = JBColor.background()
    private val borderColor = JBColor.gray
    private val fontSize = 24

    private fun createBalloon(text: String) =
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(
                /* htmlContent = */ "<span style='font-size: ${fontSize}pt; font-weight:bold'>$text</span>",
                /* icon = */ null,
                /* fillColor = */ backgroundColor,
                /* listener = */ null
            )
            .setBorderColor(borderColor)
            .setFadeoutTime(3000)
            .setAnimationCycle(50)
            .setCloseButtonEnabled(false)
            .setHideOnClickOutside(false)
            .setDisposable(parentDisposable)
            .setHideOnFrameResize(false)
            .setHideOnKeyOutside(false)
            .setBlockClicksThroughBalloon(true)
            .setHideOnAction(false)
            .setShowCallout(false)
            .setCornerRadius(15)
            .createBalloon()

    private fun Balloon.showIn(project: Project, relativeToBalloon: Balloon? = null) = apply {
        if (relativeToBalloon == null) {
            val component = project.currentEditor?.component ?: return@apply
            val point = Point(component.width - preferredSize.width / 2, 170)
            show(RelativePoint(component, point), Balloon.Position.atLeft)
        } else {
            val component = (relativeToBalloon as BalloonImpl).component ?: return@apply
            val gap = (UISettingsUtils.getInstance().currentIdeScale * 40).toInt()
            val point = Point(-preferredSize.width / 2 + gap, component.height / 2)
            show(RelativePoint(component, point), Balloon.Position.atLeft)
        }
    }
}
