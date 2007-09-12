package org.smartfrog.sfcore.languages.sf;

import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.languages.sf.sfcomponentdescription.SFComponentDescriptionImpl;
import org.smartfrog.sfcore.languages.sf.sfcomponentdescription.SFComponentDescription;
import org.smartfrog.sfcore.languages.sf.sfreference.SFApplyReference;
import org.smartfrog.sfcore.common.ContextImpl;
import org.smartfrog.sfcore.common.SmartFrogRuntimeException;
import org.smartfrog.sfcore.common.SmartFrogFunctionResolutionException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.reference.Reference;

import java.util.Stack;
import java.util.Iterator;

/**
 * Construct the function apply reference object, and replace self with this
 */
public class ConstructFunction implements PhaseAction {
    final static String functionClass = "sfFunctionClass";
    final static String functionLazy = "sfFunctionLazy";
    final static String functionPhase = "phase.function";

    // do the work
    String phaseName = null;
    SFComponentDescription cd = null;
    Stack path;

    public void doit() throws SmartFrogFunctionResolutionException {
        //SFComponentDescription comp = new SFComponentDescriptionImpl(null, (SFComponentDescription)(cd.sfParent()), new ContextImpl(), false);
        SFComponentDescription comp = new SFComponentDescriptionImpl(null, null, new ContextImpl(), false);

        try {
            comp.sfAddAttribute(functionClass, cd.sfResolve(functionClass));
        } catch (SmartFrogRuntimeException e) {
            throw new SmartFrogFunctionResolutionException("Unable to construct apply reference as sfFunctionClass is missing in phase: " + phaseName +
                    " for component: " + cd.sfCompleteName(), e);
        }

        for (Iterator i = cd.sfAttributes(); i.hasNext();) {
            Object key = i.next();
            Object value = null;
            try {
                value = cd.sfResolveHere(key);
            } catch (SmartFrogResolutionException e) {
                //shouldn't happen
            }
            if ((key.toString()).equals(functionPhase)) {
                //ignore
            } else {
                try {
                    comp.sfAddAttribute(key, value);
                } catch (SmartFrogRuntimeException e) {
                    //shouldn't happen
                }
            }
        }

        ComponentDescription parent = cd.sfParent();
        if (parent != null) {
            Object name = parent.sfContext().keyFor(cd);
            Reference newRef = new SFApplyReference(comp);
             try {
            // LAZY = false eager;
               newRef.setEager(!cd.sfResolve(functionLazy,false,false));
            } catch (SmartFrogRuntimeException e) {
                throw new SmartFrogFunctionResolutionException("Problem reading ("+functionLazy+"): " + phaseName + " for component: " + cd.sfCompleteName(), e);
            }
            parent.sfContext().put(name, newRef );
        }
    }

    // the component description which is to be transformed
    public void forComponent(SFComponentDescription componentDescription, String phase, Stack pathStack) {
        this.phaseName = phase;
        this.cd = componentDescription;
        this.path = pathStack;
    }
}
