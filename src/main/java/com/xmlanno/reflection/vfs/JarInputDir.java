package com.xmlanno.reflection.vfs;

import com.google.common.collect.AbstractIterator;
import com.xmlanno.reflection.ReflectionsException;
import com.xmlanno.reflection.utils.XmlAnnoUtil;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import static java.util.Objects.isNull;

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


        return () -> new AbstractIterator<Vfs.File>() {
            @Override
            protected Vfs.File computeNext() {

                while (true) {

                    try {

                        ZipEntry entry = jarInputStream.getNextJarEntry();

                        if (isNull(entry)) return endOfData(); // return

                        long size = entry.getSize();

                        if (size < 0) size += 0xffffffffl;

                        nextCursor += size;

                        if (!entry.isDirectory()) return new JarInputFile(entry, JarInputDir.this, cursor, nextCursor); // return

                    } catch (IOException e) { throw new ReflectionsException("Could not get next Zip entry", e); }
                }
            }
        };
    }

    @Override
    public void close() { XmlAnnoUtil.close(jarInputStream); }
}
