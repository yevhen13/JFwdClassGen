package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

final class FcInterfaceStyleFactory
    implements FcStyleFactory, FcTypeContentProvider {
  private final FcTypeElement element;
  private final MethodSpec delegateMethod;

  public FcInterfaceStyleFactory(final FcTypeElement element) {
    this.element = requireNonNull(element, "element == null");
    this.delegateMethod = createDelegateMethod(element.asType());
  }

  private final MethodSpec createDelegateMethod(final TypeMirror returnType) {
    return MethodSpec.methodBuilder("delegate")
                     .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                     .returns(TypeName.get(returnType))
                     .build();
  }

  private CodeBlock forwardingCall(final ExecutableElement methodElement) {
    final CodeBlock.Builder code = CodeBlock.builder();
    code.add("$[");
    if (methodElement.getReturnType().getKind() != TypeKind.VOID) {
      code.add("return ");
    }
    code.add("$N()", this.delegateMethod);
    new FcMethodCallAppender(code).append(methodElement);
    code.add("\n$]");
    return code.build();
  }

  private MethodSpec forwardingMethod(final ExecutableElement methodElement) {
    final MethodSpec.Builder code = MethodSpec.overriding(methodElement);
    code.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT);
    code.addCode(forwardingCall(methodElement));
    return code.build();
  }

  @Override
  public Collection<FieldSpec> fields() {
    return Collections.emptyList();
  }

  @Override
  public Collection<MethodSpec> methods() {
    return Stream.concat(Stream.of(this.delegateMethod),
        this.element.getMethods().map(this::forwardingMethod))
                 .collect(Collectors.toList());
  }

  @Override
  public TypeSpec createForwardingClass() {
    return new FcInterfaceTypeFactory(this.element).createType(this);
  }
}
