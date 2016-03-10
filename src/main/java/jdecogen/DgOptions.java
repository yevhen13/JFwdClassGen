package jdecogen;

import java.io.IOException;
import java.util.Objects;

final class DgOptions {
  private final Decorator options;

  public DgOptions(final Decorator options) throws IOException {
    this.options = Objects.requireNonNull(options);
    this.requireCorrectNaming(options.naming());
  }

  private void requireCorrectNaming(final String naming) throws IOException {
    if (naming.codePoints().filter(ch -> ch == '*').count() != 1) {
      throw new IOException(String.format("Annotation %s has incorrect naming pattern %s.", Decorator.class.getName(), naming));
    }
  }

  public DgStyle getStyle() {
    return this.options.style();
  }

  public String applyNaming(final String className) {
    return this.options.naming().replace("*", className);
  }

  public boolean isPublic() {
    return this.options.isPublic();
  }
}
