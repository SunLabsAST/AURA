/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component is a source of users that need to be refreshed.
 * @author plamere
 */
class UserRefreshManager {
    private ItemStore itemStore;
    private UserMonitor monitor;
    private Map<Long, User> allUsers = new HashMap<Long, User>();
    private long delayBetweenUserRefreshes = 15 * 60 * 1000L;
    private User nextUserToRefresh = null;

    /**
     * Creates a UserRefreshManager
     */
    public UserRefreshManager() {
        List<User> users = itemStore.getAll(User.class);
        for (User user : users) {
            allUsers.put(user.getID(), user);
        }
        monitor = new UserMonitor();
        itemStore.addItemListener(User.class, monitor);
    }

    /**
     * Gets the next user that should be refreshed.  This method blocks until any user in the 
     * itemstore needs to be refreshed.  A user needs to be refreshed when the current time is later
     * than the time of a user's last refresh plus the delay between refreshes.
     * 
     * @return the user to be refreshed (or null if the component has been shutdown)
     * @throws java.lang.InterruptedException if the thread has been interrupted
     * @throws com.sun.labs.aura.aardvark.util.AuraException if an error occurs while 
     *      updating the item store.
     */
    public synchronized User getNextUserForRefresh() throws InterruptedException, AuraException  {
        User user = null;

        while (monitor != null && user == null) {
            if (nextUserToRefresh == null) {
                nextUserToRefresh = findNextUserToRefresh();
            }
            long delay = getDelayUntilRefresh(nextUserToRefresh);

            wait(delay);

            if (getDelayUntilRefresh(nextUserToRefresh) <= 0L) {
                user = nextUserToRefresh;
                user.setLastFetchTime(System.currentTimeMillis());
                nextUserToRefresh = null;
                itemStore.put(user);
            }
        }
        return user;
    }


    /**
     * Finds the user that has the smallest time until the next refresh
     * @return the user with the smallest time until needing to be refreshed.
     */
    private User findNextUserToRefresh() {
        User nUser = null;
        for (User user : allUsers.values()) {
            if (nUser == null || user.getLastFetchTime() < nUser.getLastFetchTime()) {
                nUser = user;
            }
        }
        return nUser;
    }
    
    /**
     * Gets the delay in milliseconds until the given user needs to be refreshed
     * @param user the user of interest
     * @return the number of milliseconds until the user should be refreshed
     */
    private long getDelayUntilRefresh(User user) {
        if (user == null) {
            return Long.MAX_VALUE;
        } else {
            return (user.getLastFetchTime() + delayBetweenUserRefreshes) 
                    - System.currentTimeMillis();
        }
    }

    /**
     * Closes this manager
     */
    public synchronized void close() {
        if (monitor != null) {
            itemStore.removeItemListener(User.class, monitor);
            monitor = null;
        }
    }


    /**
     * Manages a user changed notification from the ItemStore
     * @param user the user that has changed
     */
    private synchronized void updateUser(User user) {
        allUsers.put(user.getID(), user);
        nextUserToRefresh = null;
        notifyAll();
    }

    /**
     * Manages a user deleted notification from the ItemStore
     * @param user the user that has changed
     */
    private synchronized void clearUser(User user) {
        allUsers.remove(user.getID());
        nextUserToRefresh = null;
        notifyAll();
    }


    /**
     * Handles ItemListener notifications from the item store
     */
    private class UserMonitor implements ItemListener {

        public void itemCreated(Item[] items) {
            for (Item item : items) {
                updateUser((User) item);
            }
        }

        public void itemChanged(Item[] items) {
            for (Item item : items) {
                updateUser((User) item);
            }
        }

        public void itemDeleted(Item[] items) {
            for (Item item : items) {
                clearUser((User) item);
            }
        }
    }
}