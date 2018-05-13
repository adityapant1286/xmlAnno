package com.xmlanno.annotations;

import com.xmlanno.enums.DocFieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.LOCAL_VARIABLE)
public @interface XmlDocField {

    String name() default "";

    DocFieldType field() default DocFieldType.TEXT;
}
