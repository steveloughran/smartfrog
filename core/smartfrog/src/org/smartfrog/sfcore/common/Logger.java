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

package org.smartfrog.sfcore.common;

//import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.SFSystem;

/**
 * Class used to log all the messages in SmartFrog system. Logger provides
 * static methods to log the messages.
 */
public class Logger implements MessageKeys {

//   /** String name for caller. */
//    public static final String CALLER = "caller";

    /** Property to enable stack trace. The default value is overridden by the
     * value specified in default.ini file.
     */
    public static boolean logStackTrace = false;

    /** Property to enable sfPing log. The default value is overridden by the
     * value specified in default.ini file.
     */
    public static boolean logLiveness = false;

    private static boolean initialized=false;

    private Logger(){
    }

    public static synchronized void  init() {
        if (initialized) return;
        /**
         * Reads System property "org.smartfrog.logger.logStrackTrace" and
         * updates Logger with the value to enable stack tracing.
         */
        String source = System.getProperty(SmartFrogCoreProperty.propLogStackTrace);
        if ("true".equals(source)) {
            Logger.logStackTrace = true;
            if (SFSystem.sflog().isWarnEnabled()) {
              SFSystem.sflog().warn(MessageUtil.
                    formatMessage(MSG_WARNING_STACKTRACE_ENABLED));
            }
        }
        /**
         * Reads System property "org.smartfrog.logger.logLiveness" and
         * updates Logger with the value to enable sfPing tracing.
         */
        source="false";
        source = System.getProperty(SmartFrogCoreProperty.propLogLiveness);
        if ("true".equals(source)) {
            Logger.logLiveness = true;
        }

        initialized = false;
    }



//    /**
//     * Logs the message on the standard output.
//     *
//     * @param message The message to be logged
//     */
//    public static void log(String message) {
//        System.out.println(message);
//    }
//
//    /**
//     * Logs the exception messages and stack trace.
//     *
//     * @param ex a Throwable object to be logged
//     */
//    public static void log(Throwable ex) {
//        if (ex==null) return;
//        System.err.println(ex.toString());
//        if (logStackTrace) {
//          System.err.println(" ");
//          System.err.println(" ");
//          System.err.println(" --- StackTrace Throwable Begins --");
//          System.err.println(ex.toString());
//          System.err.println(" *" + MessageUtil.
//                                    formatMessage(MSG_STACKTRACE_FOLLOWS));
//          ex.printStackTrace();
//          System.err.println(" --- StackTrace Throwable Ends --");
//          System.err.println(" ");
//          System.err.println(" ");
//        }
//    }
//
//    /**
//     * Logs the SmartFrogException messages and stack trace.
//     *
//     * @param sfex SmartFrogException to be logged
//     */
//    public static void log (SmartFrogException sfex){
//        if (sfex==null) return;
//        System.err.println((sfex).toString("\n   "));
//        if (logStackTrace) {
//            System.err.println(" ");
//            System.err.println(" ");
//            System.err.println(" --- StackTrace sfex Begins --");
//            System.err.println(" *" + MessageUtil.
//                                    formatMessage(MSG_STACKTRACE_FOLLOWS)+"\n");
//            sfex.printStackTrace();
//            System.err.print(" *"+" Dump: \n");
//            System.err.println((sfex).toStringAll("\n"));
//            System.err.println(" --- StackTrace sfex Ends --");
//            System.err.println(" ");
//            System.err.println(" ");
//        }
//    }
//
//
//    /**
//     * Logs the Exception messages when org.smartfrog.logStackTrace=true.
//     *
//     * @param ex a Throwable object to be logged
//     */
//    public static void logQuietly(Throwable ex){
//        if (logStackTrace) log("QUIET ",ex);
//    }
//
//    /**
//     * Logs the Exception messages when org.smartfrog.logStackTrace=true.
//     *
//     * @param msg the message in the context of the exception
//     * @param ex a Throwable object to be logged
//     *
//     */
//    public static void logQuietly(String msg, Throwable ex){
//        if (logStackTrace) {
//          log("QUIET- "+msg);
//          log("QUIET- ", ex);
//        }
//    }
//
//    /**
//     * Logs the customized message and exception.
//     *
//     * @param msg the message in the context of the exception
//     * @param ex a Throwable object to be logged
//     */
//    public static void log(String msg, Throwable ex) {
//        log(msg);
//        log(ex);
//    }
//
//    /**
//     * Logs the customized message and exception.
//     *
//     * @param msg the message in the context of the exception
//     * @param sfex SmartFrogException to be logged
//     */
//    public static void log(String msg, SmartFrogException sfex) {
//        log(msg);
//        log(sfex);
//    }
//
//
//    /**
//     * Logs the customized TerminationRecord and exception.
//     *
//     * @param componentName the component for which exception is logged
//     * @param tr a TerminationRecord object
//     * @param sfex SmartFrogException to be logged
//     */
//    public static void log(String componentName, TerminationRecord tr, SmartFrogException sfex) {
//        log(componentName, tr);
//        log(sfex);
//    }
//
//    /**
//     * Logs the customized TerminationRecord and exception.
//     *
//     * @param componentName the component for which exception is logged
//     * @param tr a TerminationRecord object
//     */
//    public static void log(String componentName, TerminationRecord tr) {
////      if (!tr.errorType.equals(TerminationRecord.NORMAL)){
////          log("Component: " +componentName +", "+ tr.toString());
////      } else
//      if ((logStackTrace)&&!tr.errorType.equals(TerminationRecord.NORMAL)) {
//        StringBuffer strb = new StringBuffer();
//        strb.append("LOG TR: Component: " +componentName +", "+ tr.toString());
//        log(strb.toString());
//      }
//    }


}
