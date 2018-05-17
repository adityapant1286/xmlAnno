package com.xmlanno.reflection.scanners;

import com.xmlanno.reflection.utils.FilterBuilder;

public class SubTypesScanner extends AbstractScanner {

    public SubTypesScanner()  {
        this(true);
    }

    public SubTypesScanner(boolean excludeObjectClass) {

        if (excludeObjectClass)
            filterResultsBy(new FilterBuilder().exclude(Object.class.getName()));
    }

    @Override
    public void scan(Object cls) {
        final String className = getMetadataAdapter().getClassName(cls);
        final String superclass = getMetadataAdapter().getSuperclassName(cls);

        if (acceptResult(superclass)) getWarehouse().put(superclass, className);

        getMetadataAdapter().getInterfacesNames(cls)
                .stream()
                .filter(anInterface -> acceptResult((String) anInterface))
                .forEach(anInterface -> getWarehouse().put((String) anInterface, className));
    }
}
