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
package org.smartfrog.services.assertions;

import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.workflow.combinators.DelayedTerminator;
import org.smartfrog.sfcore.workflow.conditional.ConditionCompound;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.utils.ShouldDetachOrTerminate;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;

import java.rmi.RemoteException;

/**
 * created 22-Sep-2006 16:43:35
 */

public class TestCompoundImpl extends ConditionCompound
        implements TestCompound {
    private ComponentDescription teardownCD;
    private Prim teardown;
    private ComponentDescription waitForCD;
    private ComponentDescription tests;
    private Prim waitFor,testsPrim;

    protected static final String ACTION_RUNNING = ATTR_ACTION;
    protected static final String TESTS_RUNNING = ATTR_TESTS;
    protected static final String TEARDOWN_RUNNING = ATTR_TEARDOWN;
    private long undeployAfter;
    private long testTimeout;
    private boolean expectTerminate;
    private DelayedTerminator actionTerminator;
    private DelayedTerminator testsTerminator;
    private Prim actionPrim;
    private String exitType;
    private String exitText;
    private volatile boolean finished = false;
    private volatile boolean failed = false;
    private volatile boolean succeeded = false;
    private volatile boolean forcedTimeout = false;
    private volatile boolean skipped = false;
    private volatile TerminationRecord status;

    /**
     * The message of a forced shutdown. This is important, as we look for
     * it when the component is terminated, and can use its presence to infer
     * that the helper thread did the work.
     */
    public static final String FORCED_TERMINATION = "timed shutdown of test components";
    public static final String TEST_FAILED_WRONG_STATUS = "Expected action to terminate with the status ";
    private boolean shouldTerminate;

    public TestCompoundImpl() throws RemoteException {
    }

    /**
     * {@inheritDoc}
     *
     * @return the skipped state
     */
    public boolean isSkipped() {
        return skipped;
    }

    /**
     * Deploys and reads the basic configuration of the component.
     * Overrides EventCompoundImpl.sfDeploy.
     *
     * @throws RemoteException In case of network/rmi error
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *                         In case of any error while
     *                         deploying the component
     */
    public synchronized void sfDeploy() throws SmartFrogException,
            RemoteException {
        super.sfDeploy();
        //look for the action
        checkActionDefined();
        name = sfCompleteNameSafe();

        waitForCD = sfResolve(ATTR_WAITFOR, waitForCD, false);
        tests = sfResolve(ATTR_TESTS, tests, false);
        testTimeout = sfResolve(ATTR_TEST_TIMEOUT, 0L, true);
        //teardownCD = sfResolve(ATTR_TEARDOWN, teardownCD, false);
        undeployAfter = sfResolve(ATTR_UNDEPLOY_AFTER, 0L, true);
        expectTerminate = sfResolve(ATTR_EXPECT_TERMINATE, false, true);
        exitType = sfResolve(ATTR_EXIT_TYPE, exitType, true);
        exitText = sfResolve(ATTR_EXIT_TEXT, exitText, true);
        shouldTerminate = sfResolve(ShouldDetachOrTerminate.ATTR_SHOULD_TERMINATE,true,true);
    }

    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        Exception exception=null;
        //deploy and evaluate the condition.
        //then decide whether to run or not.

        if (getCondition() !=null && !evaluate()) {
            skipped=true;
            sfLog().info("Skipping test run " + name);
            finish();
            //end do not deploy anything else
            return;
        }

        //deploy the action under a terminator, then the assertions, finally teardown afterwards.
        final boolean isNormalTerminationExpected = TerminationRecord.NORMAL.equals(exitType);
        try {
            actionPrim = deployComponentDescription(ACTION_RUNNING, action);
        } catch (SmartFrogDeploymentException e) {
            //did we expect a normal termination?
            if (isNormalTerminationExpected) {
                //if so, it didnt happen. rethrow the exception
                throw e;
            }
            //we expected failure, so cache it for-post mortem analsysis
            exception = e;
        }

        if(exception==null) {
            //a null exception meant the action was deployed successfully

            //the teardown CD is deployed at this time, but not started.
            //it is brought to life during termination.
            if (teardownCD != null) {
                teardown = deployComponentDescription(ATTR_TEARDOWN, teardownCD);
            }

            //if we get here. then it is time to actually start the action.
            //exceptions are caught and compared to expectations.
            try {
                actionPrim.sfStart();
            } catch (SmartFrogException e) {
                //split on normal/abnormal.
                if (isNormalTerminationExpected) {
                    throw e;
                }
                exception = e;

            } catch (RemoteException e) {
                if (isNormalTerminationExpected) {
                    throw e;
                }
                exception = e;
            }
        }

        //did we catch something during deployment?
        if (exception!=null) {
            //get the message and check it against expections
            String message= exception.getMessage();
            if(message==null) {
                message="";
            }
            if (message.indexOf(exitText) < 0) {
                throw new SmartFrogException("Wrong exitText in the fault raised at creation time\n"
                        +"expected: '"+exitText+"'\n"
                        +"found   : '"+message+"'\n",
                        exception);
            }
            //valid exit. trigger undeploy
            new ComponentHelper(this).sfSelfDetachAndOrTerminate(TerminationRecord.NORMAL,
                    null,null,exception);
        }

        //start the terminator
        actionTerminator = new DelayedTerminator(actionPrim, undeployAfter, sfLog(),
                FORCED_TERMINATION,
                !expectTerminate);
        actionTerminator.start();

        //now deploy the tests.
        //any failure in tests is something to report, as is any failure of the tests to finish.

        startTests();
    }

    /**
     * Start the test runner and a terminator
     * @throws RemoteException
     * @throws SmartFrogDeploymentException
     */
    private void startTests() throws RemoteException, SmartFrogDeploymentException {
        if(tests !=null) {
            testsPrim = sfCreateNewChild(TESTS_RUNNING, tests, null);
            //the test terminator reports a termination as a failure
            testsTerminator = new DelayedTerminator(testsPrim, testTimeout, sfLog(),
                    FORCED_TERMINATION,
                    false);
            testsTerminator.start();
        }
    }

    /**
     * Ping handler. This would be the place to provide feedback w.r.t the result of the deployed action.
     * @param source
     * @throws SmartFrogLivenessException
     * @throws RemoteException
     */
    public void sfPing(Object source) throws SmartFrogLivenessException, RemoteException {
        super.sfPing(source);
    }

    /**
     * Called by {@link #sfPing(Object)} to run through the list
     * of children and ping each in turn. If any child fails,
     * {@link #sfLivenessFailure(Object, Object, Throwable)} is called and the
     * iteration continues.
     * <p/>
     * Override this method to implement different child ping behaviour.
     */
    protected void sfPingChildren() {

        if(actionPrim!=null) {
            sfPingChildAndTerminateOnFailure(actionPrim);
        }
        if (testsPrim != null) {
            sfPingChildAndTerminateOnFailure(testsPrim);
        }
    }

    /**
     * When terminating we shutdown the action and the tests
     * @param status
     */
    public synchronized void sfTerminateWith(TerminationRecord status) {
        super.sfTerminateWith(status);
        if (teardown != null) {
            try {
                teardown.sfStart();
            } catch (SmartFrogException e) {
                sfLog().info("When starting the teardown action", e);
            } catch (RemoteException e) {
                sfLog().info("When starting the teardown action", e);
            }
        }
        shutdown(actionTerminator);
        shutdown(testsTerminator);
    }

    /**
     * shut down the action terminator if we need to. We assume the superclass
     * terminator is already terminating the children, so there is no need
     * to tell the action terminator to do it. Therefore, this should only be called
     * from sfTerminateWith
     *
     * @param terminator
     */
    private void shutdown(DelayedTerminator terminator) {
        if (terminator != null) {
            terminator.shutdown(false);
        }
    }


    /**
     * This is an override point; it is where subclasses get to change their workflow
     * depending on what happens underneath.
     * It is only called outside of component termination, i.e. when {@link #isWorkflowTerminating()} is
     * false, and when the comp parameter is a child, that is <code>sfContainsChild(comp)</code> holds.
     * If the the method returns true, the event is forwarded up the object heirarchy, which
     * will eventually trigger a component termination.
     * <p/>
     * Always return false if you start new components from this method!
     * </p>
     *
     * @param childStatus exit record of the component
     * @param child   child component that is terminating
     * @return true if the termination event is to be forwarded up the chain.
     */
    protected boolean onChildTerminated(TerminationRecord childStatus, Prim child) {
        boolean terminate = shouldTerminate;
        boolean tearDownTime=false;
        TerminationRecord error=null;
        if (actionPrim == child) {
            if (actionTerminator.isForcedShutdown() && !expectTerminate) {
                //this is a forced shutdown, all is well
                sfLog().info("Graceful shutdown of test components");
            } else {
                //not a forced shutdown, so why did it die?
                boolean expected = false;
                //act on whether or not a fault was expected.
                if (childStatus.errorType.indexOf(exitType) >= 0) {
                    //we have a match
                    sfLog().debug("Exit type is as expected");
                    expected = true;
                    if (exitText != null && exitText.length()>0) {
                        String description = childStatus.description;
                        if (description == null) {
                            description = "";
                        }

                        if (description.indexOf(exitText) < 0) {
                            sfLog().debug("Exit text mismatch");
                            expected=false;
                        }
                    }

                } else {
                    sfLog().debug("Exit type mismatch");
                }
                if (!expected) {
                    String errorText = TEST_FAILED_WRONG_STATUS + exitType + "\n"
                            + "and error text " + exitText + "\n"
                            + "but got " + childStatus;
                    sfLog().error(errorText);
                    error = TerminationRecord.abnormal(errorText, childStatus.id);
                    //propagate any exception
                    error.cause=childStatus.cause;
                }
            }
            tearDownTime=true;
        } else if(child == testsPrim) {
            //tests are terminating.
            //it is an error if these terminated abnormally, for any reason at all.
            //that is: test failure triggers an undeployment.
            //There is no need to check this, because its implicit.
            if(!childStatus.isNormal()) {
                sfLog().info("Tests have failed");
            }
            tearDownTime=true;
        }

        //start teardown, etc.
        //kicks in on normal abnormal ter
        if(tearDownTime && teardownCD!=null) {
            try {
                sfLog().debug("Starting teardown component");
                sfCreateNewChild(TEARDOWN_RUNNING, teardownCD, null);
                terminate = false;
            } catch (Exception e) {
                error = TerminationRecord.abnormal("failed to start teardown",
                        name,e);
            }
        }
        synchronized (this) {
            //update internal data structures
            finished = true;
            if (error != null) {
                status = error;
                failed = true;
                succeeded = false;
            } else {
                status = childStatus;
                failed = false;
                succeeded = true;
            }
        }

        //if the error record is non null, terminate ourselves with the new record
        if (error != null) {
            status=error;
            failed=true;
            sfTerminate(error);
            //dont forward, as we are terminating with an error
            terminate = false;
        } else {
            status = childStatus;
            finished=true;
            succeeded=true;
        }
        //trigger termination.
        return terminate;
    }


    /**
     * Return true iff the component is finished. Spin on this, with a (delay)
     * between calls
     *
     * @return
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @return true only if the test has finished and failed
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * @return true iff the test succeeded
     */

    public boolean isSucceeded() {
        return succeeded;
    }

    /**
     * Get the exit record
     *
     * @return the exit record, will be null for an unfinished child
     */


    public TerminationRecord getStatus() {
        return status;
    }

    /**
     * return the tests prim
     *
     * @return the child component. this will be null after termination.
     *
     */
    public Prim getAction() {
        return actionPrim;
    }
}
