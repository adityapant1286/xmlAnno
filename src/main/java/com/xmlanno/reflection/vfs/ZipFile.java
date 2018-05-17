package com.xmlanno.reflection.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class ZipFile implements Vfs.File {

    private final ZipDir root;
    private final ZipEntry entry;

    public ZipFile(ZipDir root, ZipEntry entry) {
        this.root = root;
        this.entry = entry;
    }

    @Override
    public String getName() {
        final String name = entry.getName();
        return name.substring(name.lastIndexOf("/") + 1);
    }

    @Override
    public String getRelativePath() { return entry.getName(); }

    @Override
    public InputStream openInputStream() throws IOException {
        return root.jarFile.getInputStream(entry);
    }

    @Override
    public String toString() { return String.format("%s!%s", root.getPath(), File.separatorChar + entry.toString()); }
}
