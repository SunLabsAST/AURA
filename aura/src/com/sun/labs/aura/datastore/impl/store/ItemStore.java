
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
     * way will only be stored in the index and can be retrieved from the item, 
     * but cannot be searched for.
     * 
     * @param itemType the type of the item with which this field is assoiciated.
     * @param field the name of the field that we want to define
     * @throws AuraException if the given field name has already been defined and the 
     * provided capabilities and type for the field are not an exact match for those
     * already provided.  This exception will also be thrown when a field type
     * is supplied with a set of attributes that do not require a field type or
     * when a field type is not supplied when one is required.
     */
    public void defineField(ItemType itemType, String field)
            throws AuraException, RemoteException;
    
    /**
     * Defines a particular field for a given item type.  It is acceptable to define
     * a field multiple times if the same capabilities and type are provided each
     * time that the field is defined.
     * 
     * @param itemType the type of the item with which this field is assoiciated.
     * @param field the name of the field that we want to define
     * @param caps a set of the capapbilities that the field should have.  If this
     * set is empty (or null), then the value of the field will be stored and can be retrieved,
     * but it cannot be used for similarity or query operations on items
     * @param fieldType the type of the value in the field for this item type.
     * The value should be non-<code>null</code> if the {@link Item.FieldCapabilities.SEARCH},
     * {@link Item.FieldCapabilities.FILTER}, or {@link Item.FieldCapabilities.SORT}
     * capabilities are provided.
     * @throws AuraException if the given field name has already been defined and the 
     * provided capabilities and type for the field are not an exact match for those
     * already provided.  This exception will also be thrown when a field type
     * is supplied with a set of attributes that do not require a field type or
     * when a field type is not supplied when one is required.
     */
    public void defineField(ItemType itemType, String field, EnumSet<Item.FieldCapability> caps, 
            Item.FieldType fieldType) throws AuraException, RemoteException;
    
    /**
     * Gets all of the items in the store that are of the given type.  This
     * could be a very large operation.
     * 
     * @param itemType the type of items that are of interest
     * @return a list containing all items of the given type
     */
    public List<Item> getAll(ItemType itemType)
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
    public List<Item> getItems(
            User user,
            Attention.Type attnType,
            ItemType itemType) throws AuraException, RemoteException;


    /**
     * Get all the attention related to a particular item
     * 
     * @param srcKey the source to fetch attention for
     * @return the set of all attention
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    @Deprecated
    public List<Attention> getAttentionForSource(String srcKey)
            throws AuraException, RemoteException;

    /**
     * Get all the attention related to a particular item
     * 
     * @param itemKey the item to fetch attention for
     * @return the set of all attention
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    @Deprecated
    public List<Attention> getAttentionForTarget(String itemKey)
            throws AuraException, RemoteException;
    
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
     * Gets all the attention that has been added to the store since a
     * particular date.  Returns an iterator over the attention that must be
     * closed when reading is done.
     * 
     * @param timeStamp the time to search back to
     * @return the Attentions added since that time
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    @Deprecated
    public DBIterator<Attention> getAttentionSince(Date timeStamp)
            throws AuraException, RemoteException;
    
    /**
     * Get all the attention for a source since a particular time.  Returns
     * an iterator over the attention that must be closed when reading is
     * done.
     * 
     * @param sourceKey the key of the source to limit the search to
     * @param timeStamp the time to search back to
     * @return the Attentions added for the source since the time
     * @throws com.sun.labs.aura.util.AuraException
     */
    @Deprecated
    public DBIterator<Attention> getAttentionForSourceSince(String sourceKey,
            Date timeStamp) throws AuraException, RemoteException;
    
    /**
     * Get all the attention for a target since a particular time.  Returns
     * an iterator over the attention that must be closed when reading is
     * done.
     * 
     * @param targetKey the key of the target to limit the search to
     * @param timeStamp the time to search back to
     * @return the Attentions added for the target since the time
     * @throws com.sun.labs.aura.util.AuraException
     */
    @Deprecated
    public DBIterator<Attention> getAttentionForTargetSince(String targetKey,
            Date timeStamp) throws AuraException, RemoteException;

    /**
     * Gets the N most recent attention objects that an attention source
     * has recorded.
     * 
     * @param srcKey the source to examine
     * @param count the number of attentions to fetch
     * @return the most recent attentions, sorted by date
     */
    public List<Attention> getLastAttentionForSource(String srcKey,
                                                          int count)
            throws AuraException, RemoteException;

    /**
     * Gets the N most recent attention objects of a particular type that
     * an attention source has recorded.
     * 
     * @param srcKey the source to examine
     * @param type the type of attention
     * @param count the number of attentions to fetch
     * @return the most recent attentions, sorted by date
     */
    public List<Attention> getLastAttentionForSource(String srcKey,
                                                          Attention.Type type,
                                                          int count)
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
     * Gets the count of the number of attentions in the data store
     * 
     * @return the count
     */
    @Deprecated
    public long getAttentionCount() throws AuraException, RemoteException;
    
    /**
     * Closes the item store cleanly.  This should be called before the
     * application exits.
     */
    public void close() throws AuraException, RemoteException;

}
