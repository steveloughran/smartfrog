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
package org.smartfrog.sfcore.workflow.combinators;

import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.utils.ComponentHelper;

import java.lang.ref.WeakReference;
import java.rmi.RemoteException;

/**
 * Component to shut down a task after a delay.
 * We retain a (weak) reference to a prim. The component can go off the graph
 * and we can still find it, but we don't preclude distributed GC taking place and killing the reference.
 * This stops accidental retention of a terminator thread from keeping the prim around.
 */
public class DelayedTerminator implements Runnable {
    private long time;
    private WeakReference/*<Prim>*/ primref;
    private Throwable terminationFault;
    private volatile Thread self;
    private volatile boolean shutdown;
    private volatile boolean shouldTerminate = true;
    private LogSF log;
    private String description;
    private volatile boolean normalTermination;
    private volatile boolean forcedShutdown;
    private String name;


    /**
     * Create a delayed time.
     * If the time is -1, then the wait is for {@link Long#MAX_VALUE}, otherwise it
     * is for as many milliseconds as needed.
     *
     * <i>Important</i> This does not start the thread. Call {@link #start()} to do that.
     * @param prim component to shut down
     * @param time how long to sleep
     * @param log a log to log to
     * @param description text to use in the termination record (or null to have something made up)
     * @param normalTermination should the termination be normal.
     */
    public DelayedTerminator(Prim prim, long time, LogSF log, String description, boolean normalTermination) {
        if(time<0) {
            time=Long.MAX_VALUE;
        }
        this.time = time;
        primref = new WeakReference/*<Prim>*/(prim);
        this.log = log;
        if (description == null) {
            this.description = "Terminate "
                    + new ComponentHelper(prim).completeNameSafe().toString()
                    + " after " + time + " milliseconds";
        } else {
            this.description = description;
        }
        this.normalTermination = normalTermination;
    }

    public Throwable getTerminationFault() {
        return terminationFault;
    }

    public void setTerminationFault(Throwable terminationFault) {
        this.terminationFault = terminationFault;
    }


    public boolean isNormalTermination() {
        return normalTermination;
    }

    public void setNormalTermination(boolean normalTermination) {
        this.normalTermination = normalTermination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Call from the owner to shut down the target.
     * @param terminateTarget should we terminate the child?
     */
    public synchronized void shutdown(boolean terminateTarget) {
        if (self != null) {
            shutdown = true;
            shouldTerminate = terminateTarget;
            self.interrupt();
        }
    }


    /**
     * Start the new thread
     */
    public synchronized void start() {
        if(self==null) {
            self = new Thread(this);
            if(name!=null) {
                self.setName(name);
            }
            self.start();
        }
    }

    /**
     * this is the thread that runs in the background
     */
    public void run() {

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            //we have been interrupted here.
            log.debug("Interrupted " + description);
        }

        synchronized (this) {
            try {
                if (shutdown) {

                    //initiated shutdown
                    log.debug("initiated shutdown " + description);
                }

                if (shouldTerminate) {
                    //termination time
                    Prim target = getTarget();
                    if (target == null) {
                        log.debug("Target no longer exists for " + description);
                    } else {
                        try {
                            if (target.sfIsStarted()) {
                                forcedShutdown = true;
                                TerminationRecord record = createTerminationRecord(target);
                                target.sfTerminate(record);
                            }
                        } catch (RemoteException e) {
                            terminationFault = e;
                        }
                    }
                }
            } finally {
                //cease to exist
                self = null;
                primref = null;
            }
        }

    }

    private TerminationRecord createTerminationRecord(Prim target) {
        Reference ref = new ComponentHelper(target).completeNameSafe();
        TerminationRecord record = new TerminationRecord(
                normalTermination ? TerminationRecord.NORMAL : TerminationRecord.ABNORMAL,
                "termination by delayed terminator",
                ref);
        return record;
    }

    private Prim getTarget() {
        return (Prim) primref.get();
    }

    /**
     * Flag set to true if we forced system shutdown
     * @return whether system was forced
     */
    public synchronized boolean isForcedShutdown() {
        return forcedShutdown;
    }
}
