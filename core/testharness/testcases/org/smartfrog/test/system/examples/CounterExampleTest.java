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

import java.net.*;
import java.io.*;

/**
 * JUnit test class for test cases related to Conter Example 
 */
public class CounterExampleTest
    extends SmartFrogTestBase {

    private static final String FILES = "org/smartfrog/examples/counter/";

    public CounterExampleTest(String s) {
        super(s);
    }

    public void testCaseCE01() throws Throwable {
          Prim applicationCE01 = deployExpectingSuccess(FILES+"example.sf", "tcCE01");
		  assertNotNull(applicationCE01);
	
			String actualSfClass = (String)applicationCE01.sfResolveHere("sfClass");
			assertEquals("org.smartfrog.examples.counter.CounterImpl", actualSfClass);
			int actual = 0;
			int expected = 20;
			actual = applicationCE01.sfResolve("limit",actual,true);
			assertEquals(expected, actual);

		  

    }
	 public void testCaseCE02() throws Throwable {
          deployExpectingException(FILES+"example2.sf", "tcCE02", "SmartFrogLifecycleException" ,"Illegal ClassType");

    }
	
	 public void testCaseCE03() throws Throwable {
       
	 deployExpectingException(FILES+"example3.sf", "tcCE03", "SmartFrogLifecycleException" ,"Unresolved Reference: HERE counter");

    }
	public void testCaseCE04() throws Throwable {
       
	 deployExpectingException(FILES+"example4.sf", "tcCE04", "SmartFrogLifecycleException" ,"Unresolved Reference: HERE limit");

    }
	public void testCaseCE05() throws Throwable {
          Prim applicationCE05 = deployExpectingSuccess(FILES+"example5.sf", "tcCE05");
		  assertNotNull(applicationCE05);

			String actualSfClass = (String)applicationCE05.sfResolveHere("sfClass");
			assertEquals("org.smartfrog.sfcore.compound.CompoundImpl", actualSfClass);

			Prim counter1 = (Prim)applicationCE05.sfResolveHere("counter1");
			String actualSfClass1 = (String)counter1.sfResolveHere("sfClass");
			assertEquals("org.smartfrog.sfcore.compound.CompoundImpl", actualSfClass1);


			Prim counter1A = (Prim)counter1.sfResolveHere("counter1A");

			String actualSfClass1A = (String)counter1A.sfResolveHere("sfClass");
			assertEquals("org.smartfrog.examples.counter.CounterImpl", actualSfClass1A);

			int actual1A = 0;
			int expected1A = 2;
			actual1A = counter1A.sfResolve("limit",actual1A,true);
			assertEquals(expected1A, actual1A);
			
			Prim counter2 = (Prim)applicationCE05.sfResolveHere("counter2");
			int actual2 = 0;
			int expected2 = 2;
			actual2 = counter2.sfResolve("limit",actual2,true);
			assertEquals(expected2, actual2);
			
			Prim counter3 = (Prim)applicationCE05.sfResolveHere("counter3");
			Prim counter3A = (Prim)counter3.sfResolveHere("counter3A");

			int actual3A = 0;
			int expected3A = 2;
			actual3A = counter3A.sfResolve("limit",actual3A,true);
			assertEquals(expected3A, actual3A);

    }

}

