package jfwdclassgen;

import com.squareup.javapoet.TypeSpec;


interface FcTypeFactory {
  TypeSpec createType(FcTypeContentProvider provider);
}
