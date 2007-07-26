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
package org.smartfrog.test;

import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.common.ConfigurationDescriptor;
import org.smartfrog.sfcore.workflow.events.LifecycleEvent;
import org.smartfrog.sfcore.workflow.events.TerminatedEvent;
import org.smartfrog.services.assertions.TestBlock;
import org.smartfrog.services.assertions.events.TestCompletedEvent;
import org.smartfrog.services.assertions.events.TestEventSink;
import org.smartfrog.services.assertions.TestTimeoutException;
import org.smartfrog.SFSystem;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import junit.framework.AssertionFailedError;

/**
 * An extension of SmartFrogTestBase with test awareness
 * created 13-Oct-2006 16:28:33
 */

public abstract class DeployingTestBase extends SmartFrogTestBase implements TestProperties {

    private static final int SPIN_INTERVAL = 10;

    /**
     * The event sink for the deployed test application
     */
    private TestEventSink eventSink;

    protected DeployingTestBase(String name) {
        super(name);
    }


    protected void tearDown() throws Exception {
        if(!stopListening()) {
            getLog().warn("Failed to unsubscribe event sink");
        }
        super.tearDown();
    }

    /**
     * Stop listening to events
     * this call is synchronous, and idempotent.
     * @return true if we are no longer listening -that is we werent listening, or we unsubscribed quietly
     */
    private synchronized boolean stopListening() {
        if (eventSink != null) {
            Thread t = eventSink.asyncUnsubscribe();
            eventSink = null;
            return t != null;
        } else {
            //we are implicitly no longer listening
            return true;
        }
    }

    /**
     * Start listening; unsubscribe the event sink and bind to a new application
     * @param prim the event source
     * @throws RemoteException for subscription problems
     */
    private synchronized void startListening(Prim prim) throws RemoteException {
        stopListening();
        eventSink=new TestEventSink(prim);
    }


    public TestEventSink getEventSink() {
        return eventSink;
    }

    protected TerminationRecord expectSuccessfulTermination(TestBlock testBlock) throws Throwable {
        return expectTermination(testBlock, true);
    }

    protected TerminationRecord expectAbnormalTermination(TestBlock testBlock) throws Throwable {
        return expectTermination(testBlock, false);
    }

    protected TerminationRecord expectTermination(TestBlock testBlock,boolean normal) throws Throwable {
        TerminationRecord status = spinUntilFinished(testBlock);
        assertNotNull("Null termination record",status);
        assertTrue("Expected "+(normal?"normal":"abnormal") + " termination, but got "+status,
                normal==status.isNormal());
        return status;
    }

    /**
     * Delay until a test has finished, sleeping (and yielding the CPU) until
     * that point is reached. There is no timeout.
     * @param testBlock component to spin on
     * @param timeout how long to wait (in millis)
     * @return the termination record of the component
     * @throws Throwable if something went wrong
     */
    protected TerminationRecord spinUntilFinished(TestBlock testBlock, long timeout) throws Throwable {
        try {
            long endtime = System.currentTimeMillis() + timeout;
            while (!testBlock.isFinished() && System.currentTimeMillis() < endtime) {
                Thread.sleep(SPIN_INTERVAL);
            }
            assertTrue("timeout ("+timeout+"ms) waiting for application to finish", testBlock.isFinished());
            return testBlock.getStatus();
        } catch (NoSuchObjectException e) {
            //some kind of remoting problem may happen during termination.
            logThrowable("Object has been deleted", e);
            throw e;
        } catch (RemoteException e) {
            //some kind of remoting problem may happen during termination.
            logThrowable("RMI exceptions during spin-waits may be network race conditions", e);
            throw e;
        }
    }

    /**
     * Delay until a test has finished, sleeping (and yielding the CPU) until
     * that point is reached. There is no timeout.
     * @param testBlock component to spin on 
     * @return the termination record of the component
     * @throws Throwable
     */
    protected TerminationRecord spinUntilFinished(TestBlock testBlock) throws Throwable {
        return spinUntilFinished(testBlock, getTestTimeout());
    }

    /**
     * Load but do not start a component description. Any error raised during loading is passed on
     *
     * @param appName application name
     * @param testURL URL of the application
     * @return the loaded CD, which is not yet deployed or started
     * @throws Throwable in the event of trouble.
     */
    private Prim loadApplication(String appName, String testURL) throws Throwable {
        ConfigurationDescriptor cfgDesc =
                new ConfigurationDescriptor(appName,
                        testURL,
                        ConfigurationDescriptor.Action.LOAD,
                        hostname,
                        null);
        Object loaded = SFSystem.runConfigurationDescriptor(cfgDesc, true);
        lookForThrowableInDeployment(loaded);
        return (Prim) loaded;
    }

    public String createUrlString(String packageName, String filename) {
        StringBuffer buffer=new StringBuffer(packageName);
        if(!packageName.endsWith("/")) {
            buffer.append('/');
        }
        buffer.append(filename);
        if (!filename.endsWith(".sf")) {
            buffer.append(".sf");
        }
        return buffer.toString();
    }

    /**
     * This runs tests to completion, be it success or failure. The application is saved to the application field;
     * the event sink to eventSink. Both will be cleaned up during teardown.
     * @param packageName name of the package
     * @param filename filename excluding .sf
     * @param startupTimeout limit in millis to start up
     * @param executeTimeout limit in millis to execute
     * @return the lifecycle at the end of the run. This s either a {@link TestCompletedEvent} or a
     * {@link org.smartfrog.sfcore.workflow.events.TerminatedEvent}
     * @throws Throwable on failure. A {@link TestTimeoutException} indicates timeout.
     */
    protected LifecycleEvent runTestDeployment(String packageName, String filename,int startupTimeout,int executeTimeout) throws Throwable {
        String urlstring=createUrlString(packageName, filename);
        application= loadApplication(filename, urlstring);
        startListening(application);
        LifecycleEvent event = getEventSink().runTestsToCompletion(startupTimeout, executeTimeout);
        return event;
    }

    /**
     * This runs tests to completion, be it success or failure. The application is saved to the application field;
     * the event sink to eventSink. Both will be cleaned up during teardown.
     * @param packageName name of the package
     * @param filename filename excluding .sf
     *
     * The default test startup and execution timeout will be used, unless overridden by
     * system properties.
     *
     * @return the lifecycle at the end of the run. This s either a {@link TestCompletedEvent} or a
     * {@link org.smartfrog.sfcore.workflow.events.TerminatedEvent}
     * @throws Throwable on failure. A {@link TestTimeoutException} indicates timeout.
     */
    protected LifecycleEvent runTestDeployment(String packageName, String filename) throws Throwable {
        return runTestDeployment(packageName, filename, getTestStartupTimeout(),
                getTestTimeout());
    }

    /**
     * Run tests until they are completed, then analyse the results.
     * The application is saved to the application field;
     * the event sink to eventSink. Both will be cleaned up during teardown.
     *
     * The method will fail if the
     * application terminated abnormally, or returned a test failure. Skipped and successful tests
     * are both viewed as successes.
     *
     * @param packageName name of the package
     * @param filename test file name, excluding .sf
     * @param startupTimeout limit in millis to start up
     * @param executeTimeout limit in millis to execute
     * @return the test results
     * @throws Throwable on any problem.
     *  A {@link TestTimeoutException} indicates timeout waiting for results
     *  An {@link AssertionFailedError} is raised if the tests were not successful
     */
    protected TestCompletedEvent completeTestDeployment(String packageName, String filename, int startupTimeout, int executeTimeout)
            throws Throwable {
        LifecycleEvent event= runTestDeployment(packageName, filename,
                startupTimeout,
                executeTimeout);
        conditionalFail(event instanceof TerminatedEvent,
                "Test run terminated without completing the tests", event);
        //if not a terminated event, its test results
        TestCompletedEvent results = (TestCompletedEvent) event;
        conditionalFail(results.isForcedTimeout(),
                "Forced timeout", event);
        conditionalFail(results.isFailed(),
                "Test failed", event);
        return results;
    }



    /**
    * Run tests until they are completed, then analyse the results.
    * The application is saved to the application field;
    * the event sink to eventSink. Both will be cleaned up during teardown.
    *
    * The default test startup and execution timeout will be used, unless overridden by
    * system properties.
    *
    * The method will fail if the application terminated abnormally, or returned a test failure.
    * Skipped and successful tests are both viewed as successes.
    *
    * @param packageName name of the package
    * @param filename test file name, excluding .sf
    * @return the test results
    * @throws Throwable on any problem.
    *  A {@link TestTimeoutException} indicates timeout waiting for results
    *  An {@link AssertionFailedError} is raised if the tests were not successful
    */
    protected TestCompletedEvent runTestsToCompletion(String packageName,String filename) throws Throwable {
        return completeTestDeployment(packageName, filename,
                getTestStartupTimeout(),
                getTestTimeout());
    }

    protected int getTestStartupTimeout() {
        return TestHelper.getTestPropertyInt(TEST_TIMEOUT_STARTUP, STARTUP_TIMEOUT);
    }

    /**
     * Fail if a condition is not met; the message raised includes the message and the
     * string value of the event.
     * @param test condition to evaluate
     * @param message message to print
     * @param event related event
     * @throws {@link AssertionFailedError} if the condition is true
     */
    private void conditionalFail(boolean test,String message,LifecycleEvent event) {
        if(test) {
            fail(message+"\n"+event);
        }
    }

    /**
    * return the max time in milliseconds for tests
    * @return the value set by a system property {@link #TEST_TIMEOUT_EXECUTE} or the default {@link #EXECUTE_TIMEOUT}
    */
    protected int getTestTimeout() {
        return TestHelper.getTestPropertyInt(TEST_TIMEOUT_EXECUTE,EXECUTE_TIMEOUT);
    }

    /**
     * Assert that a test failed for a specific reason
     * @param event event to analyse
     * @param abnormalStatus is an abnormal status expected
     * @param errorText optional error text
     */
    protected void assertTestRunFailed(LifecycleEvent event,boolean abnormalStatus,String errorText) {
        assertTrue("not a TestCompletedEvent: "+event,event instanceof TestCompletedEvent);
        TestCompletedEvent testBlock=(TestCompletedEvent) event;
        assertTrue("test did not fail",testBlock.isFailed());
        assertFalse("test succeeded",testBlock.isSucceeded());
        TerminationRecord status = testBlock.getStatus();
        assertNotNull("No termination record",status);
        if(abnormalStatus) {
            assertFalse("Status is normal when it should be abnormal:"+status,status.isNormal());
        } else {
            assertTrue("Status is abnormal when it should be normal:" + status, status.isNormal());
        }
        if(errorText!=null) {
            assertEquals(errorText, status.description);
        }
    }

    /**
     * Do a test run, assert that it failed. The application and eventSink are
     * both saved in member variables, ready for cleanup in teardown
     * @param packageName package containing the deployment
     * @param filename filename (with no .sf extension)
     * @return the test completion event
     * @throws Throwable if things go wrong
     */
    protected TestCompletedEvent expectSuccessfulTestRun(String packageName, String filename) throws Throwable {
        return runTestsToCompletion(packageName,filename);
    }

    /**
     * Do a test run, assert that it failed. The application and eventSink are
     * both saved in member variables, ready for cleanup in teardown
     * @param packageName package containing the deployment
     * @param filename filename (with no .sf extension)
     * @return the test completion event
     * @throws Throwable if things go wrong
     */
    protected TestCompletedEvent expectAbnormalTestRun(String packageName, String filename) throws Throwable {
        LifecycleEvent event = runTestDeployment(packageName, filename);
        assertTestRunFailed(event,false, null);
        return (TestCompletedEvent)event;
    }

}
