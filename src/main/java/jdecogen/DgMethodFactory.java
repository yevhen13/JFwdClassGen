package jdecogen;

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

final class DgMethodFactory {
  private final DgStyle style;
  private final MethodSpec delegateMethod;

  public DgMethodFactory(final TypeMirror baseType, final DgStyle style) {
    this.style = requireNonNull(style);
    this.delegateMethod = this.createDelegateMethod(baseType);
  }

  private final MethodSpec createDelegateMethod(final TypeMirror returnType) {
    final MethodSpec.Builder code = MethodSpec.methodBuilder("delegate");
    if (this.style == DgStyle.INTERFACE) {
      code.addModifiers(Modifier.PUBLIC);
    } else {
      code.addModifiers(Modifier.PROTECTED);
    }
    code.addModifiers(Modifier.ABSTRACT);
    code.returns(TypeName.get(returnType));
    return code.build();
  }

  public MethodSpec getDelegateMethod() {
    return this.delegateMethod;
  }

  private void appendParameters(final CodeBlock.Builder code, final Collection<? extends VariableElement> parameterElements) {
    final Iterator<? extends VariableElement> it = parameterElements.iterator();
    while (it.hasNext()) {
      final VariableElement parameterElement = it.next();
      String parameterFormat = "$N";
      if (it.hasNext()) {
        parameterFormat += ", ";
      }
      code.add(parameterFormat, parameterElement.getSimpleName().toString());
    }
  }

  private CodeBlock decorationCall(final ExecutableElement methodElement) {
    final String methodName = methodElement.getSimpleName().toString();
    final CodeBlock.Builder code = CodeBlock.builder();
    code.add("$[");
    if (methodElement.getReturnType().getKind() != TypeKind.VOID) {
      code.add("return ");
    }
    code.add("$N().$N(", this.delegateMethod, methodName);
    this.appendParameters(code, methodElement.getParameters());
    code.add(");\n$]");
    return code.build();
  }

  public MethodSpec decorate(final ExecutableElement methodElement) {
    final MethodSpec.Builder code = MethodSpec.overriding(methodElement);
    if (this.style == DgStyle.INTERFACE) {
      code.addModifiers(Modifier.DEFAULT);
    }
    code.addCode(this.decorationCall(methodElement));
    return code.build();
  }
}
