package com.xmlanno.adapters;

import com.google.common.collect.Lists;
import com.xmlanno.utils.XmlAnnoUtil;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xmlanno.utils.XmlAnnoUtil.hasText;
import static java.util.Objects.isNull;

public class JavaAssistAdapter implements MetadataAdapter<ClassFile, FieldInfo, MethodInfo> {

    public static boolean INCLUDE_INVISIBLE_TAG = true;

    @Override
    public String getClassName(ClassFile cls) {
        return null;
    }

    @Override
    public String getSuperclassName(ClassFile cls) {
        return null;
    }

    @Override
    public List<String> getInterfacesNames(ClassFile cls) {
        return null;
    }

    @Override
    public List<FieldInfo> getFields(ClassFile cls) { return cls.getFields(); }

    @Override
    public List<MethodInfo> getMethods(ClassFile cls) { return cls.getMethods(); }

    @Override
    public String getMethodName(MethodInfo method) { return method.getName(); }

    @Override
    public List<String> getParameterNames(MethodInfo method) {
        return null;
    }

    @Override
    public List<String> getClassAnnotationNames(ClassFile aClass) {
        return null;
    }

    @Override
    public List<String> getFieldAnnotationNames(FieldInfo field) {
        return null;
    }

    @Override
    public List<String> getMethodAnnotationNames(MethodInfo method) {
        return null;
    }

    @Override
    public List<String> getParameterAnnotationNames(MethodInfo method, int parameterIndex) {
        return null;
    }

    @Override
    public String getReturnTypeName(MethodInfo method) {
        return null;
    }

    @Override
    public String getFieldName(FieldInfo field) {
        return null;
    }

    @Override
    public String getMethodModifier(MethodInfo method) {
        return null;
    }

    @Override
    public String getMethodKey(ClassFile cls, MethodInfo method) {
        return null;
    }

    @Override
    public String getMethodFullKey(ClassFile cls, MethodInfo method) {
        return null;
    }

    @Override
    public boolean isPublic(Object o) {
        return false;
    }

    @Override
    public boolean acceptsInput(String file) {
        return false;
    }


    private List<String> getAnnotationNames(final AnnotationsAttribute... annotationsAttributes) {

        return Stream.of(annotationsAttributes)
                    .flatMap(aa -> Stream.of(aa.getAnnotations()))
                    .filter(an -> !isNull(an))
                    .map(Annotation::getTypeName)
                    .collect(Collectors.toList());

//
//        List<String> result = Lists.newArrayList();
//
//        if (annotationsAttributes != null) {
//            for (AnnotationsAttribute annotationsAttribute : annotationsAttributes) {
//                if (annotationsAttribute != null) {
//                    for (Annotation annotation : annotationsAttribute.getAnnotations()) {
//                        result.add(annotation.getTypeName());
//                    }
//                }
//            }
//        }
//
//        return result;
    }

    private List<String> getAnnotationNames(final Annotation[] annotations) {

        return Stream.of(annotations)
                    .map(Annotation::getTypeName)
                    .collect(Collectors.toList());

//        List<String> result = Lists.newArrayList();
//
//        for (Annotation annotation : annotations) {
//            result.add(annotation.getTypeName());
//        }
//
//        return result;
    }

    private List<String> splitDescriptorToTypeNames(final String descriptors) {

        final List<String> result = Lists.newArrayList();

        if (hasText(descriptors)) {

            final List<Integer> indices = new ArrayList<>();
            final Descriptor.Iterator iterator = new Descriptor.Iterator(descriptors);
            while (iterator.hasNext())
                indices.add(iterator.next());

            indices.add(descriptors.length());

            for (int i = 0; i < indices.size() - 1; i++)
                result.add(Descriptor.toString(descriptors.substring(indices.get(i), indices.get(i + 1))));
        }

        return result;
    }
}
