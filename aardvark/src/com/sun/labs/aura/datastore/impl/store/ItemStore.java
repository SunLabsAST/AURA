
package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

/**
 * The interface for a simple item store that allow the storage of users, items,
 * and attentions.  Methods in the Item Store may throw AuraExceptions when an
 * error has occurred in the underlying database that backs the item store.
 */
public interface ItemStore extends Component, Remote {
    /**
     * Gets all of the items in the store that are of the given type.  This
     * could be a very large operation.
     * 
     * @param itemType the type of items that are of interest
     * @return a list containing all items of the given type
     */
    public Set<Item> getAll(ItemType itemType)
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
    public Set<Item> getItems(
            User user,
            Attention.Type attnType,
            ItemType itemType) throws AuraException, RemoteException;


    /**
     * Get all the attention related to a particular item
     * 
     * @param itemKey the item to fetch attention for
     * @return the set of all attention
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public Set<Attention> getAttentionForTarget(String itemKey)
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
     * Gets all the attention that has been added to the store since a
     * particular date.  Returns an iterator over the attention that must be
     * closed when reading is done.
     * 
     * @param timeStamp the time to search back to
     * @return the Attentions added since that time
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public DBIterator<Attention> getAttentionAddedSince(Date timeStamp)
            throws AuraException, RemoteException;
    
    /**
     * Gets the N most recent attention objects that an attention source
     * has recorded.
     * 
     * @param srcKey the source to examine
     * @param count the number of attentions to fetch
     * @return the most recent attentions, sorted by date
     */
    public SortedSet<Attention> getLastAttentionForSource(String srcKey,
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
    public SortedSet<Attention> getLastAttentionForSource(String srcKey,
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
    public long getAttentionCount() throws AuraException, RemoteException;
    
    /**
     * Closes the item store cleanly.  This should be called before the
     * application exits.
     */
    public void close() throws AuraException, RemoteException;

}
