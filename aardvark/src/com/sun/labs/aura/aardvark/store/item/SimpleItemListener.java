/*
 * ItemListener.java
 * 
 * Created on Oct 25, 2007, 4:11:16 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.store.item;


/**
 * A Listener for events related to Items.
 * 
 * @author ja151348
 */
public interface SimpleItemListener {
    /**
     * Called when an Item or set of Items have been created.
     * 
     * @param e the event containing the set of Items that were created
     */
    public void itemCreated(SimpleItemEvent e);
    
    /**
     * Called when an Item or set of Items have been changed.
     * 
     * @param e the event containing the set of Items that changed
     */
    public void itemChanged(SimpleItemEvent e);

    /**
     * Called when an Item or set of Items have been deleted.
     * 
     * @param e the event containing the set of Items that were deleted
     */
    public void itemDeleted(SimpleItemEvent e);
}
