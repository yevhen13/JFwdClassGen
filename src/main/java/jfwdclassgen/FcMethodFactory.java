package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

final class FcMethodFactory {
  static final ClassName OVERRIDE = ClassName.get(Override.class);

  private final FcStyle style;
  private final MethodSpec delegateMethod;

  public FcMethodFactory(final TypeMirror baseType, final FcStyle style) {
    this.style = requireNonNull(style);
    this.delegateMethod = this.createDelegateMethod(baseType);
  }

  private final MethodSpec createDelegateMethod(final TypeMirror returnType) {
    final MethodSpec.Builder code = MethodSpec.methodBuilder("delegate");
    if (this.style == FcStyle.INTERFACE) {
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

  /**
   * @see {@link MethodSpec#overriding}
   */
  private MethodSpec.Builder overriding(final ExecutableElement method) {
    requireNonNull(method, "method == null");

    Set<Modifier> modifiers = method.getModifiers();
    if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.FINAL) || modifiers.contains(Modifier.STATIC)) {
      throw new IllegalArgumentException("cannot override method with modifiers: " + modifiers);
    }

    final String methodName = method.getSimpleName().toString();
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);

    methodBuilder.addAnnotation(OVERRIDE);
    for (final AnnotationMirror mirror : method.getAnnotationMirrors()) {
      final AnnotationSpec annotationSpec = AnnotationSpec.get(mirror);
      if (annotationSpec.type.equals(OVERRIDE)) {
        continue;
      }
      methodBuilder.addAnnotation(annotationSpec);
    }

    modifiers = new LinkedHashSet<>(modifiers);
    modifiers.remove(Modifier.ABSTRACT);
    modifiers.remove(Modifier.DEFAULT);
    methodBuilder.addModifiers(modifiers);

    for (final TypeParameterElement typeParameterElement : method.getTypeParameters()) {
      final TypeVariable var = (TypeVariable) typeParameterElement.asType();
      methodBuilder.addTypeVariable(TypeVariableName.get(var));
    }

    methodBuilder.returns(TypeName.get(method.getReturnType()));

    final List<? extends VariableElement> parameters = method.getParameters();
    for (final VariableElement parameter : parameters) {
      final TypeName type = TypeName.get(parameter.asType());
      final String name = parameter.getSimpleName().toString();
      final Set<Modifier> parameterModifiers = parameter.getModifiers();
      final ParameterSpec.Builder parameterBuilder =
          ParameterSpec.builder(type, name).addModifiers(parameterModifiers.toArray(new Modifier[parameterModifiers.size()]));
      for (final AnnotationMirror mirror : parameter.getAnnotationMirrors()) {
        parameterBuilder.addAnnotation(AnnotationSpec.get(mirror));
      }
      methodBuilder.addParameter(parameterBuilder.build());
    }
    methodBuilder.varargs(method.isVarArgs());

    for (final TypeMirror thrownType : method.getThrownTypes()) {
      methodBuilder.addException(TypeName.get(thrownType));
    }

    return methodBuilder;
  }

  public MethodSpec forwardingMethod(final ExecutableElement methodElement) {
    final MethodSpec.Builder code = this.overriding(methodElement);
    if (this.style == FcStyle.INTERFACE) {
      code.addModifiers(Modifier.DEFAULT);
    }
    code.addCode(this.forwardingCall(methodElement));
    return code.build();
  }
}
