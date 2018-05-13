package com.xmlanno.reflection.scanners;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.xmlanno.configs.Configurations;

public interface Scanner {

    void setConfiguration(Configurations configuration);

    Multimap<String, String> getWarehouse();

    void setStore(Multimap<String, String> store);

    Scanner filterResultsBy(Predicate<String> filter);

    boolean acceptsInput(String file);

//    Object scan(Vfs.File file, @Nullable Object classObject);

    boolean acceptResult(String fqn);
}
