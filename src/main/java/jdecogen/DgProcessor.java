package jdecogen;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.JavaFile;

public final class DgProcessor extends AbstractProcessor {
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(Decorator.class.getName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  private void processElement(final DgTypeElement typeElement) throws IOException {
    JavaFile.builder(typeElement.getPackageName(), new DgClassFactory(typeElement, this.processingEnv.getTypeUtils()).createDecoratorClass())
      .build()
      .writeTo(this.processingEnv.getFiler());
  }

  private void requireInterface(final TypeElement typeElement) throws IOException {
    if (typeElement.getKind() != ElementKind.INTERFACE) {
      throw new IOException(String.format("Annotation %s is only supported for interfaces.", Decorator.class.getName()));
    }
  }

  public static <T> Optional<T> castAs(final Class<T> type, final Object value) {
    return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
  }

  public static <T> Stream<T> toStream(final Optional<T> value) {
    return value.isPresent() ? Stream.of(value.get()) : Stream.empty();
  }

  private static Stream<TypeElement> getTypes(final Collection<? extends Element> elements) {
    return elements.stream().map(it -> castAs(TypeElement.class, it)).flatMap(it -> toStream(it));
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    DgProcessor.getTypes(roundEnv.getElementsAnnotatedWith(Decorator.class)).forEach(typeElement -> {
      try {
        this.requireInterface(typeElement);
        this.processElement(new DgTypeElement(typeElement));
      } catch (final IOException e) {
        this.processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage(), typeElement);
      }
    });
    return true;
  }
}
