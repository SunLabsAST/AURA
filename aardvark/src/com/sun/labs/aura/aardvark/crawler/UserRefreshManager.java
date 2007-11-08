/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.ItemEvent;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigDouble;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This component is a source of users that need to be refreshed.  This 
 * component is designed to work in multi-threaded environments when there is
 * a single UserRefreshManager.
 *
 * @author plamere
 */
public class UserRefreshManager implements Configurable {

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = ItemStore.class)
    public final static String PROP_ITEM_STORE = "itemStore";

    /**
     * the configurable property for the minimum time in milliesconds between user refreshes
     */
    //@ConfigDouble(defaultValue = 15 * 60 * 1000.0, range = {0, 24 * 60 * 60 * 1000.0})
    @ConfigDouble(defaultValue = 15 * 60 * 1000.0)
    public final static String PROP_USER_REFRESH_TIME = "userRefreshTime";
    private long delayBetweenUserRefreshes = 15 * 60 * 1000L;

    private ItemStore itemStore;
    private UserMonitor monitor;
    private Map<Long, User> allUsers = new HashMap<Long, User>();
    private Set<User> outstandingUsers = new HashSet<User>();
    private volatile User nextUserToRefresh = null;

    /**
     * Creates a UserRefreshManager
     */
    public UserRefreshManager() {
    }

    public synchronized void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
        delayBetweenUserRefreshes = (long) ps.getDouble(PROP_USER_REFRESH_TIME);

        allUsers.clear();
        outstandingUsers.clear();

        // Get all users from the item store and add them to our list of
        // all users

        List<User> users = itemStore.getAll(User.class);
        for (User user : users) {
            allUsers.put(user.getID(), user);
        }

        // Add a user monitor to keep the all user list fresh and up to date
        monitor = new UserMonitor();
        itemStore.addItemListener(User.class, monitor);
    }

    /**
     * Gets the next user that should be refreshed.  This method blocks until any user in the 
     * itemstore needs to be refreshed.  A user needs to be refreshed when the current time is later
     * than the time of a user's last refresh plus the delay between refreshes.  The returned
     * user must be released after it has been processed.
     * 
     * @return the user to be refreshed (or null if the component has been shutdown)
     * @throws java.lang.InterruptedException if the thread has been interrupted
     * @throws com.sun.labs.aura.aardvark.util.AuraException if an error occurs while 
     *      updating the item store.
     */
    public synchronized User getNextUserForRefresh() throws InterruptedException, AuraException {
        User user = null;

        while (monitor != null && user == null) {
            if (nextUserToRefresh == null) {
                nextUserToRefresh = findNextUserToRefresh();
            }
            long delay = getDelayUntilRefresh(nextUserToRefresh);

            if (delay > 0L) {
                wait(delay);
            }

            if (!isOutstanding(nextUserToRefresh) && getDelayUntilRefresh(nextUserToRefresh) <= 0L) {
                user = nextUserToRefresh;
                setOutstanding(user);
                nextUserToRefresh = null;
            }
        }
        return user;
    }

    /**
     * Release the user making it available once again to be refreshed
     * @param user a previously retrieved user
     * @throws IllegalArgumentException if the user was not outstanding
     */
    public synchronized void release(User user) {
        if (isOutstanding(user)) {
            outstandingUsers.remove(user);
        } else {
            throw new IllegalArgumentException("unexpected release of " + user);
        }
    }

    /**
     * Determines if a user has been retrieved, but not yet released
     * @param user the user of interest
     * @return true if the user is outstanding
     */
    private boolean isOutstanding(User user) {
        return user != null && outstandingUsers.contains(user);
    }

    /**
     *  Sets the outstanding state of the user
     * @param user the user of interest
     */
    private void setOutstanding(User user) {
        outstandingUsers.add(user);
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
            long delay = (user.getLastFetchTime() + delayBetweenUserRefreshes) - System.currentTimeMillis();
            if (delay < 0L) {
                delay = 0L;
            }
            return delay;
        }
    }

    /**
     * Closes this manager
     */
    public synchronized void close() {
        if (monitor != null) {
//            itemStore.removeItemListener(User.class, monitor);
            monitor = null;
            nextUserToRefresh = null;
            notifyAll();
        }
    }

    /**
     * Manages a user changed notification from the ItemStore
     * @param user the user that has changed
     */
    private synchronized void updateUsers(Item[] userItems) {
        for (Item item : userItems) {
            User user = (User) item;
            allUsers.put(user.getID(), user);
        }
        nextUserToRefresh = null;
        notifyAll();
    }

    /**
     * Manages a user deleted notification from the ItemStore
     * @param user the user that has changed
     */
    private synchronized void clearUsers(Item[] userItems) {
        for (Item item : userItems) {
            User user = (User) item;
            allUsers.remove(user.getID());
        }
        nextUserToRefresh = null;
        notifyAll();
    }


    /**
     * Handles ItemListener notifications from the item store
     */
    private class UserMonitor implements ItemListener {

        public void itemCreated(ItemEvent e) {
            updateUsers(e.getItems());
        }

        public void itemChanged(ItemEvent e) {
            updateUsers(e.getItems());
        }

        public void itemDeleted(ItemEvent e) {
            clearUsers(e.getItems());
        }
    }
}