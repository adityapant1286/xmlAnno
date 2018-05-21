package com.xmlanno.reflection.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SystemFile implements Vfs.File {

    private final SystemDir root;
    private final File file;

    public SystemFile(SystemDir root, File file) {
        this.root = root;
        this.file = file;
    }

    @Override
    public String getName() { return file.getName(); }

    @Override
    public String getRelativePath() {
        final String replace = file.getPath().replace("\\", "/");
        return replace.startsWith(root.getPath()) ? replace.substring(root.getPath().length() + 1) : null;
    }

    @Override
    public InputStream openInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) { throw new RuntimeException(e); }
    }

    @Override
    public String toString() { return file.toString(); }
}
