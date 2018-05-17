package com.xmlanno.reflection.vfs;

import java.net.URL;
import java.util.jar.JarInputStream;

public class JarInputDir implements Vfs.Dir {

    private final URL url;
    JarInputStream jarInputStream;

    long cursor = 0;
    long nextCursor = 0;

    public JarInputDir(URL url) { this.url = url; }

    @Override
    public String getPath() { return url.getPath(); }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return null;
    }

    @Override
    public void close() {

    }
}
