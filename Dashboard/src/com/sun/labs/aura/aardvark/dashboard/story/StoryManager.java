/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.story;

import java.util.List;

/**
 *
 * @author plamere
 */
public interface StoryManager {
    public List<Story> getNextStories(int count);
    public void thumbsUp(Story story);
    public void thumbsDown(Story story);
    public List<Story> findSimilar(Story story, int count);
}
