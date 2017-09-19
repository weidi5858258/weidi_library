package com.weidi.dbutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by root on 16-7-30.
 * @DbVersion(version = 1)
 * 其值必须是大于等于1的的整数
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassVersion {

    int version() default 1;

}