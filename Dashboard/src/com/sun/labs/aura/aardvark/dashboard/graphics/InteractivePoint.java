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
public class InteractivePoint extends CPoint {
    Story story;
    private Command[] pokeSet = {
        new CmdJiggle(true),
        new CmdWait(3),
        new CmdJiggle(false),
    };

    InteractivePoint(Story story, float x, float y, float z) {
        super(x, y, z);
        this.story = story;
        addSet("poke", pokeSet);
    }

    InteractivePoint(float x, float y, float z) {
        this(null, x, y, z);
    }

    public void init() {

    }

    public void findStories() {
    }

    public void findTags() {
    }

    public void open() {
    }
}
