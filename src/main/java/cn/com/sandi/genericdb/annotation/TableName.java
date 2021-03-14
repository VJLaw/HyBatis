package cn.com.sandi.genericdb.annotation;

import java.lang.annotation.*;


@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableName{
    String value() default "";
}
