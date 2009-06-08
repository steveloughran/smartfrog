/* (C) Copyright 2008 Hewlett-Packard Development Company, LP

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
package org.smartfrog.sfcore.utils;

import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;

import java.rmi.RemoteException;

/**
 * The Workflow thread will terminate its owner when finishing.
 *
 */

public class WorkflowThread extends SmartFrogThread {
    private Prim owner;
    private Reference ownerID;
    private boolean workflowTermination;

    /**
     * termination message: {@value}
     */
    public static final String WORKER_THREAD_COMPLETED = "Worker thread completed";
    /**
     * abnormal termination message: {@value}
     */
    public static final String WORKER_THREAD_FAILED = "Worker thread failed";

    /**
     * Allocates a new <code>SmartFrogThread</code> object.
     *
     * @param owner owner obj
     * @param notifyObject to notify afterwards. If null, "this" is used
     * @param workflowTermination should workflow (optional) termination rules be used? If false, this thread
     * always terminates the parent when finished
     */
    public WorkflowThread(Prim owner, boolean workflowTermination, Object notifyObject) {
        super(notifyObject);
        bind(owner, workflowTermination);
    }

    public Prim getOwner() {
        return owner;
    }

    public Reference getOwnerID() {
        return ownerID;
    }

    public boolean isWorkflowTermination() {
        return workflowTermination;
    }

    /**
     * Bind to the owner
     * @param owner owner prim
     * @param workflowTermination workflow policy
     */
    private void bind(Prim owner, boolean workflowTermination) {
        this.owner = owner;
        this.workflowTermination = workflowTermination;
        try {
            ownerID = owner.sfCompleteName();
        } catch (RemoteException e) {
            ownerID = null;
        }
    }

    /**
     * Create a basic thread. Notification is bound to a local notification object.
     *
     * @param owner owner thread
     * @param workflowTermination is workflow termination expected
     */
    public WorkflowThread(Prim owner, boolean workflowTermination) {
        this(owner,workflowTermination, new Object());
    }

    /**
     * Create a basic thread bound to a runnable
     *
     * @param owner owner prim
     * @param target the object whose <code>run</code> method is called.
     * @param workflowTermination workflow policy
     */
    public WorkflowThread(Prim owner, Executable target, boolean workflowTermination) {
        super(target);
        bind(owner, workflowTermination);
    }

    /**
     * Runs the {@link #execute()} method, catching any exception it throws and storing it away for safe keeping After
     * the run, the notify object is notified, and we trigger a workflow termination
     */
    @Override
    public void run() {
        //do the work and catch the result
        super.run();
        processRunResults();
    }

    /**
     * Analyse the results of the run -assume a thrown run means failure,
     * and decide whether or not to terminate normally.
     * {@link #didThreadTerminateNormally()} is called to make that assessment,
     * the {@link #aboutToTerminate(TerminationRecord)} method is called, then
     * if either the termination was normal and this is a workflow thread,
     * or if the thread terminated abnormally, then the owner is terminated
     * with a termination record created by {@link #createTerminationRecord()} 
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    protected void processRunResults() {
        //now analyse the result, create a term record and maybe terminate the owner
        boolean isNormal = didThreadTerminateNormally();
        TerminationRecord tr = createTerminationRecord();
        aboutToTerminate(tr);
        ComponentHelper helper = new ComponentHelper(owner);
        if (workflowTermination && isNormal) {
            //put up for workflow termination
            helper.sfSelfDetachAndOrTerminate(tr);
        } else {
            //workflow termination is disabled, or something went wrong
            //put up for termination
            helper.targetForTermination(tr, false, false, false);
        }
    }

    /**
     * This is an override point -did the thread terminate normally.
     * The base class assumes that a non-empty value from {@link #getThrown()} implies
     * that the thread failed.
     * @return true if the the thread was considered to have terminated normally.
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    protected boolean didThreadTerminateNormally() {
        return getThrown() == null;
    }

    /**
     * Create a TR from the termination message of {@link #getTerminationMessage()}
     * and any exception thrown -the latter determines whether or not the TR
     * is considered normal or not
     * @return a termination record
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    protected TerminationRecord createTerminationRecord() {
        TerminationRecord tr = new TerminationRecord(
                didThreadTerminateNormally() ? TerminationRecord.NORMAL : TerminationRecord.ABNORMAL,
                getTerminationMessage(),
                ownerID,
                getThrown());
        return tr;
    }

    /**
     * this is an override point. The TR is passed in for examination and
     * editing. The base implementation does nothing.
     * @param tr the termination record about to be passed up
     */
    protected void aboutToTerminate(TerminationRecord tr) {

    }

    /**
     * Override point: the termination message
     * @return {@link #WORKER_THREAD_COMPLETED} or {@link #WORKER_THREAD_FAILED} depending on the outcome
     */
    protected String getTerminationMessage() {
        return didThreadTerminateNormally() ? WORKER_THREAD_COMPLETED : WORKER_THREAD_FAILED;
    }
    
}
