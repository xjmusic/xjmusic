package io.xj.hub.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HubAccessLevel {
  HubAccessType value() default HubAccessType.PERMIT_ALL;
}

