package org.smartfrog.sfcore.reference;

import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;

/**
 * Interface for SF functions to implement
 */
public interface Function {
    /**
     * Evaluate the function
     *
     * @param c A context that contains all the parameters
     * @param name the reference form the root to this function instance
     * @param container the ReferenceResolver (CompoenentDescription) within which the fucntion resides
     * @throws SmartFrogException Failure to evaluate the function
     */
    public Object doit(Context c, Reference name, ReferenceResolver container) throws SmartFrogException;

        /**
     * Evaluate the function
     *
     * @param c A context that contains all the parameters
     * @param name the reference form the root to this function instance
     * @param container the RemoteReferenceResolver (Prim) within which the fucntion resides
     * @throws SmartFrogException Failure to evaluate the function
     */
    public Object doit(Context c, Reference name, RemoteReferenceResolver container) throws SmartFrogException;
}
