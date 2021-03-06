/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.LuaIndentRange
import com.tang.intellij.lua.psi.LuaTypes

/**
 * 回车时的自动补全
 * Created by tangzx on 2016/11/26.
 */
class LuaEnterAfterUnmatchedBraceHandler : EnterHandlerDelegate {

    private fun getEnd(range: IElementType): IElementType {
        if (range === LuaTypes.TABLE_EXPR)
            return LuaTypes.RCURLY
        return if (range === LuaTypes.REPEAT_STAT) LuaTypes.UNTIL else LuaTypes.END
    }

    override fun preprocessEnter(psiFile: PsiFile,
                                 editor: Editor,
                                 caretOffsetRef: Ref<Int>,
                                 caretAdvance: Ref<Int>,
                                 dataContext: DataContext,
                                 editorActionHandler: EditorActionHandler?): EnterHandlerDelegate.Result {
        if (!psiFile.language.`is`(LuaLanguage.INSTANCE))
            return EnterHandlerDelegate.Result.Continue
        if (!LuaSettings.instance.isSmartCloseEnd)
            return EnterHandlerDelegate.Result.Continue

        val caretOffset = caretOffsetRef.get()

        val lElement = psiFile.findElementAt(caretOffset - 1)
        if (lElement != null) {
            var shouldClose = false
            var range: PsiElement? = null
            var cur: PsiElement = lElement
            while (true) {
                val searched = cur.parent
                if (searched == null || searched is PsiFile) break
                if (searched is LuaIndentRange) {
                    val endType = getEnd(searched.node.elementType)
                    val endChild = searched.node.findChildByType(endType)
                    if (endChild == null) {
                        shouldClose = true
                        range = searched
                        break
                    }
                }
                cur = searched
            }

            if (shouldClose && range != null) {
                val rElement = psiFile.findElementAt(caretOffset)
                val endType = getEnd(range.node.elementType)
                val document = editor.document
                if (rElement !is PsiWhiteSpace)
                    document.insertString(caretOffset, "\n$endType ")
                else
                    document.insertString(caretOffset, "\n$endType")
                editorActionHandler?.execute(editor, editor.caretModel.currentCaret, dataContext)

                val project = lElement.project

                PsiDocumentManager.getInstance(project).commitDocument(document)
                val styleManager = CodeStyleManager.getInstance(project)
                styleManager.adjustLineIndent(psiFile, editor.caretModel.offset)

                /*val newRange = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caretOffset, LuaIndentRange::class.java, false)
                if (newRange != null) {
                    val textRange = newRange.textRange
                    ApplicationManager.getApplication().runWriteAction {
                        val styleManager = CodeStyleManager.getInstance(project)
                        styleManager.adjustLineIndent(psiFile, textRange)
                        styleManager.adjustLineIndent(psiFile, editor.caretModel.offset)
                    }
                }*/
                return EnterHandlerDelegate.Result.Stop
            }
        }

        return EnterHandlerDelegate.Result.Continue
    }

    override fun postProcessEnter(psiFile: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result {
        return EnterHandlerDelegate.Result.Continue
    }
}
