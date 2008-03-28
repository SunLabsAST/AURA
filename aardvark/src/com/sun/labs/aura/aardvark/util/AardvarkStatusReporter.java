/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.util;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemEvent;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;


/**
 *
 * @author plamere
 */
public class AardvarkStatusReporter implements Configurable, AuraService, ItemListener {

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;

    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);

        try {
            ItemListener exportedItemListener = (ItemListener) ps.getConfigurationManager().getRemote(this, dataStore);
            dataStore.addItemListener(ItemType.FEED, exportedItemListener);
            // dataStore.addItemListener(ItemType.BLOGENTRY, exportedItemListener);

        } catch (RemoteException ex) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_DATA_STORE, "remote exception " + ex.getMessage());
        } catch (AuraException ex) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_DATA_STORE, "aura exception " + ex.getMessage());
        }
    }

    public void start() {
        System.out.println("AardvarkStatusReporter started");
    }

    public void stop() {
        System.out.println("AardvarkStatusReporter stopped");
    }

    public void itemCreated(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            System.out.println("============ new ========= ");
            System.out.println(ItemAdapter.toString(item));
        }
    }

    public void itemChanged(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            System.out.println("============ mod ========= ");
            System.out.println(ItemAdapter.toString(item));
        }
    }

    public void itemDeleted(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            System.out.println("============ del ========= ");
            System.out.println(ItemAdapter.toString(item));
        }
    }
}