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

package org.smartfrog.examples.counter;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.smartfrog.sfcore.common.SmartFrogException;

import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.logging.LogSF;


/**
 *  Basic example component.
 *  The Counter component (in components.sf) is a basic primitive component so
 *  its component description class CounterImpl extends PrimImpl (the base
 *  class for all the deployed components) which provides the default lifecycle
 *  template methods for a primitive component.
 *  Although PrimImpl itself implements Prim (the base interface for all the
 *  deployed components) CounterImpl also implements Prim because it is
 *  necessary for RMI that component also does so; the rmic compiler will
 *  otherwise not behave correctly.
 *  The CounterImpl class needs to be prepared for RMI for remote deployment
 *  This is done by creating and compiling the stubs and skeletons using the
 *  rmic compiler.
 *  This class is included in rmitargets that is read by the rmic compiler.
 */
public class CounterImpl extends PrimImpl implements Prim, Counter, Runnable {
    /** Counter component data - counter. */
    protected int counter = 1;
    /** Counter component data - message. */
    protected String message = "Hola Mundo!";
    /** Counter component data - limit. */
    protected int limit = 2;
    /** Thread object */
    protected Thread action = null;
    /** sleep time */
    protected int sleeptime=1000;

    protected LogSF logCore = null;
    protected LogSF logApp = null;

    /**
     *  Shows debug messages.
     */
    protected boolean debug = true;

    /**
     *  Should pause during sfDeploy and sfStart?
     */
    protected boolean pause = false;

    /**
     *  Terminates component when counter reaches limit
     */
    protected boolean terminate = false;

    /**
     * Component name used for debug messages.
     */
    protected String myName = "CounterImpl";

    /**
     *  Constructor for the Counter object.
     *
     *@exception  RemoteException In case of network/rmi error
     */
    public CounterImpl() throws RemoteException {
    }

    // LifeCycle methods

    /**
     *  sfDeploy: reads Counter attributes and configures counter thread
     *  The superclass implementation of sfDeploy is called before the
     *  component specific initialization code (reading Counter attributes
     *  and configuring counter thread) to maintain correct behaviour of
     *  initial deployment and starting the heartbeat monitoring of this
     *  component.
     *
     * @exception  SmartFrogException In case of error in deploying
     * @exception  RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy() throws SmartFrogException,
    RemoteException {
            super.sfDeploy();
            logCore = sfGetProcessLog();
            logApp = this.sfGetApplicationLog();
            /**
             *  Returns the complete name for Counter component from the root
             *  of application.If an exception is thrown it returns null
             *  This name is used for printing debug messages in utility methods
             */
            myName = this.sfCompleteNameSafe().toString();
            readSFAttributes();
            if (pause) {
                this.log("sleepDeploy","sleeping");
                try {
                    this.wait(limit*sleeptime);
                } catch (InterruptedException ex) {
                }
                this.log("sleepDeploy","end-sleeping");
            }
    }

    /**
     *  sfStart: starts counter thread.
     *  The superclass implementation of sfStart is called before the
     *  component specific code (starting counter thread) to maintain correct
     *  behaviour of starting the active threads for components.
     *
     * @exception  SmartFrogException In case of error while starting
     * @exception  RemoteException In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException,
    RemoteException {
        super.sfStart();
        log("sfStart", "Starting with msg-" + message);
        System.out.println("********Temporal test for logging*************");
        try {
            if (logApp.isTraceEnabled()) {
                System.out.println("TRACE:"+message);
                logApp.trace(message);
                System.out.println();
            }
            if (logApp.isDebugEnabled()) {
                System.out.println("DEBUG:"+message);
                logApp.debug(message);
                System.out.println();
            }
            if (logApp.isInfoEnabled()) {
                System.out.println("INFO:"+message);
                logApp.info(message);
                System.out.println();
            }

            if (logApp.isWarnEnabled()) {
                System.out.println("WARN:"+message);
                logApp.warn(message);
                System.out.println();
            }

            if (logApp.isErrorEnabled()) {
                System.out.println("ERROR:"+message);
                logApp.error(message);
                System.out.println();
            }
            if (logApp.isFatalEnabled()) {
                System.out.println("FATAL:"+message);
                logApp.fatal(message);
                System.out.println();
            }

            if (logApp.isInfoEnabled()) {
                System.out.println(".out:"+message);
                logApp.out(message);
                System.out.println();
                System.out.println(".err:"+message);
                logApp.err(message);
                logApp.err(message+1, null);
                System.out.println();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        System.out.println("******************************");

        if (pause) {
            this.log("sleepStart", "sleeping");
            try {
                this.wait(limit*sleeptime);
            } catch (InterruptedException ex) {
            }
            this.log("sleepStart", "end-sleeping");
        }
        action = new Thread(this);
        action.setName("Counter");
        action.start();
    }

    /**
     *  sfTerminate: The superclass implementation of sfTerminateWith is called
     *  after the component specific termination code to implement useful
     *  termination behaviour of the component.
     *
     * @param  t TerminationRecord object
     */
    public synchronized void sfTerminateWith(TerminationRecord t) {
        log("sfTerminateWith", " Terminating for reason: " + t.toString());

        if (action != null) {
            action.interrupt();
        }

        super.sfTerminateWith(t);
    }

    // End LifeCycle methods

    // Read Attributes from description

    /**
     *  Reads optional and mandatory attributes.
     *
     * @exception  SmartFrogException error while reading attributes
     * @exception  RemoteException In case of network/rmi error
     */
    protected void readSFAttributes() throws SmartFrogException, RemoteException {
        //
        // Mandatory attributes.
        try {
            /*
             * Resolves the mandatory attribute "limit" from sf description
             * The resolution method takes the following parameters:
             * String name "attrLIMIT" of the attribute,
             * default value "limit" of the attribute,and
             * a boolean "true" indicating that it is a mandatory attribute
             * If the mandatory attribute is not present in the description, it
             * triggers a SmartFrogResolutionException
             */
            limit = sfResolve(ATR_LIMIT, limit, true);
            //True to Get exception thown!
        } catch (SmartFrogResolutionException e) {
            error("readSFAttributes","Failed to read mandatory attribute: "+
                    limit +"Error:"+ e.toString());
            throw e;
        }
        //Optional attributes.
            /*
             * Resolves the optional attribute "debug" from sf description
             * The resolution method takes the following parameters:
             * String name "attrDEBUG" of the attribute,
             * default value "debug" of the attribute,and
             * a boolean "false" indicating that it is a optional attribute
             * If the optional attribute is not present in the description, it
             * returns the default value
             */
        debug = sfResolve(ATR_DEBUG, debug, false);

        pause = sfResolve(ATR_PAUSE, pause, false);
            /*
             * Resolves the optional attribute "counter" from sf description
             * The resolution method takes the following parameters:
             * String name "attrDEBUG" of the attribute,
             * default value "debug" of the attribute,
             * min value "null" of the attribute,
             * max value of the attribute,and
             * a boolean "false" indicating that it is a optional attribute
             * If the optional attribute is not present in the description, it
             * returns the default value
             * If the resolved value < min value, it triggers
             * SmartFrogResolutionException
             * If the resolved value > max value, it triggers
             * SmartFrogResolutionException
             */
        counter = sfResolve(ATR_COUNTER, counter, null, new Integer(limit),
            false);
            /*
             * Resolves the optional attribute "message" from sf description
             * The resolution method takes the following parameters:
             * String name "attrMESSAGE" of the attribute,
             * default value "message" of the attribute,and
             * a boolean "false" indicating that it is a optional attribute
             * If the optional attribute is not present in the description, it
             * returns the default value
             */
        message = sfResolve(ATR_MESSAGE, message, false);

        terminate = sfResolve (ATR_TERMINATE, terminate, false);
          /*
          sleep time, >=0;.
          */
        sleeptime = sfResolve(ATR_SLEEP,sleeptime,false);
        if(sleeptime<0) {
            throw new SmartFrogResolutionException("Attribute "
                    +ATR_SLEEP+" cannot be less than zero");
        }

    }

    // Main component action methods

    /**
     *  Main processing method for the Counter object (implements 'Runnable').
     */
    public void run() {
        try {
            while (limit >= counter) {
                //System.out.println("COUNTER: " + message + " " + counter);
                String messageSt = ("COUNTER: " + message + " " + counter);
                System.out.println(messageSt.toString());
                logApp.out(messageSt);
                logCore.info(messageSt);
                logApp.info(messageSt);

//                if (logApp.isTraceEnabled()){
//                  logApp.trace(messageSt);
//                }
//                if (logApp.isDebugEnabled()){
//                  System.out.print("DEBUG: messageSt");
//                  logApp.debug(messageSt);
//                }
//                if (logApp.isInfoEnabled()){
//                  logApp.info(messageSt);
//                }
//
//                if (logApp.isWarnEnabled()){
//                  logApp.warn(messageSt);
//                }
//
//
//                if (logApp.isErrorEnabled()){
//                  logApp.fatal(messageSt);
//                }
//                if (logApp.isFatalEnabled()){
//                  logApp.fatal(messageSt);
//                }


                if(sleeptime>0) {
                    Thread.sleep(sleeptime);

                }
                counter++;
            }
            if (terminate) {
                new org.smartfrog.sfcore.common.TerminatorThread(this, TerminationRecord.normal(this.sfCompleteNameSafe())).start();
            }
            //end while
        } catch (InterruptedException ie) {
            exception("run", ie);
        }

    }

    // Utility methods

    /**
     * Utility method for printing error message.
     * @param method error method
     * @param message error message
     */
    protected void error(String method, String message) {
        if (debug) {
            StringBuffer msg = new StringBuffer();
            msg.append (myName);
            msg.append (".");
            msg.append (method);
            msg.append ( " [" );
            msg.append ((new SimpleDateFormat("HH:mm:ss.SSS z, yyyy/MM/dd").
                    format(new Date())));
            msg.append ("]> ");
            msg.append (message);
            if (logCore.isErrorEnabled()) logCore.error(msg.toString()+"\n");
            System.err.println(msg.toString());
        }
    }

    /**
     * Utility method for logging error message.
     * @param method error method
     * @param message error message
     */
    protected void log(String method, String message) {
        if (debug) {
            StringBuffer msg = new StringBuffer();
            msg.append (myName);
            msg.append (".");
            msg.append (method);
            msg.append ( " [" );
            msg.append ((new SimpleDateFormat("HH:mm:ss.SSS z, yyyy/MM/dd").
                    format(new Date())));
            msg.append ("]> ");
            msg.append (message);
            System.out.println(msg.toString());
            if (logCore.isInfoEnabled()) logCore.info(msg.toString()+"\n");
        }
    }

    /**
     * Utility method for printing exception message.
     * @param method error method
     * @param exception exception
     */
    protected void exception(String method, Throwable exception) {
        if (debug) {
            StringBuffer msg = new StringBuffer();
            msg.append (myName);
            msg.append (".");
            msg.append ( "Exception");
            msg.append ( " [" );
            msg.append ((new SimpleDateFormat("HH:mm:ss.SSS z, yyyy/MM/dd").
                    format(new Date())));
            msg.append ("]> ");
            msg.append (message);
//            msg.append("\n StackTrace: ");
//            msg.append(exception.getStackTrace().toString());
             if (logCore.isErrorEnabled())  logCore.error(msg.toString()+"\n",exception);
            msg.append("\n StackTrace: ");
            msg.append(exception.getStackTrace().toString());
            System.err.println(msg.toString());
        }
    }
}
