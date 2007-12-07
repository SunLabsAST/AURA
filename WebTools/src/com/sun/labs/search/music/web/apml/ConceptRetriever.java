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
public interface ConceptRetriever {

    APML getAPMLForUser(String user) throws IOException;

}
