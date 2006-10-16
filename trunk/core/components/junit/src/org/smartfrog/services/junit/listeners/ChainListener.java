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
package org.smartfrog.services.junit.listeners;

import org.smartfrog.services.junit.TestListener;
import org.smartfrog.services.junit.TestListenerFactory;
import org.smartfrog.services.junit.TestSuite;
import org.smartfrog.services.junit.data.LogEntry;
import org.smartfrog.services.junit.data.TestInfo;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is a bulk listener for things. 
 * created 03-Jun-2005 16:49:03
 */

public class ChainListener implements TestListener,Iterable<TestListener> {

    private List<TestListener> listeners =new ArrayList<TestListener>();


    public ChainListener() {
    }

    public ChainListener(List<TestListenerFactory> factories, TestSuite suite,
                         String hostname,
                         String processname, String suitename,
                         long timestamp) throws SmartFrogException, RemoteException {
        createAndAddListeners(factories,suite,hostname, processname, suitename,timestamp);
    }

    /**
     * Run through every factory and create a listener from each one, adding each
     * returned instance to the listener factory.
     * @param factories
     * @param suite     the test suite that is about to run. May be null,
     *                  especially during testing.
     * @param hostname  name of host
     * @param processname process of the tests
     * @param suitename name of test suite
     * @param timestamp start timestamp (UTC)
     */
    public void createAndAddListeners(List<TestListenerFactory> factories,
                                      TestSuite suite,
                                      String hostname,
                                      String processname,
                                      String suitename,
                                      long timestamp) throws SmartFrogException, RemoteException {
        //reset the list of listeners
        listeners =new ArrayList<TestListener>(factories.size());
        //run through the factories
        for(TestListenerFactory factory:factories) {
            TestListener listener = factory.listen(suite, hostname, processname, suitename, timestamp);
            addListener(listener);
        }
    }

    public synchronized void addListener(TestListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TestListener listener) {
        listeners.remove(listener);
    }

    /**
     * Iterator operator.
     * @return
     */
    public Iterator<TestListener> iterator() {
        return listeners.iterator();
    }

    /**
     * end this test suite. After calling this, caller should discard all
     * references; they may no longer be valid. <i>No further methods may be
     * called</i>
     */
    public void endSuite() throws RemoteException, SmartFrogException {
        Iterator i=iterator();
        while (i.hasNext()) {
            TestListener testListener = (TestListener) i.next();
            testListener.endSuite();
        }
    }

    /**
     * An error occurred.
     */
    public void addError(TestInfo test) throws RemoteException,
            SmartFrogException {

        Iterator i = iterator();
        while (i.hasNext()) {
            TestListener testListener = (TestListener) i.next();
            testListener.addError(test);
        }
    }

    /**
     * A failure occurred.
     */
    public void addFailure(TestInfo test) throws RemoteException,
            SmartFrogException {
        Iterator i = iterator();
        while (i.hasNext()) {
            TestListener testListener = (TestListener) i.next();
            testListener.addFailure(test);
        }

    }

    /**
     * A test ended.
     */
    public void endTest(TestInfo test) throws RemoteException,
            SmartFrogException {
        Iterator i = iterator();
        while (i.hasNext()) {
            TestListener testListener = (TestListener) i.next();
            testListener.endTest(test);
        }
    }

    /**
     * A test started.
     */
    public void startTest(TestInfo test) throws RemoteException,
            SmartFrogException {
        Iterator i = iterator();
        while (i.hasNext()) {
            TestListener testListener = (TestListener) i.next();
            testListener.startTest(test);
        }
    }

    public void log(LogEntry event) throws RemoteException {
        Iterator i = iterator();
        while (i.hasNext()) {
            TestListener testListener = (TestListener) i.next();
            testListener.log(event);
        }

    }

}
