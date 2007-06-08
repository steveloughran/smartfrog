package org.smartfrog.sfcore.common;

import java.util.Set;
import java.util.Iterator;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that defines the access to, and manipulation of, tags of
 * component descriptions and (in its remote form) prims.
 */
public interface RemoteTagsComponent extends Remote {

   /**
    * Set the TAGS for this Component. TAGS are simply uninterpreted strings associated
    * with each attribute.
    *
    * @param tags a set of tags
    *
    * @throws SmartFrogException the attribute does not exist;
    */
   public void sfSetTags( Set tags) throws SmartFrogContextException, RemoteException;

   /**
    * Get the TAGS for this Component. TAGS are simply uninterpreted strings associated
    * with each attribute.
    *
    * @return the set of tags
    *
    * @throws SmartFrogException the attribute does not exist;
    */
   public Set sfGetTags() throws SmartFrogContextException, RemoteException;

   /**
    * add a tag to the tag set of this component
    *
    * @param tag a tag to add to the set
    *
    * @throws SmartFrogException the attribute does not exist;
    */
   public void sfAddTag( String tag) throws SmartFrogContextException, RemoteException;

   /**
    * remove a tag from the tag set of this component if it exists
    * @param tag a tag to remove from the set
    *
    * @throws SmartFrogException the attribute does not exist;
    *
    */
   public void sfRemoveTag( String tag) throws SmartFrogContextException, RemoteException;

  /**
    * add a tag to the tag set of this component
    *
    * @param tags  a set of tags to add to the set
    * @throws SmartFrogException
    *          the attribute does not exist;
    */
   public void sfAddTags( Set tags) throws SmartFrogContextException, RemoteException;

   /**
    * remove a tag from the tag set of this component if it exists
    *
    * @param tags  a set of tags to remove from the set
    * @throws SmartFrogException
    *          the attribute does not exist;
    */
   public void sfRemoveTags(Set tags)  throws SmartFrogContextException, RemoteException;

   /**
    * Return whether or not a tag is in the list of tags for this component
    *
    * @param tag the tag to chack
    *
    * @return whether or not the attribute has that tag
    * @throws SmartFrogException the attribute does not exist
    */
   public boolean sfContainsTag( String tag) throws SmartFrogContextException, RemoteException;

}
