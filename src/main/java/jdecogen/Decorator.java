package jdecogen;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(TYPE)
public @interface Decorator {
  String naming() default "*Decorator";

  DgStyle style() default DgStyle.ABSTRACT_CLASS;

  boolean isPublic() default true;
}
