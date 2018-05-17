package com.xmlanno.reflection.adapters;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xmlanno.reflection.utils.XmlAnnoUtil.hasText;
import static java.util.Objects.isNull;
import static javassist.Modifier.isPrivate;
import static javassist.Modifier.isProtected;

public class JavaAssistAdapter implements MetadataAdapter<ClassFile, FieldInfo, MethodInfo> {

    public static boolean INCLUDE_INVISIBLE_TAG = true;

    @Override
    public String getClassName(ClassFile cls) { return cls.getName(); }

    @Override
    public String getSuperclassName(ClassFile cls) { return cls.getSuperclass(); }

    @Override
    public List<String> getInterfacesNames(ClassFile cls) { return Arrays.asList(cls.getInterfaces()); }

    @Override
    public List<FieldInfo> getFields(ClassFile cls) { return cls.getFields(); }

    @Override
    public List<MethodInfo> getMethods(ClassFile cls) { return cls.getMethods(); }

    @Override
    public String getMethodName(MethodInfo method) { return method.getName(); }

    @Override
    public List<String> getParameterNames(MethodInfo method) {
        String descriptor = method.getDescriptor().trim();

        descriptor = descriptor.substring(descriptor.indexOf("(") + 1,
                                            descriptor.lastIndexOf(")"));

        return splitDescriptorToTypeNames(descriptor);
    }

    @Override
    public List<String> getClassAnnotationNames(ClassFile aClass) {

        final List<AnnotationsAttribute> attributes = new ArrayList<>();

        attributes.add((AnnotationsAttribute) aClass.getAttribute(AnnotationsAttribute.visibleTag));

        if (INCLUDE_INVISIBLE_TAG)
            attributes.add((AnnotationsAttribute) aClass.getAttribute(AnnotationsAttribute.invisibleTag));

        return getAnnotationNames(attributes.toArray(new AnnotationsAttribute[0]));
    }

    @Override
    public List<String> getFieldAnnotationNames(FieldInfo field) {

        final List<AnnotationsAttribute> attributes = new ArrayList<>();

        attributes.add((AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.visibleTag));

        if (INCLUDE_INVISIBLE_TAG)
            attributes.add((AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.invisibleTag));

        return getAnnotationNames(attributes.toArray(new AnnotationsAttribute[0]));
    }

    @Override
    public List<String> getMethodAnnotationNames(MethodInfo method) {
        final List<AnnotationsAttribute> attributes = new ArrayList<>();

        attributes.add((AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag));

        if (INCLUDE_INVISIBLE_TAG)
            attributes.add((AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.invisibleTag));

        return getAnnotationNames(attributes.toArray(new AnnotationsAttribute[0]));
    }

    @Override
    public List<String> getParameterAnnotationNames(MethodInfo method, int parameterIndex) {

        final List<ParameterAnnotationsAttribute> attributes = new ArrayList<>();

        attributes.add((ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.visibleTag));
        attributes.add((ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.invisibleTag));

        // filter non null -> get all annotations -> filter only having size greater than index -> collect annotation names

        return attributes.stream()
                .filter(a -> !isNull(a))
                .map(ParameterAnnotationsAttribute::getAnnotations)
                .filter(aa -> parameterIndex < aa.length)
                .map(an -> getAnnotationNames(an[parameterIndex]))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getReturnTypeName(MethodInfo method) {

        String descriptor = method.getDescriptor();
        descriptor = descriptor.substring(descriptor.lastIndexOf(")") + 1);

        return splitDescriptorToTypeNames(descriptor).get(0);
    }

    @Override
    public String getFieldName(FieldInfo field) { return field.getName(); }

    @Override
    public String getMethodModifier(MethodInfo method) {

        int accessFlags = method.getAccessFlags();

        return isPrivate(accessFlags)
                ? "private"
                : isProtected(accessFlags)
                    ? "protected"
                    : isPublic(accessFlags)
                        ? "public"
                        : "";
    }

    @Override
    public String getMethodKey(ClassFile cls, MethodInfo method) {
        return String.format("%s(%s)",
                            getMethodName(method), Joiner.on(", ").join(getParameterNames(method)));
    }

    @Override
    public String getMethodFullKey(ClassFile cls, MethodInfo method) { return String.format("$s.%s", getClassName(cls), getMethodKey(cls, method)); }

    @Override
    public boolean isPublic(Object o) {

        Integer accessFlag = o instanceof ClassFile
                                ? ((ClassFile) o).getAccessFlags()
                                : o instanceof FieldInfo
                                    ? ((FieldInfo) o).getAccessFlags()
                                    : o instanceof MethodInfo
                                        ? ((MethodInfo) o).getAccessFlags()
                                        : null;

        return accessFlag != null && AccessFlag.isPublic(accessFlag);
    }

    @Override
    public boolean acceptsInput(String file) { return file.endsWith(".class"); }

    private List<String> getAnnotationNames(final AnnotationsAttribute... annotationsAttributes) {

        return Stream.of(annotationsAttributes)
                    .flatMap(aa -> Stream.of(aa.getAnnotations()))
                    .filter(an -> !isNull(an))
                    .map(Annotation::getTypeName)
                    .collect(Collectors.toList());
    }

    private List<String> getAnnotationNames(final Annotation[] annotations) {

        return Stream.of(annotations)
                    .map(Annotation::getTypeName)
                    .collect(Collectors.toList());
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
