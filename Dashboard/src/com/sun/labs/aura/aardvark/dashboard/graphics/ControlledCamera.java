/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
