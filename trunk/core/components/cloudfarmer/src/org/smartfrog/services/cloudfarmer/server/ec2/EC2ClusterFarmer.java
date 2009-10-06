/* (C) Copyright 2009 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.cloudfarmer.server.ec2;

import org.smartfrog.services.amazon.ec2.EC2Component;
import org.smartfrog.services.cloudfarmer.api.ClusterFarmer;

/**
 * Created 16-Sep-2009 16:06:06
 */


public interface EC2ClusterFarmer extends ClusterFarmer, EC2Component {

    String ATTR_ROLES = "roles";
    /**
     * This is there to stop users accidentally running up large bills. If <0, it means ignore
     */
    String ATTR_CLUSTER_LIMIT = "clusterLimit";
   
}