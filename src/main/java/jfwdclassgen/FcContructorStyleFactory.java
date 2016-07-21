package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

final class FcContructorStyleFactory
    implements FcStyleFactory, FcTypeContentProvider {
  private final FcTypeElement element;
  private final FieldSpec delegateField;
  private final MethodSpec contructor;

  public FcContructorStyleFactory(final FcTypeElement element) {
    this.element = requireNonNull(element, "element == null");
    this.delegateField = createDelegateField(element);
    this.contructor = createConstructor(this.delegateField);
  }

  private final FieldSpec createDelegateField(final FcTypeElement element) {
    return FieldSpec.builder(TypeName.get(element.asType()), "delegate",
        Modifier.PROTECTED, Modifier.FINAL).build();
  }

  private final MethodSpec createConstructor(final FieldSpec delegateField) {
    final ParameterSpec param =
        ParameterSpec.builder(delegateField.type, "delegate", Modifier.FINAL)
                     .build();
    return MethodSpec.constructorBuilder()
                     .addModifiers(Modifier.PROTECTED)
                     .addParameter(param)
                     .addCode("this.$N = $N;\n", delegateField, param)
                     .build();
  }

  private CodeBlock forwardingCall(final ExecutableElement methodElement) {
    final CodeBlock.Builder code = CodeBlock.builder();
    code.add("$[");
    if (methodElement.getReturnType().getKind() != TypeKind.VOID) {
      code.add("return ");
    }
    code.add("$N", this.delegateField);
    new FcMethodCallAppender(code).append(methodElement);
    code.add("\n$]");
    return code.build();
  }

  private MethodSpec forwardingMethod(final ExecutableElement methodElement) {
    final MethodSpec.Builder code = MethodSpec.overriding(methodElement);
    code.addCode(forwardingCall(methodElement));
    return code.build();
  }

  @Override
  public Collection<FieldSpec> fields() {
    return Collections.singletonList(this.delegateField);
  }

  @Override
  public Collection<MethodSpec> methods() {
    return Stream.concat(Stream.of(this.contructor),
        this.element.getMethods().map(this::forwardingMethod))
                 .collect(Collectors.toList());
  }

  @Override
  public TypeSpec createForwardingClass() {
    return new FcAbstractClassTypeFactory(this.element).createType(this);
  }
}
