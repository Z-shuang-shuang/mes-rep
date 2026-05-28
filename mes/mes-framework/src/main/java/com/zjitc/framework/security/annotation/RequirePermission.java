// RequirePermission.java
package com.zjitc.framework.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String[] value();           // 需要的权限代码
    Logical logical() default Logical.AND;  // 权限逻辑：AND需要全部满足，OR满足任意一个

    public enum Logical {
        AND, OR
    }
}
