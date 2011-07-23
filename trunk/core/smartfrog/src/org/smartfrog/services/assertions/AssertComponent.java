/** (C) Copyright 2004-2007 Hewlett-Packard Development Company, LP
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
package org.smartfrog.services.assertions;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.workflow.conditional.Condition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.Locale;


/**
 * created 28-Apr-2004 11:40:53
 */
public class AssertComponent extends PrimImpl implements Condition, Assert {

    private static final String ERROR_VECTOR = "Vector too small, expected ";
    private static final String ERROR_COULD_NOT_RESOLVE_ATTRIBUTE = "Could not resolve attribute ";

    /**
     * Constructor .
     *
     * @throws RemoteException In case of network/rmi error
     */
    public AssertComponent() throws RemoteException {
    }

    /**
     * {@inheritDoc}
     *
     * @return true if all the assertions evaluate true
     *
     * @throws RemoteException
     * @throws SmartFrogException
     */
    @Override
    public boolean evaluate() throws RemoteException, SmartFrogException {
        String result = check();
        if (result != null) {
            sfLog().debug(result);
        }
        return result == null;
    }

    /**
     * Evaluate the condition.
     *
     * @return true if it is successful, false if not
     *
     * @throws RemoteException for network problems
     * @throws SmartFrogException for any other problem
     */
    public String check() throws RemoteException, SmartFrogException {
        boolean isTrue = sfResolve(ATTR_IS_TRUE, true, false);
        boolean isFalse = sfResolve(ATTR_IS_FALSE, false, false);
        boolean equalityIgnoresCase = sfResolve(ATTR_EQUALITY_IGNORES_CASE,
                false,
                true);
        String evaluatesTrue = sfResolve(ATTR_EVALUATES_TRUE,
                (String) null,
                false);
        String evaluatesFalse = sfResolve(ATTR_EVALUATES_FALSE, (String) null,
                false);
        String attribute = sfResolve(Assert.ATTR_HAS_ATTRIBUTE,
                (String) null,
                false);
        String attributeEquals = sfResolve(Assert.ATTR_ATTRIBUTE_EQUALS,
                (String) null,
                false);

        String attributeVectorValue = sfResolve(Assert.ATTR_VECTOR_VALUE,
                (String) null,
                false);
        Integer attributeVectorIndex = (Integer) sfResolve(ATTR_VECTOR_INDEX,
                (Integer) null, false);

        Integer attributeVectorMinLength = (Integer) sfResolve(
                ATTR_VECTOR_MIN_LENGTH,
                (Integer) null,
                false);
        Integer attributeVectorMaxLength = (Integer) sfResolve(
                ATTR_VECTOR_MAX_LENGTH,
                (Integer) null,
                false);
        if (!isTrue) {
            return ATTR_IS_TRUE + " evaluates to false";
        }
        if (isFalse) {
            return ATTR_IS_FALSE + " evaluates to true";
        }

        boolean referenceRequired = sfResolve(ATTR_REFERENCE_REQUIRED, true, true);
        Prim prim;
        try {
            prim = sfResolve(ATTR_REFERENCE, (Prim) null, false);
        } catch (SmartFrogResolutionException e) {
            //the reason we ignore this is to handle lazy resolution
            //by ignoring it.
            prim = null;
            if(referenceRequired) {
                return "The reference does not resolve " + e;
            }
        }
        if (prim == null) {
            if (referenceRequired) {
                //there was no reference
                return "referenceRequired is true but there is no reference value";
            }
        } else {
            if (evaluatesTrue != null) {
                if (!evaluate(prim, evaluatesTrue)) {
                    return "Evaluated to false: " + prim.sfCompleteName() + '.' + evaluatesTrue;
                }
            }

            if (evaluatesFalse != null) {
                if (evaluate(prim, evaluatesFalse)) {
                    return "Evaluated to true: " + prim.sfCompleteName() + '.' + evaluatesFalse;
                }
            }

            if (attribute != null) {
                //look for a named attribute existing
                Object resolved = prim.sfResolve(attribute, false);
                if (resolved == null) {
                    return ERROR_COULD_NOT_RESOLVE_ATTRIBUTE + attribute + " of " + prim;
                }
                if (attributeEquals != null) {
                    //do string match if needed
                    String attrValue = resolved.toString();
                    if (!equal(attributeEquals,
                            attrValue,
                            equalityIgnoresCase)) {
                        return " Expected <" + attributeEquals + "> actual <" + attrValue + '>';
                    }
                } else {
                    boolean needsVector = attributeVectorMinLength != null
                            || attributeVectorIndex != null
                            || attributeVectorMaxLength != null;
                    Vector<?> v;
                    int length;
                    if (needsVector) {
                        if (!(resolved instanceof Vector)) {
                            return "Not a list " + resolved;
                        }
                        v = (Vector<?>) resolved;
                        length = v.size();
                        if (attributeVectorMinLength != null &&
                                length < attributeVectorMinLength) {
                            return "List too short: needs to be " + attributeVectorMinLength
                                    + " elements long, not " + length;
                        }
                        if (attributeVectorMaxLength != null &&
                                length < attributeVectorMaxLength) {
                            return "List too long: maximum is" + attributeVectorMaxLength
                                    + " not " + length;
                        }

                        if (attributeVectorIndex != null) {
                            //vector element
                            int index = attributeVectorIndex.intValue();
                            if (index >= length) {
                                //vector was too small, complain.
                                return ERROR_VECTOR
                                        + index + " elements, found "
                                        + v.size() + " in " + v.toString();
                            }
                            Object vectorValue = v.elementAt(index);
                            String actualVectorValue = vectorValue.toString();
                            if (attributeVectorValue != null
                                    && !equal(attributeVectorValue,
                                    actualVectorValue,
                                    equalityIgnoresCase)) {
                                return " Expected <" + attributeVectorValue + "> actual <" + actualVectorValue + '>';
                            }
                        }
                    }
                }
            }
        }

        //file existence check
        String filename = sfResolve(ATTR_FILE_EXISTS, (String) null, false);
        if (filename != null) {
            File file = new File(filename);
            if (!file.exists()) {
                return "File not found:" + filename;
            }
            if (!file.isFile()) {
                return "Not a file:" + filename;
            }
        }

        //directory existence
        filename = sfResolve(ATTR_DIR_EXISTS, (String) null, false);
        if (filename != null) {
            File dir = new File(filename);
            if (!dir.exists()) {
                return "Directory not found:" + filename;
            }
            if (!dir.isDirectory()) {
                return "Not a directory:" + filename;
            }
        }

        String equals1;
        String equals2;
        Object e1 = sfResolve(Assert.ATTR_EQUALS_STRING1, (Object) null, false);
        equals1 = e1 == null ? null : e1.toString();
        Object e2 = sfResolve(Assert.ATTR_EQUALS_STRING2, (Object) null, false);
        equals2 = e2 == null ? null : e2.toString();
        if (equals1 != null) {
            if (equals2 == null) {
                return "Undefined attribute: " + ATTR_EQUALS_STRING2;
            }
            if (!equal(equals1, equals2, equalityIgnoresCase)) {
                return "Expected \"" + equals1 + "\""
                        + " (class:" + e1.getClass().getName() + ")"
                        + " actual \"" + equals2 + '\"'
                        + " (class:" + e2.getClass().getName() + ")";
            }
        } else {
            if (equals2 != null) {
                return "Undefined attribute: " + ATTR_EQUALS_STRING1;
            }

        }
        //we get here, with no message to return
        return null;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * verify that assertions are valid.
     *
     * @throws RemoteException In case of network/rmi error
     * @throws SmartFrogAssertionException error in verification
     * @throws SmartFrogException error in verification
     */
    public void checkAssertions()
            throws RemoteException, SmartFrogException {
        String result = check();
        if (result != null) {
            throw new SmartFrogAssertionException(createAssertionMessage(result));
        }
    }

    /**
     * make an assertion if two strings are equal
     *
     * @param equals1             string 1
     * @param equals2             string 2
     * @param equalityIgnoresCase flag to ignore case while comparing
     *
     * @throws SmartFrogAssertionException error in assertion
     */
    private void assertEqualStrings(String equals1, String equals2,
                                    boolean equalityIgnoresCase)
            throws SmartFrogAssertionException {
        boolean fact = equal(equals1, equals2, equalityIgnoresCase);
        assertTrue(fact,
                equals2 + " equals " + equals1);
    }

    /**
     * Test for two strings being equal
     * @param equals1             string 1
     * @param equals2             string 2
     * @param equalityIgnoresCase flag to ignore case while comparing
     * @return true iff the two strings are equal
     */
    private boolean equal(String equals1,
                          String equals2,
                          boolean equalityIgnoresCase) {
        if (equalityIgnoresCase) {
            equals1 = equals1.toLowerCase(Locale.ENGLISH);
            equals2 = equals2 != null ? equals2.toLowerCase(Locale.ENGLISH) : null;
        }
        return equals1.equals(equals2);
    }


    /**
     * try and resolve a reference, return null if there was some kind of
     * failure including lazy references not yet ready.
     *
     * @return Prim the reference or null
     *
     * @throws RemoteException In case of network/rmi error
     */
    private Prim maybeResolveReference() throws RemoteException {
        Reference reference = new Reference();
        Prim prim = null;
        try {
            reference = sfResolve(ATTR_REFERENCE, reference, false);
            if (reference == null) {
                //there was no reference
                return null;
            }
            prim = sfResolve(reference, (Prim) null, false);
        } catch (SmartFrogResolutionException ignore) {
            //the reason we ignore this is to handle lazy resolution
            //by ignoring it.
        }
        return prim;
    }

    /**
     * make an assertion
     *
     * @param fact result flag
     * @param test test String
     *
     * @throws SmartFrogAssertionException error in assertion
     */
    protected void assertTrue(boolean fact, String test)
            throws SmartFrogAssertionException {
        if (!fact) {
            throw new SmartFrogAssertionException(createAssertionMessage(test));
        }
    }

    /**
     * get the failure message. This is done by attempting to resolve the
     * message, falling back to a declared one if there is no declared message,
     * or the resolution process failed.
     *
     * @param test test message
     *
     * @return String
     */
    private String createAssertionMessage(String test) {
        String message = test;
        try {
            message = sfResolve(ATTR_MESSAGE + " ("+ test + ")", message, false);
        } catch (SmartFrogResolutionException ignore) {

        } catch (RemoteException ignore) {

        }
        return message;
    }

    /**
     * evaluate a named method on an object; expect it to return a boolean
     *
     * @param target     object to invoke
     * @param methodName name of method
     *
     * @return the boolean value of the invocation
     *
     * @throws SmartFrogException error in evaluation
     * @throws RemoteException In case of network/rmi error
     */
    public boolean evaluate(Object target, String methodName)
            throws SmartFrogException, RemoteException {
        try {

            Class clazz = target.getClass();
            Class[] params = new Class[0];
            Method method = clazz.getMethod(methodName, params);
            Object result = null;
            result = method.invoke(target, (Object[]) null);

            if (!(result instanceof Boolean)) {
                throw new SmartFrogAssertionException("method " + methodName
                        + " is not boolean");
            }

            Boolean b = (Boolean) result;

            return b.booleanValue();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RemoteException) {
                throw (RemoteException) (e.getCause());
            }

            if (e.getCause() instanceof SmartFrogException) {
                throw (SmartFrogException) (e.getCause());
            }

            String errorText = "invoking " + methodName + " on " + target;
            throw new SmartFrogAssertionException(errorText, e);
        } catch (SmartFrogAssertionException e) {
            throw e;
        } catch (Exception e) {
            String errorText = "invoking " + methodName + " on " + target;
            throw new SmartFrogAssertionException(errorText, e);
        }
    }

    /**
     * Liveness call in to check if this component is still alive.
     *
     * @param source source of call
     *
     * @throws SmartFrogLivenessException component is terminated
     * @throws RemoteException In case of network/rmi error
     */
    @Override
    public void sfPing(Object source) throws SmartFrogLivenessException,
            RemoteException {
        super.sfPing(source);

        try {
            boolean checkOnLiveness = sfResolve(ATTR_CHECK_ON_LIVENESS,
                    true,
                    false);
            if (checkOnLiveness) {
                checkAssertions();
            }
        } catch (RemoteException e) {
            throw new SmartFrogLivenessException(e);
        } catch (SmartFrogLivenessException e) {
            throw e;
        } catch (SmartFrogException e) {
            throw new SmartFrogLivenessException(e);
        }
    }


    /**
     * Can be called to start components. Subclasses should override to provide
     * functionality Do not block in this call, but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException In case of network/rmi error
     */
    @Override
    public synchronized void sfStart()
            throws SmartFrogException, RemoteException {
        super.sfStart();
        boolean checkOnStartup = sfResolve(ATTR_CHECK_ON_STARTUP, true, false);
        if (checkOnStartup) {
            checkAssertions();
        }
        //Workflow integration
        new ComponentHelper(this).sfSelfDetachAndOrTerminate(null,
                "Assert",
                null,
                null);
    }
}
