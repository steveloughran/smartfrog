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


package org.smartfrog.test.system.examples;

import org.smartfrog.test.SmartFrogTestBase;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import java.net.*;
import java.io.*;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.security.SFGeneralSecurityException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.test.unit.sfcore.common.ConfigurationDescriptorTestURLs;
import org.smartfrog.sfcore.common.ConfigurationDescriptor;
import org.smartfrog.sfcore.common.SmartFrogInitException;
import org.smartfrog.SFSystem;

/**
 * JUnit test class for test cases related to Subprocess Example
 */
public class SubProcessExampleTest
    extends SmartFrogTestBase {

    // In this particular case we use the examples without screens
    //private static final String FILES = "org/smartfrog/examples/subprocesses/";
    //#include "org/smartfrog/test/system/deploy/subprocessTestHarness.sf"
    private static final String FILES = "org/smartfrog/test/system/deploy/";

    public SubProcessExampleTest(String s) {
        super(s);
    }

public void testCaseSPE01 ()throws Throwable
{

    Prim applicationSPE01 = deployExpectingSuccess(FILES+"subprocessTestHarness.sf", "tcSPE01");
		  assertNotNull(applicationSPE01);
//	Prim applicationSPE01 = deployExpectingSuccess(FILES+"subprocess.sf", "tcSPE01");
//		  assertNotNull(applicationSPE01);

	String actualSfClass = (String)applicationSPE01.sfResolveHere("sfClass");
		assertEquals("org.smartfrog.sfcore.compound.CompoundImpl", actualSfClass);

	Prim sys = (Prim)applicationSPE01.sfResolveHere("system");
	//System.out.println();
		assertEquals("first", sys.sfDeployedProcessName());

	Prim foo = (Prim)sys.sfResolveHere("foo");
		assertEquals("test", foo.sfDeployedProcessName());

	Prim bar = (Prim)foo.sfResolveHere("bar");
		assertEquals("test2", bar.sfDeployedProcessName());
	Prim traceTest2 = (Prim)bar.sfResolveHere("traceTest2");
		String actualSfClassTT2= (String)traceTest2.sfResolveHere("sfClass");
		assertEquals("org.smartfrog.services.trace.SFTrace", actualSfClassTT2);


	Prim demoE = (Prim)bar.sfResolveHere("demoE");
		int actualdemoE = 0;
		int expecteddemoE = 9;
		actualdemoE = demoE.sfResolve("limit",actualdemoE,true);
		assertEquals(expecteddemoE, actualdemoE);


	Prim foobar = (Prim)sys.sfResolveHere("foobar");

		Prim demoF = (Prim)foobar.sfResolveHere("demoF");
		int actualdemoF = 0;
		int expecteddemoF = 12;
		actualdemoF = demoF.sfResolve("limit",actualdemoE,true);
		assertEquals(expecteddemoE, actualdemoE);

	Prim displayLOCALHOST = (Prim)foobar.sfResolveHere("displayLOCALHOST");
		String actualSfClassDLH = (String)displayLOCALHOST.sfResolveHere("sfClass");
		assertEquals("org.smartfrog.services.display.SFDisplay", actualSfClassDLH);

	Prim baz = (Prim)sys.sfResolveHere("baz");
		Prim demoC = (Prim)baz.sfResolveHere("demoC");
		int actualdemoC = 0;
		int expecteddemoC = 9;
		actualdemoC = demoC.sfResolve("limit",actualdemoC,true);
		assertEquals(expecteddemoC, actualdemoC);

	Prim sys_bar = (Prim)sys.sfResolveHere("bar");
		Prim demoD = (Prim)sys_bar.sfResolveHere("demoD");
		int actualdemoD = 0;
		int expecteddemoD = 3;
		actualdemoD = demoD.sfResolve("limit",actualdemoD,true);
		assertEquals(expecteddemoD, actualdemoD);





}


}

