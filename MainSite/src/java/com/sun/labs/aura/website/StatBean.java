
package com.sun.labs.aura.website;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;

/**
 * A JavaBean that holds some stats about the data store
 */
public class StatBean {
    public static DecimalFormat longForm = new DecimalFormat("###,###,###,###");
    public static DecimalFormat doubForm = new DecimalFormat("###,###,###,###.#");

    protected long numUsers = 0;
    protected long numItems = 0;
    protected long numAttn = 0;
    
    public StatBean() {
    }
    
    public StatBean(DataStore dataStore) {
        try {
            numUsers = dataStore.getItemCount(ItemType.USER);
            numItems = dataStore.getItemCount(null);
            numAttn = dataStore.getAttentionCount(null);
        } catch (AuraException e) {
            
        } catch (RemoteException e) {
            
        }
    }

    public String getNumUsers() {
        return longForm.format(numUsers);
    }

    public void setNumUsers(long numUsers) {
        this.numUsers = numUsers;
    }

    public String getNumItems() {
        return longForm.format(numItems);
    }

    public void setNumItems(long numItems) {
        this.numItems = numItems;
    }

    public String getNumAttn() {
        return longForm.format(numAttn);
    }

    public void setNumAttn(long numAttn) {
        this.numAttn = numAttn;
    }
}
