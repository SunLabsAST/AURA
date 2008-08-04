/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import java.util.Set;

/**
 *
 * @author mailletf
 */
public interface TaggingListener extends WebListener {

    public void onTag(String itemId, Set<String> tags);

}
