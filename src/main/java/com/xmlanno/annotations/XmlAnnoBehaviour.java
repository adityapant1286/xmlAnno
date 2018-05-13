package com.xmlanno.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlAnnoBehaviour {

//    /**
//     * A class on which XmlDoc behaviour should be applied
//     */
//    public Class<?> type();

    /**
     * <pre>
     *  Java methods to be excluded from XmlDoc behaviour.
     *
     *  If there are overloaded methods, then they will be excluded too.
     *  If one or more methods are also part of inclusion list,
     *  then it will be disregarded from exclusions.
     * </pre>
     * Use this to improve performance
     *
     */
    String[] exclusions() default {};

    /**
     * <pre>
     *  Java methods to be included for XmlDoc behaviour.
     *
     *  If there are overloaded methods, then they will be included too
     *  If one or more methods are also part of exclusion list,
     *  then it will be disregarded from exclusions.
     * </pre>
     * Use this to improve performance
     *
     */
    String[] inclusions() default {};
}
