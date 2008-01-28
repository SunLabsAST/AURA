
package com.sun.labs.aura.aardvark.store.item;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * A Listener for events related to Items.
 */
public interface SimpleItemListener extends Remote {
    /**
     * Called when an Item or set of Items have been created.
     * 
     * @param e the event containing the set of Items that were created
     */
    public void itemCreated(SimpleItemEvent e) throws RemoteException;
    
    /**
     * Called when an Item or set of Items have been changed.
     * 
     * @param e the event containing the set of Items that changed
     */
    public void itemChanged(SimpleItemEvent e) throws RemoteException;

    /**
     * Called when an Item or set of Items have been deleted.
     * 
     * @param e the event containing the set of Items that were deleted
     */
    public void itemDeleted(SimpleItemEvent e) throws RemoteException;
}
