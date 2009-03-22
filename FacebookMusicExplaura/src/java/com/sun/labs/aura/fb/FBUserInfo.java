
package com.sun.labs.aura.fb;

/**
 * Holds info about a user on Facebook
 */
public class FBUserInfo {
    protected Long uid;
    protected String name;
    protected String musicString;
    protected boolean isAppUser;
    protected boolean hasMusic;

    public FBUserInfo(Long uid, String name, String musicString, boolean isAppUser) {
        this.uid = uid;
        this.name = name;
        this.musicString = musicString;
        this.isAppUser = isAppUser;
        if (musicString == null || musicString.isEmpty()) {
            hasMusic = false;
        } else {
            hasMusic = true;
        }
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
        if (!hasMusic) {
            return "Coldplay";
        }
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

    /**
     * @return the hasMusic
     */
    public boolean hasMusic() {
        return hasMusic;
    }

    /**
     * @param hasMusic the hasMusic to set
     */
    public void setHasMusic(boolean hasMusic) {
        this.hasMusic = hasMusic;
    }

    /**
     * @return the uid
     */
    public Long getUID() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUID(Long uid) {
        this.uid = uid;
    }
}
