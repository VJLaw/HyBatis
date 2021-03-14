package cn.com.sandi.genericdb.annotation;

import java.lang.annotation.*;


@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldName{
    String value() default "";
}
