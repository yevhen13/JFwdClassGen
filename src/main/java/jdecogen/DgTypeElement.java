package jdecogen;

import java.io.IOException;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

final class DgTypeElement {
  private final TypeElement typeElement;
  private final DgOptions options;

  public DgTypeElement(final TypeElement typeElement) throws IOException {
    this.typeElement = typeElement;
    this.options = new DgOptions(this.typeElement.getAnnotation(Decorator.class));
  }

  public TypeMirror asType() {
    return this.typeElement.asType();
  }

  private static PackageElement getEnclosingPackage(Element element) {
    while (element.getKind() != ElementKind.PACKAGE) {
      element = element.getEnclosingElement();
    }
    return (PackageElement) element;
  }

  public String getPackageName() {
    return getEnclosingPackage(this.typeElement).getQualifiedName().toString();
  }

  public String getClassName() {
    return this.typeElement.getSimpleName().toString();
  }

  public String getDecoratorClassName() {
    return this.options.applyNaming(this.getClassName());
  }

  public DgStyle getStyle() {
    return this.options.getStyle();
  }

  public boolean isPublic() {
    return this.options.isPublic();
  }

  private static Stream<ExecutableElement> getDeclaredMethods(final TypeElement typeElement) {
    return typeElement.getEnclosedElements().stream().filter(it -> it.getKind() == ElementKind.METHOD).map(ExecutableElement.class::cast);
  }

  private Stream<ExecutableElement> getDeclaredMethods() {
    return DgTypeElement.getDeclaredMethods(this.typeElement);
  }

  private static Stream<TypeElement> getSuperInterfaces(final TypeElement typeElement, final Types typeUtils) {
    return typeElement.getInterfaces()
      .stream()
      .map(typeUtils::asElement)
      .map(TypeElement.class::cast)
      .flatMap(it -> Stream.concat(Stream.of(it), getSuperInterfaces(it, typeUtils)));
  }

  private Stream<TypeElement> getSuperInterfaces(final Types typeUtils) {
    return getSuperInterfaces(this.typeElement, typeUtils);
  }

  private Stream<ExecutableElement> getInheritedMethods(final Types typeUtils) {
    return this.getSuperInterfaces(typeUtils).flatMap(DgTypeElement::getDeclaredMethods);
  }

  public Stream<ExecutableElement> getMethods(final Types typeUtils) {
    return Stream.concat(this.getDeclaredMethods(), this.getInheritedMethods(typeUtils));
  }
}
