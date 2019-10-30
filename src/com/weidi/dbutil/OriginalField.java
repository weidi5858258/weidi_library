package com.weidi.dbutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 Created by root on 16-7-30.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OriginalField {

    /***
     数据库中的字段对应java bean中的属性名.
     如果将来某个时候想要改一下数据库中的某个字段,
     那么只要改一下java bean中的属性名,然后在这个属性上添加此注解.
     使用方式:
     @OriginalField(value = "name")
     这个name是没改之前的属性名
     */
    String value() default "";

}