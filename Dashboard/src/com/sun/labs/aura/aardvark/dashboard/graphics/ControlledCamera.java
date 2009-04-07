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

package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.input.NodeHandler;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.Box;

/**
 *
 * @author plamere
 */
public class ControlledCamera extends CPoint {
    CameraNode cameraNode;
    NodeHandler handler;


    private Command[] home = { 
        new CmdInputEnable(false),
        new CmdControl(true),
        new CmdVeryStiff(),
        new CmdMove(0, 0, 25),
        new CmdWait(3f),
        new CmdControl(false),
        new CmdStop(),
        new CmdInputEnable(true),
    };

    private Command[] pan = { 
        new CmdInputEnable(false),
        new CmdControl(true),
        new CmdMove(20, 0, 25),
        new CmdWait(3f),
        new CmdMove(-20, 0, 25),
        new CmdWait(3f),
        new CmdWaitSettled(),
        new CmdControl(false),
        new CmdInputEnable(true),
    };

    private Command[] controlOff = { 
        new CmdControl(false),
    };

    ControlledCamera(Camera cam) {
        super(cam.getLocation().x, cam.getLocation().y, cam.getLocation().z);
        cameraNode = new CameraNode("cam", cam);
        Box box = new Box("tilebox", new Vector3f(0, 0, 0), 1, 1f, .01f);
        getNode().attachChild(box);
        getNode().attachChild(cameraNode);
        addSet("home", home);
        addSet("pan", pan);
        add(controlOff);
        handler = new NodeHandler(cameraNode, 10, .2f);
        add("home");
        add("pan");
        add("home");
    }

    CameraNode getCameraNode() {
        return cameraNode;
    }

    NodeHandler getInputHandler() {
        return handler;
    }


    class CmdInputEnable implements Command {
        private boolean enable;

        CmdInputEnable(boolean enable) {
            this.enable = enable;
        }
        public boolean update(CPoint cp) {
            handler.setEnabled(enable);
            //handler.setEnabledOfAttachedHandlers(enable);
            return true;
        }
    }
}
