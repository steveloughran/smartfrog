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
package org.smartfrog.test.system.java;

import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.test.SmartFrogTestBase;
import org.smartfrog.services.os.java.JavaPackage;

/**
 * created Oct 1, 2004 10:54:11 AM
 */

public class JavaPackageTest extends SmartFrogTestBase {

    public static final String FILES = "org/smartfrog/test/system/java/";

    public JavaPackageTest(String name) {
        super(name);
    }

    public void testSimplePackage() throws Throwable {
        Prim application=null;
        try {
            application = deployExpectingSuccess(FILES +
                            "testSimplePackage.sf", "testSimplePackage");
            assertStringAttributeExists(application,JavaPackage.ATTR_URICLASSPATH);
            assertAttributeExists(application, JavaPackage.ATTR_URICLASSPATHLIST);
        } finally {
            terminateApplication(application);
        }
    }

    public void testMissingResource() throws Throwable {
        deployExpectingException(FILES +
                "testMissingResource.sf",
                "testMissingResource",
                "SmartFrogLifecycleException",
                null,
                "SmartFrogLivenessException",
                null);
    }

    public void testClassAsResource() throws Throwable {
        Prim application = null;
        try {
            application = deployExpectingSuccess(FILES +
                    "testClassAsResource.sf", "testClassAsResource");
        } finally {
            terminateApplication(application);
        }
    }

}
