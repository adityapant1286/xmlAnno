package com.xmlanno.reflection.vfs;

import com.google.common.base.Predicate;
import com.xmlanno.reflection.Reflections;
import com.xmlanno.reflection.ReflectionsException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class UrlTypeVfs implements Vfs.UrlType {

    public final static String[] REPLACE_EXTENSION = new String[]{".ear/", ".jar/", ".war/", ".sar/", ".har/", ".par/"};

    final String VFSZIP = "vfszip";
    final String VFSFILE = "vfsfile";
    final Pattern p;

    public UrlTypeVfs() { p = Pattern.compile("\\.[ejprw]ar/"); }

    public boolean matches(URL url) { return VFSZIP.equals(url.getProtocol()) || VFSFILE.equals(url.getProtocol()); }

    public Vfs.Dir createDir(final URL url) {
        try {

            return new ZipDir(new JarFile(adaptURL(url).getFile()));

        } catch (Exception e) {

            try {
                return new ZipDir(new JarFile(url.getFile()));
            } catch (IOException e1) {
                if (isNull(Reflections.log)) {
                    Reflections.log.warning("Could not get URL");
                    e.printStackTrace();
                    Reflections.log.warning("Could not get URL");
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    public URL adaptURL(URL url) throws MalformedURLException {

        return VFSZIP.equals(url.getProtocol())
                ? replaceZipSeparators(url.getPath(), realFile)
                : VFSFILE.equals(url.getProtocol())
                    ? new URL(url.toString().replace(VFSFILE, "file"))
                    : url;
    }

    URL replaceZipSeparators(String path, Predicate<File> acceptFile) throws MalformedURLException {

        int pos = 0;
        while (pos != -1) {
            pos = findFirstMatchOfDeployableExtention(path, pos);

            if (pos > 0) {
                File file = new File(path.substring(0, pos - 1));
                if (acceptFile.apply(file)) { return replaceZipSeparatorStartingFrom(path, pos); }
            }
        }

        throw new ReflectionsException("Unable to identify the real zip file in path '" + path + "'.");
    }

    int findFirstMatchOfDeployableExtention(String path, int pos) {
        Matcher m = p.matcher(path);
        return m.find(pos) ? m.end() : -1;
    }

    final Predicate<File> realFile = file -> file.exists() && file.isFile();

    URL replaceZipSeparatorStartingFrom(String path, int pos) throws MalformedURLException {
        String zipFile = path.substring(0, pos - 1);
        String zipPath = path.substring(pos);

        int numSubs = 1;
        for (String ext : REPLACE_EXTENSION) {
            while (zipPath.contains(ext)) {
                zipPath = zipPath.replace(ext, ext.substring(0, 4) + "!");
                numSubs++;
            }
        }

        String prefix = "";
        for (int i = 0; i < numSubs; i++)
            prefix += "zip:";

        return zipPath.trim().length() == 0
                ? new URL(prefix + "/" + zipFile)
                : new URL(prefix + "/" + zipFile + "!" + zipPath);
    }
}
