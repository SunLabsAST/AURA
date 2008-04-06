/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.math.FastMath;
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

    private Command[] center = {
        new CmdControl(true),
        new CmdVeryStiff(),
        new CmdMove(0, 0, 45),
        //new CmdRotate(0, FastMath.PI, 0, .5f),
        new CmdWait(.5f),
        new CmdRotate(0, 0, 0),
        new CmdWait(.5f)
    };

    InteractivePoint(Story story, float x, float y, float z) {
        super(x, y, z);
        this.story = story;
        addSet("poke", pokeSet);
        addSet("center", center);
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
    
    public void makeCurrent(boolean isCur) {

    }
}
