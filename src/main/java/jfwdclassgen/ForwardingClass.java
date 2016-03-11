package jfwdclassgen;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(TYPE)
public @interface ForwardingClass {
  String naming() default "Forwarding*";

  FcStyle style() default FcStyle.ABSTRACT_CLASS;

  boolean isPublic() default true;
}
