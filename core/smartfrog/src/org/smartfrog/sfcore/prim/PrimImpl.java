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

package org.smartfrog.sfcore.prim;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.util.Enumeration;
import java.util.Iterator;

import org.smartfrog.sfcore.common.SmartFrogCoreKeys;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.Logger;
import org.smartfrog.sfcore.common.MessageKeys;
import org.smartfrog.sfcore.common.MessageUtil;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLifecycleException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.SmartFrogRuntimeException;
import org.smartfrog.sfcore.common.SmartFrogContextException;
import org.smartfrog.sfcore.common.TerminatorThread;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.processcompound.SFProcess;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.reference.ReferencePart;
import org.smartfrog.sfcore.security.SFGeneralSecurityException;
import org.smartfrog.sfcore.security.SecureRemoteObject;

import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.utils.ComponentHelper;

import org.smartfrog.sfcore.reference.RemoteReferenceResolverHelperImpl;
import org.smartfrog.sfcore.reference.HereReferencePart;

import java.rmi.NoSuchObjectException;
import java.net.UnknownHostException;
import org.smartfrog.sfcore.common.SFMarshalledObject;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.common.ContextImpl;
import org.smartfrog.sfcore.common.Diagnostics;

/**
 * Defines the base class for all deployed components. A deployed component
 * knows how to react to termination, deployment requests and heart beats.
 * This implementation allows subclasses to define how a deployed component is
 * communicated with. The sfExport attribute is examined. If set to "true" the
 * sfExportRef method is called, which by default uses RMI to export the
 * component. Subclasses may choose to export a component another way.
 *
 * <p>
 * A liveness thread is started for a primitive if it has no parent, but is
 * expected to have children (like Compound), or if the parent is remote.
 * Liveness attribute sfLivenessDelay dictates how often (in seconds) sfPing
 * is supposed to be called. sfLivenessFactor is the multiplier for
 * sfLivenessDelay to wait until the primitive declares that there is a
 * liveness failure. Both attributes are looked up using ATTRIB in order to
 * find out whether a parent has set liveness on.
 * </p>
 *
 */
public class PrimImpl extends RemoteReferenceResolverHelperImpl implements Prim, MessageKeys {

    /** Component Log. This log is used to from any component.
     *  Initialized to log into the core log: SF_CORE_LOG
     *  It can be replaced using sfSetLog()
     */
    private LogSF  sflog = null;

    /** Static attribute that hold the lifecycle hooks for sfDeploy. */
    public static PrimHookSet sfDeployHooks = new PrimHookSet();

    /** Static attribute that hold the lifecycle hooks for sfStart. */
    public static PrimHookSet sfStartHooks = new PrimHookSet();

    /** Static attribute that hold the lifecycle hooks for sfDeployWith. */
    public static PrimHookSet sfDeployWithHooks = new PrimHookSet();

    /** Static attribute that hold the lifecycle hooks for sfTerminateWith. */
    public static PrimHookSet sfTerminateWithHooks = new PrimHookSet();

    /** Reference used to look up sfLivenessDelay attributes. */
    protected static final Reference refLivenessDelay = new Reference(ReferencePart.attrib(
                SmartFrogCoreKeys.SF_LIVENESS_DELAY));

    /** Reference used to look up sfLivenessFactor attributes. */
    protected static final Reference refLivenessFactor = new Reference(ReferencePart.attrib(
                SmartFrogCoreKeys.SF_LIVENESS_FACTOR));

    /** Flag indicating that this component has been terminated. */
    protected boolean sfIsTerminated = false;

    /** Flag indicating that this component termination is initiated. */
    protected boolean sfIsTerminating = false;

    /** Flag indicating that this component has been deployed. */
    protected boolean sfIsDeployed = false;

    /** Flag indicating that this component has been started. */
    protected boolean sfIsStarted = false;

    /** Parent. */
    protected Prim sfParent = null;

    /** Attribute context. */
    protected Context sfContext = null;

    /** Timer for initiating heartbeats. */
    protected LivenessSender sfLivenessSender;

    /** Current count for liveness, updated through sfPing. */
    protected int sfLivenessCount;

    /**
     * Livneess factor. Initializer for liveness count. How many multiples of
     * livenss delay to wait till a liveness failure of the parent is
     * declared, Defaults to 2.
     */
    protected int sfLivenessFactor = 2;

    /**
     * How often to send liveness in seconds. Defaults to 0 (off, ie. no
     * liveness).
     */
    protected long sfLivenessDelay = 0;

    /** Reference to export form of self if sfExport is true. */
    protected Object sfExportRef = null;

    /** Reference that caches cannonical name. */
    protected Reference sfCompleteName = null;


    /**
     * Used in conjunction with sfDeployWith to set parent and context after
     * creation.
     *
     * @throws RemoteException In case of network/rmi error
     */
    public PrimImpl() throws RemoteException {
    }

    /**
     * Find an attribute in this component context.
     *
     * @param name attribute key to resolve
     *
     * @return Object Reference
     *
     * @throws SmartFrogResolutionException failed to find attribute
     */
    public Object sfResolveHere(Object name)
        throws SmartFrogResolutionException {
        Object result = null;
        try {
           result = sfContext.sfResolveAttribute(name);
           try {
               if (sfLog().isTraceEnabled()) {
                   sfLog().trace("sfResolved HERE '"+name.toString()+"' to '"+ result.toString()+"'");
               }
           } catch (Throwable thr) {thr.printStackTrace();} //ignore

        } catch (SmartFrogContextException ex) {
            //sfCompleteName() uses sfResolveHere() and therefore it can be a problem here :-)!
            // Using the sfCompleteName cache to provide as much info a it is available.
            throw SmartFrogResolutionException.notFound(new Reference(name), sfCompleteName ,ex);

        }
        return result;
    }

    /**
     * Find an attribute in this context.
     *
     * @param name attribute key to resolve
     * @param mandatory boolean that indicates if this attribute must be
     *        present in the description. If it is mandatory and not found it
     *        throws a SmartFrogResolutionException
     *
     * @return Object value for attribute
     *
     * @throws SmartFrogResolutionException failed to find attribute
     */
    public Object sfResolveHere(Object name, boolean mandatory)
        throws SmartFrogResolutionException {
        try {
            return sfResolveHere(name);
        } catch (SmartFrogResolutionException e) {
            if (mandatory) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Returns the parent for this component.
     *
     * @return parent for this component or null if none
     */
    public Object sfResolveParent() {
        return sfParent;
    }

    /**
     * Resolves a given reference. Forwards to indexed resolve with index 0
     * and return resulting attribute value.
     *
     * @param r reference to resolve
     *
     * @return resolved reference
     *
     * @throws SmartFrogResolutionException occurred while resolving
     * @throws RemoteException In case of network/rmi error
     */
    public Object sfResolve(Reference r)
        throws SmartFrogResolutionException, RemoteException {
        Object obj = sfResolve(r, 0);
        if (obj instanceof SFMarshalledObject){
            //  Unmarshall!Obj.
            obj = ((SFMarshalledObject)obj).get();
        }
        try {
            if (sfLog().isTraceEnabled()) {
                sfLog().trace(sfCompleteNameSafe()+ " sfResolved '"+r.toString()+"' to '"+ obj.toString()+"'");
            }
        } catch (Throwable thr) {thr.printStackTrace();} //ignore
        return obj;
    }


    /**
     * Resolves given reference starting at given index. This is forwarded to
     * the reference (and on to each reference part).
     *
     * @param r reference to resolve
     * @param index index in reference to start resolving
     *
     * @return resolved reference
     *
     * @throws SmartFrogResolutionException error occurred while resolving
     * @throws RemoteException In case of network/rmi error
     */
    public Object sfResolve(Reference r, int index)
        throws SmartFrogResolutionException, RemoteException {
        Object obj = null;

        try {
            obj = r.resolve(this, index);
        } catch (SmartFrogResolutionException rex) {
            if ((!(rex.containsKey(SmartFrogRuntimeException.SOURCE)))
                    || (rex.get(SmartFrogRuntimeException.SOURCE)== null)) {
                rex.put(SmartFrogRuntimeException.SOURCE, this.sfCompleteNameSafe());
                rex.put(SmartFrogResolutionException.DEPTH, new Integer(index));
            }
            rex.appendPath(this.sfCompleteName().toString() + " ");
            throw rex;
        } catch (java.lang.StackOverflowError st){
            throw new SmartFrogResolutionException(r,this.sfCompleteNameSafe(),
               st.toString() +". Possible cause: cyclic reference",null,st,this);
        } catch (Throwable thr){
             throw new SmartFrogResolutionException(r,this.sfCompleteNameSafe(),
              null,null,thr,this);
        }

        return obj;
    }

    /**
     * Request the host on which this component is deployed.
     *
     * @return the host InetAddress
     *
     * @throws RemoteException In case of network/rmi error
     */
    public InetAddress sfDeployedHost() throws RemoteException {
        try {
            String hostName = System.getProperty("java.rmi.server.hostname");
            try {
                if (hostName!=null) {
                    return java.net.InetAddress.getByName(hostName);
                }
            } catch (UnknownHostException ex) {
               if (sfLog().isIgnoreEnabled()){
                 sfLog().ignore(MessageUtil.formatMessage(MSG_FAILED_INET_ADDRESS_LOOKUP),ex);
               }
//               Logger.logQuietly(MessageUtil.formatMessage(MSG_FAILED_INET_ADDRESS_LOOKUP),ex);
            }
            return java.net.InetAddress.getLocalHost();
        } catch (Exception ex) {
//            Logger.logQuietly(MessageUtil.formatMessage(MSG_FAILED_INET_ADDRESS_LOOKUP),ex);
          if (sfLog().isIgnoreEnabled()){
            sfLog().ignore(MessageUtil.formatMessage(MSG_FAILED_INET_ADDRESS_LOOKUP),ex);
          }
        }
        return null;
    }

    /**
     * Request the process in which this component is deployed, the name being
     * that defined in the sfProcessName attribute or the string ROOT if in
     * the root process compound.
     *
     * @return the name of the process
     *
     * @throws RemoteException In case of Remote/network error
     */
    public String sfDeployedProcessName() throws RemoteException {
        String value = (String) System.getProperty(
                "org.smartfrog.sfcore.processcompound.sfProcessName");

        if (value == null) {
            return SmartFrogCoreKeys.SF_ROOT;
        } else {
            return value;
        }
    }

    /**
     * Adds an attribute to this component under given name.
     *
     * @param name name of attribute
     * @param value value of attribute
     *
     * @return added attribute if non-existent or null otherwise
     *
     * @throws SmartFrogRuntimeException when name or value are null
     * @throws RemoteException In case of Remote/nework error
     */
    public synchronized Object sfAddAttribute(Object name, Object value)
        throws SmartFrogRuntimeException, RemoteException {
        try {
            return sfContext.sfAddAttribute(name, value);
        } catch (SmartFrogContextException ex) {
            ex.init(this);
            throw ex;
        }
    }

    /**
     * Removes an attribute from this component.
     *
     * @param name of attribute to be removed
     *
     * @return removed attribute value if successfull or null if not
     *
     * @throws SmartFrogRuntimeException when name is null
     * @throws RemoteException In case of Remote/nework error
     */
    public synchronized Object sfRemoveAttribute(Object name)
        throws SmartFrogRuntimeException, RemoteException {
        try {
            return sfContext.sfRemoveAttribute(name);
        } catch (SmartFrogContextException ex) {
            ex.init(this);
            throw ex;
        }
    }

    /**
     * Replace named attribute in component context. If attribute is not
     * present it is added to the context.
     *
     * @param name of attribute to replace
     * @param value value to add or replace
     *
     * @return the old value if present, null otherwise
     *
     * @throws SmartFrogRuntimeException when name or value are null
     * @throws RemoteException In case of Remote/nework error
     */
    public synchronized Object sfReplaceAttribute(Object name, Object value)
        throws SmartFrogRuntimeException, RemoteException {
        try {
            return sfContext.sfReplaceAttribute(name, value);
        } catch (SmartFrogContextException ex) {
            ex.init(this);
            throw ex;
        }
    }


    /**
     * Returns the attribute key given a value.
     *
     * @param value value to look up key for
     *
     * @return key for attribute value or null if none
     */

    // perhaps this should be synchronized... but causes problems with sfCompleteName if it is
    public Object sfAttributeKeyFor(Object value) {
        return sfContext.sfAttributeKeyFor(value);
    }

    /**
     * Returns true if the context contains value.
     *
     * @param value object to check
     *
     * @return true if context contains value, false otherwise
     *
     * @throws RemoteException In case of Remote/nework error
     */
    public boolean sfContainsValue(Object value) throws RemoteException{
       return sfContext.contains(value);
    }


    /**
     * Returns true if the context contains attribute.
     * @param attribute to check
     *
     * @return true if context contains attribute, false otherwise
     *
     * @throws RemoteException In case of Remote/nework error
     */
    public boolean sfContainsAttribute(Object attribute) throws RemoteException {
       return sfContext.containsKey(attribute);
    }

    /**
     * Returns an ordered iterator over the attribute names in this component.
     * The remove operation of this Iterator won't affect
     * the contents of this component
     *
     * @return iterator
     *
     * @throws RemoteException In case of Remote/nework error
     */
    public  Iterator sfAttributes() throws RemoteException{
        return sfContext.sfAttributes();
    }

    /**
     * Returns an ordered iterator over the attibute values in this component.
     * The remove operation of this Iterator won't affect
     * the contents of this component
     *
     * @return iterator
     */
    public  Iterator sfValues() throws RemoteException{
      return sfContext.sfValues();
    }



    /**
     * Returns the context of this component.
     *
     * @return component context
     */
    public Context sfContext() {
        return sfContext;
    }


    /**
     * Returns the complete name for this component from the root of the
     * application.
     *
     * @return reference of attribute names to this component
     *
     * @throws RemoteException In case of network/rmi error
     */
    public Reference sfCompleteName() throws RemoteException {
        if (sfCompleteName==null) {
            Reference r;
            Object key;
            if (sfParent==null) {
                r = SFProcess.getProcessCompound().sfCompleteName();
                key = SFProcess.getProcessCompound().sfAttributeKeyFor(this);
            } else {
                r = sfParent.sfCompleteName();
                key = sfParent.sfAttributeKeyFor(this);
            }
            sfCompleteName= (Reference)r.clone();

            if (key!=null) {
                sfCompleteName.addElement(ReferencePart.here(key));
            } else {
                // This will happen when sfCompleteName is called before Prim is registered with its parent
                // A call to sfParentageChanged after the registration happens will clean up the cache.
                //@todo we will replace unknown with its name once we modify the interface so that the component registers itself with its parent
                sfCompleteName.addElement(new HereReferencePart("*unknown*"));
                if (sfLog().isTraceEnabled()){
                    sfLog().trace("Internal error generating complete name - child not named in parent yet");
                }
            }
        }
        return sfCompleteName;
    }

    /**
     * Returns the complete name for this component from the root of the
     * application and does not throw any exception. If an exception is
     * thrown it will return a new empty reference.
     *
     * @return reference of attribute names to this component or an empty reference
     *
     */
    public Reference sfCompleteNameSafe() {
       return ComponentHelper.completeNameSafe(this);
    }



    /**
     * Returns the parent of this component.
     *
     * @return parent component
     */
    public Prim sfParent() throws RemoteException {
        return sfParent;
    }


    /**
     * Private method to set up newly created component. Primitives should only
     * override sfDeploy since this is the one which does the actual work
     *
     * @param parent parent of component
     * @param cxt context for component
     *
     * @throws SmartFrogDeploymentException In case of any error while
     *         deploying the component
     * @throws RemoteException In case of network/rmi error
     */
    public void sfDeployWith(Prim parent, Context cxt) throws
        SmartFrogDeploymentException, RemoteException {

        try {
            sfParent = parent;
            sfContext = cxt;

            sfDeployWithHooks.applyHooks(this, null);

            // set the prim parent link of any contained component description to this Prim
            // so that references work
            for (Enumeration e = sfContext.keys(); e.hasMoreElements(); ) {
                Object value = sfContext.get(e.nextElement());
                // Only set prim parent for LAZY descriptions. Otherwise not needed because it will be deployed.
                if ((value instanceof ComponentDescription) && (!((ComponentDescription)value).getEager())) {
                    ((ComponentDescription)value).setPrimParent(this);
                }
            }

            boolean es; // allow exportRef to be defined by string (backward compatability) or boolean
            Object eso = sfResolveHere(SmartFrogCoreKeys.SF_EXPORT, false);

            if (eso==null) {
                es = true;
            } else if (eso instanceof String) {
                es = Boolean.valueOf((String)eso).booleanValue();
            } else {
                es = ((Boolean)eso).booleanValue();
            }

            if (es) {
                //Default value=0 = Anonymous port.
                int port = 0;
                Object portObj = sfResolveHere(SmartFrogCoreKeys.SF_EXPORT_PORT,false);
                if ((portObj!=null) && (portObj instanceof Integer)){
                    port = ((Integer)portObj).intValue();
                }
                sfExportRef(port);
            }

            if (sfParent!=null) {
                ((ChildMinder)sfParent).sfAddChild(this);
            }

            registerWithProcessCompound();

            // Look up delay, if not there never mind looking up factor
            sfLivenessDelay = sfResolve(refLivenessDelay, sfLivenessDelay, false);
            sfLivenessFactor = sfResolve(refLivenessFactor, sfLivenessFactor, false);

            // copy in local description for efficiency when subcomponents looking up
            sfReplaceAttribute(SmartFrogCoreKeys.SF_LIVENESS_DELAY, new Long(sfLivenessDelay));
            // copy in local description for efficiency when subcomponents looking up
            sfReplaceAttribute(SmartFrogCoreKeys.SF_LIVENESS_FACTOR, new Integer(sfLivenessFactor));

            sfLivenessCount = sfLivenessFactor;

            // start the liveness thread
            sfStartLivenessSender();

            // add location information attributes
            sfReplaceAttribute(SmartFrogCoreKeys.SF_HOST, sfDeployedHost());
            sfReplaceAttribute(SmartFrogCoreKeys.SF_PROCESS, sfDeployedProcessName());

        } catch (Exception sfex) {
            if (sfLog().isErrorEnabled()) {
                sfLog().error(sfex);
            }
            //Logger.log(sfex);
            new TerminatorThread(this, sfex, null).quietly().run();
            throw (SmartFrogDeploymentException)SmartFrogDeploymentException.forward(sfex);
        }
    }

    /**
     * Registers component with local ProcessCompound only if it is root component,
     * its parent is remote or the attribute sfProcessComponentName is defined.
     * @throws RemoteException
     * @throws SmartFrogException
     */
    protected void registerWithProcessCompound() throws RemoteException, SmartFrogException {
        //Registers component with local ProcessCompound
        if ((sfParent==null)||
            sfIsRemote(sfParent)||
            sfContext.containsKey(SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME)) {
            try {
                SFProcess.getProcessCompound().sfRegister(sfResolveHere(
                    SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME, false), this);
            } catch (Exception ex) {
                throw (SmartFrogDeploymentException)SmartFrogDeploymentException.forward (ex);
            }
        }
    }

    /**
     * Deregisters this component from local ProcessCompound (if ever registered)
     */
    private void deregisterWithProcessCompound() {
        //
        try {
            if (SFProcess.getProcessCompound() != null) {
                SFProcess.getProcessCompound().sfDeRegister(this);
            }
        } catch (Exception ex) {
            // @TODO: Log. Ignore.
            //Logger.logQuietly(ex);
            if (sfLog().isIgnoreEnabled()){
              sfLog().ignore(ex);
            }
        }

    }

    /**
     * Export this primitive to accept remote method calls using port. If port
     * value is 0 it will be using an anonymous port. Default
     * implementation is to use UnicastRemoteObject. Check is done if
     * sfExportRef is already set, in which case this is returned. <b>Note</b>
     * that for remote methods to work equals, hashCode and toString must be
     * implemented. This is done for PrimImpl in which case those requests are
     * forwarded to the sfExportRef.
     *
     * @param port where to export. If 0, it will use any port available.
     * @return exported primitive
     *
     * @throws SmartFrogException failed to export primitive
     */
    public Object sfExportRef(int port) throws SmartFrogException {
        if (sfExportRef == null) {
            try {
                sfExportRef = SecureRemoteObject.exportObject(this,port);
            } catch (RemoteException rex) {
                throw new SmartFrogLifecycleException(MessageUtil.formatMessage(
                    MSG_OBJECT_REGISTRATION_FAILED),rex);
            } catch (SFGeneralSecurityException sfgsex) {
                throw new SmartFrogLifecycleException(MessageUtil.formatMessage(
                    MSG_OBJECT_REGISTRATION_FAILED),sfgsex);
            }
        }

        return sfExportRef;
    }

    /**
     * Export this primitive to accept remote method calls using an anonymous
     * port. Implementation is to use UnicastRemoteObject. Check is done if
     * sfExportRef is already set, in which case this is returned. <b>Note</b>
     * that for remote methods to work equals, hashCode and toString must be
     * implemented. This is done for PrimImpl in which case those requests are
     * forwarded to the sfExportRef.
     *
     * @return exported primitive
     *
     * @throws SmartFrogException failed to export primitive
     */
    public Object sfExportRef() throws SmartFrogException {
        return sfExportRef(0);
    }

    /**
     * initializes and starts the liveness sender for this primitive. A
     * component needs a liveness sender if it has no parent, but is has
     * children (ie. is a ChildMinder) or if the parent is remote.
     *
     * @throws SmartFrogLivenessException failed to initialize or start sender
     */
    protected void sfStartLivenessSender() throws SmartFrogLivenessException {
        if ((sfLivenessSender != null) || (sfLivenessDelay == 0)) {
            return;
        }

        if (((sfParent == null) && (this instanceof ChildMinder)) ||
                sfIsRemote(sfParent)) {
            try {
                sfLivenessSender = new LivenessSender(this,
                        sfLivenessDelay * 1000, this.sfCompleteNameSafe().toString() );
                sfLivenessSender.start();
            } catch (Throwable t) {
                throw new SmartFrogLivenessException(MSG_LIVENESS_START_FAILED,
                    t, this);
            }
        }
    }

    /**
     * Stops the liveness sender if this component has one.
     */
    protected void sfStopLivenessSender() {
        if (sfLivenessSender != null) {
            sfLivenessSender.stop();
        }
    }

    /**
     * Checks whether given object is a non-local object. This implementation
     * is RMI oriented, so checks whether the given object is a RemoteStub,
     * which would indicate that this object is remote. Subclasses can
     * override this (and associated sfExport method) if the underlying remote
     * infrastructure is not RMI.
     *
     * @param o object to check remoteness on
     *
     * @return true if object a remote reference, false if local
     */
    protected boolean sfIsRemote(Object o) {
        return (o instanceof RemoteStub);
    }

    /**
     * Called after instantiation for deployment purposes. Heart monitor is
     * started and if there is a parent the deployed component is added to the
     * heartbeat. Subclasses can override to provide additional deployment
     * behavior.
     *
     * @throws SmartFrogException error while deploying
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy()
        throws SmartFrogException, RemoteException {
        this.sfSetLog(sfGetApplicationLog());
        Reference componentId = sfCompleteName();
        if (sfIsTerminated) {
            throw new SmartFrogDeploymentException(MessageUtil.formatMessage(MSG_DEPLOY_COMP_TERMINATED, componentId.toString()),
                    this);
        }
        sfDeployHooks.applyHooks(this, null);
        sfIsDeployed = true;
    }

    /**
     * Can be called to start components. Subclasses should override to provide
     * functionality Do not block in this call, but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfStart()
        throws SmartFrogException, RemoteException {

        if (sfIsTerminated) {
            throw new SmartFrogLifecycleException(MessageUtil.formatMessage(
                    MSG_START_COMP_TERMINATED, this.sfCompleteNameSafe().toString()),
                this);
        }
        sfStartHooks.applyHooks(this, null);
        sfIsStarted = true;
    }

    /**
     * Default dump state behavior is to send the context to the target.
     *
     * @param target dump interface to dump to
     */
    public void sfDumpState(Dump target) {
        try {
            target.dumpState(sfContext, this);
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * Request this component to terminate. Termination causes the object to
     * set the sfIsTerminated flag (so a component can not be terminated
     * twice). Need some way of stopping this server object, so that remote
     * stubs will fail, but does not solve the local reference problem (local
     * objects can still call this object)
     *
     * @param status termination status record
     */
    public void sfTerminate(TerminationRecord status) {
        terminateNotifying(status, sfParent);
    }

    /**
     * Provides hook for subclasses to implement useful termination behavior.
     * Deregisters component from local process compound (if ever registered)
     * @param status termination status
     */
    public synchronized void sfTerminateWith(TerminationRecord status) {
        //org.smartfrog.sfcore.common.Logger.log (this.sfCompleteNameSafe().toString(),status);
        if (sfLog().isTraceEnabled()){
          sfLog().trace(this.sfCompleteNameSafe().toString(),null,status);
        }
        try {
            sfTerminateWithHooks.applyHooks(this, status);
        } catch (Exception ex) {
            // @TODO: Log. Ignore.
            //Logger.logQuietly(ex);
            if (sfLog().isIgnoreEnabled()){
              sfLog().ignore(ex);
            }
        }

        deregisterWithProcessCompound();
    }


    protected final Object termLock = new Object();

    /**
     * Notifies a component that this component has terminated through
     * sfTerminatedWith and calls the local hook (sfTerminateWith).
     *
     * @param status termination status
     * @param comp component to notify of termination
     */

    protected void terminateNotifying(TerminationRecord status, Prim comp) {
        // Provide ID to termination record
        // Note that it uses the name of the first component terminated not the actual caller id.

	    // protect aganist two callers invoing this
        //  can't synchronize of "this" as it can cause deqdlock in sync termination
        synchronized (termLock) {
            if (sfIsTerminating || sfIsTerminated) {
                return;
            }
            sfIsTerminating = true;
        }

        if (status.id == null) {
            try {
                status.id = this.sfCompleteNameSafe();
            } catch (Exception ex) {
                //Usually when disconnected from network and Parent not reachable
                try {
                    status.id = SFProcess.getProcessCompound().sfCompleteName();
                    status.id.addElement(ReferencePart.here(
                    SFProcess.getProcessCompound().sfAttributeKeyFor(this)));
                } catch (Exception ex2) {
                    //ignore
                }
            }
        }

        try {
            sfTerminateWith(status);
        } catch (Exception ex) {
            // ignore
            //Logger.logQuietly(ex);
            if (sfLog().isIgnoreEnabled()){
              sfLog().ignore(ex);
            }
        }

        if (comp != null) {
            try {
                comp.sfTerminatedWith(status, this);
            } catch (Exception ex) {
               // Logger.logQuietly(ex);
               if (sfLog().isIgnoreEnabled()){
                 sfLog().ignore(ex);
               }
            }
        }

        //UnExports this component.
        // It has to be placed after comp.sfTerminatedWith(status, this);
        try {
            org.smartfrog.sfcore.security.SecureRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException ex) {
            // @TODO: Log. Ignore.
            //Logger.logQuietly(ex);
            if (sfLog().isIgnoreEnabled()){
              sfLog().ignore(ex);
            }
        }

        //synchronized (this) {
            sfIsTerminated = true;
        //}

    }

    /**
     * Get this object to detach from its parent. Calls sfStartLivenessSender
     * since this component might now be eligible for a liveness sender.
     * Every detached component will be re-parented with its local
     * ProcessCompound.The detached component will be registed using
     * "sfProcessComponentName" value or a random name if it does not exist.
     * It sends sfParantageChanged() notification.
     *
     * @throws SmartFrogException detachment failed
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfDetach()
        throws SmartFrogException, RemoteException {
        if (sfParent == null) {
            return;
        }
        try {
            //First remove child
            ((ChildMinder) sfParent).sfRemoveChild(this);
            //Second register child with ProcessCompound if needed
            //(ProcessCompound will have a temporal parentage with application
            // root components and then it will detach!). Keep this order!)
            if (!(SFProcess.getProcessCompound().sfContainsChild(this))) {
                //Registers with local process compound!
               SFProcess.getProcessCompound().sfRegister(this.sfResolveHere(
                   SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME,false), this);
            }
            sfParent = null;
            sfStartLivenessSender();
            sfParentageChanged();
        } catch (SmartFrogResolutionException ex){
            //@Todo: log
            //ignore
        }

    }


    /**
     * Get this object to terminate, after detaching itself from its parent.
     *
     * @param status termination status
     */
    public synchronized void sfDetachAndTerminate(TerminationRecord status) {
        try {
            if (sfParent != null) {
                ((ChildMinder) sfParent).sfRemoveChild(this);
            }
            sfParent = null;
            terminateNotifying(status, null);
        } catch (Exception ex) {
            //Logger.logQuietly(ex);
            if (sfLog().isIgnoreEnabled()){
              sfLog().ignore(ex);
            }
        }
    }

    /**
     * Called from sub-component (normally) to indicate termination. Subclasses
     * should implement useful behavior.
     *
     * @param status termination status
     * @param comp component that has terminated
     */
    public void sfTerminatedWith(TerminationRecord status, Prim comp) {
    }

    /**
     * Called from up the containment tree to terminate. This is to avoid the
     * upcall to the parent which sfTerminate causes.  Will only cause
     * termination if the sender component is the parent of this component.
     *
     * @param status termination status
     */
    public void sfTerminateQuietlyWith(TerminationRecord status) {
        terminateNotifying(status, null);
    }

    /**
     * Liveness call in to check if this component is still alive. This method
     * can be overriden to check other state of a component. An example is
     * Compound where all children of the compound are checked. This basic
     * check updates the liveness count if the ping came from its parent.
     * Otherwise (if source non-null) the liveness count is decreased by the
     * sfLivenessFactor attribute. If the count ever reaches 0 liveness
     * failure on tha parent has occurred and sfLivenessFailure is called with
     * source this, and target parent. Note: the sfLivenessCount must be
     * decreased AFTER doing the test to correctly count the number of ping
     * opportunities that remain before invoking sfLivenessFailure. If done
     * before then the number of missing pings is reduced by one. E.g. if
     * sfLivenessFactor is 1 then a sfPing from the parent sets
     * sfLivenessCount to 1. The sfPing from a non-parent would reduce the
     * count to 0 and immediately fail.
     *
     * @param source source of call
     *
     * @throws SmartFrogLivenessException component is terminated
     * @throws RemoteException for consistency with the {@link Liveness} interface
     */
    public void sfPing(Object source) throws SmartFrogLivenessException, RemoteException {
	if (Logger.logLiveness &&(sfLog().isTraceEnabled())) {
	    sfLog().trace("ping received from " + source + ": in " + sfCompleteNameSafe() + ", counter " + sfLivenessCount);
	}

	if (sfIsTerminated) {
	    if (Logger.logLiveness &&(sfLog().isTraceEnabled())) {
		sfLog().trace("ping returning that I am terminated : in " + sfCompleteNameSafe());
	    }
	    throw new SmartFrogLivenessException(MessageUtil.formatMessage(COMPONENT_TERMINATED));
	}

	if (source == null) {
	    return;
	}

	if (sfParent == null) {
	    // if am root - don't check counters...!
	    return;
	}

	// memory model hack...
	boolean fail = false;
	synchronized (this) {
	    if (source.equals(sfParent)) {
		if (Logger.logLiveness &&(sfLog().isTraceEnabled())) {
		    sfLog().trace("parent ping received - resetting counter: in " + sfCompleteNameSafe());
		}
		sfLivenessCount = sfLivenessFactor;
	    } else if ((sfLivenessSender != null) &&
		       source.equals(sfLivenessSender) &&
		       (sfLivenessCount-- <= 0)) {
		fail = true;
	    }
	}

	if (fail) {
	    if (sfLog().isDebugEnabled()) {
		sfLog().debug("failing as parent liveness checking had counted down: in " + sfCompleteNameSafe());
	    }
	    sfLivenessFailure(this, sfParent, null);
	}
    }

    /**
     * Handle ping failures. Default behavior is to terminate with a liveness
     * send failure record storing the name of the target of the ping (which
     * generally is one of the children or the parent of this component).
     *
     * @param source source of update
     * @param target target that update was trying to reach
     * @param failure error that occurred
     */
    protected void sfLivenessFailure(Object source, Object target,
        Throwable failure) {
        // Failed to send liveness to children, terminate by default
        Reference targetName = null;
        Reference myName = null;

        try {
            targetName = ((Prim) target).sfCompleteName();
        } catch (Exception ex) {
            // ignore, leave null
        }

        try {
            myName = this.sfCompleteNameSafe();
        } catch (Exception ex) {
            //Usually when disconnected from network and Parent not reachable
            if (myName == null) {
                try {
                    myName = SFProcess.getProcessCompound().sfCompleteName();
                    myName.addElement(ReferencePart.here(SFProcess.getProcessCompound().sfAttributeKeyFor(this)));
                } catch (Exception ex2) {
                    //ignore
                }
            }
        }

        String failureMsg="";
        if (failure!=null){
          failureMsg = " (Failure: "+failure.getMessage()+")";
        }

        if (myName != null) {
            sfTerminate(TerminationRecord.abnormal(
                 MessageUtil.formatMessage(LIVENESS_SEND_FAILURE_IN
                 ,myName,targetName + failureMsg), targetName, failure));
        } else {
            sfTerminate(TerminationRecord.abnormal(
                 MessageUtil.formatMessage(LIVENESS_SEND_FAILURE
                 ,targetName + failureMsg), targetName, failure));
        }
    }

    /**
     * Implemented to provide for remote equality checking. If the primitive
     * was export the sfExportRef is used to compare objects, otherwise the
     * super class (Object) is requested to check equality.
     *
     * @param o object to compare with
     *
     * @return true if equal, false if not
     */
    public boolean equals(Object o) {
        if (sfExportRef == null) {
            return super.equals(o);
        } else {
            return sfExportRef.equals(o);
        }
    }

    /**
     * Returns the hashcode for the exported object if it was exported, the
     * object itself if not.
     *
     * @return hashcode for this object or its remote reference
     */
    public int hashCode() {
        if (sfExportRef == null) {
            return super.hashCode();
        } else {
            return sfExportRef.hashCode();
        }
    }

    /**
     * Returns the string of the remote reference if this primitive was
     * exported, the superclass toString if not.
     *
     * @return string form for this component
     */
    public String toString() {
        if (sfExportRef == null) {
            return super.toString();
        } else {
            return sfExportRef.toString();
        }
    }

    /**
     * Terminates the component in a seperate thread. A component should call
     * this method when it encounters any fatal exception that requires
     * termination of the component.
     *
     * @param targetComponent The component that encountered the error
     * @param excp The error that caused the termination
     * @param componentId Component Identifier
     */
    protected void terminateComponent(Prim targetComponent, Throwable excp,
        Reference componentId) {
        TerminatorThread terminator = new TerminatorThread(targetComponent, excp, componentId);//.quietly();
        // start the thead
        terminator.start();
    }

    /**
     *  To log into sfCore logger. This method should be used to log Core messages
     *
     * Types of possible logs in SF:
     *  - Core log
     *  - Application log
     *  - Component log - Using sfSetLog()
     *  - Specific log - Using sfLog attribute in component
     *
     *  When initialized, sflog uses CoreLog, once the component enters in the
     *  sfDeploy lifecycle phase, it is changed to use the specific log if sfLog
     *  attribute is defined or if it is not defined it defaults to the core log.
     *
     * @return Logger implementing LogSF and Log
     */
    public LogSF sfLog() {
       if (sflog!=null)
         return sflog;
       else {
        try {
          return sfGetApplicationLog();
        } catch (Exception ex) {
          return sfGetCoreLog();
        }
       }
    }


    /**
     *  Method to replace logger used by components.
     *  @param newlog replacement for Prim core log
     *  @return oldlog
     */
    public synchronized LogSF sfSetLog(LogSF newlog) {
       LogSF oldlog = sflog;
       this.sflog = newlog;
       // add attribute
       try {
           sfReplaceAttribute(SmartFrogCoreKeys.SF_APP_LOG_NAME, newlog.getLogName());
       } catch (Exception ex) {
         if (sfLog().isErrorEnabled()){ sfLog().err(ex);}
       }
       return oldlog;
    }


    /**
     * To get a logger with a particular name.
     * @param name logger name
     * @return Logger implementing LogSF and Log
     */
    public LogSF sfGetLog (String name){
       return LogFactory.getLog(name);
    }

    /**
     *  To get the sfCore logger
     * @return Logger implementing LogSF and Log
     */
    public LogSF sfGetCoreLog() {
       return LogFactory.sfGetProcessLog();
    }

    /**
     *  To get application logger using ROOT name.
     *  The name used is cached in attritube {@link SmartFrogCoreKeys.SF_APP_LOG_NAME}
     *  If this attribute has been pre-set then it is used to get the application logger,
     *  otherwise ROOT cannonical name is used.
     *
     * @return Logger implementing LogSF and Log
     * @throws SmartFrogException
     * @throws RemoteException
     */
    public LogSF sfGetApplicationLog() throws SmartFrogException, RemoteException {
        //@todo should we use prim name and get a hierarchy of logs?
         //this.sfResolveHere(SmartFrogCoreKeys.SF_APP_LOG_NAME,false);
        String sfLogName=null;
        try {
            // Check or sfLog attribute in component, if not initially defined
            // then it will be created during sfDeploy.
            Object obj =sfResolve(SmartFrogCoreKeys.SF_APP_LOG_NAME, true);
            if (obj instanceof Prim) {
                sfLogName = ((Prim)obj).sfCompleteName().toString();
            } else {
              sfLogName = obj.toString();
            }
            return sfGetLog(sfLogName);
        } catch (SmartFrogResolutionException ex) {
            //Get root Log name
            // Very expensive call
            //String sfLogName = ((Prim)sfResolveWithParser(SmartFrogCoreKeys.SF_ROOT)).sfCompleteName().toString();
            if (sfParent()!=null){
                try {
                    // Check or sfLog attribute in parent
                    sfLogName = (sfParent().sfResolve(SmartFrogCoreKeys.SF_APP_LOG_NAME,"", true));
                } catch (SmartFrogResolutionException rex){
                    sfLogName = (this.sfCompleteName().toString());
                }
            } else {
                //I am the Root component for this application
                sfLogName = (this.sfCompleteName().toString());
            }
            return sfGetLog(sfLogName);
        }

    }

    /**
     * Parentage changed in component hierachy.
     * Actions: sfCompleteName cache is cleaned
     */
    public void sfParentageChanged() throws RemoteException{
       sfCompleteName=null;
    }

    /** Returns value of flag indicating if this component has been terminated. */
    public boolean sfIsTerminated() {
        return sfIsTerminated;
    }

    /** Returns value of flag indicating if this component is terminating. */
    public boolean sfIsTerminating() {
        return sfIsTerminating;
    }

    /** Returns value of flag indicating if this component has been deployed. */
    public boolean sfIsDeployed() {
        return sfIsDeployed;
    }

    /** Returns value of flag indicating if this component has been started. */
    public boolean sfIsStarted() {
        return sfIsStarted;
    }

    /** Creates diagnostics report */
    public ComponentDescription sfDiagnosticsReport() {
      ComponentDescription cd = null;
      try {
        cd = new ComponentDescriptionImpl(null,(Context)new ContextImpl(), false);
        cd.setPrimParent(this);
        StringBuffer report = new StringBuffer();
        Diagnostics.doReport(report,this);
        cd.sfReplaceAttribute(SmartFrogCoreKeys.SF_DIAGNOSTICS_REPORT, report );
      } catch (Throwable thr){
        //ignore
        if (sfLog().isWarnEnabled()){ sfLog().warn(thr);}
      }
      return cd;
    }

}
