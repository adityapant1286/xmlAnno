package com.xmlanno.reflection.scanners;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import com.xmlanno.adapters.MetadataAdapter;
import com.xmlanno.configs.Configurations;


public abstract class AbstractScanner implements Scanner {

    private Configurations configuration;
    private Multimap<String, String> warehouse;
    private Predicate<String> resultFilter = Predicates.alwaysTrue(); //accept all by default

    public boolean acceptsInput(String file) { return getMetadataAdapter().acceptsInput(file); }

//    public Object scan(Vfs.File file, Object classObject) {
//        if (classObject == null) {
//            try {
//                classObject = configuration.getMetadataAdapter().getOrCreateClassObject(file);
//            } catch (Exception e) {
//                throw new ReflectionsException("could not create class object from file " + file.getRelativePath(), e);
//            }
//        }
//        scan(classObject);
//        return classObject;
//    }

    public abstract void scan(Object cls);

    public Configurations getConfiguration() { return configuration; }

    public void setConfiguration(final Configurations configuration) { this.configuration = configuration; }

    public Multimap<String, String> getWarehouse() { return warehouse; }

    public void setWarehouse(final Multimap<String, String> warehouse) { this.warehouse = warehouse; }

    public Predicate<String> getResultFilter() { return resultFilter; }

    public void setResultFilter(Predicate<String> resultFilter) { this.resultFilter = resultFilter; }

    public Scanner filterResultsBy(Predicate<String> filter) { this.setResultFilter(filter); return this; }

    public boolean acceptResult(final String fqn) { return fqn != null && resultFilter.apply(fqn); }

    protected MetadataAdapter getMetadataAdapter() { return configuration.getMetadataAdapter(); }

    public boolean equals(Object o) { return this == o || o != null && getClass() == o.getClass(); }

    public int hashCode() { return getClass().hashCode(); }
}
