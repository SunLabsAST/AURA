
package com.sun.labs.aura.datastore;

/**
 * Represents a user in the system.  A user is a specialized type of item.
 */
public interface User extends Item {

    /**
     * Gets the random string that is used in URLs to this user's data
     *
     * @return the random string
     */
    public String getUserRandString();
}
