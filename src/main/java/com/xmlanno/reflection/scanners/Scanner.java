package com.xmlanno.reflection.scanners;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.xmlanno.reflection.configs.Configurations;

public interface Scanner {

    void setConfiguration(Configurations configuration);

    Multimap<String, String> getWarehouse();

    void setWarehouse(Multimap<String, String> warehouse);

    Scanner filterResultsBy(Predicate<String> filter);

    boolean acceptsInput(String file);

//    Object scan(Vfs.File file, @Nullable Object classObject);

    boolean acceptResult(String fqn);
}
