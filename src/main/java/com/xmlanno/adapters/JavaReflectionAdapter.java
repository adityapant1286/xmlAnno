package com.xmlanno.adapters;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xmlanno.utils.XmlAnnoUtil.repeat;
import static java.util.Objects.isNull;

public class JavaReflectionAdapter implements MetadataAdapter<Class, Field, Member> {

    @Override
    public String getClassName(Class cls) { return cls.getName(); }

    @Override
    public String getSuperclassName(Class cls) { return cls.getSuperclass().getName(); }

    @Override
    public List<String> getInterfacesNames(Class cls) { return Stream.of(cls.getInterfaces()).map(Class::getName).collect(Collectors.toList()); }

    @Override
    public List<Field> getFields(Class cls) { return Lists.newArrayList(cls.getDeclaredFields()); }

    @Override
    public List<Member> getMethods(Class cls) {
        return Stream.concat(Stream.of(cls.getDeclaredMethods()), Stream.of(cls.getDeclaredConstructors()))
                     .collect(Collectors.toList());
    }

    @Override
    public String getMethodName(Member method) { return method instanceof Constructor ? "<init>" : method.getName(); }

    @Override
    public List<String> getParameterNames(Member member) {

//        List<String> result = Lists.newArrayList();
//
//        Class<?>[] parameterTypes = member instanceof Method
//                                        ? ((Method) member).getParameterTypes()
//                                        : member instanceof Constructor
//                                            ? ((Constructor) member).getParameterTypes()
//                                            : null;
//        if (parameterTypes != null) {
//            for (Class<?> paramType : parameterTypes) {
//                String name = getName(paramType);
//                result.add(name);
//            }
//        }
//
//        return result;

        return Stream.of(((Executable) member).getParameterTypes())
                    .map(JavaReflectionAdapter::getName)
                    .collect(Collectors.toList());
    }

    @Override
    public List<String> getClassAnnotationNames(Class aClass) { return getAnnotationNames(aClass.getAnnotations()); }

    @Override
    public List<String> getFieldAnnotationNames(Field field) { return getAnnotationNames(field.getDeclaredAnnotations()); }

    @Override
    public List<String> getMethodAnnotationNames(Member method) { return getAnnotationNames(((Executable) method).getDeclaredAnnotations()); }

    @Override
    public List<String> getParameterAnnotationNames(Member method, int parameterIndex) {

        return getAnnotationNames(Stream.of(((Executable) method).getParameterAnnotations())
                                        .collect(Collectors.toList())
                                        .get(parameterIndex));
    }

    @Override
    public String getReturnTypeName(Member method) { return ((Method) method).getReturnType().getName(); }

    @Override
    public String getFieldName(Field field) { return field.getName(); }

    @Override
    public String getMethodModifier(Member method) { return Modifier.toString(method.getModifiers()); }

    @Override
    public String getMethodKey(Class cls, Member method) { return String.format("%s (%s)", getMethodName(method), Joiner.on(", ").join(getParameterNames(method))); }

    @Override
    public String getMethodFullKey(Class cls, Member method) { return String.format("%s.%s", getClassName(cls), getMethodKey(cls, method)); }

    @Override
    public boolean isPublic(Object o) { return Modifier.isPublic(o instanceof Class ? ((Class) o).getModifiers() : ((Member) o).getModifiers()); }

    @Override
    public boolean acceptsInput(String file) { return file.endsWith(".class"); }

    private List<String> getAnnotationNames(Annotation[] annotations) {
        return Stream.of(annotations)
                    .map(a -> a.annotationType().getName())
                    .collect(Collectors.toList());
    }

    public static String getName(Class type) {

        if (type.isArray()) {
            try {
                Class cl = type;
                int dim = 0; while (cl.isArray()) { dim++; cl = cl.getComponentType(); }
                return cl.getName() + repeat("[]", dim);
            } catch (Throwable e) {
                // swallow
            }
        }
        return type.getName();
    }
}
