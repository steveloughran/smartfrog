/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information: www.smartfrog.org

*/

package org.smartfrog.sfcore.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.server.RMIClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;


/**
 * Provides static methods to obtain a class loader used to download SmartFrog
 * components and their descriptions. If the system level property
 * org.smartfrog.codebase has been set to a URL (or semi-colon separated
 * URLs), it will return an RMI class loader with these addresses. Otherwise
 * it will return the context class loader of the current thread.
 */
public class SFClassLoader {
    /**
     * Name of the system property that specifies the URL (or semi-colon
     * separated URLS) from which we download componenents and their
     * descriptions.
     */
    public static final String SF_CODEBASE_PROPERTY = "org.smartfrog.codebase";

    /** Space separated urls from which we download the components */
    private static String targetClassBase = null;

    /** A debugging utility to print messages. */
    private static SFDebug debug;

    /**
     * Initializes the debugging.
     */
    static {
        debug = SFDebug.getInstance("SFClassLoader");
    }

    /**
     * Don't let anyone create one of these.
     */
    private SFClassLoader() {
    }

    /**
     * Loads (or reloads) from a system property from where we are downloading
     * components, represented by a space separated urls.
     */
    public synchronized static void loadTargetClassBase() {
        targetClassBase = System.getProperty(SF_CODEBASE_PROPERTY);
    }

    /**
     * Gets the codebase URL(s) path from a system property, formatted for the
     * RMIClassLoader syntax (space sparated URLs).
     *
     * @return a string containing the space separated URLs.
     */
    public synchronized static String getTargetClassBase() {
        if (targetClassBase == null) {
            loadTargetClassBase();
        }

        return targetClassBase;
    }
    /**
     * Gets a class loader for a particular codebase using an RMICLassLoader.If
     * a class loader with the same codebase URL path already exists,i.e., the
     * codebase did not change from the last invocation , it will be returned;
     * otherwise, a new class loader will be created. In any case, the
     * delegation parent of the URL class loader is the current thread context
     * class loader, so this one will always be queried. If the codebase is
     * null we return the current thread context class loader.
     *
     * @param codebase A codebase that defines the class loader.
     *
     * @return A class loader for that codebase.
     */
    static ClassLoader getClassLoader(String codebase) {
        if (codebase != null) {
            try {
                return RMIClassLoader.getClassLoader(codebase);
            } catch (Exception e) {
                //Just log and continue
                if (debug != null) {
                    debug.println("getClassLoader: Cannot get codebase " +
                        codebase + " getting exception " + e.getMessage());
                }
            }
        }

        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Returns a stream that points to a resource specified in a string,
     * typically a configuration file. This string could be either directly
     * loadable from our current class loaders or converted to a file-relative
     * URL. If security is activated, we check that it comes from a trusted
     * source and it has not been modified. We use the default codebase.
     *
     * @param resource name of the resource wanted.
     *
     * @return A stream to the resource or null if either is not available
     *         through our class loaders or does not fullfill the security
     *         requirements.
     */
    public static InputStream getResourceAsStream(String resource) {
        return getResourceAsStream(resource, null, true);
    }

    /**
     * Returns a stream that points to a resource specified in a string,
     * typically a configuration file. This string could be either directly
     * loadable from our current class loaders or converted to a file-relative
     * URL. If security is activated, we check that it comes from a trusted
     * source and it has not been modified.
     *
     * @param resource name of the resource wanted.
     * @param codebase suggested codebase for the classloader
     * @param useDefaultCodebase whether to try to find the class in the
     *        default codebase before using codebase.
     *
     * @return A stream to the resource or null if either is not available
     *         through our class loaders or does not fullfill the security
     *         requirements.
     */
    public static InputStream getResourceAsStream(String resource,
        String codebase, boolean useDefaultCodebase) {
        URL resourceURL;

        try {
            // Try first to directly generate a URL from resource
            resourceURL = new URL(resource);

            return getURLAsStream(resourceURL);
        } catch (Throwable e) {
            // Didn't work, the input is a malformed url or it is inside a
            // jar file and this is not explicit in the url.
            if (debug != null) {
                debug.println("getResourceAsStream:1 Cannot get url " +
                    e.getMessage());
            }
        }

        try {
            // Let's use a relative file path...
            resourceURL = stringToURL(resource);

            return getURLAsStream(resourceURL);
        } catch (Throwable e) {
            // Still in trouble, cannot obtain the resource from the file
            // system directly.
            if (debug != null) {
                debug.println("getResourceAsStream:2 Cannot get url " +
                    e.getMessage());
            }
        }

        // The preceeding / does not work when looking inside jar files.
        String resourceInJar = (resource.startsWith("/")
            ? resource.substring(1) : resource);

        // Try the class loaders
        Object result = classLoaderHelper(resourceInJar, codebase,
                useDefaultCodebase, false);

        return ((result instanceof InputStream) ? (InputStream) result : null);
    }

    /**
     * Takes a string and converts to a URL. If the string is not in URL format
     * an attempt is made to create a file based URL relative to where this
     * process started. If it is pointing to a jar file we use a jar-type URL.
     *
     * @param s string to convert
     *
     * @return URL form of the input string
     *
     * @throws Exception if failed to convert string to URL
     */
    static URL stringToURL(String s) throws Exception {
        try {
            return (((s.endsWith(".jar")) && (!(s.startsWith("jar:"))))
            ? new URL("jar:" + s + "!/") : new URL(s));
        } catch (Exception ex) {
            // ignore, try another one
        }

        String fUrl = (new File(s)).getAbsolutePath();
        fUrl = "file:" + (fUrl.startsWith("/") ? fUrl : ("/" + fUrl));

        return (((s.endsWith(".jar")) && (!(s.startsWith("jar:"))))
        ? new URL("jar:" + fUrl + "!/") : new URL(fUrl));
    }

    /**
     * Returns a stream pointing to a given resource specified by a URL after
     * performing security checks and making sure the resource exists.
     *
     * @param resourceURL URL of the input resource.
     *
     * @return Stream that points to that resource
     *
     * @throws Exception The resource does not exist or it does not meet the
     *            security requirements.
     */
    protected static InputStream getURLAsStream(URL resourceURL)
        throws Exception {
        URLConnection con = resourceURL.openConnection();
        InputStream in = getLocalInputStream(con);

        if (in != null) {
            return in;
        } else {
            // We want the caller to keep trying... 
            throw new Exception("SFClassLoader::getURLAsStream cannot find " +
                resourceURL);
        }
    }

    /**
     * Checks that the resource pointed by a URLConnection comes from a trusted
     * source, this is, it has been granted the SFCommunityPermission. If this
     * is not the case it throws a security exception. Then, it uses that URL
     * to obtain an input stream to a locally cached object.
     *
     * @param con URLConnection to the resource to be checked.
     *
     * @return Stream that point to the resource. If the resource is in a
     *         signed jar we return a ByteArrayInputStream to a local copy for
     *         security reasons.
     *
     * @throws Exception in case of any error
     */
    protected static InputStream getLocalInputStream(URLConnection con)
        throws Exception {
        InputStream in = con.getInputStream();
        Certificate[] certs = null;

        if (con instanceof JarURLConnection) {
            // Loaded from a jar file, let's add the certicates.
            JarURLConnection conJar = (JarURLConnection) con;

            // Need to read the full entry so that I can get the certificates.
            int numBytes = in.available();
            byte[] resourceBytes = new byte[numBytes];
            int readBytes = 0;

            while (readBytes != numBytes) {
                // Sometimes the read returns early... 
                readBytes += in.read(resourceBytes, readBytes,
                    numBytes - readBytes);
            }

            certs = conJar.getCertificates();

            // Need to return an InputStream to a local copy to avoid that
            // the entry changes after being checked.
            in = new ByteArrayInputStream(resourceBytes);
        }

        CodeSource cs = new CodeSource(con.getURL(), certs);
        Policy pc = Policy.getPolicy();
        PermissionCollection perms = ((pc == null) ? null : pc.getPermissions(cs));
        quickReject(new ProtectionDomain(cs, perms));

        // No security exception, continues...
        return in;
    }

    /**
     * Checks that the given protection domain include a permission that
     * ensures that is coming from a trusted origin (SFCommunityPermission).
     * If this is not the case, and security is active, it throws a security
     * exception.
     *
     * @param pd a protection domain that needs to be checked.
     */
    public static void quickReject(ProtectionDomain pd) {
        if ((pd != null) && (pd.implies(new SFCommunityPermission()))) {
            // Resource came from a trusted sourced, no problem.
            return;
        }

        if (SFSecurity.isSecurityOn()) {
            // Didn't pass, we should not load this resource.
            throw new SecurityException("SFClassLoader:quickReject: " +
                "access check failed for " + pd);
        } else {
            if (debug != null) {
                debug.println("WARNING!!:quickReject:  " +
                    "access check failed for " + pd);
            }
        }
    }

    /**
     * Checks that a class is coming from a trusted origin. If this is not the
     * case, and security is active, it throws a security exception. This
     * allows to check the origin of resources even if they do not perform any
     * security sensitive operation.
     *
     * @param cl Class whose origin we want to check.
     */
    public static void quickRejectClass(Class cl) {
        quickReject(cl.getProtectionDomain());
    }

    /**
     * Checks that an object comes from a  class of a trusted origin. If this
     * is not the case, and security is active, it throws a security
     * exception. This allows to check the origin of resources even if they do
     * not perform any security sensitive operation.
     *
     * @param obj Object whose origin we want to check.
     */
    public static void quickRejectObject(Object obj) {
        quickRejectClass(obj.getClass());
    }

    /**
     * A helper class to implement getResourceAsStream
     *
     * @param resourceInJar resource to be located
     * @param codebase used to locate the resource
     *
     * @return input stream to the resource
     *
     * @throws Exception if unable to locate the resource
     */
    static InputStream getResourceHelper(String resourceInJar, String codebase)
        throws Exception {
        ClassLoader cl = getClassLoader(codebase);

        return getURLAsStream(cl.getResource(resourceInJar));
    }

    /**
     * A helper class to implement forName.
     *
     * @param className fully qualified name of the desired class
     * @param codebase suggested codebase for the classloader
     *
     * @return class object representing the desired class
     *
     * @throws ClassNotFoundException if the class cannot be located
     */
    static Class forNameHelper(String className, String codebase)
        throws ClassNotFoundException {
        Class cl = Class.forName(className, true, getClassLoader(codebase));

        /* The classes referenced by cl, and loaded by the same class loader
           when cl is linked, are not checked here. This implies that if
           they are in a different jar file they might not have the same
           priviledges. For this reason, we have to use sealed packages
           and make jar files as self-contained as possible. */
        quickRejectClass(cl);

        return cl;
    }

    /**
     * Switch between loading operations.
     *
     * @param name Resource name to be loaded
     * @param codebase A suggested codebase to load this resource.
     * @param isForName whether we are loading a class or other resource
     *
     * @return A class or an input stream to the resource
     *
     * @throws Exception if any error
     */
    static Object opHelper(String name, String codebase, boolean isForName)
        throws Exception {
        if (isForName) {
            return (Object) forNameHelper(name, codebase);
        } else {
            return (Object) getResourceHelper(name, codebase);
        }
    }

    /**
     * A helper class that encapsulates resource loading behaviour using a
     * class loader.
     *
     * @param name Resource name to be loaded
     * @param codebase A suggested codebase to load this resource.
     * @param useDefaultCodebase Whether to look in the default codebase or
     *        not.
     * @param isForName whether we are loading a class or other resource
     *
     * @return A class or an input stream to the resource
     */
    static Object classLoaderHelper(String name, String codebase,
        boolean useDefaultCodebase, boolean isForName) {
        String msg = (isForName ? "forName" : "getResourceAsStream");

        // "default" equivalent to "not set".
        if ((codebase != null) && (codebase.equals("default"))) {
            codebase = null;
        }

        //First, try the thread context class loader
        try {
            return opHelper(name, null, isForName);
        } catch (Throwable e) {
            // Not valid, continuing ...
            if (debug != null) {
                debug.println(msg + "#1 cannot find object in thread CL " +
                    " getting exception " + e.getMessage());
            }
        }

        // Second try the default codebase (if enabled)
        if ((useDefaultCodebase) && (getTargetClassBase() != null)) {
            try {
                return opHelper(name, getTargetClassBase(), isForName);
            } catch (Throwable e) {
                // Not valid, continuing ...
                if (debug != null) {
                    debug.println(msg + "#2 cannot find object in " +
                        getTargetClassBase() + " getting exception " +
                        e.getMessage());
                }
            }
        }

        //Last, try the class loader for the suggested codebase
        if (codebase != null) {
            try {
                return opHelper(name, codebase, isForName);
            } catch (Throwable e) {
                // Not valid, continuing ...
                if (debug != null) {
                    debug.println(msg + "#3 cannot find object in " + codebase +
                        " getting exception " + e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * This method is equivalent to Class.forName but it uses the SmartFrog
     * class loader (optionally using a remote web server), and it checks
     * whether the class comes from a trusted origin. Note that the classes
     * referenced by this one, and loaded when this one is linked, are not
     * checked here.
     *
     * @param className fully qualified name of the desired class
     * @param codebase suggested codebase for the classloader
     * @param useDefaultCodebase whether to try to find the class in the
     *        default codebase before using codebase.
     *
     * @return class object representing the desired class
     *
     * @throws ClassNotFoundException if the class cannot be located
     */
    public static Class forName(String className, String codebase,
        boolean useDefaultCodebase) throws ClassNotFoundException {
        // Try the class loaders
        Object result = classLoaderHelper(className, codebase,
                useDefaultCodebase, true);

        if (result instanceof Class) {
            return (Class) result;
        }

        throw new ClassNotFoundException("forName: Cannot find " + className);
    }

    /**
     * This method is equivalent to Class.forName but it uses the SmartFrog
     * class loader (optionally using a remote web server), and it checks
     * whether the class comes from a trusted origin. Note that the classes
     * referenced by this one, and loaded when this one is linked, are not
     * checked here. We use the default codebase.
     *
     * @param className fully qualified name of the desired class
     *
     * @return class object representing the desired class
     *
     * @throws ClassNotFoundException if the class cannot be located
     */
    public static Class forName(String className) throws ClassNotFoundException {
        return forName(className, null, true);
    }
}
