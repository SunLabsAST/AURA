/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.apml;

import com.sun.labs.search.music.web.lastfm.Item;
import java.io.IOException;

/**
 *
 * @author plamere
 */
public interface ConceptRetriever {
    Concept[] getImplicitFromExplicit(Item[] explicit) throws IOException ;
}
