package jdecogen;

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.util.Types;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

final class DgClassFactory {
  private final DgTypeElement element;
  private final Types typeUtils;

  private final DgMethodFactory methodFactory;

  public DgClassFactory(final DgTypeElement element, final Types typeUtils) {
    this.element = requireNonNull(element);
    this.typeUtils = requireNonNull(typeUtils);
    this.methodFactory = new DgMethodFactory(element.asType(), element.getStyle());
  }

  private void appendDelegationMethods(final DgStyle style, final TypeSpec.Builder decoratorClass) {
    decoratorClass.addMethod(this.methodFactory.getDelegateMethod());
    this.element.getMethods(this.typeUtils).forEach(method -> decoratorClass.addMethod(this.methodFactory.decorate(method)));
  }

  public TypeSpec createDecoratorClass() {
    final DgStyle style = this.element.getStyle();
    final String className = this.element.getDecoratorClassName();
    final TypeSpec.Builder decoratorClass;
    switch (style) {
      case INTERFACE:
        decoratorClass = TypeSpec.interfaceBuilder(className);
        decoratorClass.addAnnotation(FunctionalInterface.class);
        break;
      default:
      case ABSTRACT_CLASS:
        decoratorClass = TypeSpec.classBuilder(className);
        decoratorClass.addModifiers(Modifier.ABSTRACT);
        break;
    }
    if (this.element.isPublic()) {
      decoratorClass.addModifiers(Modifier.PUBLIC);
    }
    decoratorClass.addSuperinterface(TypeName.get(this.element.asType()));
    this.appendDelegationMethods(style, decoratorClass);
    return decoratorClass.build();
  }
}
