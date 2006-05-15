package org.smartfrog.sfcore.languages.sf.sfreference;

import org.smartfrog.sfcore.reference.*;
import org.smartfrog.sfcore.languages.sf.sfcomponentdescription.SFComponentDescription;
import org.smartfrog.sfcore.parser.ReferencePhases;
import org.smartfrog.sfcore.common.*;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.security.SFClassLoader;

import java.util.Iterator;

/**
 * Representation of ApplyReference for the SF Language
 */
public class SFApplyReference extends SFReference implements ReferencePhases {
    protected SFComponentDescription comp;

    public SFApplyReference(SFComponentDescription comp) {
        super();
        this.comp = comp;
    }

    /**
     * Get tje run-time version of the reference
     *
     * @return the reference
     * @throws SmartFrogCompilationException
     */
    public Reference sfAsReference() throws SmartFrogCompilationException {
        ApplyReference ar = new ApplyReference(comp.sfAsComponentDescription());
        ar.setEager(getEager());
        ar.setData(getData());
        return ar;
    }

    /**
     * Returns a copy of the reference, by cloning itself and the function part
     *
     * @return copy of reference
     * @see org.smartfrog.sfcore.common.Copying
     */
    public Object copy() {
        SFApplyReference ret = (SFApplyReference) clone();

        ret.comp = (SFComponentDescription) comp.copy();

        return ret;
    }

    /**
     * Makes a clone of the reference. The inside ref holder is cloned, but the
     * contained component is NOT.
     *
     * @return clone of reference
     */
    public Object clone() {
        SFApplyReference res = (SFApplyReference) super.clone();
        res.comp = comp;
        return res;
    }

    /**
     * Checks if this and given reference are equal. Two references are
     * considered to be equal if the component they wrap are ==
     *
     * @param ref to be compared
     * @return true if equal, false if not
     */
    public boolean equals(Object ref) {
        if (!(ref instanceof SFApplyReference)) {
            return false;
        }

        return ((SFApplyReference) ref).comp == comp;

    }

    /**
     * Returns the hashcode for this reference. Hash code for reference is made
     * out of the sum of the parts hashcodes
     *
     * @return integer hashcode
     */
    public int hashCode() {
        return comp.hashCode();
    }

    /**
     * Resolves this apply reference by applying the function - unless this is data..
     *
     * @param rr    ReferenceResolver to be used for resolving this reference
     * @param index index of first referencepart to start resolving at
     * @return value found on resolving this function
     * @throws org.smartfrog.sfcore.common.SmartFrogResolutionException
     *          if reference failed to resolve
     */
    public Object resolve(ReferenceResolver rr, int index)
            throws SmartFrogResolutionException {
        //take a new context...
        //     iterate over the attributes of comp- ignoring any beginning with sf;
        //     cache sfFunctionClass attribute;
        //     resolve all non-sf attributes, if they are links
        //     if any return s LAZY object, set self to lazy and return self, otherwise update copy
        //     and invoke function with copy of CD, return result
        
        Context forFunction = new ContextImpl();
        String functionClass = null;
        Object result;
        boolean hasLazy = false;

        if (getData()) return this;

        if (rr instanceof ComponentDescription)
            comp.setParent((ComponentDescription) rr);
        else if (rr instanceof Prim)
            comp.setPrimParent((Prim) rr);

        for (Iterator v = comp.sfAttributes(); v.hasNext();) {
            Object name = v.next();

            String nameS = name.toString();
            if (nameS.startsWith("sf")) {
                if (nameS.equals("sfFunctionClass")) {
                    try {
                        functionClass = (String) comp.sfResolveHere("sfFunctionClass");
                    } catch (ClassCastException e) {
                        throw new SmartFrogResolutionException("function class is not a string", e);
                    }
                } //ignore all others named sf*
            } else {
                Object value = comp.sfResolve(new Reference(ReferencePart.here(name)));

                if (value instanceof Reference && !((Reference) value).getData()) {
                    hasLazy = true;
                } else {
                    try {
                        comp.sfReplaceAttribute(name, value);
                        forFunction.sfAddAttribute(name, value);
                    } catch (SmartFrogContextException e) {
                        //shouldn't happen
                    } catch (SmartFrogRuntimeException e) {
                        //shouldn't happen
                    }
                }
            }
        }
        if (functionClass == null) {
            throw new SmartFrogResolutionException("unknown function class ");
        }
        if (hasLazy) {
            setEager(false);
            return this;
        } else {
            try {
                Function function = (Function) SFClassLoader.forName(functionClass).newInstance();
                result = function.doit(forFunction, null);
            } catch (Exception e) {
                throw new SmartFrogResolutionException("failed to create or evaluate function class " + functionClass + " with data " + forFunction, e);
            }
        }

        return result;
    }

    /**
     * Resolves this apply reference by applying the function - unless this is data..
     *
     * @param rr    ReferenceResolver to be used for resolving this reference
     * @param index index of first referencepart to start resolving at
     * @return value found on resolving this function
     * @throws SmartFrogResolutionException if reference failed to resolve
     */
    public Object resolve(RemoteReferenceResolver rr, int index)
            throws SmartFrogResolutionException {
        //take a new context...
        //     iterate over the attributes of comp- ignoring any beginning with sf;
        //     cache sfFunctionClass attribute;
        //     resolve all non-sf attributes, if they are links
        //     if any return s LAZY object, set self to lazy and return self, otherwise update copy
        //     and invoke function with copy of CD, return result
        Context forFunction = new ContextImpl();
        String functionClass = null;
        Object result;
        boolean hasLazy = false;

        if (getData()) return this;

        if (rr instanceof ComponentDescription)
            comp.setParent((ComponentDescription) rr);
        else if (rr instanceof Prim)
            comp.setPrimParent((Prim) rr);

        for (Iterator v = comp.sfAttributes(); v.hasNext();) {
            Object name = v.next();

            String nameS = name.toString();
            if (nameS.startsWith("sf")) {
                if (nameS.equals("sfFunctionClass")) {
                    try {
                        functionClass = (String) comp.sfResolveHere("sfFunctionClass");
                    } catch (ClassCastException e) {
                        throw new SmartFrogResolutionException("function class is not a string", e);
                    }
                } //ignore all others named sf*
            } else {
                Object value = comp.sfResolve(new Reference(ReferencePart.here(name)));

                if (value instanceof Reference && !((Reference) value).getData()) {
                    hasLazy = true;
                } else {
                    try {
                        comp.sfReplaceAttribute(name, value);
                        forFunction.sfAddAttribute(name, value);
                    } catch (SmartFrogContextException e) {
                        //shouldn't happen
                    } catch (SmartFrogRuntimeException e) {
                        //shouldn't happen
                    }
                }
            }
        }
        if (functionClass == null) {
            throw new SmartFrogResolutionException("unknown function class ");
        }
        if (hasLazy) {
            setEager(false);
            return this;
        } else {
            try {
                Function function = (Function) SFClassLoader.forName(functionClass).newInstance();
                result = function.doit(forFunction, null);
            } catch (Exception e) {
                throw new SmartFrogResolutionException("failed to create or evaluate function class " + functionClass + " with data " + forFunction, e);
            }
        }

        return result;
    }

    /**
     * Returns string representation of the reference.
     * Overrides Object.toString.
     *
     * @return String representing the reference
     */
    public String toString() {
        String res = "";
        res += (eager ? "" : "LAZY ");
        res += (data ? "DATA " : "");

        res += "APPLY {";
        res += comp.sfContext().toString();
        res += "}";

        return res;
    }
}
