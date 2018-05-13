package com.xmlanno.annotations;

import com.xmlanno.enums.InputType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface XmlDoc {

    String root() default "xml";

    InputType type() default InputType.XML;
}
