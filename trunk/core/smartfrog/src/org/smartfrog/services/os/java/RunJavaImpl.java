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
package org.smartfrog.services.os.java;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogInitException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.utils.PlatformHelper;
import org.smartfrog.services.os.runshell.RunShellImpl;

import java.rmi.RemoteException;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.io.File;

/**
 * created 21-May-2004 17:23:22
 */

public class RunJavaImpl extends RunShellImpl implements RunJava {

    PlatformHelper platform;

    public RunJavaImpl() throws RemoteException {
        platform = PlatformHelper.getLocalPlatform();
    }

    /**
     * Reads SF description = initial configuration.
     * Override this to read/set properties before we read ours, but remember to call
     * the superclass afterwards
     */
    protected void readSFAttributes() throws SmartFrogException, RemoteException {
        Vector args=new Vector();
        //get classname and classpath and verify that one is defined
        String classname=null;
        String jar=null;
        classname=sfResolve(varClassname,classname,false);
        jar= platform.convertFilename(sfResolve(varJarFile, jar, false));

        Vector environment = (Vector) sfResolve(varEnvironment, (Vector) null, false);
        Vector flatEnv = flatten(environment, null, "=", null);

        Vector jvmArgs=(Vector) sfResolve(varJVMArgs,(Vector)null,false);

        Vector sysProperties=(Vector)sfResolve(varSysProperties, (Vector) null, false);
        Vector flatSysProperties = flatten(sysProperties, "-D", "=", "");
        Boolean assertions=(Boolean) sfResolve(varAssertions,(Boolean)null,false);
        Boolean sysAssertions = (Boolean) sfResolve(varSystemAssertions, (Boolean) null, false);
        Vector arguments=sfResolve(varAssertions, (Vector) null, false);


        addArgs(args, flatSysProperties);
        addArgs(args, jvmArgs);

        //assertions
        if(assertions!=null && assertions.booleanValue()) {
            args.add("-ea:...");
        }
        if (sysAssertions != null && sysAssertions.booleanValue()) {
            args.add("-esa");
        }

        //classpath setup
        String classpath=buildClasspath(false);
        if(classpath!=null) {
            args.add("-classpath");
            args.add(classpath);
        }

        //endorsed dirs
        String endorsedDirs=buildEndorsedDirs();
        if(endorsedDirs!=null) {
            args.add("-Djava.endorsed.dirs="+endorsedDirs);
        }

        //set the classname or jar to run
        if (jar == null && classname == null) {
            throw new SmartFrogInitException("One of " + varClassname + " and " + varJarFile + " must be supplied");
        }
        if(classname!=null) {
            args.add(classname);
        } else {
            args.add("-jar");
            args.add(jar);
        }

        //add any command arguments
        addArgs(args,arguments);


        //now patch our attributes, ready for our parent class
        sfReplaceAttribute(varEnvProp, flatEnv);
        sfReplaceAttribute(varShellArguments, args);

        //and verify that the shell command is not empty
        String javacmd=sfResolve(varShellCommand,(String)null,true);

        super.readSFAttributes();
    }

    /**
     * append one collection to another, if the appendee is not empty
     * @param v one to append to
     * @param v2 one to append, can be null
     */
    private void addArgs(Collection v, Collection v2) {
        if(v2!=null) {
            v.addAll(v2);
        }
    }


    /**
     * take a vector of name value pairs like [["a",true],["b",3]] and create something like
     * ["a=true","b=3"] with configurable prefix, joiner and suffix strings
     * @param source
     * @param prefix
     * @param joiner
     * @param suffix
     * @return a merged/flattened array or null if an empty list came in
     * @throws SmartFrogInitException
     */
    private Vector flatten(List source,String prefix,String joiner,String suffix) throws SmartFrogInitException {
        if(source==null) {
            return null;
        }
        if(prefix==null) {
            prefix="";
        }
        if(joiner==null) {
            joiner="";
        }
        if(suffix==null) {
            suffix="";
        }
        Vector results=new Vector(source.size());
        Iterator it=source.listIterator();
        while (it.hasNext()) {
            Vector subvector= (Vector) it.next();
            if(subvector.size()!=2) {
                throw new SmartFrogInitException("Wrong number of list elements");
            }
            Iterator subit=subvector.iterator();
            String key = (String) subit.next();
            //take any value and stringify it -we dont care about its underlying type
            Object valueObj=(Object) subit.next();
            String value;
            value=valueObj.toString();
            String entry=prefix+key+joiner+value+suffix;
            results.add(entry);
        }
        return results;
    }

    /**
     * recursive support for classpaths, lets us cross reference more easily.
     * @param mandatory
     * @return
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     */
    String buildClasspath(boolean mandatory) throws SmartFrogResolutionException, RemoteException {
        Vector classpathV=(Vector)sfResolve(varClasspath,(Vector)null,mandatory);
        if(classpathV==null) {
            return null;
        }
        String path=makePath(classpathV);
        return path;
    }

    String buildEndorsedDirs() throws SmartFrogResolutionException, RemoteException {
        Vector pathV = (Vector) sfResolve(varEndorsedDirs, (Vector) null, false);
        if(pathV==null) {
            return null;
        }
        Vector classpathFlat=recursivelyFlatten(pathV);
        Iterator entries=classpathFlat.iterator();
        StringBuffer result= new StringBuffer();
        while (entries.hasNext()) {
            Object entry = (Object) entries.next();
            String dirname=entry.toString();
            //turn this into a directory if it is a file
            dirname=platform.convertFilename(dirname);
            File file=new File(dirname);
            if(file.exists() && file.isFile()) {
                String dir=file.getParent();
                if(dir!=null) {
                    dirname=dir;
                } else {
                    //there is no parent dir. what to do?
                }
            }
            result.append(dirname);
            result.append(platform.getPathSeparator());
        }
        String path = result.toString();
        return path;
    }

    /**
     * turn a path vector into a flat path string; use the given separator
     * @param pathVector
     * @return
     */
    private String makePath(Vector pathVector) {
        Vector classpathFlat=recursivelyFlatten(pathVector);
        Iterator entries=classpathFlat.iterator();
        StringBuffer result= new StringBuffer();
        while (entries.hasNext()) {
            Object entry = (Object) entries.next();
            String file=entry.toString();
            file= platform.convertFilename(file);
            result.append(file);
            result.append(platform.getPathSeparator());
        }
        return result.toString();
    }

    private Vector recursivelyFlatten(Collection in) {
        Vector flat=new Vector(in.size());
        Iterator flattener=in.iterator();
        while (flattener.hasNext()) {
            Object o = (Object) flattener.next();
            if(o instanceof Collection) {
                Collection c=(Collection)o;
                //todo
                Vector v=recursivelyFlatten(c);
                flat.addAll(v);
            } else {
                flat.add(o);
            }
        }
        return flat;
    }
}
