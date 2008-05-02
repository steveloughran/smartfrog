/* (C) Copyright 2007 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.assertions.events;

import org.smartfrog.services.assertions.TestFailureException;
import org.smartfrog.services.assertions.TestTimeoutException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogRuntimeException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.utils.SmartFrogThread;
import org.smartfrog.sfcore.workflow.eventbus.EventRegistration;
import org.smartfrog.sfcore.workflow.eventbus.EventSink;
import org.smartfrog.sfcore.workflow.events.LifecycleEvent;
import org.smartfrog.sfcore.workflow.events.StartedEvent;
import org.smartfrog.sfcore.workflow.events.TerminatedEvent;

import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handler for test lifecycle events.
 *
 * This class can register interest in remote events, and subscribe to their lifecycle events.
 *
 * Important: this component does not unsubscribe in its finalizer, because finalizers are so unreliable. Manually
 * {@link #unsubscribe()} or use {@link #unsubscribeQuietly()} in the teardown
 *
 * Created 10-Jul-2007 12:23:27
 *
 * Currently this tool uses simple RMI to register itself. At some point it will need to switch to smartfrog
 * registration when running under a daemon, so that all the security kicks in.
 *
 * Important: be very careful with synchronisation here, because RMI calls come in on different threads. It is easy to
 * deadlock, especially during teardown operations.
 */

public class TestEventSink implements EventSink {


    /**
     * Queue of incoming events
     */
    private Queue<LifecycleEvent> incoming = new ConcurrentLinkedQueue<LifecycleEvent>();

    /**
     * History of events
     */
    private List<LifecycleEvent> history = new ArrayList<LifecycleEvent>();

    /**
     * The source of events
     */
    private volatile EventRegistration source;

    /**
     * This is a remote stub to ourselves, which is set after exporting the instance using RMI
     */
    private volatile RemoteStub remoteStub;

    /**
     * error message : {@value}
     */
    public static final String ERROR_STARTUP_TIMEOUT = "Timeout waiting for the application to start";
    /**
     * error message : {@value}
     */
    public static final String ERROR_TEST_RUN_TIMEOUT = "Timeout waiting for a test run to complete";
    /**
     * error message : {@value}
     */
    private static final String ERROR_PREMATURE_TERMINATION = "Test component terminated before starting up";
    public static final String ERROR_WRONG_TYPE
            = "Cannot cast a component to an EventRegistration instance, as it is of the wrong type: ";


    /**
     * Simple constructor
     */
    public TestEventSink() {
    }

    /**
     * Create and subscribe
     *
     * @param source source of events
     * @throws RemoteException for network problems
     */
    public TestEventSink(EventRegistration source) throws RemoteException {
        subscribe(source);
    }


    /**
     * Cast an application to an EventRegistration interface and subscribe to its events
     *
     * @param application the application (which must implement{@link EventRegistration})
     * @throws RemoteException if something goes wrong with the subscription
     * @throws SmartFrogRuntimeException if the class is the wrong time
     */
    public TestEventSink(Prim application) throws RemoteException, SmartFrogRuntimeException {
        if (!(application instanceof EventRegistration)) {
            throw new SmartFrogRuntimeException(ERROR_WRONG_TYPE
                    + application.getClass(), application);
        }
        subscribe((EventRegistration) application);
    }

    /**
     * Unsubscribe from the current event source
     *
     * @return true if we unsubscribe This is not synchronised, to avoid cross-machine deadlocks.
     * @throws RemoteException for network problems
     */
    public boolean unsubscribe() throws RemoteException {
        boolean shouldUnexport = false;
        if (source != null) {
            EventRegistration registration;
            registration = source;
            source = null;
            remoteStub = null;
            registration.deregister(this);
            shouldUnexport = true;
        }
        return !shouldUnexport || UnicastRemoteObject.unexportObject(this, true);
    }


    /**
     * Unsubscribe from the current event source
     *
     * @return true if we unsubscribe
     */
    public boolean unsubscribeQuietly() {
        try {
            return unsubscribe();
        } catch (RemoteException ignored) {
            return false;
        }
    }

    /**
     * unsubscribe in a different thread
     *
     * @return the thread or null if we were already unsubscribed
     */
    public SmartFrogThread asyncUnsubscribe() {
        if (!isListening()) {
            return null;
        }
        SmartFrogThread thread = new SmartFrogThread(new AsyncUnsubscribe(this));
        thread.start();
        return thread;
    }

    /**
     * Subscribe to an event source
     *
     * @param target what to subscribe to
     * @throws RemoteException for network problems
     */
    private synchronized void subscribe(EventRegistration target) throws RemoteException {
        if (source != null) {
            throw new IllegalStateException("Cannot subscribe more than once");
        }
        remoteStub = UnicastRemoteObject.exportObject(this);
        setSource(target);
        if (target != null) {
            target.register(this);
        }
    }

    public EventRegistration getSource() {
        return source;
    }


    /**
     * Is this sink listening
     *
     * @return true iff the source is not null
     */
    public boolean isListening() {
        return source != null;
    }

    /**
     * Get the application/event source we are bonded to.
     *
     * @return the application
     */
    public Prim getApplication() {
        return (Prim) source;
    }

    /**
     * Set the source
     *
     * @param source source
     */
    private void setSource(EventRegistration source) {
        this.source = source;
    }


    /**
     * Get the remote stub of this object
     *
     * @return the remote stub (which is null when we are not exported)
     */
    public RemoteStub getRemoteStub() {
        return remoteStub;
    }

    /**
     * return the object at the head of the event queue, or null. The event is removed from the queue and added to the
     * history
     *
     * @return the polled object.
     */
    public synchronized LifecycleEvent poll() {
        if (incoming.size() == 0) {
            return null;
        } else {
            LifecycleEvent event = incoming.remove();
            history.add(event);
            return event;
        }
    }


    /**
     * Get at the history of past events
     *
     * @return the history
     */
    public List<LifecycleEvent> getHistory() {
        return history;
    }

    /**
     * block for any event
     *
     * @param timeout timeout delay; can be 0
     * @return the polled object or null for a timeout
     * @throws InterruptedException if the thread waiting was interrupted
     */

    public synchronized LifecycleEvent waitForEvent(long timeout) throws InterruptedException {
        LifecycleEvent event;
        event = poll();
        if (event == null) {
            wait(timeout);
            event = poll();
            if (event == null) {
                return null;
            }
        }
        return event;
    }

    /**
     * Wait for an event of a specific type
     *
     * @param clazz   classname
     * @param timeout time to wait between incoming events
     * @return the event or null for a timeout or interruption
     * @throws InterruptedException if the thread waiting was interrupted, or a TestInterruptedEvent was encountered
     */
    public synchronized LifecycleEvent waitForEvent(Class clazz, long timeout) throws InterruptedException {
        LifecycleEvent event;
        boolean isNotInstance;
        TimeoutTracker timedout = new TimeoutTracker(timeout);
        do {
            event = waitForEvent(timeout);
            if (event == null) {
                return null;
            }
            if(event instanceof TestInterruptedEvent) {
                throw new InterruptedException();
            }
            isNotInstance = !clazz.isInstance(event);

        } while (isNotInstance && !timedout.isTimedOut());
        return event;
    }


    /**
     * Handle an event by adding it to the log, then raising a notification
     *
     * @param event the received event
     * @throws RemoteException if the event is not a LifecyleEvent
     */
    public void event(Object event) throws RemoteException {
        if (!(event instanceof LifecycleEvent)) {
            throw new RemoteException("Only instances of LifecycleEvent are supported");
        }
        synchronized (this) {
            incoming.add((LifecycleEvent) event);
            notifyAll();
        }
    }

    /**
     * Push an interruption onto the event queue.
     * @throws RemoteException on network problems
     */
    public void interrupt() throws RemoteException {
        event(new TestInterruptedEvent());
    }

    /**
     * Deploy by asking the applicaton to deploy
     *
     * @throws SmartFrogException error while deploying
     * @throws RemoteException    In case of Remote/nework error
     */
    public void invokeDeploy() throws SmartFrogException, RemoteException {
        getApplication().sfDeploy();
    }

    /**
     * Deploy by asking the applicaton to deploy
     *
     * @throws SmartFrogException error while deploying
     * @throws RemoteException    In case of Remote/nework error
     */
    public void invokeStart() throws SmartFrogException, RemoteException {
        getApplication().sfStart();
    }

    /**
     * Start the application and block until the component reports itself as started. If the component terminates during
     * this time. If the application is not yet deployed, that is done too.
     *
     * @param timeout time in ms to wait
     * @return the startup event or null if it didn't start
     * @throws SmartFrogException   for deployment problems
     * @throws TestTimeoutException if a timeout occurred
     * @throws TestFailureException if the component terminated before starting up
     * @throws RemoteException      for network problems
     * @throws InterruptedException if the thread waiting was interrupted
     */
    public StartedEvent startApplication(long timeout)
            throws SmartFrogException, RemoteException, InterruptedException {
        if (!getApplication().sfIsDeployed()) {
            invokeDeploy();
        }
        invokeStart();
        TimeoutTracker timedout = new TimeoutTracker(timeout);
        LifecycleEvent event;
        do {
            event = waitForEvent(LifecycleEvent.class, timeout);
            if (event == null) {
                throw new TestTimeoutException(ERROR_STARTUP_TIMEOUT, timeout);
            }
            if (event instanceof TerminatedEvent) {
                throw new TestFailureException(ERROR_PREMATURE_TERMINATION, event);
            }
        } while (!(event instanceof StartedEvent) && !timedout.isTimedOut());
        return (StartedEvent) event;
    }

    /**
     * Run the tests to completion, return the lifecycle event
     *
     * @param startupTimeout time to wait in millis for startup
     * @param executeTimeout time to wait in millis for execution
     * @return the {@link TestCompletedEvent} or {@link TerminatedEvent} at the end of the run
     * @throws SmartFrogException   deployment problems including timeout
     * @throws TestTimeoutException if a timeout occurred
     * @throws RemoteException      for network problems
     * @throws InterruptedException if the thread waiting was interrupted
     */
    public LifecycleEvent runTestsToCompletion(long startupTimeout, long executeTimeout)
            throws SmartFrogException, InterruptedException, RemoteException {
        if(getApplication().sfIsTerminated()) {
            //we are (somehow) already terminated, so report this as a problem
            LifecycleEvent termEvent=new TerminatedEvent(getApplication(),
                    TerminationRecord.abnormal("Test component has already terminated",getApplication().sfCompleteName()));
            return termEvent;
        }
        startApplication(startupTimeout);
        LifecycleEvent event;
        TimeoutTracker timedout = new TimeoutTracker(executeTimeout);
        do {
            event = waitForEvent(LifecycleEvent.class, executeTimeout);
            if (event == null) {
                throw new TestTimeoutException(ERROR_TEST_RUN_TIMEOUT + "\n" + dumpHistory(), executeTimeout);
            }
        } while (!(event instanceof TerminatedEvent) && !(event instanceof TestCompletedEvent)
                && !timedout.isTimedOut());
        return event;
    }


    /**
     * Dump the history to a string, one event per line. Used in timeout reports
     *
     * @return the history of recevied events.
     */
    public String dumpHistory() {
        StringBuilder buffer = new StringBuilder("Event history has " + history.size() + " events\n");
        for (LifecycleEvent event : history) {
            buffer.append(event.toString());
            buffer.append('\n');
        }
        return buffer.toString();
    }


    /**
     * A little source of asynchronous unsubscriptions...this is done to remove re-entrancy on unsub operations.
     */
    private static class AsyncUnsubscribe implements Runnable {

        private TestEventSink owner;
        private RemoteException result;

        private AsyncUnsubscribe(TestEventSink owner) {
            this.owner = owner;
        }

        public void run() {
            try {
                owner.unsubscribe();
            } catch (RemoteException e) {
                result = e;
            }
        }


        public RemoteException getResult() {
            return result;
        }
    }
}
