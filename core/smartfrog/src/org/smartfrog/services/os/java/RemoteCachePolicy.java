/* (C) Copyright 2005 Hewlett-Packard Development Company, LP

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

import java.rmi.RemoteException;

/**
 * Interface that remote cache policies should implement
 */
public interface RemoteCachePolicy extends LibraryCachePolicy {
    
    /**
     * create a path to the remote system, all but the base URL,
     * using the artifact as the source of the path information.
     * @param artifact the artifact
     * @return String the generated path
     * @throws RemoteException for network problems
     * @throws SmartFrogException for other problems
     */
    String createRemotePath(SerializedArtifact artifact) throws RemoteException,SmartFrogException;

}
