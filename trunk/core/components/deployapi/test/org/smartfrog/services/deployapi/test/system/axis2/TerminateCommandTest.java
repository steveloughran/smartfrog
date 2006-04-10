/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.deployapi.test.system.axis2;

import org.smartfrog.services.deployapi.client.Endpointer;
import org.smartfrog.services.deployapi.client.SystemEndpointer;
import org.smartfrog.services.deployapi.client.Terminate;
import org.smartfrog.services.deployapi.system.Constants;
import org.smartfrog.services.deployapi.test.system.axis2.ApiTestBase;

import java.util.ArrayList;
import java.util.List;

/**

 */
public class TerminateCommandTest extends ApiTestBase {

    public TerminateCommandTest(String name) {
        super(name);
    }

    public void testMain() throws Exception {
        SystemEndpointer system=createSystem();
        try {
            List<String> args=new ArrayList<String>();
            args.add(Endpointer.URL_COMMAND+system.getURL().toString());
            String[] args2 = toStringArray(args);
            boolean b = Terminate.innerMain(args2);
            assertTrue("terminate failed ",b);
            String timestamp = system.getStringProperty(Constants.PROPERTY_SYSTEM_TERMINATED_TIME);
            assertIsoDate(timestamp);
        } finally {
            destroySystem(system);
        }
    }
}
