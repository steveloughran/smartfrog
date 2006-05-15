/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.deployapi.test.system.alpine.deployapi.api;

/**
 * created 04-May-2006 13:44:37
 */

public abstract class UnimplementedTestBase extends StandardTestBase {
    /**
     * {@value}
     */
    public static final String TEST_IMPLEMENTED = "test.implemented";

    public UnimplementedTestBase(String name) {
        super(name);
    }

    /**
     * raise a fault if unimplemented stuff is not excluded by setting the
     *  test.implemented property
     * @throws Exception
     */
    public void testNotImplemented() throws Exception {
        String implemented = getJunitParameter(TEST_IMPLEMENTED, false);
        if(implemented==null) {
            fail("This test has not been implemented, which may mean that the"
                + " underlying features have not been implemented in the client");
        }
    }
}
