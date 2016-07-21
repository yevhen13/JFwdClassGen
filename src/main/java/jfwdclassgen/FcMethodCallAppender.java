package jfwdclassgen;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import com.squareup.javapoet.CodeBlock;

final class FcMethodCallAppender {
  private final CodeBlock.Builder code;

  public FcMethodCallAppender(final CodeBlock.Builder code) {
    this.code = requireNonNull(code, "code == null");
  }

  private void appendArguments(
      final Collection<? extends VariableElement> argumentElements) {
    final Iterator<? extends VariableElement> it = argumentElements.iterator();
    while (it.hasNext()) {
      final VariableElement parameterElement = it.next();
      String parameterFormat = "$N";
      if (it.hasNext()) {
        parameterFormat += ", ";
      }
      this.code.add(parameterFormat,
          parameterElement.getSimpleName().toString());
    }
  }

  public void append(final ExecutableElement methodElement) {
    final String methodName = methodElement.getSimpleName().toString();
    this.code.add(".$N(", methodName);
    appendArguments(methodElement.getParameters());
    this.code.add(");");
  }
}
