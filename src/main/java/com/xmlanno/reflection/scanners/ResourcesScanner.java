package com.xmlanno.reflection.scanners;

public class ResourcesScanner extends AbstractScanner {

    public boolean acceptInput(String file) { return !file.endsWith(".class"); }


    @Override
    public void scan(Object cls) {
        throw new UnsupportedOperationException();
    }
}
