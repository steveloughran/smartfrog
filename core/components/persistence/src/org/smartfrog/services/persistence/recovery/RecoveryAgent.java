/** (C) Copyright 1998-2005 Hewlett-Packard Development Company, LP
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

package org.smartfrog.services.persistence.recovery;

import java.io.File;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;

import org.smartfrog.services.persistence.recoverablecomponent.RComponent;
import org.smartfrog.services.persistence.storage.Storage;
import org.smartfrog.services.persistence.storage.StorageException;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.ContextImpl;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.compound.Compound;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.processcompound.SFProcess;


/**
 *
 * A RecoveryAgent is a Component responsible for recovering basic
 * components in the system. What it does now is to deploy the components
 * found in the stable storage directory under its own ProcessCompound.
 *
 * @todo This class is tailored for a berkley db storage implementation. It should be split into a generic class and remove the implementation specific parts.
 *
 */
public class RecoveryAgent extends CompoundImpl implements Compound {

    Recoverer recoverer = null;
    String classname = null;
    ComponentDescription configData = null;


    private class Recoverer extends Thread {

        public void run() {
            String dirname = null;
            try {
                dirname = (String) configData.sfResolve("wfStorageRepository");
                System.out.println("Recovery agent using repository " + dirname);
            } catch (Exception exc) {
                System.out.println("No attribute wfStorageRepository found.");
                return;
            }

            File dir = new File(dirname);

//          The recovery agent used to be inside a while loop, periodically restarted failed components.
//
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException exc) {}

                File[] filenames = dir.listFiles();

                if (filenames == null) {
                    continue;
                }

                for (int i = 0; i < filenames.length; i++) {
                    if (filenames[i].isDirectory()) {
                        try {
                            System.out.println("***** Using repository " + dir.getAbsolutePath());
                            Storage storage = openStorage(classname,
                                    /* dir.getAbsolutePath(), */
                                    filenames[i].getName(),
                                    configData);
                            System.out.println("Recovering " +
                                               filenames[i].getName());
                            if (!storage.getEntry(RComponent.WFSTATUSENTRY).
                                equals(RComponent.WFSTATUS_DEAD)) {
                                restoreFromStorage(storage);
                            } else {
                                System.out.println(filenames[i].getName() +
                                        " has already finished.");
                            }
                        } catch (StorageException exc) {
                            exc.printStackTrace();
                            System.err.println("Component " +
                                               filenames[i].getName() +
                                               " is apparently running.");
                        }
                    }
                }

            }
        }
    }


    static public Storage openStorage(String classname, String dbname,
                                      Object configData) throws
            StorageException {
        Class storageclass = null;
        try {
            storageclass = Class.forName(classname);
            System.out.println("Recovery agent is using Storage class " +
                               classname);
        } catch (ClassNotFoundException cause) {
            throw new StorageException(
                    "Storage class not found! - looking for " + classname,
                    cause);
        }
        Class[] constparam = new Class[2];
        constparam[0] = String.class;
        constparam[1] = ComponentDescription.class;

        Constructor storageconstructor = null;
        try {
            storageconstructor = storageclass.getConstructor(constparam);
        } catch (NoSuchMethodException cause) {
            throw new StorageException("Storage constructor method not found!",
                                       cause);
        }
        Object[] params = new Object[2];
        params[0] = dbname;
        params[1] = configData;

        try {
            return (Storage) storageconstructor.newInstance(params);
        } catch (Exception cause) {
            throw new StorageException("Problems instantiating stable storage",
                                       cause);
        }
    }

    public RecoveryAgent() throws RemoteException {
        super();
    }

    private void restoreFromStorage(Storage storage) {
        try {
            Object[] v = storage.getEntries(RComponent.ATTRIBUTESDIRECTORY);
            ContextImpl cntxt = new ContextImpl();
            for (int i = 0; i < v.length; i++) {
                String entryname = (String) v[i];
                cntxt.sfAddAttribute(entryname, storage.getEntry(entryname));
                storage.commit();
            }

            cntxt.sfAddAttribute(RComponent.STORAGEATTRIB,
                                 storage.getStorageRef());
            storage.close();
            ComponentDescriptionImpl compdesc = new ComponentDescriptionImpl(null,
                    cntxt, true);

            RComponent nprim = (RComponent) SFProcess.getProcessCompound().
                               sfDeployComponentDescription(null, null,
                    compdesc, null);

            nprim.sfRecover();
            //System.out.println(nprim.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sfDeployWith(Prim parent, Context cxt) throws
            SmartFrogDeploymentException, RemoteException {

        try {
            classname = (String) cxt.sfRemoveAttribute(RComponent.
                    STORAGECLASSATTRIB);
            configData = (ComponentDescription) cxt.sfRemoveAttribute(
                    RComponent.STORAGECONFIGDATA);
        } catch (Exception cause) {
            throw new SmartFrogDeploymentException(cause);
        }
        super.sfDeployWith(parent, cxt);
    }

    public synchronized void sfStart() throws SmartFrogException,
            RemoteException {
        super.sfStart();
        recoverer = new Recoverer();
        recoverer.start();
    }

    public synchronized void sfDeploy() throws SmartFrogException,
            RemoteException {
        super.sfDeploy();
    }

}
