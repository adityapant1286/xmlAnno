package com.xmlanno.reflection.vfs;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.xmlanno.reflection.Reflections;
import com.xmlanno.reflection.ReflectionsException;
import com.xmlanno.reflection.utils.ClasspathHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

import static com.xmlanno.reflection.utils.XmlAnnoUtil.fileExists;
import static com.xmlanno.reflection.utils.XmlAnnoUtil.hasText;
import static java.util.Objects.nonNull;

public abstract class Vfs {

    private static List<UrlType> defaultUrlTypes = Lists.newArrayList(DefaultUrlTypes.values());

    /** an abstract vfs dir */
    public interface Dir {
        String getPath();
        Iterable<File> getFiles();
        void close();
    }

    /** an abstract vfs file */
    public interface File {
        String getName();
        String getRelativePath();
        InputStream openInputStream() throws IOException;
    }

    /** a matcher and factory for a url */
    public interface UrlType {
        boolean matches(URL url) throws Exception;
        Dir createDir(URL url) throws Exception;
    }

    /** the default url types that will be used when issuing {@link Vfs#fromURL(java.net.URL)} */
    public static List<UrlType> getDefaultUrlTypes() { return defaultUrlTypes; }

    /** sets the static default url types. can be used to statically plug in urlTypes */
    public static void setDefaultURLTypes(final List<UrlType> urlTypes) { defaultUrlTypes = urlTypes; }

    /** add a static default url types to the beginning of the default url types list. can be used to statically plug in urlTypes */
    public static void addDefaultURLTypes(UrlType urlType) { defaultUrlTypes.add(0, urlType); }

    /** tries to create a Dir from the given url, using the defaultUrlTypes */
    public static Dir fromURL(final URL url) { return fromURL(url, defaultUrlTypes); }

    /** tries to create a Dir from the given url, using the given urlTypes*/
    public static Dir fromURL(final URL url, final List<UrlType> urlTypes) {

        for (UrlType type : urlTypes) {
            try {
                if (type.matches(url)) {
                    Dir dir = type.createDir(url);
                    if (nonNull(dir)) return dir;
                }
            } catch (Throwable e) {

                if (nonNull(Reflections.log))
                    Reflections.log.warning(String.format("could not create Dir using %s from url %s. skipping.", type.toString(), url.toExternalForm()));

                e.printStackTrace();
            }
        }

        throw new ReflectionsException("could not create Vfs.Dir from url, no matching UrlType was found [" + url.toExternalForm() + "]\n" +
                "either use fromURL(final URL url, final List<UrlType> urlTypes) or " +
                "use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) " +
                "with your specialized UrlType.");
    }

    /** tries to create a Dir from the given url, using the given urlTypes*/
    public static Dir fromURL(final URL url, final UrlType... urlTypes) { return fromURL(url, Lists.newArrayList(urlTypes)); }

    /** return an iterable of all {@link Vfs.File} in given urls, starting with given packagePrefix and matching nameFilter */
    public static Iterable<File> findFiles(final Collection<URL> inUrls, final String packagePrefix, final Predicate<String> nameFilter) {

        Predicate<File> fileNamePredicate = file -> {
            String path = file.getRelativePath();
            if (path.startsWith(packagePrefix)) {
                String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());
                return hasText(filename) && nameFilter.apply(filename.substring(1));
            } else {
                return false;
            }
        };

        return findFiles(inUrls, fileNamePredicate);
    }

    /** return an iterable of all {@link Vfs.File} in given urls, matching filePredicate */
    public static Iterable<File> findFiles(final Collection<URL> inUrls, final Predicate<File> filePredicate) {

        Iterable<File> result = new ArrayList<>();

        for (final URL url : inUrls) {
            try {
                result = Iterables.concat(result,
                        Iterables.filter(() -> fromURL(url).getFiles().iterator(), filePredicate));
            } catch (Throwable e) {
                if (nonNull(Reflections.log))
                    Reflections.log.severe("could not findFiles for url. continuing. [" + url + "]");
                e.printStackTrace();
            }
        }

        return result;
    }

    /**try to get {@link java.io.File} from url*/
    public static @Nullable java.io.File getFile(URL url) {

        java.io.File file;
        String path;

        try {
            if (fileExists(url.toURI().getSchemeSpecificPart()))
                return new java.io.File(url.toURI().getSchemeSpecificPart());
        } catch (URISyntaxException e) {
        }

        try {
            path = URLDecoder.decode(url.getPath(), "UTF-8");
            if (path.contains(".jar!")) path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
            if ((file = new java.io.File(path)).exists()) return file;

        } catch (UnsupportedEncodingException e) {
        }

        try {
            path = url.toExternalForm();

            path = path.startsWith("jar:")
                    ? path.substring("jar:".length())
                    : path.startsWith("wsjar:")
                        ?  path.substring("wsjar:".length())
                        : path.startsWith("file:")
                            ? path.substring("file:".length())
                            : path.contains(".jar!")
                                ? path.substring(0, path.indexOf(".jar!") + ".jar".length())
                                : path.contains(".war!")
                                    ? path.substring(0, path.indexOf(".war!") + ".war".length())
                                    : path;

            //if (path.startsWith("jar:")) path = path.substring("jar:".length());
            //if (path.startsWith("wsjar:")) path = path.substring("wsjar:".length());
            //if (path.startsWith("file:")) path = path.substring("file:".length());
            //if (path.contains(".jar!")) path = path.substring(0, path.indexOf(".jar!") + ".jar".length());
            //if (path.contains(".war!")) path = path.substring(0, path.indexOf(".war!") + ".war".length());

            if (fileExists(path)) return new java.io.File(path);

            path = path.replace("%20", " ");

            if (fileExists(path)) return new java.io.File(path);

        } catch (Exception e) {
        }

        return null;
    }

    private static boolean hasJarFileInPath(URL url) { return url.toExternalForm().matches(".*\\.jar(\\!.*|$)"); }

    /** default url types used by {@link Vfs#fromURL(java.net.URL)}
     * <p>
     * <p>jarFile - creates a {@link ZipDir} over jar file
     * <p>jarUrl - creates a {@link ZipDir} over a jar url (contains ".jar!/" in it's name), using Java's {@link JarURLConnection}
     * <p>directory - creates a {@link SystemDir} over a file system directory
     * <p>jboss vfs - for protocols vfs, using jboss vfs (should be provided in classpath)
     * <p>jboss vfsfile - creates a {@link UrlTypeVfs} for protocols vfszip and vfsfile.
     * <p>bundle - for bundle protocol, using eclipse FileLocator (should be provided in classpath)
     * <p>jarInputStream - creates a {@link JarInputDir} over jar files, using Java's JarInputStream
     * */
    public static enum DefaultUrlTypes implements UrlType {

        jarFile {

            public boolean matches(URL url) { return url.getProtocol().equals("file") && hasJarFileInPath(url); }

            public Dir createDir(final URL url) throws Exception { return new ZipDir(new JarFile(getFile(url))); }
        },

        jarUrl {
            public boolean matches(URL url) { return "jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol()); }

            public Dir createDir(URL url) throws Exception {
                try {
                    URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection) {
                        urlConnection.setUseCaches(false);
                        return new ZipDir(((JarURLConnection) urlConnection).getJarFile());
                    }
                } catch (Throwable e) { /*fallback*/ }
                java.io.File file = getFile(url);

                return nonNull(file) ? new ZipDir(new JarFile(file)) : null;
            }
        },

        directory {
            public boolean matches(URL url) {
                if (url.getProtocol().equals("file") && !hasJarFileInPath(url)) {
                    java.io.File file = getFile(url);
                    return file != null && file.isDirectory();
                } else
                    return false;
            }

            public Dir createDir(final URL url) { return new SystemDir(getFile(url)); }
        },

        jboss_vfs {
            public boolean matches(URL url) { return url.getProtocol().equals("vfs"); }

            public Vfs.Dir createDir(URL url) throws Exception {
                Object content = url.openConnection().getContent();
                Class<?> virtualFile = ClasspathHelper.contextClassLoader().loadClass("org.jboss.vfs.VirtualFile");
                java.io.File physicalFile = (java.io.File) virtualFile.getMethod("getPhysicalFile").invoke(content);
                String name = (String) virtualFile.getMethod("getName").invoke(content);
                java.io.File file = new java.io.File(physicalFile.getParentFile(), name);
                if (!file.exists() || !file.canRead()) file = physicalFile;
                return file.isDirectory() ? new SystemDir(file) : new ZipDir(new JarFile(file));
            }
        },

        jboss_vfsfile {
            public boolean matches(URL url) throws Exception { return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol()); }

            public Dir createDir(URL url) throws Exception { return new UrlTypeVfs().createDir(url); }
        },

        bundle {
            public boolean matches(URL url) throws Exception { return url.getProtocol().startsWith("bundle"); }

            public Dir createDir(URL url) throws Exception {
                return fromURL((URL) ClasspathHelper.contextClassLoader().
                                                    loadClass("org.eclipse.core.runtime.FileLocator")
                                                    .getMethod("resolve", URL.class)
                                                    .invoke(null, url));
            }
        },

        jarInputStream {
            public boolean matches(URL url) throws Exception { return url.toExternalForm().contains(".jar"); }

            public Dir createDir(final URL url) throws Exception { return new JarInputDir(url); }
        }
    }
}
