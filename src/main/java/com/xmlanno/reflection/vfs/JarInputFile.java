package com.xmlanno.reflection.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class JarInputFile implements Vfs.File {

    private final ZipEntry entry;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getRelativePath() {
        return null;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return null;
    }
}
