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

package org.smartfrog.sfcore.logging;

import org.smartfrog.sfcore.common.Logger;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLogException;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.reference.Reference;

import org.smartfrog.sfcore.security.SFClassLoader;
import org.smartfrog.sfcore.common.MessageKeys;
import org.smartfrog.sfcore.common.MessageUtil;
import org.smartfrog.sfcore.common.SmartFrogCoreKeys;

import java.lang.Double;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.smartfrog.sfcore.common.*;


/**
 *
 */
public class LogImpl implements LogSF, LogRegistration, Serializable {

    /** Default Log object */
    protected Log localLog = null;

    /** Name of this log instance */
    protected String logName = null;
    /** Current log level */
    protected int currentLogLevel= LOG_LEVEL_INFO;



    public static final Method TRACE_O =
          getObjectMethod("trace", new Class[] {Object.class});
    public static final Method TRACE_O_T =
          getObjectMethod("trace", new Class[] {Object.class,Throwable.class});

      public static final Method DEBUG_O =
          getObjectMethod("debug", new Class[] {Object.class});
      public static final Method DEBUG_O_T =
          getObjectMethod("debug", new Class[] {Object.class, Throwable.class});

      public static final Method INFO_O =
          getObjectMethod("info", new Class[] {Object.class});
      public static final Method INFO_O_T =
          getObjectMethod("info", new Class[] {Object.class, Throwable.class});

      public static final Method WARN_O =
          getObjectMethod("warn", new Class[] {Object.class});
      public static final Method WARN_O_T =
          getObjectMethod("warn", new Class[] {Object.class, Throwable.class});

      public static final Method ERROR_O =
          getObjectMethod("error", new Class[] {Object.class});
      public static final Method ERROR_O_T =
          getObjectMethod("error", new Class[] {Object.class, Throwable.class});

      public static final Method FATAL_O =
          getObjectMethod("fatal", new Class[] {Object.class});
      public static final Method FATAL_O_T =
          getObjectMethod("fatal", new Class[] {Object.class, Throwable.class});


    /**
     * Gets the corresponding method of java.lang.Object.
     *
     * @param method A method name.
     * @param args An array with the arguments of that method.
     * @return A  corresponding method of java.lang.Object.
     */
    private static Method getObjectMethod(String method, Class[] args) {
        try {
            return (Log.class).getDeclaredMethod(method, args);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <p> Invokes method in localLog and registered logs. </p>
     *
     * @param method method to call
     * @param args args to invoke
     */
    public void  invoke (Method method, Object[] args) {
        try {
            if (localLog!=null)
                method.invoke(localLog,args);
        }catch (Throwable thr){
            if (localLog!=null)
                     localLog.error("Error Invoke LogImpl",thr);
                 else thr.printStackTrace();
        }
        //Registered logs
        synchronized (registeredLogs){
            Log log=null;
            Enumeration logs = registeredLogs.elements();
            while (logs.hasMoreElements()){
                try {
                    log = (Log) logs.nextElement();
                    if (isCurrentLevelEnabled(log)) {
                        method.invoke(logs.nextElement(), args);
                    }
                } catch (Throwable thr) {
                    if (log!=null) log.trace("",thr);
                    else if (localLog!=null)
                        localLog.trace("",thr);
                    else thr.printStackTrace();

                }
            }
        }
    }

    /**
     * Constructor
     * @param name for Log
     */
    public LogImpl (String name){
        this (name,null);
    }


    /**
     * Constructor
     * @param name for Log
     * @param componentComponentDescription configuration that overwrites class configuraion
     */
    public LogImpl (String name, ComponentDescription componentComponentDescription){
        //Configuration for LogImpl class
        ComponentDescription classComponentDescription = null;

        // Level set in configuration
        int configurationLevel = currentLogLevel;
        // logger class set in configuration (Vector or String)
        Object configurationClass = "org.smartfrog.sfcore.logging.LogToFileImpl";
        // codebase used to load  class set in configuration
        String configurationCodeBase = null;

        logName = name;
        try {
            //Check Class and read configuration...including system.properties
            classComponentDescription = ComponentDescriptionImpl.getClassComponentDescription(this, true,null);
            if (classComponentDescription!=null){
               configurationClass = this.getConfigurationClass(classComponentDescription,configurationClass);
               configurationLevel = this.getConfigurationLevel(classComponentDescription,configurationLevel);
               configurationCodeBase = this.readConfigurationCodeBase(classComponentDescription,configurationCodeBase);
            }
            // overwriting class configuration with component configuration if provided.
            if (componentComponentDescription!=null){
                configurationClass = this.getConfigurationClass(componentComponentDescription,configurationClass);
                configurationLevel = this.getConfigurationLevel(componentComponentDescription,configurationLevel);
                configurationCodeBase = this.readConfigurationCodeBase(componentComponentDescription,configurationCodeBase);
            }

            setLevel (configurationLevel);

            localLog = getLocalLog(name, new Integer(currentLogLevel), (String)configurationClass, configurationCodeBase);

            //Set lower level of the two, just in case local logger has its own mechanism to set log level
            int i= getLevel(localLog);
            if (currentLogLevel>i){
              setLevel(i);
            }
        } catch (Exception ex ){
            String msg = "Error during initialization of localLog for LogImpl. Next trying to using Default (LogToFile)";
            String msg2 = "Log '"+name+"' , values [class,level,codebase]: "+ configurationClass +", "+  configurationLevel +", "+ configurationCodeBase +
                        "\nusing Class ComponentDescription:\n{"+classComponentDescription+
                        "}\n, and using Component ComponentDescription:\n{"+ componentComponentDescription+"}";
            System.err.println("[WARN] "+msg2);
            System.err.println("[WARN] "+msg+", Reason: "+ex.getMessage());
            try {
                localLog=new LogToFileImpl(name,new Integer(currentLogLevel));
                if ((localLog.isWarnEnabled())) localLog.warn(msg2);
                if ((localLog.isWarnEnabled())) localLog.warn(msg, ex);
            } catch (java.lang.NullPointerException nex){
                msg = "Error during emergency initialization of localLog using default LogToFile for LogImpl. No logger available.";
                System.err.println("[FATAL] "+msg+", Reason: "+nex.toString());
                if (org.smartfrog.sfcore.common.Logger.logStackTrace) nex.printStackTrace();
                throw nex;
            }
        }
        if ((localLog!=null)&&(localLog.isTraceEnabled())) {
            String msg2 = "Log '"+name+"' , values [class,level,codebase]: "+ configurationClass +", "+  configurationLevel +", "+ configurationCodeBase +
                        "\nusing Class ComponentDescription:\n {"+classComponentDescription+
                        "}\n, and using Component ComponentDescription:\n{"+ componentComponentDescription+"}";
            localLog.trace(msg2);
        }
    }

    /**
     * Reads configurationClass attribute for LogImpl from a componentDescription
     *
     * @param componentDescription ComponentDescription
     * @param default configurationClass Object
     * @return Object (Vector or String)
     * @throws SmartFrogResolutionException
     */
    private Object getConfigurationClass(ComponentDescription componentDescription, Object configurationClass) throws SmartFrogResolutionException {
        if (componentDescription==null) return configurationClass;
        return (String)componentDescription.sfResolve( ATR_LOCAL_LOGGER_CLASS, configurationClass, false);
    }

    /**
     * Reads configurationLevel attribute for LogImpl from a componentDescription
     *
     * @param componentDescription ComponentDescription
     * @param default configurationLevel int
     * @return int
     * @throws SmartFrogResolutionException
     */
    private int getConfigurationLevel(ComponentDescription componentDescription, int configurationLevel) throws SmartFrogResolutionException {
        if (componentDescription==null) return configurationLevel;
        return componentDescription.sfResolve(ATR_LOG_LEVEL, getLevel(), false);
    }

    /**
     * Reads configurationCodeBase attribute for LogImpl from a componentDescription
     *
     * @param componentDescription ComponentDescription
     * @param default configurationCodeBase String
     * @return String
     * @throws SmartFrogResolutionException
     */
    private String readConfigurationCodeBase(ComponentDescription componentDescription,String configurationCodeBase) throws SmartFrogResolutionException {
        if (componentDescription==null) return configurationCodeBase;
        return getSfCodeBase(componentDescription);
    }

    /**
     *  Registered inputs, distribution list
     */
    protected Hashtable registeredLogs = new Hashtable();

    //LogImpl configuration


   protected Log getLocalLog(String name, Integer logLevel, String targetClassName , String targetCodeBase)
          throws SmartFrogLogException{
          try {
            Class deplClass = SFClassLoader.forName(targetClassName, targetCodeBase, true);

            Class[] deplConstArgsTypes = { name.getClass(), logLevel.getClass() };

            Constructor deplConst = deplClass.getConstructor(deplConstArgsTypes);

            Object[] deplConstArgs = { name, logLevel};

            return (Log) deplConst.newInstance(deplConstArgs);
        } catch (NoSuchMethodException nsmetexcp) {
            throw new SmartFrogLogException(MessageUtil.formatMessage(
                    MessageKeys.MSG_METHOD_NOT_FOUND, targetClassName, "getConstructor()"),
                nsmetexcp);
        } catch (ClassNotFoundException cnfexcp) {
            throw new SmartFrogLogException(MessageUtil.formatMessage(
                    MessageKeys.MSG_CLASS_NOT_FOUND, targetClassName), cnfexcp);
        } catch (InstantiationException instexcp) {
            throw new SmartFrogLogException(MessageUtil.formatMessage(
                    MessageKeys.MSG_INSTANTIATION_ERROR, targetClassName), instexcp);
        } catch (IllegalAccessException illaexcp) {
            throw new SmartFrogLogException(MessageUtil.formatMessage(
                    MessageKeys.MSG_ILLEGAL_ACCESS, targetClassName, "newInstance()"), illaexcp);
        } catch (InvocationTargetException intarexcp) {
            String msg = "Error during initialization of localLog for LogImpl."+
                         " Data: name "+name+", targetClassName "+targetClassName+
                         ", logLevel "+logLevel+", targetCodeBase "+targetCodeBase;
            System.err.println("[ERROR] "+msg+", Reason: "+intarexcp.toString());
            intarexcp.getCause().printStackTrace();
            throw new SmartFrogLogException(MessageUtil.formatMessage(
                    MessageKeys.MSG_INVOCATION_TARGET, targetClassName), intarexcp);

       } catch (Exception ex){
          throw (SmartFrogLogException)SmartFrogLogException.forward(ex);
       }
   }

   /**
    * <p> Get log name. </p>
    *
    * @return log name
    */
    public String getLogName(){
        return this.logName;
    }


   /**
    * Gets the class code base by resolving the sfCodeBase attribute in the
    * given description.
    *
    * @param desc Description in which we resolve the code base.
    *
    * @return class code base for that description.
    */
   protected String getSfCodeBase(ComponentDescription desc) {
       try {
           return (String) desc.sfResolve(new Reference(SmartFrogCoreKeys.SF_CODE_BASE));
       } catch (Exception e) {
           // Not found, return null...
       }

       return null;
   }


    /**
     * <p> Set logging level. </p>
     *
     * @param currentLogLevel new logging level
     */
    public void setLevel(int currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
        if (localLog instanceof LogLevel){
           ((LogLevel)localLog).setLevel(currentLogLevel);
        }
        if (isTraceEnabled()) this.trace("setLogLevel()="+this.currentLogLevel);
    }

    /**
     * <p> Get logging level. </p>
     */
    public int getLevel() {
        return currentLogLevel;
    }

    /**
     * <p> Get logger level </p>
     *
     * @param logger
     */
    public int getLevel(Log logger) {
       if (logger.isTraceEnabled()){
         return LOG_LEVEL_TRACE;
       } else if (logger.isDebugEnabled()){
         return LOG_LEVEL_DEBUG;
       } else if (logger.isInfoEnabled()){
         return LOG_LEVEL_INFO;
       } else if (logger.isWarnEnabled()){
         return LOG_LEVEL_WARN;
       } else if (logger.isErrorEnabled()){
         return LOG_LEVEL_ERROR;
       } else if (logger.isFatalEnabled()){
          return LOG_LEVEL_FATAL;
       }
       return LOG_LEVEL_ALL;
    }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    public boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        //System.out.println("LogImpl: Current "+currentLogLevel+", compared "+logLevel);
        return (logLevel >= currentLogLevel);
    }




    //Log interface

    /**
     * <p> Are debug messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isDebugEnabled() {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }


    /**
     * <p> Are error messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isErrorEnabled() {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }


    /**
     * <p> Are fatal messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isFatalEnabled() {
        return isLevelEnabled(LOG_LEVEL_FATAL);
    }


    /**
     * <p> Are info messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isInfoEnabled() {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }


    /**
     * <p> Are trace messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }


    /**
     * <p> Are warn messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isWarnEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }


     // -------------------------------------------------------- Logging Methods

     /**
      * Gets a valid message from Throwable if message was not defined.
      * @param message Object
      * @param t Throwable
      * @return Object
      */
     private Object getLogMessageIfNull(Object message, Throwable t) {
         if ((message==null)&& (t!=null)) { message = t.getMessage(); }
         return message;
     }


     private boolean isCurrentLevelEnabled(Log log) {
         if (currentLogLevel >=LOG_LEVEL_OFF) {
             return false;
         } else if (currentLogLevel==LOG_LEVEL_FATAL) {
             return log.isFatalEnabled();
         } else if (currentLogLevel==LOG_LEVEL_ERROR) {
             return log.isErrorEnabled();
         } else if (currentLogLevel==LOG_LEVEL_WARN) {
             return log.isWarnEnabled();
         } else if (currentLogLevel==LOG_LEVEL_INFO) {
             return log.isInfoEnabled();
         } else if (currentLogLevel==LOG_LEVEL_DEBUG) {
             return log.isDebugEnabled();
         } else if (currentLogLevel==LOG_LEVEL_TRACE) {
             return log.isTraceEnabled();
         } else if (currentLogLevel<=LOG_LEVEL_ALL) {
             return true;
         }
         return false;
     }

     /**
      * <p> Log a message with trace log level. </p>
      *
      * @param message log this message
      */
     public void trace(Object message) {
         if (message instanceof Throwable){
             trace (message,(Throwable)message);
         } else {
            invoke(TRACE_O,new Object[]{message});
         }
     }



     /**
      * <p> Log an error with trace log level. </p>
      *
      * @param message log this message
      * @param t log this cause
      */
     public void trace(Object message, Throwable t){
         message = getLogMessageIfNull(message, t);
         invoke(TRACE_O_T,new Object[]{message,t});
     }


     /**
      * <p> Log a message with debug log level. </p>
      *
      * @param message log this message
      */
     public void debug(Object message){
         if (message instanceof Throwable){
             debug (message,(Throwable)message);
         } else {
            invoke(DEBUG_O,new Object[]{message});
         }
     }


     /**
      * <p> Log an error with debug log level. </p>
      *
      * @param message log this message
      * @param t log this cause
      */
     public void debug(Object message, Throwable t){
        message = getLogMessageIfNull(message, t);
        invoke(DEBUG_O_T,new Object[]{message,t});
     }


     /**
      * <p> Log a message with info log level. </p>
      *
      * @param message log this message
      */
     public void info(Object message){
         if (message instanceof Throwable){
             info (message,(Throwable)message);
         } else {
             invoke(INFO_O,new Object[]{message});
         }
     }


     /**
      * <p> Log an error with info log level. </p>
      *
      * @param message log this message
      * @param t log this cause
      */
     public void info(Object message, Throwable t){
         message = getLogMessageIfNull(message, t);
         invoke(INFO_O_T,new Object[]{message,t});
     }


     /**
      * <p> Log a message with warn log level. </p>
      *
      * @param message log this message
      */
     public void warn(Object message){
         if (message instanceof Throwable){
             warn (message,(Throwable)message);
         } else {
             invoke(WARN_O,new Object[]{message});
         }
     }


     /**
      * <p> Log an error with warn log level. </p>
      *
      * @param message log this message
      * @param t log this cause
      */
     public void warn(Object message, Throwable t){
         message = getLogMessageIfNull(message, t);
         invoke(WARN_O_T,new Object[]{message,t});
     }


     /**
      * <p> Log a message with error log level. </p>
      *
      * @param message log this message
      */
     public void error(Object message){
         if (message instanceof Throwable){
             error (message,(Throwable)message);
         } else {
            invoke(ERROR_O,new Object[]{message});
         }
     }


     /**
      * <p> Log an error with error log level. </p>
      *
      * @param message log this message
      * @param t log this cause
      */
     public void error(Object message, Throwable t){
         message = getLogMessageIfNull(message, t);
         invoke(ERROR_O_T,new Object[]{message,t});
     }


     /**
      * <p> Log a message with fatal log level. </p>
      *
      * @param message log this message
      */
     public void fatal(Object message){
         if (message instanceof Throwable){
             fatal (message,(Throwable)message);
         } else {
         invoke(FATAL_O,new Object[]{message});
         }
     }


     /**
      * <p> Log an error with fatal log level. </p>
      *
      * @param message log this message
      * @param t log this cause
      */
     public void fatal(Object message, Throwable t){
         message = getLogMessageIfNull(message, t);
         invoke(FATAL_O_T,new Object[]{message,t});
     }


    //SFLog interface


    /**
     * <p> Is ignore logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than ignore. </p>
     */
    public boolean isIgnoreEnabled(){
        return isLevelEnabled(LOG_LEVEL_IGNORE);
    }


    /**
     * <p> Log a message with ignore log level. </p>
     *
     * @param message log this message
     */
    public void ignore(Object message){
        if (message instanceof Throwable){
            ignore (message,(Throwable)message);
        } else {
            invoke(TRACE_O,new Object[]{"IGNORE - "+message.toString()});
         }
    }


    /**
     * <p> Log an error with ignore log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void ignore(Object message, Throwable t){
        message = getLogMessageIfNull(message, t);
        invoke(TRACE_O_T,new Object[]{"IGNORE - "+message.toString(),t});
    }


    /**
     * <p> Log a message with ignore log level. </p>
     *
     * @param message log this message
     */
    public void ignore(Object message, SmartFrogException t, TerminationRecord tr) {
        ignore(message,t);
    }


    /**
     * <p> Log an error with ignore log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void ignore(Object message, SmartFrogException t){
        ignore(message,t);
    }


    // Special methods for user output
    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     */
    public void out(Object message){
        try {
            if (isCurrentLevelEnabled(localLog)) {
                if ((localLog!=null)&&(localLog instanceof LogMessage)) {
                    ((LogMessage)localLog).out(message);
                } else {
                    System.out.println(message.toString());
                }
            }
        }catch (Throwable thr){
            if (localLog!=null)
                     localLog.trace(message,thr);
                 else thr.printStackTrace();
        }

        //Registered logs
        synchronized (registeredLogs){
            Log log=null;
            Enumeration logs = registeredLogs.elements();
            while (logs.hasMoreElements()){
                try {
                    log = (Log) logs.nextElement();
                    if (isCurrentLevelEnabled(log)) {
                        if (log instanceof LogMessage)
                            ((LogMessage)log).out(message);
                        else log.info(message);
                    }
                } catch (Throwable thr) {
                    if (log!=null) log.trace("",thr);
                    else if (localLog!=null)
                        localLog.trace("",thr);
                    else thr.printStackTrace();

                }
            }
        }
    }

    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     */
    public void err(Object message){
        Throwable thrNull = null;
        err (message, (Throwable)thrNull);
    }

    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void err(Object message, Throwable t){
        message = getLogMessageIfNull(message, t);
        try {
            if (isCurrentLevelEnabled(localLog)) {
                if ((localLog!=null)&&(localLog instanceof LogMessage)) {
                    ((LogMessage)localLog).err(message, t);
                    if (Logger.logStackTrace){
                      if (t != null){t.printStackTrace();}
                    }
                } else {
                    System.err.println(message.toString());
                    if (t != null){t.printStackTrace();}
                }
            }
        }catch (Throwable thr){
            if (localLog!=null)
                     localLog.trace("",thr);
             else thr.printStackTrace();
        }
        //Registered logs
        synchronized (registeredLogs){
            Log log=null;
            Enumeration logs = registeredLogs.elements();
            while (logs.hasMoreElements()){
                try {
                    log = (Log) logs.nextElement();
                    if (isCurrentLevelEnabled(log)) {
                        if (log instanceof LogMessage)
                            ((LogMessage)log).err(message,t);
                        else log.info(message,t);
                    }
                } catch (Throwable thr) {
                    if (log!=null) log.trace("",thr);
                    else if (localLog!=null)
                        localLog.trace("",thr);
                    else thr.printStackTrace();

                }
            }
        }
    }


    /**
     * <p> Log a message with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     */
    public void err(Object message, SmartFrogException t, TerminationRecord tr){
       err(message.toString()+", TR:"+tr.toString(),(Throwable)t);
    }


    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void err(Object message, SmartFrogException t){
      err(message,(Throwable)t);
    }


    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    public void trace(Object message, SmartFrogException t, TerminationRecord tr){
        trace(message, (Throwable)t);
    }


    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void trace(Object message, SmartFrogException t){
        trace(message,(Throwable)t);
    }


    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    public void debug(Object message, SmartFrogException t, TerminationRecord tr){
        debug(message,(Throwable)t);
    }


    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void debug(Object message, SmartFrogException t){
        debug(message,(Throwable)t);
    }


    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    public void info(Object message, SmartFrogException t, TerminationRecord tr){
        info(message,(Throwable)t);
    }


    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void info(Object message, SmartFrogException t){
        info(message,(Throwable)t);
    }


    /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    public void warn(Object message, SmartFrogException t, TerminationRecord tr){
        warn(message,(Throwable)t);
    }


    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void warn(Object message, SmartFrogException t){
        warn(message,(Throwable)t);
    }


    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    public void error(Object message, SmartFrogException t, TerminationRecord tr){
        error(message,(Throwable)t);
    }


    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void error(Object message, SmartFrogException t){
        error(message,(Throwable)t);
    }


    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    public void fatal(Object message, SmartFrogException t, TerminationRecord tr){
        fatal(message,(Throwable)t);
    }


    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void fatal(Object message, SmartFrogException t){
        fatal(message,(Throwable)t);
    }


    //Log Registration interface
    public void register(String name,Log log)  throws SmartFrogLogException , RemoteException{
        try {
            registeredLogs.put(logName+"."+name, log);
        } catch (Exception ex) {
            throw (SmartFrogLogException)SmartFrogLogException.forward(ex);
        }
    }

   public void register(String name,Log log, int logLevel)  throws RemoteException, SmartFrogLogException{
       register(name,log);
       if (currentLogLevel>=logLevel){
         currentLogLevel=logLevel;
       }
   }

    public boolean deregister(String name)  throws SmartFrogLogException, RemoteException {
       try {
           if (registeredLogs.remove(logName+"."+name) == null) {
              return false;
           } else {
              return true;
           }

       } catch (Exception ex) {
            throw (SmartFrogLogException)SmartFrogLogException.forward(ex);
       }
    }


//    /**
//        * Main method used in unit testing.
//        *
//        *@param  args  Command line arguments
//        */
//       public static void main(String[] args) {
//          // Example of use
//          LogSF log = new LogImpl("test");
//          log.setLevel(LogSF.LOG_LEVEL_ALL);
//          String message = "message - ";
//          int i = 1;
//          log.out(message+ i++);
//          log.info(message+ i++);
//          try {
//              log.trace(message+ i++);
//          } catch (Exception ex){
//              log.fatal("Oppss!"+message+ i++,ex);
//          }
//          try {
//              log.trace("Hola -2-"+message+ i++);
//          } catch (Exception ex){
//              log.fatal("Oppss! -2-"+message+ i++,ex);
//          }
//
//       }
//


/*

@TODO add method for user message output! -> Use default Logger!


---
System.err.println((sfex).toString("\n   "));
---
TR and ComponentName
          if ((logStackTrace)&&!tr.errorType.equals(TerminationRecord.NORMAL)) {
            StringBuffer strb = new StringBuffer();
            strb.append("LOG TR: Component: " +componentName +", "+ tr.toString());
            log(strb.toString());


          }

// SET the level of current log according to the level used to register!

*/


}


