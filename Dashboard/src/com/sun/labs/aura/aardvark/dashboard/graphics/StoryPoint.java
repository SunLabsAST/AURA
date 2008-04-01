/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.sun.labs.aura.aardvark.dashboard.story.Story;

/**
 *
 * @author plamere
 */
public class StoryPoint extends CPoint {

    protected Story story;

    StoryPoint(Story story, float x, float y, float z) {
        super(x, y, z);
        this.story = story;
    }

    public Story getStory() {
        return story;
    }
}
