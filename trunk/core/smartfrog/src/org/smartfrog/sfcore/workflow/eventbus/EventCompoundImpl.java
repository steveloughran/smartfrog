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

package org.smartfrog.sfcore.workflow.eventbus;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.NoSuchElementException;

import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.compound.Compound;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.common.TerminatorThread;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.ContextImpl;
import org.smartfrog.sfcore.common.*;


/**
 * An extension of Compound providing the SmartFrog Component
 * event handling and the ability of the subclass to control which children get deployed and when.
 * This compound is a good starting block for implementing any Compound extension with complex lifecycles,
 * as most of the setup is handled, and there are override points to tweak behaviour.
 */
public class EventCompoundImpl extends CompoundImpl implements EventBus,
    EventRegistration, EventSink, EventCompound {
    private static Reference receiveRef = new Reference(ATTR_REGISTER_WITH);
    private static Reference sendRef = new Reference(ATTR_SEND_TO);
    private Vector receiveFrom = new Vector();


    private Vector sendTo = new Vector();
    protected ComponentDescription action=null;
    protected Context actions=null;
    protected Enumeration actionKeys=null;

    protected Reference name=null;
    private boolean oldNotation = true;
    private static final Reference actionsRef = new Reference(ATTR_ACTIONS);
    private static final Reference actionRef =  new Reference(ATTR_ACTION);

    /**
     * Constructs EventCompoundImpl.
     *
     * @throws java.rmi.RemoteException In case of RMI or network failure.
     */
    public EventCompoundImpl() throws RemoteException {
        super();
    }

    /**
     * This is an override point. The original set of event components
     * suppored the 'old' notation, in which actions were listed in the {@link #ATTR_ACTIONS element}
     * New subclasses do not need to remain backwards compatible and should declare this fact by
     * returning false from this method
     * @return true
     */
    protected boolean isOldNotationSupported() {
        return true;
    }

    /*
    * Method that overwrites compoundImpl behavior and delays loading eager components to sfStart phase.
    * If action or actions atributes are present then it behaves like compound and loads all eager components.
    */
    protected void sfDeployWithChildren() throws SmartFrogDeploymentException {
      if (isOldNotationSupported() && sfContext().containsKey(ATTR_ACTIONS)){
          oldNotation=true;
          //Old WF notation using actions
          // Here follows normal CompoundImpl deployment
          super.sfDeployWithChildren();

      } else {
          oldNotation=false;
          // New WF notation
          // Delays any child component deployment
          try { // if an exception is thrown in the super call - the termination is already handled
              Context childCtx= new ContextImpl();
              for (Enumeration e = sfContext().keys(); e.hasMoreElements(); ) {
                  Object key = e.nextElement();
                  Object elem = sfContext.get(key);
                  if ((elem instanceof ComponentDescription)&& (((ComponentDescription)elem).getEager())) {
                      childCtx.sfAddAttribute(key, elem);
                      if (action == null) {
                          action = (ComponentDescription)elem;
                      }
                  }
              }

              actionKeys = childCtx.keys();
              actions = childCtx;

          } catch (Exception sfex) {
              new TerminatorThread(this, sfex, null).quietly().start();
              throw (SmartFrogDeploymentException)SmartFrogDeploymentException.forward(sfex);
          }
      }
    }

    /**
     * Registers an EventSink for forwarding of events.
     *
     * @param sink org.smartfrog.sfcore.workflow.eventbus.EventSink
     * @see EventRegistration
     */
    public synchronized void register(EventSink sink) {
        if (sfLog().isDebugEnabled()) {
           sfLog().debug(sfCompleteNameSafe().toString()  + " had registration from " + sink.toString());
        }

        if (!sendTo.contains(sink)) {
            sendTo.addElement(sink);
        }
    }

    /**
     * Deregistera an EventSink for forwarding of events.
     *
     * @param sink org.smartfrog.sfcore.workflow.eventbus.EventSink
     * @see EventRegistration
     */
    synchronized public void deregister(EventSink sink) {
        if (sfLog().isDebugEnabled()) {
           sfLog().debug(sfCompleteNameSafe().toString()  + " had deregistration from " + sink.toString());
        }

        if (sendTo.contains(sink)) {
            sendTo.removeElement(sink);
        }
    }

    /**
     * Handles the event locally then forward to all registered EventSinks.
     *
     * @param event java.lang.Object
     * @see EventSink
     */
    public synchronized void event(Object event) {
        handleEvent(event);
        sendEvent(event);
    }

    /**
     * Default implmentation of the event Handler hook to be overridden in
     * sub-classes. The default implementation does nothing.
     *
     * @param event java.lang.Object The event
     */
    protected void handleEvent(Object event) {
        if (sfLog().isDebugEnabled()){
          sfLog().debug(sfCompleteNameSafe().toString() + " saw " + event);
        }
    }

    /**
     * Default implementation of the EventBus sendEvent method to forward all
     * events to registered EventSinks. Errors are ignored.
     *
     * @param event java.lang.String
     */
    public synchronized void sendEvent(Object event) {
        for (Enumeration e = sendTo.elements(); e.hasMoreElements();) {
            EventSink s = (EventSink) e.nextElement();
            try {
                String infoStr = sfCompleteName().toString()+" sending "+ event+" to "+s.toString();
                if (sfLog().isDebugEnabled()) { sfLog().debug(infoStr);  }
                s.event(event);
            } catch (Exception ex) {
                String evStr="null event";
                if (event!=null ) {
                    evStr=event.toString()+"["+event.getClass().toString()+"]";
                }
                if (sfLog().isErrorEnabled()) {
                   sfLog().error("Failed to send event: "+evStr+", cause: "+ex.getMessage(),ex);
               }
            }
        }
    }

    /**
     * Registers components referenced in the SendTo sub-component registers
     * itself with components referenced in the RegisterWith sub-component.
     *
     * @throws RemoteException In case of network/rmi error
     * @throws SmartFrogDeploymentException In case of any error while
     *         deploying the component
     */
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();

        /* find local registrations and register them */
        ComponentDescription sends = (ComponentDescription) sfResolve(sendRef,false);
        if (sends != null) {
            Context scxt = sends.sfContext();

            for (Enumeration e = scxt.keys(); e.hasMoreElements();) {
                Object k = e.nextElement();
                Reference l = (Reference) scxt.get(k);
                EventSink s = (EventSink) sfResolve(l);
                sendTo.addElement(s);
            }
        }

        /* find own registrations, and register remotely */
        ComponentDescription regs = (ComponentDescription)  sfResolve(receiveRef, false);
        if (regs!=null) {
            Context rcxt = regs.sfContext();

            for (Enumeration keys = rcxt.keys(); keys.hasMoreElements();) {
                Object key = keys.nextElement();
                Reference component = (Reference) rcxt.get(key);
                EventRegistration event = (EventRegistration) sfResolve(component);
                receiveFrom.addElement(event);
                event.register(this);
            }
        }

        if (oldNotation) {
            actions = ((ComponentDescription)sfResolve(actionsRef,true)).sfContext();
            actionKeys = actions.keys();
            sfLog().warn(" 'actions' workflow notation is deprecated");
        } else {
            // actions and actionKeys created during sfDeployWith
        }

        action = (ComponentDescription)sfResolve(actionRef, false);
        name = sfCompleteNameSafe();
    }

    /**
     * Handle notifications of termination
     *
     * @param status termination status of sender
     * @param comp sender of termination
     */
    public void sfTerminatedWith(TerminationRecord status, Prim comp) {
        boolean terminate;
        if (isWorkflowTerminating()) {
            try {
                //let subclasses decide what to do here
                terminate = onWorkflowTerminating(status, comp);
            } catch (Exception e) {
                //but if that fails, we terminate
                sfLog().error("Exception ", e);
                terminate = true;
            }
        } else {
            //check to see what the subclass wants
            try {
                if (sfContainsChild(comp)) {
                    terminate = onChildTerminated(status, comp);
                } else {
                    terminate = onNonChildTerminated(status, comp);
                }
            } catch (Exception e) {
                sfLog().error("Exception ",e);
                terminate =true;
            }
        }
        if (terminate) {
            sfTerminate(status);
        }
    }

    /**
     * This is an override point to handle full component terminations.
     * It is only called during component termination, i.e. when {@link #isWorkflowTerminating()} is
     * true.
     * <p>
     * Normally this should return true. If it returns false, then the termination process may
     * not go through cleanly.
     * </p>
     * @param status exit record of the component
     * @param comp child component that is terminating
     * @return true if the termination event is to be forwarded up the chain.
     * @throws SmartFrogRuntimeException for runtime exceptions
     * @throws RemoteException for network problems
     */

    protected boolean onWorkflowTerminating(TerminationRecord status, Prim comp)
            throws SmartFrogRuntimeException, RemoteException {
        return true;
    }

    /**
    * This is an override point; it is where subclasses get to change their workflow
    * depending on what happens underneath.
    * It is only called outside of component termination, i.e. when {@link #isWorkflowTerminating()} is
    * false, and when the comp parameter is a child, that is <code>sfContainsChild(comp)</code> holds.
    * If the the method returns true, we terminate the component.
    * <p>
    * Always return false if you start new components from this method!
    * </p>
    * @param status exit record of the component
    * @param comp child component that is terminating
    * @return true if the termination event is to be forwarded up the chain.
    * @throws SmartFrogRuntimeException for runtime exceptions
    * @throws RemoteException for network problems
    */
    protected boolean onChildTerminated(TerminationRecord status, Prim comp)
            throws SmartFrogRuntimeException, RemoteException {
        return true;
    }

    /**
     * This is an override point; it is where subclasses get to change their workflow
     * depending on what happens underneath.
     * It is only called outside of component termination, i.e. when {@link #isWorkflowTerminating()} is
     * false, and when <code>sfContainsChild(comp)</code> is false.
     * It is not normally overridden, but is there to provide complete coverage.
     * @param status exit record of the component
     * @param comp   non-child component that is terminating
     * @return true if the component is to be terminated
     * @throws SmartFrogRuntimeException for runtime exceptions
     * @throws RemoteException for network problems
     */
    protected boolean onNonChildTerminated(TerminationRecord status, Prim comp)
            throws SmartFrogRuntimeException, RemoteException {
        return false;
    }

    /**
    * Deregisters from all current registrations.
    *
    * @param status Termination  Record
    */
    public synchronized void sfTerminateWith(TerminationRecord status) {
        /* unregister from all remote registrations */
        for (Enumeration e = receiveFrom.elements(); e.hasMoreElements();) {
            EventRegistration s = (EventRegistration) e.nextElement();

            try {
                s.deregister(this);
            } catch (RemoteException ex) {
            }
        }

        super.sfTerminateWith(status);
    }

    /**
     * force a check for the action in, though the schema should have caught it
     * @throws org.smartfrog.sfcore.common.SmartFrogResolutionException if the action is not defined
     * @throws java.rmi.RemoteException for network problems
     */
    protected void checkActionDefined() throws SmartFrogResolutionException,
        RemoteException {

        sfResolve(actionRef, true);
    }

    /**
     * A synchronized check for termination
     * @return true iff the workflow component is terminating or is already terminated
     */
    protected synchronized boolean isWorkflowTerminating() {
        return sfIsTerminated() || sfIsTerminating();
    }


    /**
     * Get the component descriptions of all actions
     * @return
     */
    public Context getActions() {
        return actions;
    }

    /**
     * Create the children synchronously.
     * @throws java.rmi.RemoteException
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     */
    protected void synchCreateChildren() throws RemoteException, SmartFrogException {
        actionKeys = getActions().keys();
        try {
            while (actionKeys.hasMoreElements()) {
                Object key = actionKeys.nextElement();
                ComponentDescription act = (ComponentDescription) actions.get(key);
                sfDeployComponentDescription(key, this, act, null);
                if (sfLog().isDebugEnabled()) sfLog().debug("Creating "+key);
            }
        } catch (NoSuchElementException ignored){
           throw new SmartFrogRuntimeException ("Found no children to deploy",this);
        }

        //Actions are now children of this component, they are deployed and
        //started
        for (Enumeration e = sfChildren(); e.hasMoreElements();) {
            Object elem = e.nextElement();

            if (elem instanceof Prim) {
                ((Prim) elem).sfDeploy();
            }
        }

        for (Enumeration e = sfChildren(); e.hasMoreElements();) {
            Object elem = e.nextElement();

            if (elem instanceof Prim) {
                ((Prim) elem).sfStart();
            }
        }
    }

    /**
     * Helper method to deploy any component of a given name. It's template is replaced in the graph
     * by the running component
     *
     * @param name     attribute to look up
     * @param required flag to indicate the component is required
     * @return the component or null if there was no attribute and required was false.
     * @throws org.smartfrog.sfcore.common.SmartFrogResolutionException
     * @throws java.rmi.RemoteException
     * @throws org.smartfrog.sfcore.common.SmartFrogDeploymentException
     */
    protected Prim deployChildCD(String name, boolean required)
            throws SmartFrogResolutionException, RemoteException, SmartFrogDeploymentException {
        ComponentDescription cd = null;
        cd = sfResolve(name, cd, false);
        if (cd != null) {
            return sfCreateNewChild(name, cd, null);
        } else {
            return null;
        }
    }
}
