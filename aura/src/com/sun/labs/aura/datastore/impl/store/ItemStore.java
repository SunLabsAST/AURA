/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * The interface for a simple item store that allow the storage of users, items,
 * and attentions.  Methods in the Item Store may throw AuraExceptions when an
 * error has occurred in the underlying database that backs the item store.
 */
public interface ItemStore {

    /**
     * Defines a particular field for a given item type.  A field defined this 
     * way will only be stored in the database and can be retrieved from the item,
     * but cannot be searched for.
     * 
     * @param fieldName the name of the field that we want to define
     * @throws AuraException if the given field name has already been defined with
     * different attributes (e.g., if it was previously defined to be indexed).
     * @see defineField(String,boolean,Item.FieldType)
     */
    public void defineField(String fieldName)
            throws AuraException, RemoteException;

    /**
     * Defines a particular field for a given item type.  It is acceptable to define
     * a field multiple times if the same type and indexed flags are provided each
     * time that the field is defined.
     * 
     * @param fieldName the name of the field that we want to define
     * @param fieldType the type of the value in the field for this item type.
     * The value should be non-<code>null</code> if <code>indexed</code> is
     * <code>true</code>.
     * @param caps a set of field capabilities that describe how the field should
     * be treated by the search engine.
     * @throws AuraException if the given field name has already been defined and the capabilities are
     * not a match.  This exception will also be thrown when a field type is
     * not supplied when one is required.
     */
    public void defineField(String fieldName,
            Item.FieldType fieldType, EnumSet<Item.FieldCapability> caps) throws AuraException, RemoteException;

    /**
     * Gets all of the items in the store that are of the given type.  This
     * could be a very large operation.
     * 
     * @param itemType the type of items that are of interest
     * @return a list containing all items of the given type
     */
    public List<Item> getAll(ItemType itemType)
            throws AuraException, RemoteException;

    public DBIterator<Item> getAllIterator(ItemType itemType)
            throws AuraException, RemoteException;

    /**
     * Gets an item from the store that has the given key
     * 
     * @param key the string identifier of the item to fetch
     * @return the requested item
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Item getItem(String key)
            throws AuraException, RemoteException;
    
    /**
     * Gets a number of items from the store.
     * 
     * @param keys the keys of the items to get.  If a key does not occur in the 
     * data store, it will be ignored
     * @return the items corresponding to the keys that were provided.  If no
     * keys occur in the data store, an empty collection will be returned.
     */
    public Collection<Item> getItems(Collection<String> keys) 
            throws AuraException, RemoteException;

    /**
     * Gets a user from the store that has the given key
     * 
     * @param key the string identifier of the user to fetch
     * @return the requested user
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public User getUser(String key)
            throws AuraException, RemoteException;

    /**
     * Gets a user based on the random string provided.  This call must be
     * directed to the correct partition for it to succeed.
     * 
     * @param randStr the random string associated with a user
     * @return the user
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public User getUserForRandomString(String randStr)
            throws AuraException, RemoteException;

    /**
     * Delete a user from the date store.  This will remove the user item and
     * any attention that the user has created.
     * 
     * @param key the string identifier of the user to delete
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void deleteUser(String key)
            throws AuraException, RemoteException;

    /**
     * Delete an item from the date store.  This will remove the item and any
     * attention that was paid to it.
     * 
     * @param key the string identifier of the item to delete
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void deleteItem(String key)
            throws AuraException, RemoteException;

    /**
     * Puts an item into the ItemStore.  The Item may be either a new Item
     * or a modification of an existing Item that was retrieved using one of
     * the get methods.  If the Item has the same aura ID and key as an
     * existing item, the existing item will be updated.  If the aura ID and
     * key are new, the item will be stored.  In any other case, an
     * AuraException will be thrown.
     * 
     * @param item the Item that should be placed into the ItemStore
     * @throws com.sun.labs.aura.aardvark.util.AuraException if the Item is
     *         not valid for adding/updating
     */
    public Item putItem(Item item)
            throws AuraException, RemoteException;

    /**
     * Puts a user into the ItemStore.  The User may be either a new User
     * or a modification of an existing User that was retrieved using one of
     * the get methods.  If the User has the same aura ID and key as an
     * existing user, the existing user will be updated.  If the aura ID and
     * key are new, the user will be stored.  In any other case, an
     * AuraException will be thrown.
     * 
     * @param user the User that should be placed into the ItemStore
     * @throws com.sun.labs.aura.aardvark.util.AuraException if the User is
     *         not valid for adding/updating
     */
    public User putUser(User user)
            throws AuraException, RemoteException;

    /**
     * Gets all the items of a particular type that have been added since a
     * particular time.  Returns an iterator over those items that MUST be
     * closed when reading is done.
     * 
     * @param itemType the type of item to retrieve
     * @param timeStamp the time from which to search (to the present time
     * @return an iterator over the added items
     * @throws com.sun.labs.aura.aardvark.util.AuraException 
     */
    public DBIterator<Item> getItemsAddedSince(
            ItemType type,
            Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets all the items of a particular type to which a particular user has
     * given a particular type of attention.
     * 
     * @param userID the ID of the user in question
     * @param attnType the type of attention the user has paid
     * @param itemType the type of item to narrow down to
     * @return the set of matching items
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    @Deprecated
    public List<Item> getItems(
            User user,
            Attention.Type attnType,
            ItemType itemType) throws AuraException, RemoteException;


    /**
     * Get all the attention matching a particular set of constraints.  Set
     * the various fields of the AttentionConfig to describe the matching
     * constraints on the Attentions.  Use this method if you expect a
     * relatively small number of results.
     * 
     * @param ac the constraints to use on Attention objects
     * @return the matching attentions
     * @throws com.sun.labs.aura.util.AuraException if no constraints are
     * specified
     */
    public List<Attention> getAttention(AttentionConfig ac)
            throws AuraException, RemoteException;

    /**
     * Get an iterator over the attention matching a particular set of
     * constraints.  Set the various fields of the AttentionConfig to describe
     * the matching constraints on the Attentions.  Use this method if you
     * expect a relatively large number of results.
     * 
     * @param ac the constraints to use on Attention objects
     * @return an iterator over the matching attentions
     * @throws com.sun.labs.aura.util.AuraException if no constraints are
     * specified
     */
    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac)
            throws AuraException, RemoteException;

    /**
     * Get the number of attentions matching a particular set of constraints.
     * Set the various fields of the AttentionConfig to describe the matching
     * constraints on the Attentions.  Use an empty AttentionConfig to get
     * a count of all attentions.
     * 
     * @param ac the constraints to use on the Attention objects
     * @return the number of attentions that match
     */
    public Long getAttentionCount(AttentionConfig ac)
            throws AuraException, RemoteException;

    /**
     * Processes attention at the replicant level and returns custom data
     * depending on the provided script code.  The attentions passed to the
     * script are those that match the provided AttentionConfig object (as a
     * result of calling the {@link #getAttention(AttentionConfig)} method.
     * <p>
     * The script passed in must be in one of the supported languages, checked
     * by a call to {@link #getSupportedScriptLanguages()}.  The script should
     * provide a "process" method that will be given all the Attention
     * objects to process that returns an Object to send back to be collected.
     * It may also provide a "collect" method that will be called after all
     * Attentions have been processed that will be given a list of all the
     * Objects returned from the process methods.  Either the result of this
     * method (if provided) or a list of all the individual resulting Objects
     * will be returned from processAttention.
     *
     * @param ac the criteria for which attention objects to process
     * @param script the script, in the supported language of your choice
     * @param language the language in which the script is written
     * @return the result of the scripted method(s) as described above
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public Object processAttention(AttentionConfig ac, String script, String language)
            throws AuraException, RemoteException;

    /**
     * Gets all the attention recorded since a particular time that match
     * a particular set of constraints.  Set the various fields of
     * the AttentionConfig to describe the matching constraints on the
     * Attentions.  Use this method if you expect a relatively small number
     * of results.
     * 
     * @param ac the constraints to use on the Attention objects
     * @param timeStamp the time (inclusive) after which attentions should be returned
     * @return the list of attentions that match
     * @throws com.sun.labs.aura.util.AuraException if no constraints are set
     */
    public List<Attention> getAttentionSince(AttentionConfig ac,
            Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets an iterator over all the attention recorded since a particular time
     * that match a particular set of constraints.  Set the
     * various fields of the AttentionConfig to describe the matching
     * constraints on the Attentions.  Use this method if you expect a
     * relatively large number of results.
     * 
     * @param ac the constraints to use on the Attention objects
     * @param timeStamp the time (inclusive) after which attentions should be returned
     * @return an iterator over the attentions that match
     * @throws com.sun.labs.aura.util.AuraException if no cosntraints are
     * specified
     */
    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac,
            Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets a count of all the attentions recorded since a particular time that
     * match a particular set of constraints.  Set the various fields of the
     * AttentionConfig to describe the matching constraints on the Attentions.
     * 
     * @param ac the constraints to use on the Attention objects
     * @param timeStamp the time (inclusive) after which attentions should be returned
     * @return a count of the attentions that match
     */
    public Long getAttentionSinceCount(AttentionConfig ac,
            Date timeStamp)
            throws AuraException, RemoteException;

    /**
     * Gets the last N attentions recorded that match
     * a particular set of constraints.  Set the various fields of
     * the AttentionConfig to describe the matching constraints on the
     * Attentions.  This method may not execute as efficiently as the
     * getLastAttentionForSource methods if there are a large number of
     * Attentions that match the provided constraints.
     * 
     * @param ac the constraints to use on the Attention objects
     * @param count the number of most-recent attentions to return
     * @return the list of attentions that match
     * @throws com.sun.labs.aura.util.AuraException if no constraints are set
     */
    public List<Attention> getLastAttention(AttentionConfig ac,
            int count)
            throws AuraException, RemoteException;

    /**
     * Adds attention to the the ItemStore.  The Attention should contain
     * the User, the Item, the type of attention, and optionally a value
     * associated with the type (TBD).
     * 
     * @param att the attention that was paid
     * 
     * @throws com.sun.labs.aura.aardvark.util.AuraException in the event that
     *         the attention is invalid
     */
    public Attention attend(Attention att)
            throws AuraException, RemoteException;

    /**
     * Adds attention to the the ItemStore.  The Attention should contain
     * the User, the Item, the type of attention, and optionally a value
     * associated with the type.
     * 
     * @param attns the list of attentions that were paid
     * 
     * @throws com.sun.labs.aura.aardvark.util.AuraException in the event that
     *         an attention is invalid
     */
    public List<Attention> attend(List<Attention> attns)
            throws AuraException, RemoteException;

    /**
     * Remove attention of a particular type between a source object and a 
     * target object.  If more than one such attention exists, all matching
     * attentions will be removed.
     * 
     * @param srcKey the source item key
     * @param targetKey the target item key
     * @param type the type of the attention
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public void removeAttention(String srcKey, String targetKey,
            Attention.Type type)
            throws AuraException, RemoteException;

    /**
     * Adds an ItemListener to this ItemStore.  ItemListeners are sent
     * batches of Item-related events.  The policy for when to send events
     * is left to the ItemStore implementation.  The listener may request
     * events only for a specific type of Item using the type parameter.  If
     * null is provided for the type, then events for all types of Items are
     * delivered.  A single ItemListener may register itself multiple times
     * with different Item types.  A listener for "null" (all) types must
     * also be removed with "null" to stop it from receiving events.
     * 
     * @param type the type of Item for which events are delivered, or null
     *             for all events
     * @param listener the listener to which events are delivered
     */
    @Deprecated
    public void addItemListener(ItemType itemType,
            ItemListener listener)
            throws AuraException, RemoteException;

    /**
     * Removes an ItemListener from this ItemStore.  Since the same
     * ItemListener may listen for multiple different ItemTypes, the type
     * of the Item may be provided to stop only a specific set of events
     * from being delivered to the listener.  If null is provided for the type,
     * then the ItemListener will be removed entirely, even if it was added
     * only for specific types.
     * 
     * @param type the type of Item for which events should cease, or null
     *             for all events
     * @param listener the listener to which events should no longer be sent
     */
    @Deprecated
    public void removeItemListener(ItemType itemType,
            ItemListener listener)
            throws AuraException, RemoteException;

    /**
     * Gets the count of the number of items of a particular type that are
     * in the item store.
     * 
     * @param itemType the type of item to count
     * @return the number of items of that type
     */
    public long getItemCount(ItemType itemType)
            throws AuraException, RemoteException;

    /**
     * Returns a list of Strings describing the languages supported by the
     * data store's
     * <a href="http://java.sun.com/developer/technicalArticles/J2SE/Desktop/scripting/">JSR 223</a>
     * script engines.
     *
     * @return a list of language names supported by the script engine
     * @throws com.sun.labs.aura.util.AuraException
     * @throws java.rmi.RemoteException
     */
    public List<String> getSupportedScriptLanguages()
            throws AuraException, RemoteException;

    /**
     * Closes the item store cleanly.  This should be called before the
     * application exits.
     */
    public void close() throws AuraException, RemoteException;
}
