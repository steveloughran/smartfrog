/** (C) Copyright 2004 Hewlett-Packard Development Company, LP

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


package org.smartfrog.services.cddlm.test.system.console;

import org.apache.axis.types.URI;
import org.smartfrog.services.cddlm.generated.api.DeployApiConstants;
import org.smartfrog.services.cddlm.generated.api.types.DeploymentDescriptorType;

/**
 * Date: 06-Sep-2004 Time: 21:57:39
 */
public class DeploySmartFrogTest extends DeployingTestBase {

    public void testDeployAndUndeploy() throws Exception {
        String name = "simple";
        URI uri = null;
        DeploymentDescriptorType dt = operation.createSmartFrogDescriptor(
                DeploySmartFrogTest.SIMPLE_DESCRIPTOR);
        uri = deploy(name, dt, null, null);
        //now test a lookup
        String stateName = DeployApiConstants.STATE_RUNNING;
        assertInState(uri, stateName);
        final boolean result = undeploy(uri);
        assertTrue("undeploy returned false",
                result);

    }

    public void testDeployTwice() throws Exception {
        String name = "simple";
        URI uri = null;
        DeploymentDescriptorType dt = operation.createSmartFrogDescriptor(
                DeploySmartFrogTest.SIMPLE_DESCRIPTOR);
        uri = deploy(null, dt, null, null);
        URI uri2 = deploy(null, dt, null, null);
        //now test a lookup
        assertDeployed(uri);
        assertDeployed(uri2);
        boolean result = undeploy(uri);
        result = result & undeploy(uri2);
        assertTrue("undeloy returned false",
                result);

    }

}
