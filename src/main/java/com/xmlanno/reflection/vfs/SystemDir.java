package com.xmlanno.reflection.vfs;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.xmlanno.reflection.utils.XmlAnnoUtil;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static java.util.Objects.isNull;

public class SystemDir implements Vfs.Dir {

    private final File file;

    public SystemDir(File file) {
        if (isNull(file) && (!file.isDirectory() || !file.canRead()))
            throw new IllegalArgumentException("Invalid dir " + file.toString());
        this.file = file;
    }

    @Override
    public String getPath() { return isNull(file) ? "/NO-SUCH-DIRECTORY/" : file.getPath().replace("\\", "/"); }

    @Override
    public Iterable<Vfs.File> getFiles() {

        if (isNull(file) || !file.exists()) return Collections.emptyList();

        return () -> new AbstractIterator<Vfs.File>() {

            final Stack<File> stack = new Stack<>();
            { stack.addAll(listFiles(file)); }

            @Override
            protected Vfs.File computeNext() {

                while (!stack.isEmpty()) {
                    final File pop = stack.pop();

                    if (pop.isDirectory())
                        stack.addAll(listFiles(pop));
                    else
                        return new SystemFile(SystemDir.this, file);
                }

                return endOfData();
            }
        };
    }

    private static List<File> listFiles(File file) {
        final File[] files = file.listFiles();
        return isNull(files) ? Lists.newArrayList() : Lists.newArrayList(files);
    }

    @Override
    public void close() { throw new UnsupportedOperationException("Not supported"); }

    @Override
    public String toString() { return getPath(); }
}
