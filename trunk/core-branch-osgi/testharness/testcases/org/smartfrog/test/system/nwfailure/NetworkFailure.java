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

package org.smartfrog.test.system.nwfailure;

import java.rmi.RemoteException;
import java.io.*;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;


/**
 *  Basic example component
 */
public class NetworkFailure extends PrimImpl implements Prim {
    
    private String attr = "NetworkFailure";

    /**
     * Default Constructor 
     *
     *@exception  RemoteException  Description of the Exception
     */
    public NetworkFailure() throws RemoteException {
    }

    // LifeCycle methods

    /**
     * sfDeploy: default implementation
     *
     *@exception  Exception  Description of the Exception
     */
    public void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();
        // ask use to remove network cord to induce n/w failure
        BufferedReader br =  new BufferedReader(new 
                                InputStreamReader(System.in));
        System.out.print("Please disconnet the n/w cord from your machine "+
                "and type yes after done:");
        String ln = null;
        try {
            do {
                System.out.print("Please disconnect the n/w cord from your "+
                        "machine and type yes after done:");
                ln = br.readLine();
            }
            while (!ln.equalsIgnoreCase("yes"));
            System.out.println("Deployed");
        }catch (IOException ioex) {
            throw new SmartFrogDeploymentException(ioex);
        }
    }

    /**
     *  sfStart: starts counter thread
     *
     *@exception  Exception  Description of the Exception
     */
    public void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        System.out.println("In sfStart");
        try {
            // if n/w connectivity is missing attr would not get value defined
            // in sf description
            attr = sfResolve("message", attr, true);
            System.out.println("The value of attribute:"+ attr);
        }catch (RemoteException re) {
            sfTerminate(TerminationRecord.abnormal("N/w Error: "+ re,
                                  sfCompleteNameSafe()));
            throw re;
        }
    }

    /**
     *  sfTerminate
     *
     *@param  t  Description of the Parameter
     */
    public void sfTerminateWith(TerminationRecord t) {
        super.sfTerminateWith(t);
    }

    // End LifeCycle methods
}
