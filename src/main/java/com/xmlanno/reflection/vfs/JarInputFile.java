package com.xmlanno.reflection.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class JarInputFile implements Vfs.File {

    private final ZipEntry entry;
    private final JarInputDir jarInputDir;
    private final long fromIndex;
    private final long endIndex;

    public JarInputFile(ZipEntry entry, JarInputDir jarInputDir, long fromIndex, long endIndex) {
        this.entry = entry;
        this.jarInputDir = jarInputDir;
        this.fromIndex = fromIndex;
        this.endIndex = endIndex;
    }

    @Override
    public String getName() { final String name = entry.getName(); return name.substring(name.lastIndexOf("/") + 1); }

    @Override
    public String getRelativePath() { return entry.getName(); }

    @Override
    public InputStream openInputStream() throws IOException {
        return new InputStream() {
            @Override
            public int read() throws IOException {

                if (jarInputDir.cursor >= fromIndex
                        && jarInputDir.cursor <= endIndex) {

                    int read = jarInputDir.jarInputStream.read();
                    jarInputDir.cursor++;
                    return read;
                }
                return -1;
            }
        };
    }
}
