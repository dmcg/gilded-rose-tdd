import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.CENTER_UP
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import liveplugin.registerIntention
import org.jetbrains.kotlin.idea.base.psi.childrenDfsSequence
import org.jetbrains.kotlin.idea.base.util.reformat
import org.jetbrains.kotlin.idea.base.util.reformatted
import org.jetbrains.kotlin.idea.inspections.ConvertScopeFunctionToParameter
import org.jetbrains.kotlin.idea.inspections.ConvertScopeFunctionToReceiver
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

// depends-on-plugin org.jetbrains.kotlin

registerIntention(ConvertApplyToLetIntention())
registerIntention(ConvertLetToApplyIntention())
registerIntention(CreateSecondaryConstructor())
registerIntention(ConvertSecondaryConstructorToTopLevelFunction())
registerIntention(MoveExtensionFunctionToClass())

class ConvertApplyToLetIntention : MyIntentionAction {
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) =
        currentElementIn(editor, file)?.text == "apply"

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val expression = currentElementIn(editor, file)
            ?.parentOfType<KtCallExpression>(withSelf = true)
            ?.calleeExpression
            ?: return
        val quickFix = ConvertScopeFunctionToParameter("let")
        val problemDescriptor = InspectionManager.getInstance(project)
            .createProblemDescriptor(expression, "Convert to 'let' 🙈", quickFix, GENERIC_ERROR_OR_WARNING, true)
        quickFix.applyFix(project, problemDescriptor)

        val newExpression = currentElementIn(editor, file)
            ?.parentOfType<KtCallExpression>(withSelf = true) ?: return
        val factory = KtPsiFactory(project, false)
        val blockExpression = newExpression.lambdaArguments.last().childrenDfsSequence().filterIsInstance<KtBlockExpression>().first()
        blockExpression.add(factory.createNewLine())
        blockExpression.add(factory.createExpression("it"))
    }

    override fun getText() = "Convert to 'let' 🙈"
}

class ConvertLetToApplyIntention : MyIntentionAction {
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) =
        currentElementIn(editor, file)?.text == "let"

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val expression = currentElementIn(editor, file)
            ?.takeIf { it.text == "let" }
            ?.parentOfType<KtCallExpression>(withSelf = true)
            ?.calleeExpression ?: return
        val quickFix = ConvertScopeFunctionToReceiver("apply")
        val problemDescriptor = InspectionManager.getInstance(project)
            .createProblemDescriptor(expression, "Convert to 'apply' 🙈", quickFix, GENERIC_ERROR_OR_WARNING, true)
        quickFix.applyFix(project, problemDescriptor)

        val newExpression = currentElementIn(editor, file)
            ?.parentOfType<KtCallExpression>(withSelf = true) ?: return
        val blockExpression = newExpression.lambdaArguments.last().childrenDfsSequence().filterIsInstance<KtBlockExpression>().first()
        if (blockExpression.lastChild is KtThisExpression) {
            blockExpression.lastChild.delete()
        }
    }

    override fun getText() = "Convert to 'apply' 🙈"
}

class CreateSecondaryConstructor : MyIntentionAction {
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) =
        currentElementIn(editor, file)?.parentOfType<KtPrimaryConstructor>() != null

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val constructor = currentElementIn(editor, file)?.parentOfType<KtPrimaryConstructor>() ?: return
        val classBody = constructor.parentOfType<KtClass>()?.body ?: return
        val factory = KtPsiFactory(project, false)

        val secondaryParameters = (constructor.valueParameters.map { it.identifyingElement?.text to it.typeReference?.getTypeText() } + listOf("dummy" to "Boolean"))
            .joinToString(",\n") { it.first + ":" + it.second }
        val arguments = constructor.valueParameters.map { it.name }.joinToString(",\n")
        val secondaryConstructor = factory.createSecondaryConstructor("constructor($secondaryParameters): this($arguments)")
        val added = classBody.addAfter(secondaryConstructor, classBody.firstChild)
        classBody.reformat()
        editor.caretModel.moveToOffset(added.startOffset)
    }

    override fun getText() = "Create secondary constructor 🙈"
}

class ConvertSecondaryConstructorToTopLevelFunction : MyIntentionAction {
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) =
        currentElementIn(editor, file)?.parentOfType<KtSecondaryConstructor>() != null

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val constructor = currentElementIn(editor, file)?.parentOfType<KtSecondaryConstructor>() ?: return
        val ktClass = constructor.parentOfType<KtClass>() ?: return
        val factory = KtPsiFactory(project, false)
        val function = factory.createFunction(
            constructor.text
                .replace("constructor", "fun ${ktClass.name}")
                .replace(") :", ") =")
                .replace("this", "${ktClass.name}")
        )
        val nextSibling = ktClass.nextSibling
        val added = nextSibling.parent.addAfter(function, nextSibling).reformatted()
        added.parent.addBefore(factory.createNewLine(2), added)

        (constructor.nextSibling as? PsiWhiteSpace)?.delete()
        constructor.delete()
    }

    override fun getText() = "Convert to top level function 🙈"
}

class MoveExtensionFunctionToClass : MyIntentionAction {
    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val element = currentElementIn(editor, file).takeIf { it.elementType?.debugName == "IDENTIFIER" }
        val function = element?.parentOfType<KtNamedFunction>()
        return function != null && function.receiverTypeReference != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val extensionFunction = currentElementIn(editor, file)?.parentOfType<KtNamedFunction>() ?: return
        val ktClass = extensionFunction.receiverTypeReference
            ?.childrenDfsSequence()?.firstIsInstance<KtNameReferenceExpression>()?.resolve() as? KtClass ?: return

        val factory = KtPsiFactory(project, false)
        val function = factory.createFunction(extensionFunction.text.replace("fun ${ktClass.name}.", "fun ")).reformatted()
        val added = ktClass.body?.addBefore(function, ktClass.body?.lastChild) ?: return
        editor.caretModel.moveToOffset(added.startOffset)
        editor.scrollingModel.scrollToCaret(CENTER_UP)

        extensionFunction.delete()
    }

    override fun getText() = "Move to class 🙈"
}

interface MyIntentionAction : IntentionAction {
    fun currentElementIn(editor: Editor, file: PsiFile) =
        file.findElementAt(editor.caretModel.offset)

    override fun startInWriteAction() = true
    override fun getFamilyName() = "Expressive Kotlin"
}