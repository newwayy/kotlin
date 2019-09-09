/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.core.CollectingNameValidator
import org.jetbrains.kotlin.idea.core.KotlinNameSuggester
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*

class AddJvmNameAnnotationFix(element: KtElement, private val jvmName: String) : KotlinQuickFixAction<KtElement>(element) {
    override fun getText(): String = if (element is KtAnnotationEntry) {
        KotlinBundle.message("fix.change.jvm.name")
    } else {
        KotlinBundle.message("fix.add.annotation.text.self", JVM_NAME_FQ_NAME.shortName())
    }

    override fun getFamilyName(): String = text

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val element = element ?: return
        when (element) {
            is KtAnnotationEntry -> {
                val argList = element.valueArgumentList
                val newArgList = KtPsiFactory(element).createCallArguments("(\"$jvmName\")")
                if (argList != null) {
                    argList.replace(newArgList)
                } else {
                    element.addAfter(newArgList, element.lastChild)
                }
            }
            is KtFunction ->
                element.addAnnotation(JVM_NAME_FQ_NAME, annotationInnerText = "\"$jvmName\"")
        }
    }

    companion object : KotlinSingleIntentionActionFactory() {
        private val JVM_NAME_FQ_NAME = FqName("kotlin.jvm.JvmName")

        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val function = diagnostic.psiElement as? KtFunction ?: return null
            val functionName = function.name ?: return null
            val jvmName = KotlinNameSuggester.suggestNameByName(functionName, CollectingNameValidator().apply { addName(functionName) })
            return AddJvmNameAnnotationFix(function.findAnnotation(JVM_NAME_FQ_NAME) ?: function, jvmName)
        }
    }
}
