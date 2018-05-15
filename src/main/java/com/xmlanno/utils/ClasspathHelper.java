package com.xmlanno.utils;

import com.xmlanno.reflection.Reflections;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xmlanno.utils.XmlAnnoUtil.isEmpty;
import static com.xmlanno.utils.XmlAnnoUtil.toLinkedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class ClasspathHelper {

    /**
     * Gets the current thread context class loader.
     * {@code Thread.currentThread().getContextClassLoader()}.
     *
     * @return the context class loader, may be null
     */
    public static ClassLoader contextClassLoader() { return Thread.currentThread().getContextClassLoader(); }

    /**
     * Gets the class loader of this library.
     * {@code Reflections.class.getClassLoader()}.
     *
     * @return the static library class loader, may be null
     */
    public static ClassLoader staticClassLoader() { return Reflections.class.getClassLoader(); }

    /**
     * Returns an array of class Loaders initialized from the specified array.
     * <p>
     * If the input is null or empty, it defaults to both {@link #contextClassLoader()} and {@link #staticClassLoader()}
     *
     * @return the array of class loaders, not null
     */
    public static ClassLoader[] classLoaders(final ClassLoader... classLoaders) {

        if (!isEmpty(classLoaders))
            return classLoaders;
        else {

            ClassLoader contextClassLoader = contextClassLoader(), staticClassLoader = staticClassLoader();


            return !isNull(contextClassLoader)
                    ? !isNull(staticClassLoader) && contextClassLoader != staticClassLoader
                        ? new ClassLoader[]{contextClassLoader, staticClassLoader}
                        : new ClassLoader[]{contextClassLoader}
                    : new ClassLoader[]{};
        }
    }

    /**
     * Returns a distinct collection of URLs based on a package name.
     * <p>
     * This searches for the package name as a resource, using {@link ClassLoader#getResources(String)}.
     * For example, {@code forPackage(org.reflections)} effectively returns URLs from the
     * classpath containing packages starting with {@code org.reflections}.
     * <p>
     * If the optional {@link ClassLoader}s are not specified, then both {@link #contextClassLoader()}
     * and {@link #staticClassLoader()} are used for {@link ClassLoader#getResources(String)}.
     * <p>
     * The returned URLs retainsthe order of the given {@code classLoaders}.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forPackage(final String name, final ClassLoader... classLoaders) { return forResource(resourceName(name), classLoaders); }

    /**
     * Returns a distinct collection of URLs based on a resource.
     * <p>
     * This searches for the resource name, using {@link ClassLoader#getResources(String)}.
     * For example, {@code forResource(test.properties)} effectively returns URLs from the
     * classpath containing files of that name.
     * <p>
     * If the optional {@link ClassLoader}s are not specified, then both {@link #contextClassLoader()}
     * and {@link #staticClassLoader()} are used for {@link ClassLoader#getResources(String)}.
     * <p>
     * The returned URLs retains the order of the given {@code classLoaders}.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forResource(final String resourceName, final ClassLoader... classLoaders) {
        final List<URL> result = new ArrayList<>();
        final ClassLoader[] loaders = classLoaders(classLoaders);

        Stream.of(loaders).forEach(classLoader -> {

            try {
                final Enumeration<URL> urls = classLoader.getResources(resourceName);

                while (urls.hasMoreElements()) {

                    final URL url = urls.nextElement();
                    int index = url.toExternalForm().lastIndexOf(resourceName);
                    if (index != -1) {
                        // Add old url as contextUrl to support exotic url handlers
                        result.add(new URL(url, url.toExternalForm().substring(0, index)));
                    } else {
                        result.add(url);
                    }
                }
            } catch (IOException e) {
                if (nonNull(Reflections.log)) Reflections.log.severe("error getting resources for " + resourceName);
                e.printStackTrace();
            }
        });
        return distinctUrls(result);
    }

    /**
     * Returns the URL that contains a {@code Class}.
     * <p>
     * This searches for the class using {@link ClassLoader#getResource(String)}.
     * <p>
     * If the optional {@link ClassLoader}s are not specified, then both {@link #contextClassLoader()}
     * and {@link #staticClassLoader()} are used for {@link ClassLoader#getResources(String)}.
     *
     * @return the URL containing the class, null if not found
     */
    public static URL forClass(final Class<?> aClass, final ClassLoader... classLoaders) {

        final ClassLoader[] loaders = classLoaders(classLoaders);
        final String resourceName = aClass.getName().replace(".", "/") + ".class";

        Optional<URL> url1 = Stream.of(loaders)
                .map(classLoader -> classLoader.getResource(resourceName))
                .filter(Objects::nonNull)
                .map(url -> {
                    String externalForm = url.toExternalForm();
                    try {
                        return new URL(externalForm.substring(0, externalForm.lastIndexOf(aClass.getPackage()
                                .getName()
                                .replace(".", "/"))));
                    } catch (MalformedURLException e) {
                        if (nonNull(Reflections.log)) Reflections.log.warning("Could not get URL");
                        e.printStackTrace();
                        return  null;
                    }
                }).findFirst();

        return url1.get();
//        for (ClassLoader classLoader : loaders) {
//            try {
//                final URL url = classLoader.getResource(resourceName);
//                if (url != null) {
//                    final String normalizedUrl = url.toExternalForm().substring(0, url.toExternalForm().lastIndexOf(aClass.getPackage().getName().replace(".", "/")));
//                    return new URL(normalizedUrl);
//                }
//            } catch (MalformedURLException e) {
//                if (Reflections.log != null) {
//                    Reflections.log.warn("Could not get URL", e);
//                }
//            }
//        }

    }

    /**
     * Returns a distinct collection of URLs based on URLs derived from class loaders.
     * <p>
     * This finds the URLs using {@link URLClassLoader#getURLs()} using both
     * {@link #contextClassLoader()} and {@link #staticClassLoader()}.
     * <p>
     * The returned URLs retains the order of the given {@code classLoaders}.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forClassLoader() { return forClassLoader(classLoaders()); }

    /**
     * Returns a distinct collection of URLs based on URLs derived from class loaders.
     * <p>
     * This finds the URLs using {@link URLClassLoader#getURLs()} using the specified
     * class loader, searching up the parent hierarchy.
     * <p>
     * If the optional {@link ClassLoader}s are not specified, then both {@link #contextClassLoader()}
     * and {@link #staticClassLoader()} are used for {@link ClassLoader#getResources(String)}.
     * <p>
     * The returned URLs retains the order of the given {@code classLoaders}.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forClassLoader(final ClassLoader... classLoaders) {

        final Collection<URL> result = new ArrayList<>();
        final ClassLoader[] loaders = classLoaders(classLoaders);

        Stream.of(loaders)
                .forEach(classLoader -> {
                    while (nonNull(classLoader)) {
                        if (classLoader instanceof URLClassLoader) {
                            final URL[] urls = ((URLClassLoader) classLoader).getURLs();
                            if (nonNull(urls))
                                result.addAll(Arrays.asList(urls));
                        }
                        classLoader = classLoader.getParent();
                    }
                });

//        for (ClassLoader classLoader : loaders) {
//            while (classLoader != null) {
//                if (classLoader instanceof URLClassLoader) {
//                    URL[] urls = ((URLClassLoader) classLoader).getURLs();
//                    if (urls != null) {
//                        result.addAll(Arrays.asList(urls));
//                    }
//                }
//                classLoader = classLoader.getParent();
//            }
//        }
        return distinctUrls(result);
    }

    /**
     * Returns a distinct collection of URLs based on the {@code java.class.path} system property.
     * <p>
     * This finds the URLs using the {@code java.class.path} system property.
     * <p>
     * The returned collection of URLs retains the classpath order.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forJavaClassPath() {

        final Collection<URL> urls = new ArrayList<>();
        final String javaClassPath = System.getProperty("java.class.path");

        if (nonNull(javaClassPath))
            urls.addAll(Stream.of(javaClassPath.split(File.pathSeparator))
                            .map(path -> {
                                try {
                                    return new File(path).toURI().toURL();
                                } catch (MalformedURLException e) {
                                    if (nonNull(Reflections.log)) Reflections.log.warning("Could not get URL");
                                    e.printStackTrace();
                                    return null;
                                }
                            })
                            .collect(Collectors.toList()));

//            for (String path : javaClassPath.split(File.pathSeparator)) {
//                try {
//                    urls.add(new File(path).toURI().toURL());
//                } catch (Exception e) {
//                    if (nonNull(Reflections.log)) {
//                        Reflections.log.warn("Could not get URL", e);
//                    }
//                }
//            }

        return distinctUrls(urls);
    }

    /**
     * Returns a distinct collection of URLs based on the {@code WEB-INF/lib} folder.
     * <p>
     * This finds the URLs using the {@link ServletContext}.
     * <p>
     * The returned URLs retains the order of the given {@code classLoaders}.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forWebInfLib(final ServletContext servletContext) {

//        final Collection<URL> urls = new ArrayList<>();
        final Set<?> resourcePaths = servletContext.getResourcePaths("/WEB-INF/lib");
        if (isNull(resourcePaths))
            return new ArrayList<>();

        final List<URL> urls = resourcePaths.stream().map(urlStr -> {
            try {
                return servletContext.getResource((String) urlStr);
            } catch (MalformedURLException e) {
                if (nonNull(Reflections.log)) Reflections.log.severe("Unable to get resource from servlet context resource path /WEB-INF/lib");
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());

//        for (Object urlString : resourcePaths) {
//            try {
//                urls.add(servletContext.getResource((String) urlString));
//            } catch (MalformedURLException e) {
//            }
//        }
        return distinctUrls(urls);
    }

    /**
     * Returns the URL of the {@code WEB-INF/classes} folder.
     * <p>
     * This finds the URLs using the {@link ServletContext}.
     *
     * @return the collection of URLs, not null
     */
    public static URL forWebInfClasses(final ServletContext servletContext) {

        try {
            final String path = servletContext.getRealPath("/WEB-INF/classes");
            if (nonNull(path)) {
                final File file = new File(path);
                if (file.exists()) return file.toURI().toURL();
            } else
                return servletContext.getResource("/WEB-INF/classes");

        } catch (MalformedURLException e) {
            if (nonNull(Reflections.log)) Reflections.log.severe("Error in getting URL from /WEB-INF/classes");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a distinct collection of URLs based on URLs derived from class loaders expanded with Manifest information.
     * <p>
     * The {@code MANIFEST.MF} file can contain a {@code Class-Path} entry that defines
     * additional jar files to be included on the classpath. This method finds the jar files
     * using the {@link #contextClassLoader()} and {@link #staticClassLoader()}, before
     * searching for any additional manifest classpaths.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forManifest() { return forManifest(forClassLoader()); }

    /**
     * Returns a distinct collection of URLs from a single URL based on the Manifest information.
     * <p>
     * The {@code MANIFEST.MF} file can contain a {@code Class-Path} entry that defines additional
     * jar files to be included on the classpath. This method takes a single URL, tries to
     * resolve it as a jar file, and if so, adds any additional manifest classpaths.
     * The returned collection of URLs will always contain the input URL.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forManifest(final URL url) {

        final Collection<URL> result = new ArrayList<>();

        result.add(url);
        try {
            final String part = cleanPath(url);
            File jarFile = new File(part);
            JarFile myJar = new JarFile(part);
            URL validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), part);
            if (validUrl != null) {
                result.add(validUrl);
            }
            final Manifest manifest = myJar.getManifest();
            if (manifest != null) {
                final String classPath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                if (classPath != null) {
                    for (String jar : classPath.split(" ")) {
                        validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), jar);
                        if (validUrl != null) {
                            result.add(validUrl);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // don't do anything, we're going on the assumption it is a jar, which could be wrong
        }
        return distinctUrls(result);
    }

    /**
     * Returns a distinct collection of URLs by expanding the specified URLs with Manifest information.
     * <p>
     * The {@code MANIFEST.MF} file can contain a {@code Class-Path} entry that defines additional
     * jar files to be included on the classpath. This method takes each URL in turn, tries to
     * resolve it as a jar file, and if so, adds any additional manifest classpaths.
     * The returned collection of URLs will always contain all the input URLs.
     * <p>
     * The returned URLs retains the input order.
     *
     * @return the collection of URLs, not null
     */
    public static Collection<URL> forManifest(final Iterable<URL> urls) {

        final Collection<URL> result = new ArrayList<>();

        // determine if any of the URLs are JARs, and get any dependencies
        urls.forEach(url -> result.addAll(forManifest(url)));

        return distinctUrls(result);
    }

    //a little bit cryptic...
    static URL tryToGetValidUrl(final String workingDir, final String path, final String filename) {

        try {

            return exists(filename)
                    ? Paths.get(filename).toUri().toURL()
                    : exists(path + File.separator + filename)
                        ? Paths.get(path + File.separator + filename).toUri().toURL()
                        : exists(workingDir + File.separator + filename)
                            ? Paths.get(workingDir + File.separator + filename).toUri().toURL()
                            : exists(new URL(filename).getFile())
                                ? Paths.get(new URL(filename).getFile()).toUri().toURL()
                                : null;

//            if (new File(filename).exists())
//                return new File(filename).toURI().toURL();
//            if (new File(path + File.separator + filename).exists())
//                return new File(path + File.separator + filename).toURI().toURL();
//            if (new File(workingDir + File.separator + filename).exists())
//                return new File(workingDir + File.separator + filename).toURI().toURL();
//            if (new File(new URL(filename).getFile()).exists())
//                return new File(new URL(filename).getFile()).toURI().toURL();
        } catch (MalformedURLException e) {
            // don't do anything, we're going on the assumption it is a jar, which could be wrong
        }
        return null;
    }

    private static boolean exists(final String path) { return Files.exists(Paths.get(path)); }

    /**
     * Cleans the URL.
     *
     * @param url the URL to clean, not null
     * @return the path, not null
     */
    public static String cleanPath(final URL url) {

        String path = url.getPath();

        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) { }

        return path.startsWith("jar:")
                ? path.substring("jar:".length())
                : path.startsWith("file:")
                    ? path.substring("file:".length())
                    : path.endsWith("!/")
                        ? path.substring(0, path.lastIndexOf("!/")) + "/"
                        : path;
    }

    private static String resourceName(String name) {

        if (nonNull(name)) {

            String resourceName = name.replace(".", "/")
                                        .replace("\\", "/");

            if (resourceName.startsWith("/"))
                resourceName = resourceName.substring(1);

            return resourceName;
        }
        return null;
    }

    //http://michaelscharf.blogspot.co.il/2006/11/javaneturlequals-and-hashcode-make.html
    private static Collection<URL> distinctUrls(Collection<URL> urls) {

        return urls.stream()
                    .filter(Objects::nonNull)
                    .collect(LinkedHashMap::new,
                            (map, url) -> map.put(url.toExternalForm(), url),
                            LinkedHashMap<String, URL>::putAll).values();
    }
}
