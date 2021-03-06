// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaTableStub;

public interface LuaTableExpr extends LuaExpr, LuaIndentRange, StubBasedPsiElement<LuaTableStub> {

  @NotNull
  List<LuaTableField> getTableFieldList();

  @NotNull
  List<LuaTableFieldSep> getTableFieldSepList();

  @Nullable
  LuaTableField findField(String fieldName);

}
