/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

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
