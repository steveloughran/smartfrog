/* (C) Copyright 2008 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.amazon.ec2;

import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.TerminatingInstanceDescription;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Kill the listed instances; skip things that are already terminating/terminated.
 */

public class KillEC2InstanceImpl extends ListInstancesImpl implements EC2Instance {

    public KillEC2InstanceImpl() throws RemoteException {
    }


    /**
     * Process the list of instances enumerated The base class lists the instances at info level then checks the number
     * of instances found.
     *
     * @param instanceList instances
     * @throws SmartFrogException for any SF exception
     * @throws RemoteException for networking problems
     */
    @Override
    protected void processInstanceList(InstanceList instanceList) throws SmartFrogException, RemoteException {
        super.processInstanceList(instanceList);
        try {
            List<TerminatingInstanceDescription> terminating = instanceList.terminate();
            logTerminationInfo(terminating);
        } catch (EC2Exception e) {
            throw new SmartFrogEC2Exception("failed to terminate the instances", e);
        }
    }

}
