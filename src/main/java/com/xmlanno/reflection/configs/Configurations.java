package com.xmlanno.reflection.configs;

import com.xmlanno.reflection.adapters.MetadataAdapter;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

public interface Configurations {

    Set<Scanner> getScanners();

    Set<URL> getUrls();

    MetadataAdapter getMetadataAdapter();

    Predicate<String> getInputsFilter();

    ExecutorService getExecutorService();

//    Serializer getSerializer();

    /** get class loaders, might be used for resolving methods/fields */
    @Nullable
    ClassLoader[] getClassLoaders();

    /** if true (default), expand super types after scanning, for super types that were not scanned.
     * <p>see {@link org.reflections.Reflections#expandSuperTypes()}*/
    boolean shouldExpandSuperTypes();
}
