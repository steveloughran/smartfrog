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

package org.smartfrog.services.sfinstaller;

import java.io.StringReader;
import java.util.Vector;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Map;

/** Instantiates a meta-template with the customization that our
 * setting requires, e.g., the names and number of physical machines, 
 * the name of the main jar files, the generic options used for testing 
 * (security on/off, dynamic loading on/off). 
 * 
 * @author Ritu Sabharwal
 */
public class TemplateGen { 

  /** An input file with the "raw" template. */
  static String templateFileName;

  /** An out file for the processed template. */
  static String outputFileName;

  /** An input file with the description of the hosts involved. */
  static String hostsFileName;

  /** A template that will instantiate the example.*/ 
  Template template;

  /** A collection of all the daemons.*/
  Vector allDaemons;

  /** A special daemon for dynamic loading jar files. */
  //Daemon principalDaemon = null;

  /** A special daemon for dynamic loading jar files. */
  static String httpServer = null;
  
  /**Log directory for telnet/ssh sessions logs. */
  static String logDir = ".";
  
  /** A collection of jar files for dynamic classloading. */
  static Vector httpJars= new Vector();
  
 // static String Jars = "";
  /** A flag to generate templates using security.*/
  static boolean securityOn=false;

  /** A flag to generate templates downloading jars dynamically from
   * web servers */
  static boolean dynamicLoadingOn=false;

  /** Number of Daemons deployed. */
  int numberDaemons=0;
  
  static char optionFlagIndicator = '-';

  /** A temporary file for copying the template. */
  static String temp = "temp.vm";
 
  /** File object for the temporary file. */
  static File tempFile = new File("." + System.getProperty("file.separator") + temp);
 // static File tempFile = null;
 
  /** File object for the template file. */
  static File file = null;
  
  /**
   * Class Constructor.
   *
   * @param args input command line string. 
   * @exception Exception Can't instantiate template. 
   */
  public TemplateGen(String[] args) throws Exception {

    readOptions(args);
    
    readDaemons();

    if (outputFileName == null) 
      instantiateTemplate(System.out);
    else 
      instantiateTemplate(new 
        PrintStream(new FileOutputStream(outputFileName)));
  }

  /** Read the description of the daemons from a file. The format of
   * this file is :
   *   <logicalName> <hostname> <transfertype> <logintype> <username> <password> <releasefile> <javaservice> <startdaemon> <keyfile> <secproperties> <smartfrogjar> <servicesjar> <examplesjar> <releasename> <javahome> <installdir> <emailto> <emailfrom> <emailserver>
   *       ....
   *
   */
  void readDaemons() throws Exception {

    allDaemons = new Vector();
    Reader r = new BufferedReader(new FileReader(hostsFileName));
    StreamTokenizer st = new StreamTokenizer(r);
    //    st.resetSyntax();
    st.eolIsSignificant(false);
    // Allow single line coments in the input
  //  st.slashSlashComments(true);
    st.wordChars('A', 'Z');
    st.wordChars('a', 'z');    
    st.wordChars('/','/');
    st.wordChars('0', '9');
    st.wordChars('-','-');
    st.wordChars('.','.');
    st.wordChars(':',':');
    st.wordChars('_','_');
    st.wordChars('@','@');
    st.wordChars('*','*');
    st.wordChars('\\','\\');
    st.whitespaceChars(' ',' ');
    String tempLogicalName=null;
    String tempOS = null;
    String tempHostName=null;
    String tempTransferType=null;
    String tempLoginType=null;
    String tempUserName=null;
    String tempPasswordFile=null;
    String tempLocalFile1=null;
    String tempLocalFile2=null;
    String tempLocalFile3=null;
    String tempKeyFile=null;
    String tempSecProperties=null;
    String tempSmartFrogJar=null;
    String tempServicesJar=null;
    String tempExamplesJar=null;
    String tempReleaseName=null;
    String tempJavaHome = null;
    String tempInstallDir = null;
    String tempEmailTo=null;
    String tempEmailFrom=null;
    String tempEmailServer=null;
        
    while ((tempLogicalName =getWord(st))!=null) {
      tempOS = getWord(st);
      tempHostName = getWord(st);
      tempTransferType = getWord(st);
      tempLoginType = getWord(st);
      tempUserName = getWord(st);
      tempPasswordFile = getWord(st);
      tempLocalFile1 = getWord(st);
      if (tempOS.equals(Daemon.WINDOWS)) {
    	tempLocalFile2 = getWord(st);
    	tempLocalFile3 = getWord(st);
      }
      if (securityOn) {
	tempKeyFile = getWord(st);
	tempSecProperties = getWord(st);
	tempSmartFrogJar = getWord(st);
	tempServicesJar = getWord(st);
	tempExamplesJar = getWord(st);
     }
      tempReleaseName = getWord(st);
     // if (tempOS.equals(Daemon.WINDOWS))
      tempJavaHome = getWord(st); 
      tempInstallDir = getWord(st);
      tempEmailTo=getWord(st);
      tempEmailFrom=getWord(st);
      tempEmailServer=getWord(st);
      allDaemons.add(new Daemon(tempLogicalName, tempOS,tempHostName, tempTransferType, tempLoginType, tempUserName, tempPasswordFile, tempLocalFile1, tempLocalFile2, tempLocalFile3, tempKeyFile, tempSecProperties, tempSmartFrogJar, tempServicesJar, tempExamplesJar, tempReleaseName, tempJavaHome, tempInstallDir, tempEmailTo, tempEmailFrom, tempEmailServer));
    }
  }


  /** Gets the next tokenized word from a stream.
   * 
   *
   * @param st An input StreamTokenizer.
   * @return A word just read from the input.
   */
  String getWord(StreamTokenizer st) throws Exception {
    
    int token = st.nextToken();
    if (token == StreamTokenizer.TT_WORD)
      return st.sval;
    else
      return null;
  }

  /**Instantiates the velocity template.
   *
   * @param out Output stream to dump the template.
   * @exception Exception Error while instantiating the template.
   */
  void instantiateTemplate(PrintStream out) throws Exception {
    
    Velocity.init();
    VelocityContext context = new VelocityContext();
    context.put("allDaemons", allDaemons);
    context.put("dynamicLoadingOn",new Boolean(dynamicLoadingOn));
    context.put("securityOn",new Boolean(securityOn));
   // context.put("httpServer",httpServer);
    context.put("httpJars", httpJars);
   // context.put("Jars", Jars);
    context.put("logDir", logDir + System.getProperty("file.separator"));
    
    file = new File(templateFileName);
    if(file.exists()) {
	 file.renameTo(tempFile);
    } 
    template = Velocity.getTemplate(temp);
  //  template = Velocity.getTemplate(templateFileName.toString());
    BufferedWriter writer = 
      new BufferedWriter(new OutputStreamWriter(out));
    if (template != null)
      template.merge(context, writer);
    writer.flush();
    writer.close();    
  }


  /** Scans command line options.
   *
   *
   * @param args The input command line.
   */
  static void readOptions(String[] args) {

    String errorString=null;
    int i;
    for (i=0;i<args.length & errorString == null;) {
      try {
        if (args[i].charAt(0) == optionFlagIndicator) {
          switch (args[i].charAt(1)) {
          case '?':
            errorString = "SFSystem help";
            break;
          case 't':
            templateFileName = args[++i];
            break;
          case 'h':
            hostsFileName = args[++i];
            break;
          case 'o':
            outputFileName = args[++i];
            break;
          case 'd':
            dynamicLoadingOn=true;
            break;
          case 's':
            securityOn=true;
            break;
          default: 
            errorString = "unknown option " + args[i].charAt(1);
          }
        } else {
          errorString = "illegal option format for option " + args[i];
        }
        i++;
      } catch (Exception e) {
        errorString = "illegal format for options ";
      }
    }
    if (errorString != null) {
      throw new IllegalArgumentException(errorString);
    }
  }
 
  /** Creates the argument set of running the TemplateGen and generating the 
   * description file.
   *
   * @param map Map with Daemon objects for the description
   * @param templateFile Template file 
   * @param outputFile Output description file  
   * @param securityStatus A flag to keep security on of off
   * @param dynamicLoadingStatus A flag to keep dynamic classloading on or off
   * @param jars List of jar files for dynamic classloading
   * @param logdir log directory
   * @throws Exception  
   */
   static public void createTemplate(Map map, String templateFile, String outputFile, boolean securityStatus, boolean dynamicLoadingStatus, String [] jars, String logdir) throws Exception{
    try {
	    Vector dummy = new Vector();
	    
	    String arguments[] = {"-h", "data.all", "-o", outputFile, "-t", templateFile};
	    
	    for (int i = 0; i < arguments.length; i++)
	    	dummy.add(arguments[i]);
	    
	    if (securityStatus & dynamicLoadingStatus){
		dummy.add("-s");	
		dummy.add("-d");	
	    }
	    else if (dynamicLoadingStatus) {
		dummy.add("-d");	
	    }
	    else if (securityStatus){
		dummy.add("-s");	
	    }
	    
	    String args [] = (String [])dummy.toArray(new String [5]);
	    logDir = logdir;    
	    FileGen.createFile(map);  
	    createDescription(args, jars);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  } 
 
  /** Runs the TemplateGen and generates the description file.
   * 
   * @param args The input command line.
   * @param jars List of jar files for dynamic classloading  
   */
  static void createDescription(String [] args, String [] jars) throws Exception{
    try {
	    readOptions(args);
	    
	    if (dynamicLoadingOn) {
		    if ( jars == null) 
		   	throw new Exception("Httpserver or remote classes are missing for dynamic class loading");
		    else {
		//	addhttpServer(hostname);
      	    		addJars(jars);
		    }
	    }
	   TemplateGen result = new TemplateGen(args);
	   
	   
    } catch (Exception e) {
      	    System.out.println(e.getMessage());
    } finally {
	    File dataFile = new File("data.all");
	    dataFile.delete();
	    tempFile.renameTo(file);
    }
  }

  /** Sets the httpserver hostname for dynamic classloading.
   * 
   * @param hostname Httpserver hostname for dynamic classloading
   */
  static void addhttpServer(String hostname) {
    try {
	    httpServer = hostname;
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
 
  /** Sets the jar files for dynamic classloading.
   * 
   * @param jars List of jar files for dynamic classloading  
   */
  static void addJars(String [] jars) {
    try {
	    for (int i = 0; i <= jars.length; i++) {
	    	httpJars.add(jars[i]);
	    //	Jars = Jars + " http://"+httpServer+":8080/" + jars[i];
	    }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  } 

  static public void main(String[] args) {
    try {
      TemplateGen result = new TemplateGen(args);
      tempFile.renameTo(file);
      //result.dump(System.out);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
} 
