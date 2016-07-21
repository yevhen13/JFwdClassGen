package jfwdclassgen;

import com.squareup.javapoet.TypeSpec;

interface FcStyleFactory {
  TypeSpec createForwardingClass();
}
