package com.xmlanno.reflection.scanners;

import java.lang.annotation.Inherited;

public class TypeAnnotationsScanner extends AbstractScanner {

    @Override
    public void scan(Object cls) {
        final String className = getMetadataAdapter().getClassName(cls);

        getMetadataAdapter().getClassAnnotationNames(cls)
                .stream()
                .filter(annType -> acceptResult((String) annType) || annType.equals(Inherited.class.getName()))
                .forEach(annType -> getWarehouse().put((String) annType, className));
    }
}
