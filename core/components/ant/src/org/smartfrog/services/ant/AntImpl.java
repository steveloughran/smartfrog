/** (C) Copyright Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.security.SFGeneralSecurityException;
import org.smartfrog.sfcore.security.SecureRemoteObject;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.utils.SmartFrogThread;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 */
public class AntImpl extends PrimImpl implements Prim, Ant, Runnable {
    private AntProject antProject;
    private AntRuntimeImpl runtime;
    private Prim propertyTarget;
    private SmartFrogException caughtException;
    private volatile boolean exitAntNow = false;
    private SmartFrogThread worker;


    /**
     * Constructor for the Ant object.
     *
     * @throws RemoteException In case of network/rmi error
     */
    public AntImpl() throws RemoteException {
    }

    // LifeCycle methods

    /**
     * @throws SmartFrogException In case of error in deploying
     * @throws RemoteException    In case of network/rmi error
     */
    @Override
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();
        LogSF log = sfLog();
        antProject = new AntProject(this, log);
        runtime = new AntRuntimeImpl(this);
        //export the runtime
        try {
            SecureRemoteObject.exportObject(runtime);
        } catch (SFGeneralSecurityException e) {
            throw SmartFrogDeploymentException.forward(e);
        }
        //now replace the attribute
        sfReplaceAttribute(ATTR_RUNTIME, runtime);
    }


    /**
     * Iterate through all nested tasks and run them
     * @throws RemoteException
     * @throws SmartFrogException
     */
    private void executeNestedAntTasks() throws RemoteException, SmartFrogException {
        Object attribute = null;
        Object value = null;
        Iterator<Object> a = sfAttributes();
        try {
            for (Iterator<Object> i = sfValues(); i.hasNext() && !exitAntNow;) {
                attribute = a.next();
                value = i.next();
                String attributeName = attribute.toString();
                String message = "Error executing: " + attributeName;
                if (value instanceof ComponentDescription) {
                    try {
                        try {
                            if (((ComponentDescription) value).sfContainsAttribute(ATTR_TASK_NAME)) {
                                Task task = antProject.getTask(attributeName, (ComponentDescription) value);
                                task.execute();
                            } else if (((ComponentDescription) value).sfContainsAttribute(ATTR_ANT_ELEMENT)) {
                                Object element = antProject.getElement(attributeName, (ComponentDescription) value);
                                sfLog().warn("Ignoring: something with elements/datatypes" + attributeName);
                            } else {
                                sfLog().debug("Ignoring: something with attribute: " + attributeName + " " + value + ";");
                            }
                        } catch (SmartFrogResolutionException e) {
                            throw e;
                        } catch (IllegalAccessException e) {
                            throw SmartFrogException.forward(message, e);
                        } catch (InstantiationException e) {
                            throw SmartFrogException.forward(message, e);
                        } catch (NoSuchMethodException e) {
                            throw SmartFrogException.forward(message, e);
                        } catch (InvocationTargetException e) {
                            throw new SmartFrogAntBuildException(e);
                        } catch (ClassNotFoundException e) {
                            throw SmartFrogException.forward(message, e);
                        } catch (SmartFrogAntBuildException e) {
                            throw e;
                        } catch (BuildException e) {
                            throw new SmartFrogAntBuildException(e);
                        }
                    } catch (Throwable ex) {
                        Throwable thr = ex;
                        if (thr instanceof InvocationTargetException) {
                            thr = ex.getCause();
                            throw new SmartFrogAntBuildException(thr);
                        }
                        throw SmartFrogException.forward(message, thr);
                    }
                }
            }
        } finally {
            //set the static properties after we finish
            Hashtable<String, String> results = antProject.getProject().getProperties();
            runtime.setStaticProperties(results);
            //copy them to any property target specified
            if (propertyTarget != null) {
                AntRuntimeImpl.propagateAntProperties(propertyTarget, results);
            }
        }

    }

    /**
     * @throws SmartFrogException In case of error while starting
     * @throws RemoteException    In case of network/rmi error
     */
    @Override
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        propertyTarget = sfResolve(ATTR_PROPERTY_TARGET, propertyTarget, false);
        boolean asynch = false;
        if ((sfResolve(ATTR_ASYNCH, asynch, asynch))) {
            worker = new SmartFrogThread(this);
            worker.start();
        } else {
            exec();
        }

    }

    /**
     * @param t TerminationRecord object
     */
    @Override
    public synchronized void sfTerminateWith(TerminationRecord t) {
        shutDownAnt();
        super.sfTerminateWith(t);
    }

    // End LifeCycle methods

    // Read Attributes from description


    // Main component action methods

    /**
     * Get a property from ant.
     * @param name the ant property
     * @return the property; or null for no match or no ant
     */
    public synchronized String getAntProperty(String name) {
        if (antProject != null) {
            return antProject.getProject().getProperty(name);
        }
        return null;
    }

    /**
     * shut ant down; get the properties and set them as static things under the runtime
     * add ant.exit.code
     */
    private synchronized void shutDownAnt() {
        //code to end ant
        if (antProject == null) {
            return;
        }
        exitAntNow = true;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        exec();
    }

    /**
     * Executes Ant Tasks and triggers the detach and/or termination of a component
     * according to the values of the boolean attributes 'sfShouldDetach', 'sfShouldTerminate'
     * and 'sfShouldTerminateQuietly', or if the job failed.
     */
    private void exec() {
        try {
            executeNestedAntTasks();
        } catch (RemoteException e) {
            caughtException = SmartFrogException.forward(e);
        } catch (SmartFrogException e) {
            caughtException = e;
        }
        ComponentHelper helper = new ComponentHelper(this);
        if (caughtException == null) {
            //cleanup time
            helper.sfSelfDetachAndOrTerminate(
                    null,
                    "end of ant tasks",
                    null,
                    null
            );
        } else {
            //something went wrong, always fail here
            TerminationRecord tr = new TerminationRecord(
                    TerminationRecord.ABNORMAL,
                    caughtException.toString(),
                    sfCompleteName,
                    caughtException);
            //put up for termination
            helper.targetForTermination(tr, false, false, false);
        }
    }

    /**
     * Liveness call in to check if this component is still alive.
     *
     * As well as passing the call up to the parent, any caught exception from the Ant run
     * will trigger a liveness failure.
     *
     * @param source source of call
     * @throws SmartFrogLivenessException component is terminated
     * @throws RemoteException for consistency with the {@link org.smartfrog.sfcore.prim.Liveness} interface
     */
    public void sfPing(Object source) throws SmartFrogLivenessException, RemoteException {
        super.sfPing(source);
        if (caughtException != null) {
            throw (SmartFrogLivenessException) SmartFrogLivenessException.forward(caughtException);
        }
    }
}
