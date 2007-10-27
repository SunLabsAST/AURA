/*
 * ItemStore.java
 * 
 * Created on Oct 25, 2007, 3:53:59 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store;

import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.util.AuraException;
import edu.cmu.sphinx.util.props.Configurable;
import java.util.List;

/**
 * The ItemStore is responsible for storing items and their associated
 * data in some fashion that will allow quick retrieval for recommendation
 * algorithms and relatively quick retrieval for the rest of Aardvark's
 * functions.
 * 
 * @author ja151348
 */
public interface ItemStore extends Configurable {
    
    /**
     * Creates an Item of the specified type with the given string as its key.
     * This method automatically assigns a new ID to the Item and is the only
     * supported way to create a new item that is to be added to the store.
     * 
     * @param itemType the class of the specific type of item to create
     * @param key a globally unique key that identifies this item.  May be a
     *            URI that points to the source of the item.
     * @return a new Item that is empty except for an ID and the provided key
     * @throws com.sun.labs.aura.aardvark.util.AuraException if the key already
     *         exists in the ItemStore
     */
    public <T extends Item> T newItem(Class<T> itemType, String key)
            throws AuraException;
    
    /**
     * Looks up the Aura ID of an item in the ItemStore.  This operation is
     * likely to be quicker than the {@link #get(String)} method since it
     * does not have to instantiate the entire Item.  If you need to get the
     * entire Item, call that method directly.
     * 
     * @param key the globally unique key that was used to create the Item
     * @return the Aura ID of the item, or -1 if the item does not exist
     */
    public long getID(String key);


    /**
     * Gets all of the items in the store that are of the given type.
     * 
     * @param itemType the type of items that are of interest
     * @return a list containing all items of the given type
     */
    public <T extends Item> List<T> getAll(Class<T> itemType);
    
    
    /**
     * Gets an Item from the ItemStore.  The Item returned is specified by the
     * Aura ID that is passed in.
     * 
     * @param id the Aura ID of the Item to fetch
     * @return the requested Item
     */
    public Item get(long id);
    
    /**
     * Gets an Item from the ItemStore that is associated with the given
     * globally unique key.  This method will instantiate the item once it
     * is found.  If you only need the ID of the item, the {@link #getID}
     * method may be quicker.
     * 
     * @param key the globally unique key that was used to create the Item
     * @return the requested Item
     */
    public Item get(String key);
    
    /**
     * Puts an item into the ItemStore.  The Item may be either a new Item
     * created by the {@link newItem} method or a modification of an
     * existing Item that was retrieved using one of the get methods.  If the
     * Item has the same Aura ID and key as an existing item, the existing
     * item will be updated.  If the Aura ID and key are new, the item will
     * be stored.  In any other case, an AuraException will be thrown.
     * 
     * @param item the Item that should be placed into the ItemStore
     * @throws com.sun.labs.aura.aardvark.util.AuraException if the Item is
     *         not valid for adding/updating
     */
    public void put(Item item) throws AuraException;

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
    public void attend(Attention att) throws AuraException;
    
    /**
     * Adds an ItemListener to this ItemStore.  ItemListeners are sent
     * batches of Item-related events.  The policy for when to send events
     * is left to the ItemStore implementation.  The listener may request
     * events only for a specific type of Item using the type parameter.  If
     * null is provided for the type, then events for all types of Items are
     * delivered.  A single ItemListener may register itself multiple times
     * with different Item types.
     * 
     * @param type the type of Item for which events are delivered, or null
     *             for all events
     * @param listener the listener to which events are delivered
     */
    public <T extends Item> void addItemListener(Class<T> type,
                                                 ItemListener listener);
    
   
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
    public <T extends Item> void removeItemListener(Class<T> type,
                                                    ItemListener listener);

}
