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

import java.util.*;
import java.io.*;


/**
 * Implements the context interface. This implementation relies on the
 * OrderedHashtable class in the Utilities, but another class can be used. The
 * important thing for any implementation is the fact that the order in which
 * entries are added to the context should be maintained even through the
 * enumeration returning methods.
 *
 */
public class ContextImpl extends OrderedHashtable implements Context, Serializable, PrettyPrinting, Copying {

   Map attributeTags = new HashMap();

   /**
     * Creates an empty context with default capacity.
     */
    public ContextImpl() {
    }

    /**
     * Constructs a context with initial capacity and a load trigger for
     * expansion.
     *
     * @param cap initial capacity
     * @param load load capacity trigger
     */
    public ContextImpl(int cap, float load) {
        super(cap, load);
    }

    /**
     * Returns the first key for which the value is the given one (==).
     * @param value value to look up
     *
     * @return key for value or null if not found
     * @deprecated replaced by sfAttributeKeyFor(value);
     */
    public Object keyFor(Object value) {

        return sfAttributeKeyFor(value);
    }
   /**
     * Returns the first attribute which has a particular "equal" value  in the table.
     *
     * @param value value to find in table
     *
     * @return attibute object for value or null if none
     */
    public Object sfAttributeKeyForEqual(Object value){
        if (!contains(value)) {
            return null;
        }
        for (Enumeration e = keys(); e.hasMoreElements();) {
            Object theKey = e.nextElement();

            if (get(theKey).equals(value)) {
                return theKey;
            }
        }
        return null;
    }

    /**
     * Returns true if the context contains an equal value.
     * Replaces contains()
     * @param value object to check
     *
     * @return true if context contains value, false otherwise
     */
    public boolean sfContainsValue(Object value){
       return containsValue(value);
    }

   /**
     * Returns true if the context contains value reference (==).
     * Replaces contains()
     * @param value object to check
     *
     * @return true if context contains value, false otherwise
    *  @throws NullPointerException  if the value is <code>null</code>.
     */
    public boolean sfContainsRefValue(Object value){
       if (value == null) {
         throw new NullPointerException();
       }
       for (Enumeration e = keys(); e.hasMoreElements();) {
           Object theKey = e.nextElement();
            if (get(theKey) == (value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the context contains attribute.
     * Replaces containsKey()
     * @param attribute to check
     *
     * @return true if context contains key, false otherwise
     */
    public boolean sfContainsAttribute(Object attribute){
       return containsKey(attribute);
    }


    /**
     * Returns an ordered iterator over the attribute names in the context.
     * The remove operation of this Iterator won't affect
     * the contents of Context
     *
     * @return iterator
     */
    public  Iterator sfAttributes(){
        return orderedAttributes();

    }

    /**
     * Returns an ordered iterator over the values in the context.
     * The remove operation of this Iterator won't affect
     * the contents of Context

     * @return iterator
     */
    public  Iterator sfValues(){
        return orderedValues();
    }



    /**
     * Find an attribute in this context.
     *
     * @param name attribute key to resolve
     *
     * @return Object Reference
     *
     * @throws SmartFrogContextException failed to find attribute
     */
    public Object sfResolveAttribute(Object name) throws SmartFrogContextException {
        Object result = this.get(name);
        if (result == null) {
            throw new SmartFrogContextException(
                       MessageUtil.formatMessage(MessageKeys.MSG_NOT_FOUND_ATTRIBUTE, name));
        }
        return result;
    }




   /**
      * Adds an attribute to this context under given name.
      *
      * @param name name of attribute
      * @param value value of attribute
      *
      * @return previous value for name or null if none
      *
      * @throws SmartFrogContextException when name or value are null or name already used
      */
     public synchronized Object sfAddAttribute(Object name, Object value)
         throws SmartFrogContextException{
         if ((name == null) || (value == null)) {
           if (name == null) {
               throw new SmartFrogContextException(
               MessageUtil.formatMessage(MessageKeys.MSG_NULL_DEF_METHOD, "'name'",
                                         "sfAddAttribute") );
           }
           if (value == null) {
               throw new SmartFrogContextException(
               MessageUtil.formatMessage(MessageKeys.MSG_NULL_DEF_METHOD, "'value'",
                                         "sfAddAttribute name:'"+name+"'"));
           }

             return null;
         }

         if (this.containsKey(name)) {
             throw new SmartFrogContextException(
             MessageUtil.formatMessage(MessageKeys.MSG_REPEATED_ATTRIBUTE, name));

         }

         return this.put(name, value);
     }

     /**
      * Removes an attribute from this context.
      *
      * @param name of attribute to be removed
      *
      * @return removed attribute value if successfull or null if not
      *
      * @throws SmartFrogContextException when name is null
      */
     public synchronized Object sfRemoveAttribute(Object name)
         throws SmartFrogContextException{
         if (name == null) {
               throw new SmartFrogContextException(
               MessageUtil.formatMessage(MSG_NULL_DEF_METHOD, "'name'",
                                       "sfRemoveAttribute"));
         }
         return this.remove(name);
     }

     /**
      * Replace named attribute in context. If attribute is not
      * present it is added to the context.
      *
      * @param name of attribute to replace
      * @param value value to add or replace
      *
      * @return the old value if present, null otherwise
      *
      * @throws SmartFrogContextException when name or value are null
      */
     public synchronized Object sfReplaceAttribute(Object name, Object value)
         throws SmartFrogContextException {
         if ((name == null) || (value == null)) {
             if (name == null) {
                 throw new SmartFrogContextException(
                 MessageUtil.formatMessage(MSG_NULL_DEF_METHOD, "'name'",
                                           "sfReplaceAttribute"));
             }
             if (value == null) {
                 throw new SmartFrogContextException(
                 MessageUtil.formatMessage(MSG_NULL_DEF_METHOD, "'value'",
                                           "sfReplaceAttribute"));
             }

             return null;
         }

         return this.put(name, value);
     }


     /**
      * Returns the attribute key given a value. Uses == for the comparison
      *
      * @param value value to look up key for
      *
      * @return key for attribute value or null if none
      */

     // perhaps this should be synchronized... but causes problems with sfCompleteName if it is
     public Object sfAttributeKeyFor(Object value) {
         if (!contains(value)) {
            return null;
        }
        for (Enumeration e = keys(); e.hasMoreElements();) {
            Object theKey = e.nextElement();

            if (get(theKey) == (value)) {
                return theKey;
            }
        }
        return null;
     }


   /**
    * Set the TAGS for an attribute. TAGS are simply uninterpreted strings associated
    * with each attribute.
    *
    * @param name attribute key for tags
    * @param tags a set of tags
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public synchronized void sfSetTags(Object name, Set tags) throws SmartFrogContextException {
      if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for setting tags");

      Set s = Collections.synchronizedSet(new HashSet());
      s.addAll(tags);
      attributeTags.put(name, s);
   }

   /**
    * Get the TAGS for an attribute. TAGS are simply uninterpreted strings associated
    * with each attribute.
    *
    * @param name attribute key for tags
    * @return the set of tags
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public synchronized Set sfGetTags(Object name) throws SmartFrogContextException {
      if ( name==null || !containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for getting tags");
      if (attributeTags.containsKey(name)) { // return a copy
         Set s = new HashSet();
         s.addAll((Set)attributeTags.get(name));
         return s;
      } else { //return an empty set as it has not been set yet
         return new HashSet();
      }
   }

   /**
    * add a tag to the tag set of an attribute
    *
    * @param name attribute key for tags
    * @param tag  a tag to add to the set
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public synchronized void sfAddTag(Object name, String tag) throws SmartFrogContextException {
      if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for adding tags");
      if (attributeTags.containsKey(name)) { // add it
         Set s = (Set)attributeTags.get(name);
         s.add(tag);
      } else { //create a new set and add it
         Set s = Collections.synchronizedSet(new HashSet());
         s.add(tag);
         attributeTags.put(name, s);
      }
   }

   /**
    * remove a tag from the tag set of an attribute if it exists
    *
    * @param name attribute key for tags
    * @param tag  a tag to remove from the set
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public synchronized void sfRemoveTag(Object name, String tag)  throws SmartFrogContextException {
      if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for removing tags");
      if (attributeTags.containsKey(name)) { // remove it
         Set s = (Set)attributeTags.get(name);
         s.remove(tag);
         if (s.size() == 0) attributeTags.remove(name);
      } else { // do nothing as it isn't there!
      }
   }

      /**
    * add a tag to the tag set of an attribute
    *
    * @param name attribute key for tags
    * @param tags  a set of tags to add to the set
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public synchronized void sfAddTags(Object name, Set tags) throws SmartFrogContextException {
      if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for adding tags");
      if (attributeTags.containsKey(name)) { // add it
         Set s = (Set)attributeTags.get(name);
         s.addAll(tags);
      } else { //create a new set and add it
         Set s = Collections.synchronizedSet(new HashSet());
         s.addAll(tags);
         attributeTags.put(name, s);
      }
   }

   /**
    * remove a tag from the tag set of an attribute if it exists
    *
    * @param name attribute key for tags
    * @param tags  a set of tags to remove from the set
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public synchronized void sfRemoveTags(Object name, Set tags)  throws SmartFrogContextException {
      if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for removing tags");
      if (attributeTags.containsKey(name)) { // remove it
         Set s = (Set)attributeTags.get(name);
         s.removeAll(tags);
         if (s.size() == 0) attributeTags.remove(name);
      } else { // do nothing as it isn't there!
      }
   }
   /**
    * Return an iterator over the tags for an attribute
    *
    * @param name the name of the attribute
    * @return an iterator over the tags
    * @throws SmartFrogContextException
    *          the attribute does not exist;
    */
   public Iterator sfTags(Object name) throws SmartFrogContextException {
     Iterator iter;
     if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for iterating over tags");
      if (attributeTags.containsKey(name)) { // return set iterator
         Set s = (Set)attributeTags.get(name);
         iter = s.iterator();
      } else { // return iterator on empty set
         Set s = new HashSet();
         iter = s.iterator();
      }
     return iter;
   }

   /**
    * Return whether or not a tag is in the list of tags for an attribute
    *
    * @param name the name of the attribute
    * @param tag the tag to chack
    *
    * @return whether or not the attribute has that tag
    * @throws SmartFrogContextException the attribute does not exist
    */
   public boolean sfContainsTag(Object name, String tag) throws SmartFrogContextException {
     if (!containsKey(name))
         throw new SmartFrogContextException("Attribute " + name + " does not exists for validating tag's existance");
      return sfGetTags(name).contains(tag);
   }

    /**
     * Compares the specified Object with this Context Tags for equality
     *
     * @param  o object to be compared for equality with this Context
     * @return true if the specified Object is equal to this Map.
     */
    public synchronized boolean equalsTags (Object o) {
    	if (o == attributeTags)
	        return true;

	    if (!(o instanceof Map))
	        return false;

        if (!attributeTags.equals(o)){
            return false;
        }

        return true;
    }

    /**
     * Returns a string representation of the component. This will give a
     * description of the component which is parseable, and deployable
     * again... Unless someone removed attributes which are essential to
     * startup that is. Large description trees should be written out using
     * writeOn since memory for large strings runs out quick!
     *
     * @return string representation of component
     */
    public String toString() {
        StringWriter sw = new StringWriter();

        try {
            writeOn(sw);
        } catch (IOException ioex) {
            sw.write("[ERROR] ContextImpl "+ioex.toString());
            // ignore should not happen
        }

        return sw.toString();
    }

    /**
     * Writes this component description on a writer. Used by toString. Should
     * be used instead of toString to write large descriptions to file, since
     * memory can become a problem given the LONG strings created
     *
     * @param ps writer to write on
     *
     * @throws IOException failure while writing
     */
    public void writeOn(Writer ps) throws IOException {
        writeContextOn(ps, 0, this.keys());
    }

    /**
     * Writes this component description on a writer. Used by toString. Should
     * be used instead of toString to write large descriptions to file, since
     * memory can become a problem given the LONG strings created
     *
     * @param ps writer to write on
     * @param indent the indent to use for printing offset
     *
     * @throws IOException failure while writing
     */
    public void writeOn(Writer ps, int indent) throws IOException {
        writeContextOn(ps, indent, this.keys());
    }

    /**
     * Writes the context on a writer.
     *
     * @param ps writer to write on
     * @param indent level
     * @param keys enumeation over the keys of the context to write out
     *
     * @throws IOException failure while writing
     */
    protected void writeContextOn(Writer ps, int indent, Enumeration keys) throws IOException {
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = get(key);
            tabPad(ps, indent);
            writeTagsOn(ps, indent, key);
            writeKeyOn(ps, indent, key);
            ps.write(' ');
            writeValueOn(ps, indent, value);
            ps.write('\n');
        }
    }

    /**
     * Writes given attribute key on a writer.
     *
     * @param ps writer to write on
     * @param indent indent level
     * @param key key to stringify
     *
     * @throws IOException failure while writing
     */
    protected void writeTagsOn(Writer ps, int indent, Object key) throws IOException {
       if (attributeTags.containsKey(key)) {
          try {
             if (sfGetTags(key).size() > 0) {
                ps.write("[ ");
                for (Iterator i = sfTags(key); i.hasNext();) {
                   ps.write(i.next().toString() + " ");
                }
                ps.write("] ");
             }
          } catch (SmartFrogContextException e) {
             // shouldn't happen...
          }
       }
    }
   /**
     * Writes given attribute key on a writer.
     *
     * @param ps writer to write on
     * @param indent indent level
     * @param key key to stringify
     *
     * @throws IOException failure while writing
     */
    protected void writeKeyOn(Writer ps, int indent, Object key) throws IOException {
        ps.write(key.toString());
    }

    /**
     * Writes a given value on a writer. Recognizes descriptions, strings and
     * vectors of basic values and turns them into string representation.
     * Default is to turn into string using normal toString() call
     *
     * @param ps writer to write on
     * @param indent indent level
     * @param value value to stringify
     *
     * @throws IOException failure while writing
     */
    protected void writeValueOn(Writer ps, int indent, Object value) throws IOException {
        if (value instanceof PrettyPrinting) {
            try {
                ((PrettyPrinting)value).writeOn(ps, indent);
            } catch (IOException ex) {
                throw ex;
            } catch (java.lang.StackOverflowError thr) {
                   StringBuffer msg = new StringBuffer("Failed to pretty print value. Possible cause: cyclic reference.");
                   msg.append("Cause:#<0># ");
                   msg.append(thr.getCause().toString());
                   throw new java.io.IOException(msg.toString());
            }
        } else {
            writeBasicValueOn(ps, indent, value);
            ps.write(';');
        }
    }
    /**
     * Writes a given value on a writer. Recognizes descriptions, strings and
     * vectors of basic values and turns them into string representation.
     * Default is to turn into string using normal toString() call
     *
     * @param ps writer to write on
     * @param indent indent level
     * @param value value to stringify
     *
     * @throws IOException failure while writing
     */
    protected static void writeBasicValueOn(Writer ps, int indent, Object value) throws IOException {
        if (value instanceof String) {
            ps.write("\"" + unfixEscapes((String)value) + "\"");
        } else if (value instanceof Vector) {
            ps.write("[|");
            for (Enumeration e = ((Vector) value).elements(); e.hasMoreElements();) {
                writeBasicValueOn(ps, indent, e.nextElement());
                if (e.hasMoreElements()) {
                    ps.write(", ");
                }
            }
            ps.write("|]");
        } else if (value instanceof Long) {
            ps.write(value.toString() + 'L');
        } else if (value instanceof Double) {
            ps.write(value.toString() + 'D');
        } else {
            ps.write(value.toString());
        }
    }

    /**
     *  To fix escape characters in String
     * @param s  String to be fixed
     * @return String
     */
    private static String unfixEscapes(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        s = s.replaceAll("\n", "\\\\n");
        s = s.replaceAll("\t", "\\\\t");
        s = s.replaceAll("\b", "\\\\b");
        s = s.replaceAll("\r", "\\\\r");
        s = s.replaceAll("\f", "\\\\f");
        return s;
    }

    /**
     * Gets a given value in its String form. Recognizes descriptions, strings and
     * vectors of basic values and turns them into string representation.
     * Default is to turn into string using normal toString() call
     *
     * @param obj Object to be given in String form
     * @return String
     * @throws IOException failure while writing
     */
    public static String getBasicValueFor (Object obj) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        writeBasicValueOn(pw,0,obj);
        return sw.toString();
    }

    /**
     * Internal method to pad out a writer.
     *
     * @param ps writer to tab to
     * @param amount amount to tab
     *
     * @throws IOException failure while writing
     */
    protected static void tabPad(Writer ps, int amount) throws IOException {
        for (int i = 0; i < amount; i++)
            ps.write("  ");
    }


    // ///////////////////////////////////////////////////////////////////////////////////
    // reimplementation of the hash table and order hash table methods to deal with the tags
    // ///////////////////////////////////////////////////////////////////////////////////



    /**
     * Clears the tags as well as the hashtable.
     * Overwrites OrderedHashtable.clear().
     */
    public void clear() {
        super.clear();
        attributeTags.clear();
    }

    /**
     * Removes an entry from the hashtable.
     * Overwrites OrderedHashtable.remove().
     *
     * @param key key to remove
     *
     * @return removed object
     */
    public Object remove(Object key) {
        Object r = super.remove(key);
        attributeTags.remove(key);
        return r;
    }


    /**
     *   Removes the element at the specified position.
     *   Removes the key (and its corresponding value) from this hashtable.
     *   This method does nothing if the key is not in the hashtable.
     *   Overrides the method in OrderedHashtable
     *
     * @param index index to remove
     *
     * @return the value to which the index had been mapped in this hashtable,
     *         or null if the key did not have a mapping
     */
    public Object remove(int index) {
        Object key = orderedKeys.remove(index);
        Object value = super.remove(index);
        attributeTags.remove(key);
        return value;
    }


    /**
     * Renames an entry in the otable, leaving its position in the table
     * unchanged. Overrides method in OrderedHashtable.
     *
     * @param key1 the initial key to be renamed
     * @param key2 the new key name
     *
     * @return the initial key, or null if it wasn't in the table
     */
    public Object rename(Object key1, Object key2) {
        super.rename(key1, key2);

        if (attributeTags.containsKey(key1)) {
           attributeTags.put(key2, attributeTags.get(key1));
           attributeTags.remove(key1);
        }

        return key1;
    }

    /**
     * Does a shallow copy of the hashtable, the ordered keys and
     * the attributeTags.
     *
     * @return shallow copy of this table
     */


    public Object clone() {
       Object ret = super.clone();
       Map m = new HashMap();
       for (Iterator i = attributeTags.keySet().iterator(); i.hasNext(); ) {
          Object key = i.next();
          Set s = (Set) attributeTags.get(key);
          Set sc = Collections.synchronizedSet(new HashSet());
          sc.addAll(s);
          m.put(key, sc);
       }
       ((ContextImpl) ret).attributeTags = m;
        return ret;
    }

   /**
    * Does a deep copy of the hashtable. Values in the hashtable which
    * understand the Copying interface get copied properly. If the Values
    * cannot be copied, the basic SF values (numbers, strings, booleans, are
    * each properly dealt with. Other values are copied using serialize/deserialize
    * if they implement serialization - note that because of this transient data will
    * not be copied. It also copies the attributeTags.
    *
    * This overrides the one in OrderedHashtable.
    *
    * @return copy of hashtable
    */
   public Object copy() {
      // note that since the super method uses clone,
      // this is already copying attributeTags
      return super.copy();
   }


    /**
     * Compares the specified Object with this Context for equality,
     * as per the definition in the Map interface.
     *
     * @param  o object to be compared for equality with this Context
     * @return true if the specified Object is equal to this Map.
     */
    public synchronized boolean equals(Object o) {

        if (o == this)
	        return true;

        if (!(o instanceof Context))
                    return false;

        // Compares HashMap
        if (!super.equals(o))
         return false;


        // Compares Tags
        if (!((o instanceof ContextImpl) && (((ContextImpl)o).equalsTags(attributeTags)))){
            return false;
        }
        return true;
    }

    /**
     * Returns the hash code value for this Context
     *
     */
    public synchronized int hashCode() {
        // Simple hashcode using Joshua Bloch's recommendation
        int result = 17;
        result = 37 * result + attributeTags.hashCode();
        result = 37 * result + super.hashCode();
        return result;
    }

}
