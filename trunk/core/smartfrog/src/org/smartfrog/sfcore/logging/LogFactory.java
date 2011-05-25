package org.smartfrog.sfcore.logging;

import org.smartfrog.sfcore.common.SmartFrogCoreKeys;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLogException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.reference.Reference;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  Log Factory
 */
public class LogFactory {

    /**
     * hashtable of loggers
     */
    protected static Hashtable<String, LogSF> loggers = new Hashtable<String, LogSF>();

    /**
     * Protected constructor that is not available for public use.
     */
    protected LogFactory() {
    }

    /**
     * get a log for a component
     * @param prim component
     * @return a new log.
     * @throws SmartFrogLogException when something went wrong with getting a log
     */
    public static synchronized LogSF getLog(Prim prim) throws SmartFrogLogException {
        LogSF log = null;
        try {
            final Reference completeName = prim.sfCompleteName();
            //look for a log
            final String name = completeName.toString();
            log = loggers.get(name);
            //if found, return it
            if (log != null) {
                return log;
            }
            //else create a new one
            log = new LogImpl(completeName.toString());
            //and remember it
            loggers.put(name, log);
        } catch (RemoteException e) {
            throw (SmartFrogLogException) SmartFrogLogException.forward(e);
        }
        return log;
    }

    /**
     * Get a log for an object.
     * Prim and ComponentDescription will use sfCompletename
     * An unknown object uses its class name, unless it is actually a class, in which
     * case the name of the class it represents will be used.
     * @param obj (Prim, ComponentDescription, object.)
     * @return a new log.
     * @throws SmartFrogLogException when something went wrong with getting a log
     */
    public static synchronized LogSF getLog(Object obj) throws SmartFrogLogException {
        LogSF log;
        try {
            if (obj instanceof Prim) {
                log = getLog((Prim) obj);
            } else if (obj instanceof ComponentDescription) {
                log = getLog((ComponentDescription) obj);
            } else if (obj instanceof Class) {
                log = getLog((Class) obj);
            } else {
                log = getLog(obj.getClass());
            }
        } catch (Exception e) {
            throw (SmartFrogLogException) SmartFrogLogException.forward(e);
        }
        return log;
    }

    /**
     * get a log for a component
     * @param cd component description
     * @return a new log.
     * @throws SmartFrogLogException when something went wrong with getting a log
     */
    public static synchronized LogSF getLog(ComponentDescription cd) throws SmartFrogLogException {
        LogSF log = null;
        try {
            final Reference completeName = cd.sfCompleteName();
            //look for a log
            final String name = completeName.toString();
            log = loggers.get(name);
            //if found, return it
            if (log != null) {
                return log;
            }
            //else create a new one
            log = new LogImpl(name);
            //and remember it
            loggers.put(name, log);
        } catch (Exception e) {
            throw (SmartFrogLogException) SmartFrogLogException.forward(e);
        }
        return log;
    }

    /**
     * get a named log.
     * @param name log name
     * @return a log from cache or new.
     *
     */
    public static synchronized LogSF getLog(String name) {
        LogSF log = loggers.get(name);
        if (log != null) {
            return log;
        }
        log = new LogImpl(name);
        loggers.put(name, log);
        if ((log != null) && log.isTraceEnabled()) {
            log.trace("New log created: " + name);
        }
        return log;
    }

    /**
     * Convenience method to return a named logger
     *
     * @param clazz Class from which a log name will be derived
     *
     * @return Log
     */
    public static LogSF getLog(Class clazz) {
        return (getLog(clazz.toString()));
    }

    /**
     * Convenience method to derive a name from
     *
     * @param prim component to use
     * @param registerWithCoreLog flag to enable registration with the core
     * @return a log
     * @throws SmartFrogLogException if the prim name could not be completed, or when registration failed.
     *
     */
    public static LogSF getLog(Prim prim, boolean registerWithCoreLog) throws SmartFrogLogException {
        LogSF log = getLog(prim);
        if (registerWithCoreLog) {
            String name = null;
            try {
                name = prim.sfCompleteName().toString();
            } catch (RemoteException e) {
                throw (SmartFrogLogException) SmartFrogLogException.forward(e);
            }
            registerWithCore(name, log);
        }
        return log;
    }


    /**
     * <p>Construct (if necessary) and return a <code>Log</code> instance.</p>
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  loggers registered with Log )
     *
     * @param registerWithCoreLog flag to enable registration with the core
     * @return a log
     * @exception SmartFrogLogException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static LogSF getLog(String name, boolean registerWithCoreLog) throws SmartFrogLogException {
        LogSF log = getLog(name);
        if (registerWithCoreLog) {
            registerWithCore(name, log);
        }
        return log;
    }


    /**
     *  To get the sfCore logger
     * @return Logger implementing LogSF and Log
     */
    public static LogSF sfGetProcessLog() {
        LogSF sflog = _internal_getCoreLog();
        _internal_trace("getProcessLog()");
        /* LogImpl uses ComponentDescription and it needs to enable its log only when that is available */
        ComponentDescriptionImpl.initLog(sflog);
        return sflog;
    }

    /**
     * This is only for tracing problems with log setup, because the logging doesnt always exist yet
     * @param message message to trace
     * @return the core logger
     */
    private static void _internal_trace(String message) {
        LogSF sflog = _internal_getCoreLog();
        if ((sflog != null) && sflog.isTraceEnabled()) {
            sflog.trace(message);
        }
    }

    private static LogSF _internal_getCoreLog() {
        LogSF sflog = getLog(SmartFrogCoreKeys.SF_CORE_LOG);
        return sflog;
    }


    /**
     * register a log with the core
     * @param name log name
     * @param log  logger to be registered
     * @throws SmartFrogLogException if failed to register
     * @todo what happens when it changes its log level - needs to be fixed!
     */
    private static void registerWithCore(String name, LogSF log) throws SmartFrogLogException {
        try {
            LogSF coreLog = _internal_getCoreLog();
            if (!(coreLog instanceof LogRegistration)) {
                throw new SmartFrogLogException("Core log does not implement LogRegistration");
            }
            LogRegistration logRegistration = ((LogRegistration) coreLog);
            logRegistration.register(name, log);
        } catch (RemoteException e) {
            throw (SmartFrogLogException) SmartFrogLogException.forward(e);
        }
    }


    /**
     * Release any internal references to previously created {@link Log}
     * logs returned by this factory.
     */
    public void release() {
        loggers.clear();
    }

    /**
     * get the log of an owner, or, if that fails, from our classname.
     * Useful for helper classes that want to log through their owner
     * and dont want lots of error handling in their constructors
     * @param owner component that owns this helper class
     * @param clazz class own classname
     * @return a log
     */
    public static Log getOwnerLog(final Prim owner, final Class clazz) {
        Log log = null;
        try {
            if (owner instanceof PrimImpl) {
                log = ((PrimImpl) owner).sfGetApplicationLog();
            } else {
                _internal_trace("Cannot get the owner log of a remote reference " + owner);
            }
        } catch (SmartFrogException ignored) {

        } catch (RemoteException ignored) {
        }
        if (log == null) {
            log = getLog(clazz.getName());
            assert log != null;
        }
        return log;
    }

    /**
     * get the log of an owner, or, if that fails, from our classname.
     * Useful for helper classes that want to log through their owner
     * and dont want lots of error handling in their constructors
     * @param owner component that owns this helper class
     * @param object class to log off
     * @return a log
     */
    public static Log getOwnerLog(final Prim owner, final Object object) {
        return getOwnerLog(owner, object.getClass());
    }

    /**
     * get the log of an owner, or, if that fails, from its classname.
     * always returns a log of one form or other
     * @param owner  component that owns this helper class
     * @return a log
     */
    public static Log getOwnerLog(final Prim owner) {
        return getOwnerLog(owner, owner);
    }

    /**
     * Report list of Logger registered in LogFactory
     * @param out StringBuffer
     */
    public static void doReportLoggers(StringBuffer out) {
        for (Enumeration e = loggers.keys(); e.hasMoreElements();) {
            out.append((String) e.nextElement());
            out.append("\n");
        }
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        doReportLoggers(out);
        return out.toString();
    }

}
