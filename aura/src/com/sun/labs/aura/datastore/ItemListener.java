
package com.sun.labs.aura.datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * A Listener for events related to Items.
 */
public interface ItemListener extends Remote {
    /**
     * Called when an Item or set of Items have been created.
     * 
     * @param e the event containing the set of Items that were created
     */
    public void itemCreated(ItemEvent e) throws RemoteException;
    
    /**
     * Called when an Item or set of Items have been changed.
     * 
     * @param e the event containing the set of Items that changed
     */
    public void itemChanged(ItemEvent e) throws RemoteException;

    /**
     * Called when an Item or set of Items have been deleted.
     * 
     * @param e the event containing the set of Items that were deleted
     */
    public void itemDeleted(ItemEvent e) throws RemoteException;
}
