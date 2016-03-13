package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

final class FcMethodFactory {
  private final FcStyle style;
  private final MethodSpec delegateMethod;

  public FcMethodFactory(final TypeMirror baseType, final FcStyle style) {
    this.style = requireNonNull(style);
    this.delegateMethod = this.createDelegateMethod(baseType);
  }

  private final MethodSpec createDelegateMethod(final TypeMirror returnType) {
    switch (this.style) {
      default:
      case ABSTRACT_CLASS:
        return MethodSpec.methodBuilder("delegate").addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT).returns(TypeName.get(returnType)).build();
      case INTERFACE:
        return MethodSpec.methodBuilder("delegate").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(TypeName.get(returnType)).build();
      case CONSTRUCTOR:
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED).build();
    }
  }

  public MethodSpec getDelegateMethod() {
    return this.delegateMethod;
  }

  private void appendMethodArguments(final CodeBlock.Builder code, final Collection<? extends VariableElement> argumentElements) {
    final Iterator<? extends VariableElement> it = argumentElements.iterator();
    while (it.hasNext()) {
      final VariableElement parameterElement = it.next();
      String parameterFormat = "$N";
      if (it.hasNext()) {
        parameterFormat += ", ";
      }
      code.add(parameterFormat, parameterElement.getSimpleName().toString());
    }
  }

  private CodeBlock forwardingCall(final ExecutableElement methodElement) {
    final String methodName = methodElement.getSimpleName().toString();
    final CodeBlock.Builder code = CodeBlock.builder();
    code.add("$[");
    if (methodElement.getReturnType().getKind() != TypeKind.VOID) {
      code.add("return ");
    }
    code.add("$N().$N(", this.delegateMethod, methodName);
    this.appendMethodArguments(code, methodElement.getParameters());
    code.add(");\n$]");
    return code.build();
  }

  public MethodSpec forwardingMethod(final ExecutableElement methodElement) {
    final MethodSpec.Builder code = MethodSpec.overriding(methodElement);
    if (this.style == FcStyle.INTERFACE) {
      code.addModifiers(Modifier.DEFAULT);
    }
    code.addCode(this.forwardingCall(methodElement));
    return code.build();
  }
}
