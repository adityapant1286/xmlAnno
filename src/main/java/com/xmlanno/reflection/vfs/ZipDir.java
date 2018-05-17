package com.xmlanno.reflection.vfs;

import com.google.common.collect.AbstractIterator;
import com.xmlanno.reflection.Reflections;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static java.util.Objects.nonNull;

public class ZipDir implements Vfs.Dir {

    final java.util.zip.ZipFile jarFile;

    public ZipDir(JarFile jarFile) { this.jarFile = jarFile; }

    @Override
    public String getPath() { return jarFile.getName(); }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return () -> new AbstractIterator<Vfs.File>() {
            final Enumeration<? extends ZipEntry> entries = jarFile.entries();

            protected Vfs.File computeNext() {
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) return new ZipFile(ZipDir.this, entry);
                }

                return endOfData();
            }
        };
    }

    @Override
    public void close() {
        try {
            if (nonNull(jarFile)) jarFile.close();
        } catch (IOException e) {
            if (nonNull(Reflections.log))
                Reflections.log.warning("Could not close JarFile");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() { return getPath(); }
}
