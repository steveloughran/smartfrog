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
package org.smartfrog.services.junit;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogInitException;
import org.smartfrog.sfcore.common.SmartFrogRuntimeException;
import org.smartfrog.sfcore.logging.Log;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.security.SFClassLoader;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.processcompound.SFProcess;
import org.smartfrog.services.junit.data.Statistics;
import org.smartfrog.services.junit.data.TestInfo;
import org.smartfrog.services.junit.log.TestListenerLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * Implementation of the Junit3.8.x test suite component. This is where tests are
 * actually run; we bring up Junit internally and run it here created
 * 14-May-2004 15:14:23
 */

public class JUnitTestSuiteImpl extends PrimImpl implements JUnitTestSuite,
        junit.framework.TestListener {

    /**
     * our log
     */
    private Log log;

    /**
     * assistance
     */
    private ComponentHelper helper;

    /**
     * List of classes to test
     */
    private List classes;

    /**
     * Is the if= set?
     */
    private boolean ifValue = true;

    /**
     * is the unless= test clear?
     */
    private boolean unlessValue = false;

    /**
     * The package to prefix all classnames with
     */
    private String packageValue;

    /**
     * Runner configuration data
     */
    private RunnerConfiguration configuration;

    /**
     * Statistics about this test
     */
    private Statistics stats = new Statistics();

    /**
     * our hostname
     */
    private String hostname;

    /**
     * the name of the suite
     */
    private String suitename;

    /**
     * test class list we build up
     */
    private HashMap testClasses;

    /**
     * track the active tests; used to build up full statistics
     * on what a test does.
     */
    private HashMap startedTests;

    /**
     * listener for tests; set at binding time
     */
    private TestListener listener;

    /**
     * System properties
     */
    private Properties sysproperties;

    /**
     * Error if sysproperties are uneven.
     * {@value}
     */
    public static final String ERROR_UNEVEN_PROPERTIES = "There is an unbalanced number of properties in "
            + ATTR_SYSPROPS;
    private static final String SUITE_METHOD_NAME = "suite";
    public static final String WARN_IGNORING_REMOTE_FAULT = "Ignoring remote fault";

    public JUnitTestSuiteImpl() throws RemoteException {
        helper = new ComponentHelper(this);
    }


    public boolean getIf() throws RemoteException {
        return ifValue;
    }

    public void setIf(boolean ifValue) throws RemoteException {
        this.ifValue = ifValue;
    }

    public boolean getUnless() throws RemoteException {
        return unlessValue;
    }

    public void setUnless(boolean unlessValue) throws RemoteException {
        this.unlessValue = unlessValue;
    }

    /**
     * bind to the configuration. A null parameter means 'stop binding'
     *
     * @param runner the runner configuration to use
     * @throws java.rmi.RemoteException
     */
    public void bind(RunnerConfiguration runner) throws RemoteException {
        log(suitename + " binding to test runner");
        this.configuration = runner;
    }

    /**
     * Called after instantiation for deployment purposed. Heart monitor is
     * started and if there is a parent the deployed component is added to the
     * heartbeat. Subclasses can override to provide additional deployment
     * behavior.
     *
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *                                  error while deploying
     * @throws java.rmi.RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy() throws SmartFrogException,
            RemoteException {
        super.sfDeploy();
        log = helper.getLogger();
    }

    /**
     * Can be called to start components. Subclasses should override to provide
     * functionality Do not block in this call, but spawn off any main loops!
     *
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *                                  failure while starting
     * @throws java.rmi.RemoteException In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException,
            RemoteException {
        super.sfStart();
        readConfiguration();
    }


    /**
     * read in our configuration
     *
     * @throws SmartFrogException
     * @throws RemoteException
     */
    protected void readConfiguration() throws SmartFrogException,
            RemoteException {
        ifValue = sfResolve(ATTR_IF, ifValue, false);
        unlessValue = sfResolve(ATTR_UNLESS, unlessValue, false);
        List nestedClasses = (List) sfResolve(ATTR_CLASSES,
                (List) null,
                false);
        classes = flattenStringList(nestedClasses,
                ATTR_CLASSES);

        //properties. extract the list, flatten it and bind to sysproperties
        List propList = (List) sfResolve(ATTR_SYSPROPS,
                (List) null,
                false);
        if (propList != null && propList.size() > 0) {
            propList = flattenStringList(propList, ATTR_SYSPROPS);
            String[] values = new String[0];
            values = (String[]) propList.toArray(values);
            int len = values.length;
            if ((len % 2) != 0) {
                //build up an error message with as much data as we can include
                StringBuffer valuesBuffer = new StringBuffer(" [");
                for (int i = 0; i < len; i++) {
                    valuesBuffer.append(values[i]);
                    valuesBuffer.append(' ');
                }
                valuesBuffer.append(']');
                throw new SmartFrogInitException(ERROR_UNEVEN_PROPERTIES + valuesBuffer);
            }

            // system properties
            sysproperties = new Properties();

            for (int i = 0; i < len; i += 2) {
                String key = values[i];
                String value = values[i + 1];
                sysproperties.setProperty(key, value);
            }
        }

        //package attribute names a package
        packageValue = sfResolve(ATTR_PACKAGE, packageValue, false);
        if (packageValue == null) {
            packageValue = "";
        } else {
            //add a dot at the end if we need it
            if (packageValue.length() > 0 &&
                    packageValue.charAt(packageValue.length() - 1) != '.') {
                packageValue = packageValue + '.';
            }
        }
        buildClassList();
        InetAddress host = sfDeployedHost();
        hostname = host.getHostName();
        //use our short name
        suitename = getComponentShortName();
        //then look for an override, which is mandatory if we do not know who
        //we are right now.
        suitename = sfResolve(ATTR_NAME, (String) suitename, suitename==null);
        log("Running test suite " + suitename + " on host " + hostname);
    }

    /**
     * build the list of classes to run. At this point the list is already
     * flat.
     */
    protected void buildClassList() {
        testClasses = new HashMap();
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            String testclass = (String) o;
            addTest(testclass);
        }
    }

    /**
     * add a test to the list , prepending the package
     *
     * @param classname
     */
    private void addTest(String classname) {
        String fullname = packageValue + classname;
        if (testClasses.get(fullname) == null) {
            log.debug("adding test " + fullname);
            testClasses.put(fullname, fullname);
        }
    }

    /**
     * flatten a string list, validating type as we go. recurses as much as we
     * need to. At its most efficient if no flattening is needed.
     *
     * @param src
     * @param name
     * @return
     * @throws SmartFrogInitException
     */
    public List flattenStringList(final List src, String name)
            throws SmartFrogException {
        if (src == null) {
            return new ArrayList(0);
        }
        List dest = new ArrayList(src.size());
        Iterator index = src.iterator();
        while (index.hasNext()) {
            Object element = index.next();
            if (element instanceof List) {
                List l2 = flattenStringList((List) element, name);
                for (Iterator i2 = l2.iterator(); i2.hasNext();) {
                    dest.add(i2.next());
                }
            } else if (!(element instanceof String)) {
                throw new SmartFrogInitException("An element in "
                        +
                        name +
                        " is not string or a list: " +
                        element.toString() + " class=" + element.getClass());
            } else {
                dest.add(element);
            }
        }
        return dest;
    }

    private void log(String message) {
        log.info(message);
    }

    /**
     * run the test bail out if we were interrupted.
     *
     * @return true iff all tests passed
     * @throws java.rmi.RemoteException
     */
    public boolean runTests() throws RemoteException, SmartFrogException {

        log.info("running test suite " + suitename);
        if (configuration == null) {
            throw new SmartFrogException(
                    "TestSuite has not been configured yet");
        }

        //copy any system properties over
        if (sysproperties != null) {
            Utils.applySysProperties(sysproperties);

        }

        if (!getIf() || getUnless()) {
            log("Skipping test as conditions preclude it");
            return true;
        }

        //bind to our listener
        TestListenerFactory listenerFactory = configuration.getListenerFactory();

        listener = listenerFactory.listen(this,
                hostname,
                sfDeployedProcessName(),
                suitename,
                System.currentTimeMillis());
        final TestListenerLog testLog = configuration.getTestLog();
        if(testLog !=null) {
            testLog.addLogListener(listener);
        }

        //reset the logs
        startedTests = new HashMap();

        //now run all the tests
        try {
            boolean failed = false;
            Iterator it = testClasses.keySet().iterator();
            while (it.hasNext()) {
                String classname = (String) it.next();
                try {
                    testSingleClass(classname);
                    updateResultAttributes(false);
                } catch (ClassNotFoundException e) {
                    throw SmartFrogException.forward(e);
                } catch (IllegalAccessException e) {
                    throw SmartFrogException.forward(e);
                } catch (InvocationTargetException e) {
                    throw SmartFrogException.forward(e);
                }
                if (Thread.currentThread().isInterrupted()) {
                    log.error("Interrupted test thread");
                    failed=true;
                }
                updateResultAttributes(true);
                failed |= !stats.isSuccessful();
                if (failed && !configuration.getKeepGoing()) {
                    return false;
                }
            }
            return !failed;
        } finally {
            //as we exit, we mark the listener as null and end the suite.
            TestListener l = getListener();
            //mark the listener as null first, so that any failure to end the
            //suite doesnt affect our deref
            listener = null;
            startedTests = null;
            //unsubscribe
            if (testLog != null) {
                testLog.removeLogListener(l);
            }

            //end the suite.
            l.endSuite();
        }
    }

    /**
     * write all our state to the results order it so that we set the finished
     * last
     */
    private void updateResultAttributes(boolean finished)
            throws SmartFrogRuntimeException, RemoteException {
        stats.updateResultAttributes(this, finished);
    }

    /**
     * test a single class
     *
     * @param classname
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void testSingleClass(String classname)
            throws ClassNotFoundException, IllegalAccessException,
            InvocationTargetException {
        log("testing " + classname);
        Test tests;
        Class clazz = loadTestClass(classname);
        tests = extractTest(clazz);
        TestResult result = new TestResult();
        result.addListener(this);
        tests.run(result);
    }


    /**
     * load the test class, using the secure classloader framework.
     *
     * @param classname
     * @return
     * @throws ClassNotFoundException
     */
    private Class loadTestClass(String classname)
            throws ClassNotFoundException {
        return SFClassLoader.forName(classname);
    }

    /**
     * get the tests from the class, either as a suite or as introspected tests
     *
     * @param clazz
     * @return the test
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Test extractTest(Class clazz) throws IllegalAccessException,
            InvocationTargetException {
        //todo: verify that the class implements test or testsuite
        try {
            // check if there is a suite method
            Method method = clazz.getMethod(SUITE_METHOD_NAME, new Class[0]);
            //despite what IDEs say, there is no redundant cast here, as we don't want
            //java1.5 to get confused.
            return (Test) method.invoke(null, (Object[]) new Class[0]);
        } catch (NoSuchMethodException e) {
            //if not, assume that it is a testclass and do it that way
            return new junit.framework.TestSuite(clazz);
        }
    }

    /**
     * Get the short name of a component.
     * @return the final name in the list, or null for no match
     * @throws RemoteException
     */
    private String getComponentShortName() throws RemoteException {
        Object key;
        if (sfParent() == null) {
            key = SFProcess.getProcessCompound().sfAttributeKeyFor(this);
        } else {
            key = sfParent().sfAttributeKeyFor(this);
        }
        if(key!=null) {
            return key.toString();
        } else {
            return null;
        }
    }

    /**
     * get our listener
     *
     * @return the listener
     */
    public TestListener getListener() {
        return listener;
    }

    /**
     * An error occurred.
     */
    public void addError(Test test, Throwable throwable) {
        stats.incErrors();
        TestInfo info = onEnd(test, throwable);
        info.setOutcome(TestInfo.OUTCOME_ERROR);
        try {
            getListener().addError(info);
        } catch (RemoteException e) {
            IgnoreRemoteFault(e);
        } catch (SmartFrogException e) {
            IgnoreRemoteFault(e);
        }
    }

    /**
     * ignore a remote fault by logging it
     *
     * @param e
     */
    private void IgnoreRemoteFault(Exception e) {
        log.warn(WARN_IGNORING_REMOTE_FAULT, e);
    }

    /**
     * A failure occurred.
     */
    public void addFailure(Test test, AssertionFailedError error) {
        stats.incFailures();
        TestInfo info = onEnd(test, error);
        info.setOutcome(TestInfo.OUTCOME_FAILURE);
        try {
            getListener().addFailure(info);
        } catch (RemoteException e) {
            IgnoreRemoteFault(e);
        } catch (SmartFrogException e) {
            IgnoreRemoteFault(e);
        }
    }

    /**
     * A test ended.
     */
    public void endTest(Test test) {
        stats.incTestsRun();
        TestInfo info = onEnd(test, null);
        info.setOutcome(TestInfo.OUTCOME_SUCCESS);
        try {
            getListener().endTest(info);
        } catch (RemoteException e) {
            IgnoreRemoteFault(e);
        } catch (SmartFrogException e) {
            IgnoreRemoteFault(e);
        }
    }

    /**
     * A test started.
     * Note that if a test throws an exception in {@link TestCase#setUp()}
     * then this callback is <i>not</i> invoked by Junit. We need to be
     * aware of this fact.
     */
    public void startTest(Test test) {
        TestInfo info = onStart(test);
        try {
            getListener().startTest(info);
        } catch (RemoteException e) {
            IgnoreRemoteFault(e);
        } catch (SmartFrogException e) {
            IgnoreRemoteFault(e);
        }
    }

    /**
     * note that a test has started by adding the current time
     * to the {@link #startedTests} hashtable if it is not already there,
     * and incrementing the started count in the statistics.
     * If it is there, do nothing.
     *
     * @param test
     */
    public TestInfo onStart(Test test) {
        TestInfo info = new TestInfo(test);
        String testname = info.getText();
        long start = registerStartTime(testname);
        info.setStartTime(start);
        return info;
    }

    /**
     * file the start time;  if there already is one, then take that one instead.
     * Will increment the started count in the statistics if needed.
     *
     * @param testname
     * @return timestamp of test start.
     */
    private synchronized long registerStartTime(String testname) {
        if (!startedTests.containsKey(testname)) {
            long start = System.currentTimeMillis();
            startedTests.put(testname, new Long(start));
            stats.incTestsStarted();
            return start;
        } else {
            return lookupStartTime(testname);
        }
    }

    /**
     * look up the start time
     *
     * @param testname test name
     * @return start time
     * @throws RuntimeException if there is no key of that name
     */
    private long lookupStartTime(String testname) {
        return ((Long) startedTests.get(testname)).longValue();
    }

    /**
     * call this when a test ends to set up the start and end times right.
     *
     * @param test  the test
     * @param fault optional fault detail
     * @return the test information including start and end time.
     */
    public synchronized TestInfo onEnd(Test test, Throwable fault) {
        long endTime = System.currentTimeMillis();
        TestInfo testInfo = new TestInfo(test, fault);
        //force a start entry in there
        long startTime = registerStartTime(testInfo.getText());
        testInfo.setStartTime(startTime);
        testInfo.setEndTime(endTime);
        return testInfo;
    }


}
