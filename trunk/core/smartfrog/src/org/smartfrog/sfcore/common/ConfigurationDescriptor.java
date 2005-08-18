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

import org.smartfrog.SFSystem;

import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.prim.Prim;
import java.util.Hashtable;
import java.rmi.RemoteException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.ConnectException;

import org.smartfrog.sfcore.processcompound.ProcessCompound;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;

public class ConfigurationDescriptor implements MessageKeys{
    /**
     * an enumeration of our options
     */
    public static class Action {
        private Action() {
        }

        final static public String ACT_DEPLOY = "DEPLOY";
        final static public int DEPLOY = 0;
        final static public String ACT_TERMINATE = "TERMINATE";
        final static public int TERMINATE = 1;
        final static public String ACT_UNDEFINED = "UNDEFINED";
        final static public int UNDEFINED = 2;
        final static public String ACT_DETACH= "DETACH";
        final static public int DETACH = 3;
        final static public String ACT_DETaTERM = "DETaTERM";
        final static public int DETaTERM = 4;
        final static public String ACT_PING = "PING";
        final static public int PING = 5;
        final static public String ACT_PARSE = "PARSE";
        final static public int PARSE = 6;
        final static public String ACT_DIAGNOSTICS = "DIAGNOSTICS";
        final static public int DIAGNOSTICS = 7;

        static public String[] type= {
                      ACT_DEPLOY,
                      ACT_TERMINATE,
                      ACT_UNDEFINED,
                      ACT_DETACH,
                      ACT_DETaTERM,
                      ACT_PING,
                      ACT_PARSE,
                      ACT_DIAGNOSTICS};
    }


    private String originalSFACT = null;

    private String lineSeparator="\n    ";
    /**
     Action type; one of the Action enumerations. Initially set to
     #Action.UNDEFINED
     */
    private int actionType = Action.UNDEFINED;

    /**
     * the action to perform
     */
    private ConfigurationAction action;

    /**
     * application/component name
     */
    private String name = null;
    /**
     * resource to use during action. Usually a sf description
     */
    private String url = null;

    /**
     * host where to apply action. Can be null and then no rootProcess is used.
     */
    private String host = null;
    /**
     * subProcess where to apply action. Can be null.
     */
    private String subProcess = null;


    /**
     *  SmartFrog context than can be used by ACTIONS
     */
    private Context context=null;




    /**
     * class acting as an enumeration for results
     */
    public static class Result {
        private Result() {
        }

        final static public int SUCCESSFUL=0;
        final static public int FAILED=1;
        final static public int UNDEFINED=2;
        final static public int UNKNOWN=3;
        static String[] type= {"SUCCESSFUL",
                               "FAILED",
                               "UNDEFINED",
                               "UNKNOWN"};
     }

     /**
      * Result type for action
      */
     private int resultType = Result.UNDEFINED;

     /**
      * Result Object return by EXEC action
      */
     public Object resultObject = null;

     /**
      * Result message for action
      */
     private String resultMessage = null;
     /**
      * Result exception for action
      */
     public Throwable resultException = null;



     /**
      * Extra parameters for action
      */
     private Hashtable options = new Hashtable();

     /**
      *   Special Options for SF1 Language
      */
     public static class SF1Options {
         static String SFCONFIGREF = "sfConfigRef";
     }

    /**
     * To String
     * @return
     */
    public String toString() {
        //return toString(", \n");
        return toString(", ");
    }

    /**
     * To String
     * @param separator
     * @return
     */
    public String toString(String separator){
        StringBuffer str = new StringBuffer();
        if (getName()!=null) {
            str.append(" name:"); str.append(getName().toString());
        }
        str.append(separator);
        str.append(" type:"); str.append(Action.type[actionType].toString());

        if (getUrl()!=null) {
            str.append(separator);
            str.append(" url:"); str.append(getUrl().toString());
        }
        if ((getDeployReference()!=null)&&(getDeployReference().size()>0)) {
            str.append(separator);
            str.append(" depRef:"); str.append(getDeployReference().toString());
        }
        if (getHost()!=null) {
            str.append(separator);
            str.append(" host:"); str.append(getHost().toString());
        }
        if (getSubProcess()!=null) {
            str.append(separator);
            str.append(" subProc:"); str.append(getSubProcess().toString());
        }

        str.append(separator);
        str.append(" resultType:"); str.append(Result.type[resultType].toString());

        if (resultMessage!=null) {
            str.append(separator);
            str.append(" resultMessage:");  str.append(resultMessage.toString());
            }
        if (resultException!=null) {
          str.append(separator);
          str.append(" resultExceptionMessage:");
          str.append(resultException.getMessage());
          if (Logger.logStackTrace) {
             str.append(parseExceptionStackTrace(resultException,separator));
          }
        }
        return str.toString();
    }

    /**
     *  Gets status message using ', ' as separator
     * @return status message
     */
    public String statusString (){
       String s= ", ";
       return statusString(s);
    }
    /**
     * Gets status message
     * @param separator
     * @return message
     */
    public String statusString(String separator){
          StringBuffer message = new StringBuffer();
          StringBuffer messageError=null;
          String result = "";
          if ((resultObject!=null)&&(resultObject instanceof Prim)){
            try {
              message.append("'");
              message.append(getResultObjectName().toString());
              message.append("'");
            } catch(Exception ex){
               try {
                 if (getName()!=null) {
                     //This will happen when a component is terminated.
                     message.append(getName().toString());
                     message.append("'");
                 }
               } catch (Exception ex1){
                   message.append(SmartFrogCoreKeys.SF_ROOT_PROCESS);
                   message.append("'");
               }
            }
          } else if (getName()!=null) {
              message.append("'");
              message.append(getName().toString());
              message.append("'");
          }

          if ((getUrl()!=null)&& !(getUrl().trim().equals(""))) {
              message.append(separator);
              message.append(" ["); message.append(getUrl().toString()+"]");
          }
          if (getDeployReference()!=null) {
              message.append(separator);
              message.append(" deployReference: "); message.append(getDeployReference().toString());
          }
          if (getHost()!=null) {
              message.append(separator);
              message.append(" host:"); message.append(getHost().toString());
          }
          if (getSubProcess()!=null) {
              message.append(separator);
              message.append(" subProcess:"); message.append(getSubProcess().toString());
          }

          if (Logger.logStackTrace) {
            if ( (this.resultObject != null) && (this.resultObject instanceof Prim)) {
              try {
                Object time = ( (Prim)this.resultObject).sfResolveHere("sfParseTime");
                message.append(separator);
                message.append(" parse time: " + time);
              }
              catch (Exception ex1) {
                //Logger.logQuietly(ex1);
                if (SFSystem.sfLog().isIgnoreEnabled()){
                  SFSystem.sfLog().ignore(ex1);
               }
              }
            }

            if ( (this.resultObject != null) && (this.resultObject instanceof Prim)) {
              try {
                Object time = ( (Prim)this.resultObject).sfResolveHere("sfDeployTime");
                message.append(separator);
                message.append(" deploy time: " + time);
              }
              catch (Exception ex1) {
                //Logger.logQuietly(ex1);
                if (SFSystem.sfLog().isIgnoreEnabled()){SFSystem.sfLog().ignore(ex1);}
              }
            }
          }

          if (this.resultType==Result.SUCCESSFUL){
              switch (this.getActionType()) {
                case (ConfigurationDescriptor.Action.DEPLOY):
                    {
                    result= MessageUtil.formatMessage(MSG_DEPLOY_SUCCESS, message.toString());
                    }
                    break;
                case ConfigurationDescriptor.Action.DETACH:
                    {
                    result= MessageUtil.formatMessage(MSG_DETACH_SUCCESS, message.toString());
                    }
                    break;

                case ConfigurationDescriptor.Action.DETaTERM:
                    {
                    result= MessageUtil.formatMessage(MSG_DETACH_TERMINATE_SUCCESS, message.toString());
                    }
                    break;
                case ConfigurationDescriptor.Action.TERMINATE:
                   {
                   result= MessageUtil.formatMessage(MSG_TERMINATE_SUCCESS, message.toString());
                   }
                    break;
                case ConfigurationDescriptor.Action.PING: {
                    result = MessageUtil.formatMessage(MSG_PING_SUCCESS,
                                                       name.toString(),
                                                       host.toString(),
                                                       this.getResultMessage().toString());
                    }
                    break;
                 case ConfigurationDescriptor.Action.PARSE: {
                      result = "Parsed :"+this.getUrl() +"\n"+ this.getContextAttribute("parseReport").toString();
                      }
                     break;

                 case ConfigurationDescriptor.Action.DIAGNOSTICS: {
                    result = "Diagnostics report for "+this.getName()+"\n"+ this.getContextAttribute("diagnosticsReport").toString();
                    }
                     break;


                default:
                    // Unknown action.
                    messageError = new StringBuffer();
                    messageError.append(""); messageError.append(Result.type[resultType].toString());
                    messageError.append(" when trying ");
                    messageError.append(Action.type[actionType].toString());
                    messageError.append(" of ");
                    messageError.append(message);
                    result= messageError.toString();
                    break;
            }
          } else {
              messageError = new StringBuffer();
              messageError.append(""); messageError.append(Result.type[resultType].toString());
              messageError.append(" when trying ");
              messageError.append(Action.type[actionType].toString());
              messageError.append(" of ");
              messageError.append(message);
              result= messageError.toString();
          }
          if ((Logger.logStackTrace)||
              (((resultMessage!=null)||
                (resultException!=null))&&(this.resultType!=Result.SUCCESSFUL))) {
                  messageError = new StringBuffer();
                  messageError.append(lineSeparator);
                  messageError.append("Result:");
                  lineSeparator=lineSeparator+"  ";
                  if ((resultMessage!=null)&&(!(resultMessage.toString().trim().equals("")))) {
                     messageError.append(lineSeparator);
                     messageError.append("* Message: '"+ resultMessage.toString()+"'");
                  }
                  if (resultException!=null) {
                      messageError.append(lineSeparator);
                      messageError.append("* Exception: '"+parseException(resultException,lineSeparator+"   ")+"'");
                      if (Logger.logStackTrace) {
                         messageError.append(lineSeparator);
                         messageError.append("* StackTrace: '"+parseExceptionStackTrace(resultException,lineSeparator+"   ")+"'");
                       }
                  }
                  if (originalSFACT!=null) {
                    messageError.append(lineSeparator);
                    messageError.append("* Command line SFACT: '" + originalSFACT+"'");
                  }
                  messageError.append(lineSeparator);
                  messageError.append("* To String: '" + this.toString(separator)+"'");
                  result = result + messageError.toString();
           }

          return result;
    }

    /**
     * Generates a user friendly message for certain exceptions.
     * @param thr Exception
     * @return message
     */
    private String parseException (Throwable thr, String lineSeparator){
        StringBuffer messageError = new StringBuffer();
        if (thr instanceof SmartFrogException){
            //messageError.append(((SmartFrogException)thr).toString("\n   "));
        } else if (thr instanceof UnknownHostException){
          //Logger.log(MessageUtil.formatMessage(MSG_UNKNOWN_HOST, opts.host), uhex);
          messageError.append( MessageUtil.formatMessage(MSG_UNKNOWN_HOST, host));
        } else if (thr instanceof ConnectException){
          //Logger.log(MessageUtil.formatMessage(MSG_CONNECT_ERR, opts.host), cex);
          messageError.append(MessageUtil.formatMessage(MSG_CONNECT_ERR, host));
        } else if (thr instanceof RemoteException) {
            //Logger.log(MessageUtil.formatMessage(MSG_REMOTE_CONNECT_ERR,opts.host), rmiEx);
            messageError.append(MessageUtil.formatMessage(MSG_REMOTE_CONNECT_ERR,host));
        } else if (thr instanceof Exception) {
            //Logger.log(MessageUtil.formatMessage(MSG_UNHANDLED_EXCEPTION), ex);
            messageError.append(MessageUtil.formatMessage(MSG_UNHANDLED_EXCEPTION)
            );
        }

        if (thr instanceof SmartFrogException) {
            //messageError.append(lineSeparator);
            messageError.append(((SmartFrogException)thr).toString(lineSeparator));
        } else {
            if ( thr !=null) {
                //messageError.append(lineSeparator);
                messageError.append(thr.toString());
            }
        }
        return messageError.toString();
    }

    /**
     * Generates a String for the StackTrace using lineSeparator
     * @param thr Exception
     * @return message
     */
    public static String parseExceptionStackTrace(Throwable thr, String lineSeparator) {

      if (thr==null) return "";

      final StringWriter sw = new StringWriter(1024);
      final PrintWriter out = new PrintWriter(sw, false);

      StringBuffer messageError = new StringBuffer();
      messageError.append(lineSeparator);
      //thr.fillInStackTrace();
      thr.printStackTrace(out);

      LineNumberReader in = new LineNumberReader( new StringReader(sw.toString()));

      try {
        String result;
        while ( (result = in.readLine()) != null) {
          messageError.append(result.toString() + lineSeparator);
        }
      } catch (IOException ex) {
        // this should REALLY never happen
        throw new RuntimeException(ex.toString());
      }

      if (thr instanceof SmartFrogException) {
        //messageError.append(lineSeparator);
        messageError.append( ( (SmartFrogException) thr).toStringAll(lineSeparator));
      }
      //messageError.append(lineSeparator+" --- StackTrace sfex Begins --");
      return messageError.toString();
    }

    /**
     * this is a constant that defines what the inter-element token is when cracking
     * the string
     */
    private static final String separator = ":";

    /**
     * Internal buffer to parse urls.
     */
    private String tempURL = null;
    /**
     * Creates a Configuration Descriptor using a deployment URL
     * @param deploymentURL Format: 'name:ACTION:url:sfConfig:HOST:PROCESS'
     * <pre>
     *      - name: name where to apply ACTION
     *            ex. foo
     *            ex. "HOST localhost:foo"
     *            ex. 'HOST localhost:foo'
     *      - ACTION: possible actions: DEPLOY, TERMINATE, DETACH, DETaTERM, PING, PARSE, DIAGNOSTICS
     *      - url: description used by ACTION
     *            ex. /home/sf/foo.sf
     *            ex. "c:\sf\foo.sf"
     *            ex. "c:\My documents\foo.sf"
     *            ex. 'c:\My documents\foo.sf'
     *      - target: component description name to use with action. It can be empty
     *            ex: foo
     *            ex: "first:foo"
     *            ex: 'first:foo'
     *            note: sfConfig cannot be use with DEPLOY!
     *      - HOST: host name or IP where to apply ACTION. When empty it assumes localhost.
     *            ex: localhost
     *            ex: 127.0.0.1
     *      - PROCESS: process namewhere to apply ACTION. When empty it assumes rootProcess
     *     ex1: Deploy a description in local daemon
     *        counterEx:DEPLOY:org/smartfrog/examples/counter/example.sf::localhost:
     *     ex2. Terminate local sfDaemon
     *        rootProcess:TERMINATE:::localhost:
     *     ex3: Deploy "counterToSucceed" from counter/example2.sf
     *        counterEx3:DEPLOY:org/smartfrog/examples/counter/example2.sf:"testLevel1:counterToSucceed":localhost:
     *</pre>
     * @see Action
     * @throws SmartFrogInitException
     *
     * @todo fix this text for JavaDocs
     */
    public ConfigurationDescriptor (String deploymentURL) throws SmartFrogInitException {
        try {
            this.originalSFACT = deploymentURL;
            if (SFSystem.sfLog().isDebugEnabled()){SFSystem.sfLog().debug("Parsing SFACT: ["+originalSFACT+"]");               }

            if (deploymentURL==null) {
                throw new SmartFrogInitException("Deployment URL: null");
            }

            String item = null;
            deploymentURL = deploymentURL.trim();
            tempURL = deploymentURL;

            if (deploymentURL.length() < 1) {
                throw new SmartFrogInitException("Deployment URL: wrong format");
            }

            //GET SUBPROCESS_NAME (6th Element)
            try {
                this.setSubProcess(getAndCutLastFieldTempURL(separator));
            } catch (Exception ex) {
                throw new SmartFrogInitException( "Error parsing SUBPROCESS_NAME in: "+ deploymentURL+"("+ex.getMessage()+")", ex);
            }

            //GET HOST_NAME (5th Element)
            try {
                this.setHost(getAndCutLastFieldTempURL(separator));
            } catch (Exception ex) {
                throw new SmartFrogInitException("Error parsing HOST in: "+ deploymentURL+"("+ex.getMessage()+")", ex);
            }

            //GET DEPLOY_REFERENCE (4th Element)
            //If it contains : has to be between double quotes(")
            //ex. ...:"componentOne:componentTwo":...
            try {
                this.setDeployReference(getAndCutLastFieldTempURL(separator));
            } catch (Exception ex) {
                throw new SmartFrogInitException("Error parsing DEPLOY_REFERENCE in: "+ deploymentURL +"("+ex.getMessage()+")", ex);
            }

            //GET URL (3rd Element)
             //(Everything that is left at the end)
             try {
                 this.setUrl(getAndCutLastFieldTempURL(separator));
             } catch (Exception ex) {
                 throw new SmartFrogInitException( "Error parsing DEPLOY_REFERENCE in: "+ deploymentURL+"("+ex.getMessage()+")", ex);
            }

            //GET ACTION(2nd Element)
            try {
                this.setActionType(getAndCutLastFieldTempURL(separator));
            } catch (Exception ex) {
                throw new SmartFrogInitException("Error parsing ACTION_TYPE in: "+ deploymentURL +"("+ex.getMessage()+")", ex);
            }

            // GET NAME (1st Element)
            //Check if url starts with " and extract name:
            // Valid examples:
            // "HOST guijarro.hpl.hp.com":rootProcess:sfDefault:display":TER:::localhost:subprocess;
            // "HOST "127.0.0.1":rootProcess:sfDefault:display":TER:::localhost:subprocess;
            // 'HOST "127.0.0.1":rootProcess:sfDefault:display':TER:::localhost:subprocess;
            // display:TER:::localhost:subprocess;
            // :TER:::localhost:subprocess; ->Unnamed component
            try {
                String field ="";
                if (tempURL.trim().endsWith("\"")||tempURL.trim().endsWith("'")) {
                   field = tempURL.substring(1,tempURL.length()-1);
                   // shell like input will take away the " but if using -f
                   // then the " will be there
                   if (field.trim().endsWith("\"")||field.trim().endsWith("'")) {
                      field = field.substring(1,field.length()-1);
                   }

                } else {
                    field = tempURL;
                }
                if (SFSystem.sfLog().isTraceEnabled()) {SFSystem.sfLog().trace("  Extracted ["+field+"] from ["+tempURL+"]"); }
                this.setName(field);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new SmartFrogInitException("Error parsing NAME in: "+ deploymentURL+"("+ex.getMessage()+")", ex);
            }
            if (SFSystem.sfLog().isDebugEnabled()){SFSystem.sfLog().debug("Parsing SFACT results: ["+this.toString()+"]");}
        } catch (Throwable thr){
           this.resultException = thr;
           throw (SmartFrogInitException)SmartFrogInitException.forward(thr);
        }
    }

    /**
     * Returns and cuts the last field from TempURL. Token marks the beginning
     * of the field. It previously removes " or '
     * @param token
     * @return last field from TempURL marked by token
     * @throws java.lang.Exception
     */
    private String getAndCutLastFieldTempURL( String token) throws Exception{
        String field = null;
        if (tempURL.trim().endsWith("\"")||tempURL.trim().endsWith("'")) {
            String tag ="\"";
            if (tempURL.trim().endsWith("'")) tag="'";
            String newURL = tempURL.substring(0, tempURL.length()-1);
            int indexFirstQuote = newURL.lastIndexOf(tag);
            field = tempURL.substring(indexFirstQuote+1,tempURL.length()-1);
            // shell like input will take away the " but if using -f
            // then the " will be there
            if (field.trim().endsWith("\"")||field.trim().endsWith("'")) {
               field = field.substring(1,field.length()-1);
            }

            if (SFSystem.sfLog().isTraceEnabled()) {SFSystem.sfLog().trace("  Extracted ["+field+"] from ["+tempURL+"]"); }
            if (indexFirstQuote==-1) {
                indexFirstQuote = 1;
            }
            tempURL=tempURL.substring(0,indexFirstQuote-1);
        } else {
            field = tempURL.substring(tempURL.lastIndexOf(token)+1, tempURL.length());
            if (SFSystem.sfLog().isTraceEnabled()) {SFSystem.sfLog().trace("  Extracted ["+field+"] from ["+tempURL+"]"); }
            tempURL = (tempURL.substring(0, tempURL.lastIndexOf(token)));
        }
        return field;
    }
    /**
     *
     * @param name application/component name
     * @param url resource to use during action. Usually a sf description
     */
    public ConfigurationDescriptor (String name, String url){
        if (url == null) return;
        this.setUrl(url);
        this.setName(name);
    }

    /**
     *
     * @param name application/component name
     * @param url resource to use during action. Usually a sf description
     * @param actionType @see Action inner class for valid types
     * @param host host were to apply action. Can be null and then no rootProcess is used.
     * @param subProcess subProcess were to apply action. Can be null.
     * @throws SmartFrogInitException
     */
    public ConfigurationDescriptor (String name, String url,int actionType,String host, String subProcess)
            throws SmartFrogInitException{

        this.setActionType(actionType);
        this.setUrl(url);
        this.setName(name);
        this.setHost(host);
        this.setSubProcess(subProcess);
    }

    /**
     * empty constructor for people who know what they are doing
     */
    public ConfigurationDescriptor() {
    }

    /**
     * Creates Configuration Descriptor
     * @param name application/component name
     * @param url resource to use during action. Usually a sf description
     * @param actionType @see Action inner class for valid types
     * @param deployReference reference used for final resolve of a configuration
     * @param host host were to apply action. Can be null and then no rootProcess is used.
     * @param subProcess subProcess were to apply action. Can be null.
     * @throws SmartFrogInitException when a parameter is wrongly defined
     */
    public ConfigurationDescriptor (String name, String url,int actionType,
                                    String deployReference ,String host, String subProcess)
            throws SmartFrogInitException, SmartFrogResolutionException {

        this.setActionType(actionType);
        this.setUrl(url);
        this.setName(name);
        // Deploy Reference is a particular case for SF1 and therefore added to
        // options
        this.setDeployReference(deployReference);
        this.setHost(host);
        this.setSubProcess(subProcess);
    }

    /**
     * Gets defined use for final resolve of a configuration
     * @return deployReference
     * @see Reference
     */
    public Reference getDeployReference(){
        String key = SF1Options.SFCONFIGREF;
        if (getOptions().containsKey(key)){
            return ((Reference)getOptions().get(key));
        }
        return null;
    }

    /**
     * Sets reference use for final resolve of a configuration
     * By default is will use 'sfConfig'.
     * @see Reference
     * @param reference
     */
    public void setDeployReference(String reference) throws SmartFrogResolutionException{

        if (reference.trim().equals("")){
            return;
        }
         this.getOptions().put(SF1Options.SFCONFIGREF,
                                  Reference.fromString(reference));
    }

    /**
     * Gets action type
     * @return
     */
    public int getActionType(){
        return actionType;
    }

    /**
     * To set all attributes for any result
     * @param type Type of result @see Result inner class
     * @param message result message
     * @param thr result exception if it existed
     */
    public void setResult(int type, String message, Throwable thr) {
        if ((type<0)||(type>Result.type.length)) {
            try {
                throw new SmartFrogInitException("Result type unknown");
            } catch (Exception ex) {
                //Logger.log(ex);
                if (SFSystem.sfLog().isTraceEnabled()){
                  SFSystem.sfLog().trace(ex);
               }
            }
        } else this.resultType = type;
        if (message!=null) this.resultMessage = message;
        if (thr!=null) this.resultException = thr;
    }

    /**
     * Sets result as SUCCESSFULL
     */
    public void setSuccessfulResult(){
      this.resultType=Result.SUCCESSFUL;
    }

    /**
     * set the action type. this also sets the {@link #action} attribute
     * to an instance of the action which is needed to do the actual execution.
     *
     * @param type
     * @throws SmartFrogInitException
     */
    public void setActionType(int type) throws SmartFrogInitException {
        this.actionType = type;
        switch(actionType) {
            case Action.DEPLOY:
                action=new ActionDeploy();
                break;
            case Action.DETACH:
                action = new ActionDetach();
                break;
            case Action.TERMINATE:
                action = new ActionTerminate();
                break;
            case Action.DETaTERM:
                action = new ActionDetachAndTerminate();
                break;
            case Action.PING:
                action = new ActionPing();
                break;
            case Action.PARSE:
                action = new ActionParse();
                break;
            case Action.DIAGNOSTICS:
                action = new ActionDiagnostics();
                break;
            default:
                throw new SmartFrogInitException("Action type unknown");
        }
    }


    /**
     * Sets action type.
     * @throws SmartFrogInitException it the type is not valid
     * @see Action inner class for valid types
     */

    public void setActionType(String type) throws SmartFrogInitException {
        for (int i=0;i<Action.type.length;i++) {
            if(Action.type[i].equals(type)) {
                setActionType(i);
                return;
            }
        }
        //only get here on failure
        throw new SmartFrogInitException("Action type unknown: " + type);
    }


    public int getResultType() {
        return resultType;
    }

    /**
     * Parses resultMessage and resultException and generates one
     * message.
     * @return message
     *
     */
    public String getResultMessage() {
        if (this.resultMessage!=null)
            return resultMessage;
        else if (this.resultException!=null)
            return resultException.getMessage();
        return "no message";
    }

    /**
     * Performs the nominated action
     * @param targetProcess optional target process; set to null to
     * hand off process lookup to the ConfigurationAction subclass.
     * It keeps a reference to the result object in resultObject.
     * @return the object created from the operation
     * @throws SmartFrogException if smartfrog is unhappy
     * @throws RemoteException if the network is unhappy
     */
    public Object execute(ProcessCompound targetProcess) throws SmartFrogException,
            RemoteException {
        try {
            if (action==null) {
                throw new SmartFrogInitException("No valid action");
            }
            if (targetProcess==null) {
                resultObject = action.execute(this);
            } else {
                resultObject = action.execute(targetProcess, this);
            }
        } catch (SmartFrogException sex){
             this.setResult(ConfigurationDescriptor.Result.FAILED,null,sex);
             throw sex;
         } catch (RemoteException rex){
             this.setResult(ConfigurationDescriptor.Result.FAILED,null,rex);
             throw rex;
        }
        return resultObject;
    }

    /**
     * get the name of this component
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * get the name of resultObject only if it is a Prim. Otherways returns null
     * @return String
     */
    public String getResultObjectName() {
      if ((resultObject!=null)&&(resultObject instanceof Prim)){
        try {
          return (( (Prim) resultObject).sfCompleteName().toString());
        } catch (RemoteException ex) {
          return null;
        }
      }
      return null;
    }


    /**
     * set the name of this component
     * @param name
     */
    public void setName(String name) {
      if (name.trim().equals("")){
        return;
      }
        this.name = name;
    }

    /**
     * Get resource to use during action.
     * @return a url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set resource to use during action.
     * @param url
     */
    public void setUrl(String url) {
        // Added to avoid problems with "" in shell scripts (Unix vs Windows)
        url=url.trim();
        if (url.startsWith("\"")&&(url.endsWith("\""))){
           url=url.substring(1,url.length()-1);
        }
        this.url = url;
    }

    /**
     * host for action. May be null
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * host where to apply action. Can be null and then no rootProcess is used.
     * @param host
     */
    public void setHost(String host) {
        if (host==null) return;
        if (host.trim().equals("")) return;
        this.host = host;
    }

    /**
     * subProcess where to apply action. Can be null.
     * @return string or null
     */
    public String getSubProcess() {
        return subProcess;
    }

    /**
     * set subProcess where to apply action. Can be null.
     * @param subProcess
     */
    public void setSubProcess(String subProcess) {
        if (subProcess==null) return;
        if (subProcess.trim().equals("")) return;
        this.subProcess = subProcess;
    }

    /**
     * get option hashtable. This is not a copy; it is a direct
     * accessor to the table.
     * @return
     */
    public Hashtable getOptions() {
        return options;
    }

    /**
     * option table
     * @param options
     */
    public void setOptions(Hashtable options) {
        this.options = options;
    }

    public void setOption(Object name, Object value) {
       this.options.put(name, value);
    }

    public Object getOption(Object name){
       return this.options.get(name);
    }

    public Context getContext (){
       return context;
    }

    public void setContext(Context context){
       this.context=context;
    }

    public Context setContextAttribute(Object name, Object value) {
        if (this.context==null) this.context=new ContextImpl();
        context.put(name,value);
        return context;
    }

    public Object getContextAttribute(Object name){
       if (context==null)  return null;
       return context.get(name);
    }
}
