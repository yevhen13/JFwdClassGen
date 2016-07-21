package jfwdclassgen;

import java.util.Collection;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

interface FcTypeContentProvider {
  Collection<FieldSpec> fields();

  Collection<MethodSpec> methods();
}
