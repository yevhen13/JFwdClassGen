package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

public enum FcStyle {
  INTERFACE(FcInterfaceStyleFactory::new),
  ABSTRACT_CLASS(FcAbstractClassStyleFactory::new),
  CONSTRUCTOR(FcContructorStyleFactory::new);

  private final Function<FcTypeElement, FcStyleFactory> factory;

  private FcStyle(final Function<FcTypeElement, FcStyleFactory> factory) {
    this.factory = requireNonNull(factory, "factory == null");
  }

  public FcStyleFactory createFactory(final FcTypeElement element) {
    return this.factory.apply(element);
  }
}
