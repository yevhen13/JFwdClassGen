package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

final class FcInterfaceTypeFactory implements FcTypeFactory {
  private final FcTypeElement element;

  public FcInterfaceTypeFactory(final FcTypeElement element) {
    this.element = requireNonNull(element);
  }

  @Override
  public TypeSpec createType(final FcTypeContentProvider provider) {
    final String className = this.element.getForwardingClassName();
    final TypeSpec.Builder decoratorClass =
        TypeSpec.interfaceBuilder(className);
    decoratorClass.addAnnotation(FunctionalInterface.class);
    if (this.element.isPublic()) {
      decoratorClass.addModifiers(Modifier.PUBLIC);
    }
    decoratorClass.addSuperinterface(TypeName.get(this.element.asType()));
    decoratorClass.addFields(provider.fields());
    decoratorClass.addMethods(provider.methods());
    return decoratorClass.build();
  }
}
