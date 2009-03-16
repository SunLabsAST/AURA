
package com.sun.labs.aura.fb;

/**
 * Holds info about a user on Facebook
 */
public class FBUserInfo {
    protected String name;
    protected String musicString;
    protected boolean isAppUser;

    public FBUserInfo(String name, String musicString, boolean isAppUser) {
        this.name = name;
        this.musicString = musicString;
        this.isAppUser = isAppUser;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the musicString
     */
    public String getMusicString() {
        return musicString;
    }

    /**
     * @param musicString the musicString to set
     */
    public void setMusicString(String musicString) {
        this.musicString = musicString;
    }

    /**
     * @return the isAppUser
     */
    public Boolean isAppUser() {
        return isAppUser;
    }

    /**
     * @param isAppUser the isAppUser to set
     */
    public void setIsAppUser(boolean isAppUser) {
        this.isAppUser = isAppUser;
    }
}
