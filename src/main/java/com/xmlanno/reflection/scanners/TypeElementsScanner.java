package com.xmlanno.reflection.scanners;

import com.google.common.base.Joiner;

public class TypeElementsScanner extends AbstractScanner {

    private boolean includeFields = true;
    private boolean includeMethods = true;
    private boolean includeAnnotations = true;
    private boolean publicOnly = true;

    @Override
    public void scan(final Object cls) {

        final String className = getMetadataAdapter().getClassName(cls);
        if (!acceptResult(className)) return;

        getWarehouse().put(className, "");

        if (includeFields) includeFieldsToWarehouse(className, cls);

        if (includeMethods) includeMethodsToWarehouse(className, cls);

        if (includeAnnotations) includeAnnotationsToWarehouse(className, cls);
    }

    private void includeFieldsToWarehouse(final String className, Object cls) {
        getMetadataAdapter().getFields(cls)
                .forEach(field -> getWarehouse().put(className, getMetadataAdapter().getFieldName(field)));
    }

    private void includeMethodsToWarehouse(final String className, Object cls) {
        getMetadataAdapter().getMethods(cls)
                .stream()
                .filter(method -> !publicOnly || getMetadataAdapter().isPublic(method))
                .forEach(method -> getWarehouse().put(className,
                        String.format("%s(%s)", getMetadataAdapter().getMethodName(method),
                                Joiner.on(", ").join(getMetadataAdapter().getParameterNames(method)))
                        )
                );
    }

    private void includeAnnotationsToWarehouse(final String className, Object cls) {
        getMetadataAdapter().getClassAnnotationNames(cls)
                .forEach(ann -> getWarehouse().put(className, "@" + ann));
    }

    public TypeElementsScanner includeFields() { return includeFields(true); }
    public TypeElementsScanner includeFields(boolean include) { includeFields = include; return this; }
    public TypeElementsScanner includeMethods() { return includeMethods(true); }
    public TypeElementsScanner includeMethods(boolean include) { includeMethods = include; return this; }
    public TypeElementsScanner includeAnnotations() { return includeAnnotations(true); }
    public TypeElementsScanner includeAnnotations(boolean include) { includeAnnotations = include; return this; }
    public TypeElementsScanner publicOnly(boolean only) { publicOnly = only; return this; }
    public TypeElementsScanner publicOnly() { return publicOnly(true); }
}
