package com.zjitc.framework.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
    Logical logical() default Logical.AND;

    public enum Logical {
        AND, OR
    }
}
