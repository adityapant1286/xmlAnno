package com.xmlanno.reflection.configs;

import com.xmlanno.reflection.adapters.MetadataAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

/**
 * a fluent builder for {@link Configurations}, to be used for constructing a {@link com.xmlanno.reflection.Reflections} instance
 * <p>usage:
 * <pre>
 *      new Reflections(
 *          new ConfigurationBuilder()
 *              .filterInputsBy(new FilterBuilder().include("your project's common package prefix here..."))
 *              .setUrls(ClasspathHelper.forClassLoader())
 *              .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(myClassAnnotationsFilter)));
 * </pre>
 * <br>{@link #executorService} is used optionally used for parallel scanning. if value is null then scanning is done in a simple for loop
 * <p>defaults: accept all for {@link #inputsFilter},
 * {@link #executorService} is null,
 * {@link #serializer} is {@link org.reflections.serializers.XmlSerializer}
 */
public class ConfigBuilder implements Configurations {

    @Nonnull private Set<Scanner> scanners;
    @Nonnull private Set<URL> urls;
    /*lazy*/ protected MetadataAdapter metadataAdapter;
    @Nullable private Predicate<String> inputsFilter;
    /*lazy*/ private Serializer serializer;
    @Nullable private ExecutorService executorService;
    @Nullable private ClassLoader[] classLoaders;
    private boolean expandSuperTypes = true;

    @Override
    public Set<Scanner> getScanners() {
        return null;
    }

    @Override
    public Set<URL> getUrls() {
        return null;
    }

    @Override
    public MetadataAdapter getMetadataAdapter() {
        return null;
    }

    @Override
    public Predicate<String> getInputsFilter() {
        return null;
    }

    @Override
    public ExecutorService getExecutorService() {
        return null;
    }

    @Nullable
    @Override
    public ClassLoader[] getClassLoaders() {
        return new ClassLoader[0];
    }

    @Override
    public boolean shouldExpandSuperTypes() {
        return false;
    }
}
