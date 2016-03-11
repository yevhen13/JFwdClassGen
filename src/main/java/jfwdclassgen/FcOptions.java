package jfwdclassgen;

import java.io.IOException;
import java.util.Objects;

final class FcOptions {
  private final ForwardingClass options;

  public FcOptions(final ForwardingClass options) throws IOException {
    this.options = Objects.requireNonNull(options);
    this.requireCorrectNaming(options.naming());
  }

  private void requireCorrectNaming(final String naming) throws IOException {
    if (naming.codePoints().filter(ch -> ch == '*').count() != 1) {
      throw new IOException(String.format("Annotation %s has incorrect naming pattern %s.", ForwardingClass.class.getName(), naming));
    }
  }

  public FcStyle getStyle() {
    return this.options.style();
  }

  public String getNewClassName(final String className) {
    return this.options.naming().replace("*", className);
  }

  public boolean isPublic() {
    return this.options.isPublic();
  }
}
