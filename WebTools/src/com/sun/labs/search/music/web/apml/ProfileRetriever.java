/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.apml;

import java.io.IOException;

/**
 *
 * @author plamere
 */
public interface ProfileRetriever {
    /**
     * Gets an APML profile for the user
     * @param user the user of interest
     * @return the Profle
     * @throws java.io.IOException if an error occurs
     */
    Profile getProfileForUser(String user) throws IOException;

    /**
     * Wraps the profile in an APML container
     * @param user the user of interest
     * @return the apml
     * @throws java.io.IOException
     */
    APML getAPMLForUser(String user) throws IOException;

}
