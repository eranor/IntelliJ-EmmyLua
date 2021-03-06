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

package com.tang.intellij.lua.stubs.index

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass

class LuaClassMemberIndex : StringStubIndexExtension<LuaClassMember>() {
    override fun getKey() = StubKeys.CLASS_MEMBER

    companion object {
        val instance = LuaClassMemberIndex()

        fun process(key: String, context: SearchContext, processor: Processor<LuaClassMember>): Boolean {
            if (context.isDumb)
                return false
            val all = LuaClassMemberIndex.instance.get(key, context.project, context.getScope())
            if (all.isEmpty()) return true
            @Suppress("LoopToCallChain")
            for (member in all) {
                if (!processor.process(member)) {
                    return false
                }
            }
            return true
        }

        fun process(className: String, fieldName: String, context: SearchContext, processor: Processor<LuaClassMember>): Boolean {
            val key = className + "*" + fieldName
            if (process(key, context, processor)) {
                // from supper
                val classDef = LuaClassIndex.find(className, context)
                if (classDef != null) {
                    val type = classDef.type
                    val superClassName = type.superClassName
                    if (superClassName != null) {
                        return process(superClassName, fieldName, context, processor)
                    }
                }
                return true
            }
            return false
        }

        fun find(type: ITyClass, fieldName: String, context: SearchContext): LuaClassMember? {
            var perfect: LuaClassMember? = null
            processAll(type, fieldName, context, Processor {
                perfect = it
                false
            })
            return perfect
        }

        fun processAll(type: ITyClass, fieldName: String, context: SearchContext, processor: Processor<LuaClassMember>) {
            if (process(type.className, fieldName, context, processor)) {
                type.lazyInit(context)
                val alias = type.aliasName
                if (alias != null) {
                    process(alias, fieldName, context, processor)
                }
            }
        }
    }
}