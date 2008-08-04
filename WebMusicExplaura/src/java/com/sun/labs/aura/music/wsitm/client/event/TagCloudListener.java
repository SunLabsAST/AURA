/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

/**
 *
 * @author mailletf
 */
public interface TagCloudListener extends WebListener {

    public void onTagAdd(String tagId);
    public void onTagDelete(String tagId);
    public void onTagDeleteAll();
}
