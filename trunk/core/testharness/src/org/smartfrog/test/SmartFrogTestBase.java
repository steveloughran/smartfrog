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
package org.smartfrog.test;

import junit.framework.TestCase;
import org.smartfrog.SFSystem;
import org.smartfrog.services.assertions.SmartFrogAssertionException;
import org.smartfrog.sfcore.common.ConfigurationDescriptor;
import org.smartfrog.sfcore.common.MessageKeys;
import org.smartfrog.sfcore.common.MessageUtil;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogInitException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.common.SmartFrogParseException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.parser.Phases;
import org.smartfrog.sfcore.parser.SFParser;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.security.SFClassLoader;
import org.smartfrog.sfcore.security.SFGeneralSecurityException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

/**
 * A base class for smartfrog tests
 * @author steve loughran
 * created 17-Feb-2004 17:08:35
 */

public abstract class SmartFrogTestBase extends TestCase {
    /**
     * cached directory of classes
     */
    protected File classesDir;
    protected String hostname;

    /**
     * Smartforg assertion.
     * Value: {@value}
     */
    private static final String EXCEPTION_SMARTFROG_ASSERTION = "SmartFrogAssertionException";

    /**
     * Text to look for in classname when seeking a resolution exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_RESOLUTION = "SmartFrogResolutionException";

    /**
     * Text to look for in classname when seeking a type resolution exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_TYPERESOLUTION = "SmartFrogTypeResolutionException";

    /**
     * Text to look for in classname when seeking a place resolution exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_PLACERESOLUTION = "SmartFrogPlaceResolutionException";

    /**
     * Text to look for in classname when seeking a link resolution exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_LINKRESOLUTION = "SmartFrogLinkResolutionException";

    /**
     * Text to look for in classname when seeking afunction  resolution exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_FUNCTIONRESOLUTION = "SmartFrogFunctionResolutionException";

    /**
     * Text to look for in classname when seeking a assertion resolution exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_ASSERTIONRESOLUTION = "SmartFrogAssertionResolutionException";


    /**
     * Text to look for in classname when seeking a lifecycle exception.
     * Value: {@value}
     */
    public static final String EXCEPTION_LIFECYCLE = "SmartFrogLifecycleException";
    /**
     * Text to look for in classname when seeking a SmartFrogException.
     * Value: {@value}
     */
    public static final String EXCEPTION_SMARTFROG = "SmartFrogException";
    /**
     * Text to look for in classname when seeking a liveness exception.
     * Value: {@value}
     */

    public static final String EXCEPTION_LIVENESS = "SmartFrogLivenessException";

    /**
     * Text to look for in classname when seeking a SmartFrogDeploymentException.
     * Value: {@value}
     */

    public static final String EXCEPTION_DEPLOYMENT = "SmartFrogDeploymentException";

    /**
     * Text to look for in classname when seeking a ClassCastException.
     * Value: {@value}
     */
    public static final String EXCEPTION_CLASSCAST = "java.lang.ClassCastException";

    /**
     * Text to look for in classname when seeking a ClassCastException.
     * Value: {@value}
     */
    public static final String EXCEPTION_CLASSNOTFOUND = "java.lang.ClassNotFoundException";


    /**
     * Text to look for in classname when seeking a SmartFrogParseException.
     * Value: {@value}
     */
    public static final String EXCEPTION_PARSE = "SmartFrogParseException";

    /**
     * Text to look for in classname when seeking a
     * SmartFrogCompileResolutionException.
     * Value: {@value}
     */
    public static final String EXCEPTION_COMPILE_RESOLUTION = "SmartFrogCompileResolutionException";
    /**
     * This is an application that will be undeployed at teardown time
     */
    protected Prim application;

    /**
     * Construct the base class, extract hostname and test classes directory from the JVM
     * paramaters -but do not complain if they are missing
     * @param name
     */
    protected SmartFrogTestBase(String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        hostname = TestHelper.getTestProperty(TestHelper.HOSTNAME, "localhost");
        String classesdirname = TestHelper.getTestProperty(TestHelper.CLASSESDIR, null);
        if (classesdirname != null) {
            classesDir = new File(classesdirname);
        }
    }

    /**
     * get a file name relative to the classes dir directory
     * @param filename
     * @return
     */
    protected File getRelativeFile(String filename) {
        if(classesDir!=null) {
            return new File(classesDir,filename);
        } else {
            return new File(filename);
        }
    }

    /**
     * Deploy a component, expecting a smartfrog exception.
     * @param testURL   URL to test
     * @param appName   name of test app
     * @param exceptionName name of the exception thrown
     * @param searchString string which must be found in the exception message
     * @throws RemoteException in the event of remote trouble.
     */
    protected Throwable deployExpectingException(String testURL,
                                                 String appName,
                                                 String exceptionName,
                                                 String searchString) throws RemoteException,
            SmartFrogException, SFGeneralSecurityException,
            UnknownHostException {
        return deployExpectingException(testURL,
                appName,
                exceptionName,
                searchString,
                null,
                null);
    }


    /**
     * Deploy a component, expecting a smartfrog exception. You can
     * also specify the classname of a contained fault -which, if specified,
     * must be contained, and some text to be searched for in this exception.
     * @param testURL   URL to test
     * @param appName   name of test app
     * @param exceptionName name of the exception thrown (can be null)
     * @param searchString string which must be found in the exception message (can be null)
     * @param containedExceptionName optional classname of a contained
     * exception; does not have to be the full name; a fraction will suffice.
     * @param containedExceptionText optional text in the contained fault.
     * @throws RemoteException in the event of remote trouble.
     * @return the exception that was returned
     */
    protected Throwable deployExpectingException(String testURL,
                                                 String appName,
                                                 String exceptionName,
                                                 String searchString,
                                                 String containedExceptionName,
                                                 String containedExceptionText) throws SmartFrogException,
            RemoteException, UnknownHostException, SFGeneralSecurityException {

        startSmartFrog();
        ConfigurationDescriptor cfgDesc =
                createDeploymentConfigurationDescriptor(appName, testURL);

        Object deployedApp = null;
        Throwable resultException = null;
        try {
            //Deploy and don't throw exception. Exception will be contained
            // in a ConfigurationDescriptor.
            deployedApp = SFSystem.runConfigurationDescriptor(cfgDesc,false);
            if ((deployedApp instanceof ConfigurationDescriptor) &&
                    (((ConfigurationDescriptor) deployedApp).resultException != null)) {
                searchForExpectedExceptions(deployedApp, cfgDesc, exceptionName,
                        searchString, containedExceptionName,containedExceptionText);
                resultException = ((ConfigurationDescriptor) deployedApp).resultException;
                return resultException;
            } else {
                //clean up
                if(deployedApp instanceof Prim) {
                    terminateApplication((Prim)deployedApp);
                }
                //then fail
                fail("We expected an exception here:"
                        +(exceptionName!=null?exceptionName:"(anonymous)")
                     +" but got this deployment "+deployedApp.toString());
            }
         } catch (Exception fault) {
            fail(fault.toString());
         }
        return null;
    }

    private void searchForExpectedExceptions(Object deployedApp, ConfigurationDescriptor cfgDesc, String exceptionName, String searchString,
                                             String containedExceptionName,
                                             String containedExceptionText) {
        //we got an exception. let's take a look.
        Throwable returnedFault;
        returnedFault = ((ConfigurationDescriptor) deployedApp).resultException;
        assertFaultCauseAndTextContains(returnedFault, exceptionName, searchString, cfgDesc);
        //get any underlying cause
        Throwable cause = returnedFault.getCause();
        assertFaultCauseAndTextContains(cause, containedExceptionName, containedExceptionText, cfgDesc);
    }

    /**
     * assert that something we deployed contained the name and text we wanted.
     * @param cause root cause. Can be null, if faultName and faultText are also null. It is an error if they are defined
     * and the cause is null
     * @param faultName substring that must be in the classname of the fault (can be null)
     * @param faultText substring that must be in the text of the fault (can be null)
     * @param cfgDesc what we were deploying; the status string is extracted for reporting purposes
     */
    private void assertFaultCauseAndTextContains(Throwable cause, String faultName,
                                                 String faultText, ConfigurationDescriptor cfgDesc) {
        String details = cfgDesc.statusString();
        assertFaultCauseAndTextContains(cause, faultName, faultText, details);
    }

    /**
     *
     /**
     * assert that something we deployed contained the name and text we wanted.
     * @param cause root cause. Can be null, if faultName and faultText are also null. It is an error if they are defined
     * and the cause is null
     * @param faultName substring that must be in the classname of the fault (can be null)
     * @param faultText substring that must be in the text of the fault  (can be null)
     * @param details status string for reporting purposes
     */
    protected void assertFaultCauseAndTextContains(Throwable cause, String faultName,
                                                   String faultText, String details) {
        //if we wanted the name of a fault
        if (faultName != null) {
            //then look for the name of contained exception and see it matches what was
            // asked for
            assertNotNull("expected throwable of type "
                    + faultName,
                    cause);
            //verify the name
            assertThrowableNamed(cause,
                    faultName,
                    details);
        }
        //look for the exception text
        if (faultText != null) {
            assertNotNull("expected throwable containing text "
                    + faultText,
                    cause);

            assertContains(cause.toString(),
                    faultText,
                    details,
                    extractDiagnosticsInfo(cause));
        }
    }



    private ConfigurationDescriptor createDeploymentConfigurationDescriptor(String appName, String testURL)
            throws SmartFrogInitException {
        return new ConfigurationDescriptor(appName
                                               , testURL,
                         ConfigurationDescriptor.Action.DEPLOY
                                               , hostname
                                               , null);
    }

    /**
     * assert that a throwable's classname is of a given type/substring
     * @param thrown what was thrown
     * @param name the name of the class
     * @param cfgDescMsg description (can be null)
     */
    public void assertThrowableNamed(Throwable thrown,String name, String cfgDescMsg) {
        assertContains(thrown.getClass().getName(),
                name, cfgDescMsg, recursiveDump(thrown));
    }

    /**
     * Do a recursive dump of what is going wrong
     * @param thrown
     * @return
     */
    private String recursiveDump(Throwable thrown) {
        StringBuffer dump=new StringBuffer();
        String info = extractDiagnosticsInfo(thrown);
        dump.append(info);
        Throwable nested = thrown.getCause();
        if (nested != null && nested!=thrown) {
            dump.append(recursiveDump(nested));
        }
        return dump.toString();
    }


    /**
    * extract as much info as we can from a throwable.
    * @param thrown what was thrown
    * @return a string describing the throwable; includes a stack trace
    */
    protected String extractDiagnosticsInfo(Throwable thrown) {
        StringBuffer buffer=new StringBuffer();
        thrown.getStackTrace();
        buffer.append("Message:  ");
        buffer.append(thrown.getMessage());
        buffer.append('\n');
        buffer.append("Class:    ");
        buffer.append(thrown.getClass().getName());
        buffer.append('\n');
        buffer.append("Stack:    ");
        StackTraceElement[] stackTrace = thrown.getStackTrace();
        for(int i=0;i<stackTrace.length;i++) {
            StackTraceElement frame=stackTrace[i];
            buffer.append(frame.toString());
            buffer.append('\n');
        }
        return buffer.toString();
    }

    /**
     * assert that a string contains a substring
     * @param source source to scan
     * @param substring string to look for
     * @param cfgDescMsg configuration description
     * @param extraText any extra text, can be null
     */
    public static void assertContains(String source, String substring, String cfgDescMsg,String extraText) {
        assertNotNull("No string to look for ["+substring+"]",source);
        assertNotNull("No substring ", substring);
        final boolean contained = source.indexOf(substring)>=0;

        if(!contained) {
            String message = "- Did not find ["+substring+"] in ["+source+"]"+
                (cfgDescMsg!=null?("\n, Result:"+cfgDescMsg):"");
            System.out.println(message);
            if (extraText != null) {
                System.out.println(extraText);
            }
            //fail(message+ (extraText!=null?("\n"+extraText):""));
            fail(message);
        }
    }


    /**
     * assert that a string contains a substring
     * @param source source to scan
     * @param substring string to look for
     */
    public static void assertContains(String source, String substring) {
       assertContains(source,substring,"",null);
    }


    public File getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(File classesDir) {
        this.classesDir = classesDir;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * start smartfrog if it isnt already
     *
     * @throws SmartFrogException
     * @throws RemoteException
     * @throws SFGeneralSecurityException
     * @throws UnknownHostException
     */
    public void startSmartFrog() throws SmartFrogException, RemoteException, SFGeneralSecurityException, UnknownHostException {
        if(!SFSystem.isSmartfrogInit()) {
            SFSystem.runSmartFrog();
        }
    }

    public void terminateSmartFrog() throws SmartFrogException, RemoteException, SFGeneralSecurityException, UnknownHostException {
       SFSystem.runConfigurationDescriptor(new ConfigurationDescriptor(":TERMINATE:::localhost:"));
    }

    /**
     * Deploys an application and returns the refence to deployed application.
     * @param testURL  URL to test
     * @param appName  Application name
     * @return Reference to deployed application
     * @throws RemoteException in the event of remote trouble.
     */
    protected Prim deployExpectingSuccess(String testURL, String appName)
                                                    throws Throwable {

        try {
            Object deployedApp = deployApplication(appName, testURL);

            if (deployedApp instanceof Prim) {
                return ((Prim) deployedApp);
            } else if (deployedApp instanceof ConfigurationDescriptor) {
                System.out.println("\n    "
                            +"* ERROR IN: Test success in description: \n" +
                             "        "
                            +((ConfigurationDescriptor)deployedApp).toString("\n        ")
                               );
                Throwable exception = ((ConfigurationDescriptor)deployedApp).
                        resultException;
                if (exception!=null) {
                    throw exception;
                }
            }
        } catch (Throwable throwable) {
            logThrowable("thrown during deployment",throwable);
            throw throwable;
        }
        fail("something odd came back");
        //fail throws a fault; this is here to keep the compiler happy.
        return null;
    }


    /**
     * internal helper to test stuff
     * @param appName
     * @param testURL
     * @return
     * @throws SmartFrogException
     * @throws RemoteException
     * @throws SFGeneralSecurityException
     * @throws UnknownHostException
     */
    private Object deployApplication(String appName, String testURL) throws SmartFrogException, RemoteException,
            SFGeneralSecurityException, UnknownHostException {
        startSmartFrog();
        ConfigurationDescriptor cfgDesc =
                new ConfigurationDescriptor(appName,
                        testURL,
                        ConfigurationDescriptor.Action.DEPLOY,
                        hostname,
                        null);
        System.out.println("\n"+"* Testing for successful deployment of: \n    "+cfgDesc.toString("\n    ")
                               );

        Object deployedApp = SFSystem.runConfigurationDescriptor(cfgDesc,true);
        return deployedApp;
    }

    /**
     * log a chained exception if there is one; do nothing if not.
     * There because JUnit 3.8.1 is not aware of chaining (yet), presumably
     * through a need to work with pre1.4 stuff
     * @param throwable
     */
    protected void logChainedException(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if(cause!=null) {
            logThrowable("nested fault in "+throwable,cause);
        }
    }

    /**
     * log a throwable
     *
     * @param thrown
     */
    public void logThrowable(String message, Throwable thrown) {
        String info = extractDiagnosticsInfo(thrown);
        System.err.println(message);
        System.err.println(info);
        Throwable nested = thrown.getCause();
        if (nested != null) {
            logThrowable("nested fault:" , nested);
        }
    }

    /**
     * parse a file whose filename is resolved before parsing parsed.
     * @param filename the name of a file, relative to the classes.dir passed in
     * to the test JVM.
     * @throws SmartFrogException
     * @return a parsed file
     */
    protected Phases parseLocalFile(String filename) throws SmartFrogException {
        File file=getRelativeFile(filename);
        return parse(file);
    }

    /**
     * parse a smartfrog file; throw an exception if something went wrong
     * @param file
     * @throws SmartFrogException
     */
    protected Phases parse(File file) throws SmartFrogException {
        String fileUrl;
        try {
            fileUrl = file.toURL().toString();
        } catch (MalformedURLException e) {
            String msg = MessageUtil.
                    formatMessage(MessageKeys.MSG_URL_TO_PARSE_NOT_FOUND,
                            file.toString());
            throw new SmartFrogParseException(msg);
        }

        return parse(fileUrl);
    }

     /**
     * parse a smartfrog file; throw an exception if something went wrong
      * The language of the parser is determined from the file extension; if there
      * is none, then 'sf' is assumed.
     * @param resource resource to parse
     * @throws SmartFrogException
     */
    protected Phases parse(String resource) throws SmartFrogException {
        Phases phases=null;
        InputStream is=null;
        try {
            is = SFClassLoader.getResourceAsStream(resource);
            if (is == null) {
                String msg = MessageUtil.
                        formatMessage(MessageKeys.MSG_URL_TO_PARSE_NOT_FOUND, resource);
                throw new SmartFrogParseException(msg);
            }
            String extension = determineLanguage(resource);
            phases = (new SFParser(extension)).sfParse(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException swallowed) {

                }
            }
        }
        return phases;
    }

    /**
     * work out the language of a file by getting the extension
     * @param filename
     * @return the extension; default is .sf
     */
    protected String determineLanguage(String filename) {
        String extension="sf";
        int index=filename.lastIndexOf('.');
        if(index>=0) {
            extension=filename.substring(index+1);
        }
        return extension;
    }

    /**
     * deploy something from this directory; expect an exception
     * @param filename
     * @param appname
     * @throws Throwable
     */
    public Throwable deployExpectingAssertionFailure(String filename, String appname) throws Throwable {
        startSmartFrog();
        ConfigurationDescriptor cfgDesc =
                createDeploymentConfigurationDescriptor(appname, filename);
        Object deployedApp = null;
        Throwable resultException = null;
        try {
            //Deploy and don't throw exception. Exception will be contained
            // in a ConfigurationDescriptor.
            deployedApp = SFSystem.runConfigurationDescriptor(cfgDesc,false);
            if ((deployedApp instanceof ConfigurationDescriptor) &&
                    (((ConfigurationDescriptor) deployedApp).resultException != null)) {
                searchForExpectedExceptions(deployedApp, cfgDesc, EXCEPTION_LIFECYCLE,
                        null, EXCEPTION_SMARTFROG_ASSERTION,null);
                resultException = ((ConfigurationDescriptor) deployedApp).resultException;
                return resultException;
            } else {
                //here we deploy the application
                Prim application=(Prim)deployedApp;
                try {
                    application.sfPing(null);
                    application.sfPing(null);
                    application.sfPing(null);
                    application.sfPing(null);
                    application.sfPing(null);
                } catch (SmartFrogLivenessException liveness) {
                    assertFaultCauseAndTextContains(liveness, EXCEPTION_LIFECYCLE,
                            null,"expected lifecycle failure");
                    assertFaultCauseAndTextContains(liveness.getCause(),
                            EXCEPTION_SMARTFROG_ASSERTION, null,
                            "expected nested assertion failure");
                } finally {
                    terminateApplication(application);
                }
            }

         } catch (Exception fault) {
            fail(fault.toString());
         }
        return null;
    }

    /**
     * recursive search for the root cause
     *
     * @param throwable
     * @return the assertion or null
     */
    public SmartFrogAssertionException extractAssertionException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        if (throwable instanceof SmartFrogAssertionException) {
            return (SmartFrogAssertionException) throwable;
        }
        return extractAssertionException(throwable.getCause());
    }

    /**
     * terminate a named application.
     * If the application parameter is null or refers to a nonexistent node, nothing happens.
     *
     * @param application; can be null
     * @throws java.rmi.RemoteException on network trouble other than an already terminated app
     */
    public void terminateApplication(Prim application) throws RemoteException {
        if(application==null) {
            return ;
        }
        Reference name;
        try {
            name = application.sfCompleteName();
        } catch (RemoteException e) {
            name = null;
        }
        try {
            application.sfDetachAndTerminate(TerminationRecord.normal(name));
        } catch (NoSuchObjectException ignore) {
            //app is already terminated, do not fail a test.
        }
    }

    /**
     * get an  attribute from an application
     *
     * @param target
     * @param attribute
     * @return
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static Object resolveAttribute(Prim target, String attribute)
            throws SmartFrogResolutionException, RemoteException {
        assertNotNull("Target parameter is null", target);
        assertNotNull("Attribute parameter is null",attribute);
        Object value = target.sfResolve(attribute, false);
        assertNotNull("Expected non-null "+describe(target, attribute),
                value);
        return value;
    }

    /**
     * Create a description of a target/attribute pair
     * @param target
     * @param attribute
     * @return
     */
    private static String describe(Prim target,String attribute) {
        try {
            return "attribute " + attribute + " on " + target.sfCompleteName();
        } catch (RemoteException e) {
            return "attribute '" + attribute + "' on an unresponsive component";
        }
    }

    /**
     * get a string attribute from an application
     * @param prim
     * @param attribute
     * @return the attribute as a string
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static String resolveStringAttribute(Prim prim,String attribute)
            throws SmartFrogResolutionException, RemoteException {
        Object value = resolveAttributeWithTypeAssertion(prim,
                attribute,
                String.class);
        return (String) value;
    }

    /**
     * Resolve an attribute and make an assertion about the return type
     * @param prim app to resolve against
     * @param attribute attribute to resolve
     * @param expectedClass class that is required
     * @return the object
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static Object resolveAttributeWithTypeAssertion(Prim prim,
                                                    String attribute,
                                                    Class expectedClass)
            throws SmartFrogResolutionException, RemoteException {
        assertNotNull(expectedClass);
        Object value = resolveAttribute(prim,attribute);
        Class valueClass = value.getClass();
        assertEquals("Expected "+ describe(prim, attribute)
            +" to be of type "+expectedClass+" but instead it is an instance of class "
            +valueClass.toString()+" with value "+value.toString(),
                expectedClass,valueClass);
        return value;
    }

    /**
     * assert that an attribute exists
     *
     * @param prim
     * @param attribute
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static void assertAttributeExists(Prim prim, String attribute)
            throws SmartFrogResolutionException, RemoteException {
        resolveAttribute(prim,attribute);
    }

    /**
     * assert that an attribute exists
     * @param prim
     * @param attribute
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static void assertStringAttributeExists(Prim prim,String attribute)
            throws SmartFrogResolutionException, RemoteException {
        assertAttributeExists(prim,attribute);
    }

    /**
     * assert that an attribute exists and equals a specified value
     * @param prim
     * @param attribute
     * @param mustEqual
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static void assertAttributeEquals(Prim prim, String attribute,String mustEqual)
            throws SmartFrogResolutionException, RemoteException {
        String value=resolveStringAttribute(prim, attribute);
        assertEquals("Unequal "+ describe(prim, attribute),mustEqual,value);
    }

    /**
     * assert that an attribute exists and equals a specified value
     *
     * @param prim
     * @param attribute
     * @param mustEqual
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     * @throws AssertionError if a condition is not met
     */
    public static void assertAttributeEquals(Prim prim, String attribute,
                                      Object mustEqual) throws SmartFrogResolutionException,
            RemoteException {
        Object value = resolveAttribute(prim, attribute);
        assertEquals("Unequal " + describe(prim, attribute), mustEqual, value);
    }

    /**
     * Assert the value of a boolean attribute
     * @param prim
     * @param attribute
     * @param expected
     * @throws SmartFrogResolutionException
     * @throws RemoteException
     */
    public static void assertAttributeEquals(Prim prim, String attribute,
                                      boolean expected) throws SmartFrogResolutionException,
            RemoteException {
        Object value = resolveAttributeWithTypeAssertion(prim, attribute,Boolean.class);
        assertEquals("Unequal " + describe(prim, attribute),expected,((Boolean)value).booleanValue());
    }

    /**
     * assert that a property is defined
     * @param property
     */
    public static void assertSystemPropertySet(String property) {
        String value=TestHelper.getTestProperty(property,null);
        assertNotNull("Property not set: "+property, value);
    }

    /**
     * assert that an application is alive;
     * @param application
     * @throws AssertionError if something failed, wrapping the underlying exception
     */
    protected static void assertLivenessSuccess(Prim application) {
        assertNotNull("Application is null",application);
        try {
            application.sfPing(null);
        } catch (SmartFrogLivenessException e) {
            throw new AssertionError(e);
        } catch (RemoteException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * assert that an application is alive;
     * @param application application
     * @param count number of times to check
     * @throws AssertionError if something failed, wrapping the underlying exception
     */
    protected static void assertLivenessSuccess(Prim application,int count) {
        for(int i=0;i<count;i++) {
            assertLivenessSuccess(application);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        terminateApplication(application);
    }
}
