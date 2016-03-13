package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.util.Types;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

final class FcClassFactory {
  private final FcTypeElement element;
  private final Types typeUtils;

  private final FcMethodFactory methodFactory;

  public FcClassFactory(final FcTypeElement element, final Types typeUtils) {
    this.element = requireNonNull(element);
    this.typeUtils = requireNonNull(typeUtils);
    this.methodFactory = new FcMethodFactory(element.asType(), element.getStyle());
  }

  private void appendForwardingMethods(final FcStyle style, final TypeSpec.Builder decoratorClass) {
    decoratorClass.addMethod(this.methodFactory.getDelegateMethod());
    this.element.getMethods(this.typeUtils).forEach(method -> decoratorClass.addMethod(this.methodFactory.forwardingMethod(method)));
  }

  public TypeSpec createForwardingClass() {
    final FcStyle style = this.element.getStyle();
    final String className = this.element.getForwardingClassName();
    final TypeSpec.Builder decoratorClass;
    switch (style) {
      case INTERFACE:
        decoratorClass = TypeSpec.interfaceBuilder(className);
        decoratorClass.addAnnotation(FunctionalInterface.class);
        break;
      default:
      case CONSTRUCTOR:
      case ABSTRACT_CLASS:
        decoratorClass = TypeSpec.classBuilder(className);
        decoratorClass.addModifiers(Modifier.ABSTRACT);
        break;
    }
    if (this.element.isPublic()) {
      decoratorClass.addModifiers(Modifier.PUBLIC);
    }
    decoratorClass.addSuperinterface(TypeName.get(this.element.asType()));
    this.appendForwardingMethods(style, decoratorClass);
    return decoratorClass.build();
  }
}
