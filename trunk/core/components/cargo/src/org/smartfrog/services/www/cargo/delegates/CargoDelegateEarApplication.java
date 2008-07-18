package org.smartfrog.services.www.cargo.delegates;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.EAR;
import org.smartfrog.services.filesystem.FileUsingComponentImpl;
import org.smartfrog.services.www.JavaEnterpriseApplication;
import org.smartfrog.services.www.cargo.CargoServerImpl;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;

import java.rmi.RemoteException;

/**
 */
public class CargoDelegateEarApplication extends CargoDelegateDeployable
        implements
        JavaEnterpriseApplication {

    public CargoDelegateEarApplication(Prim webApplication,
                                       CargoServerImpl owner) {
        super(webApplication, owner);
    }


    /**
     * Because cargo components do cold deployment, we
     *
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *
     * @throws java.rmi.RemoteException
     */
    public void deploy() throws SmartFrogException, RemoteException {
        LocalContainer container = getOwner().getContainer();

        String filename = FileUsingComponentImpl.bind(getApplication(),
                true,
                null);
        EAR ear = new EAR(filename);
        queue(ear);
    }
}