/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.bounding.BoundingBox;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.InputActionInterface;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author plamere
 */
public class CPoint extends Controller implements InputActionInterface {

    private Node masterNode;
    private MotionController motionController;
    private final static float defaultTick = .1f;
    private float curTick;
    private float curTime;
    private Queue<Command> commandQueue = new ConcurrentLinkedQueue<Command>();
    private Map<String, Command[]> cmdSetMap = new HashMap<String, Command[]>();
    private List<ActionHandler> actionHandlers = new ArrayList<ActionHandler>();
    private float scaleSet = 1.0f;
    private float scaleCur = 1.0f;
    private float minScaleDelta = .01f;
    private boolean traceCommands = false;
    static Random rng = new Random();

    public CPoint(float initX, float initY, float initZ) {
        this.masterNode = new Node();
        this.motionController = new MotionController(masterNode, initX, initY, initZ);
        masterNode.addController(this);
        masterNode.addController(motionController);
        masterNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
    }

    public CPoint(Node node) {
        this.masterNode = node;
        this.motionController = new MotionController(masterNode, 
                node.getLocalTranslation().x, 
                node.getLocalTranslation().y, 
                node.getLocalTranslation().z);
        masterNode.addController(this);
        masterNode.addController(motionController);
        masterNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
    }

    public void addGeometry(Geometry geometry) {
        masterNode.attachChild(geometry);
        geometry.setModelBound(new BoundingBox());
        geometry.updateModelBound();
        geometry.setUserData("cpoint", this);
    }

    public void attachChild(Node Node) {
        masterNode.attachChild(Node);
    }

    public MotionController getMotionController() {
        return motionController;
    }

    public Node getNode() {
        return masterNode;
    }

    public void remove() {
        masterNode.removeFromParent();
    }

    public void setTrace(boolean trace) {
        traceCommands = trace;
    }

    public void poke() {
        motionController.setJiggle(true);
    }

    public void unpoke() {
        motionController.setJiggle(false);
    }

    public void add(Command[] cmds) {
        for (Command cmd : cmds) {
            add(cmd);
        }
    }

    public void add(Command cmd) {
        commandQueue.add(cmd);
    }

    public void setScale(float scale) {
        scaleSet = scale;
    }

    public float getScale() {
        return scaleSet;
    }

    public void flush() {
        commandQueue.clear();
    }

    public void add(String setName) {
        add(setName, 1);
    }

    public void add(String setName, int count) {
        Command[] cmds = cmdSetMap.get(setName);
        if (cmds != null) {
            for (int i = 0; i < count; i++) {
                add(cmds);
            }
        }
    }
    private float curRotTime = 0;
    private Quaternion setRotation = new Quaternion();
    private Quaternion curRotation = new Quaternion();
    private Quaternion lastRotation = new Quaternion();
    private float rotateTime = 1;

    public void setAngle(float rotx, float roty, float rotz, float rotTime) {
        lastRotation.set(setRotation);
        setRotation.fromAngles(rotx, roty, rotz);

        if (rotTime <= 0) {
            rotTime = 1;
        }
        this.rotateTime = rotTime;
        curRotTime = 0;
    }

    public void setRelativeAngle(float rotx, float roty, float rotz, float rotTime) {
        float[] angles = new float[3];
        curRotation.toAngles(angles);
        lastRotation.set(curRotation);
        setRotation.fromAngles(angles[0] + rotx, angles[1] + roty, angles[2] + rotz);

        if (rotTime <= 0) {
            rotTime = 1;
        }
        this.rotateTime = rotTime;
        curRotTime = 0;
    }

    public void setAngle(float rotx, float roty, float rotz) {
        setAngle(rotx, roty, rotz, 1);
    }

    private void manageRotation(float time) {
        if (curRotTime < rotateTime) {
            curRotation.slerp(lastRotation, setRotation, curRotTime / rotateTime);
            masterNode.setLocalRotation(curRotation);

            curRotTime += time;
            if (curRotTime >= rotateTime) {
                curRotTime = rotateTime;
                masterNode.setLocalRotation(setRotation);
            }
        }
    }

    public void addSet(String name, Command[] cmds) {
        cmdSetMap.put(name, cmds);
    }

    float getCurTime() {
        return curTime;
    }

    private void checkQueue() {
        Command command = commandQueue.peek();
        if (command != null) {
            if (command.update(this)) {
                if (traceCommands) {
                    System.out.println("CMD " + command);
                }
                commandQueue.remove();
                curTime = 0f;
            }
        }
    }

    @Override
    public void update(float time) {
        curTime += time;
        curTick += time;
        if (curTick > defaultTick) {
            curTick = 0;
            checkQueue();
        }

        manageScale();
        manageRotation(time);
    }

    private void manageScale() {
        if (scaleSet != scaleCur) {
            if (scaleSet > scaleCur) {
                float delta = scaleSet - scaleCur;
                if (delta < minScaleDelta) {
                    scaleCur = scaleSet;
                } else {
                    scaleCur += minScaleDelta;
                }
            } else {
                float delta = scaleCur - scaleSet;
                if (delta < minScaleDelta) {
                    scaleCur = scaleSet;
                } else {
                    scaleCur -= minScaleDelta;
                }
            }
            for (Spatial spatial : masterNode.getChildren()) {
                if (spatial instanceof Geometry) {
                    Geometry geometry = (Geometry) spatial;
                    geometry.setLocalScale(scaleCur);
                }
            }
        }
    }

    public synchronized void performAction(InputActionEvent evt) {
        for (ActionHandler ah : actionHandlers) {
            ah.performAction(this, evt);
        }
    }

    synchronized void addActionHandler(ActionHandler ah) {
        actionHandlers.add(ah);
    }

    synchronized void removeActionHandler(ActionHandler ah) {
        actionHandlers.remove(ah);
    }
}

class CmdWait implements Command {

    private float delay;

    CmdWait(float delay) {
        this.delay = delay;
    }

    CmdWait(float min, float max) {
        float range = max - min;
        delay = range * CPoint.rng.nextFloat() + min;
    }

    public boolean update(CPoint cp) {
        return (cp.getCurTime() > delay);
    }

    public String toString() {
        return "waiting for " + delay + " secs";
    }
}

class CmdWaitBounds implements Command {

    private Vector3f bounds;

    CmdWaitBounds(Vector3f bounds) {
        this.bounds = bounds;
    }

    CmdWaitBounds(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    public boolean update(CPoint cp) {
        Vector3f cur = cp.getNode().getWorldTranslation();

        if (cur.x > bounds.x || cur.x < -bounds.x) {
            return true;
        }

        if (cur.y > bounds.y || cur.y < -bounds.y) {
            return true;
        }

        if (cur.z > bounds.z || cur.z < -bounds.z) {
            return true;
        }

        return false;
    }

    public String toString() {
        return "waitbounds for " + bounds;
    }
}

class CmdMove implements Command {

    private Vector3f v;

    CmdMove(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    CmdMove(Vector3f v) {
        this.v = v;
    }

    public boolean update(CPoint cp) {
        cp.getMotionController().setSetPoint(v);
        return true;
    }

    public String toString() {
        return "moving to " + v;
    }
}

class CmdVel implements Command {

    private Vector3f v;

    CmdVel(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    CmdVel(Vector3f v) {
        this.v = v;
    }

    public boolean update(CPoint cp) {
        cp.getMotionController().setEnable(false);
        cp.getMotionController().setLinearVelocity(v);
        return true;
    }

    public String toString() {
        return "setting vel to " + v;
    }
}

class CmdRotate implements Command {

    private float rotx;
    private float roty;
    private float rotz;
    private float time;

    CmdRotate(float rotx, float roty, float rotz) {
        this(rotx, roty, rotz, 1);
    }

    CmdRotate(float rotx, float roty, float rotz, float time) {
        this.rotx = rotx;
        this.roty = roty;
        this.rotz = rotz;
        this.time = time;
    }

    public boolean update(CPoint cp) {
        cp.setAngle(rotx, roty, rotz, time);
        return true;
    }

    public String toString() {
        return "Rotating to " + rotx + ", " + roty + ", " + rotz;
    }
}

class CmdRotateRelative implements Command {

    private float rotx;
    private float roty;
    private float rotz;
    private float time;

    CmdRotateRelative(float rotx, float roty, float rotz) {
        this(rotx, roty, rotz, 1);
    }

    CmdRotateRelative(float rotx, float roty, float rotz, float time) {
        this.rotx = rotx;
        this.roty = roty;
        this.rotz = rotz;
        this.time = time;
    }

    public boolean update(CPoint cp) {
        cp.setRelativeAngle(rotx, roty, rotz, time);
        return true;
    }

    public String toString() {
        return "Rotating (rel) to " + rotx + ", " + roty + ", " + rotz;
    }
}

class CmdScale implements Command {

    private float scale;

    CmdScale(float scale) {
        this.scale = scale;
    }

    public boolean update(CPoint cp) {
        cp.setScale(scale);
        return true;
    }

    public String toString() {
        return "Scaling to " + scale;
    }
}

class CmdWaitSettled implements Command {

    public boolean update(CPoint cp) {
        return cp.getMotionController().isSettled();
    }

    public String toString() {
        return "wating for settled";
    }
}

class CmdGravity implements Command {

    private boolean gravityOn;

    CmdGravity(boolean on) {
        this.gravityOn = on;
    }

    public boolean update(CPoint cp) {
        cp.getMotionController().setAffectedByGravity(gravityOn);
        return true;
    }

    public String toString() {
        return "gravitiy is " + gravityOn;
    }
}

class CmdControl implements Command {

    private boolean cmdControl;

    CmdControl(boolean on) {
        this.cmdControl = on;
    }

    public boolean update(CPoint cp) {
        cp.getMotionController().setEnable(cmdControl);
        return true;
    }

    public String toString() {
        return "control is " + cmdControl;
    }
}

class CmdStop implements Command {

    public boolean update(CPoint cp) {
        cp.getMotionController().stop();
        return true;
    }

    public String toString() {
        return "stopped";
    }
}

class CmdJiggle implements Command {

    private boolean state;

    CmdJiggle(boolean on) {
        this.state = on;
    }

    public boolean update(CPoint cp) {
        cp.getMotionController().setJiggle(state);
        return true;
    }

    public String toString() {
        return "jiggle is " + state;
    }
}

class CmdAddSet implements Command {

    private String setName;
    private int repeat;

    CmdAddSet(String name) {
        this(name, 1);
    }

    CmdAddSet(String name, int count) {
        this.setName = name;
        this.repeat = count;
    }

    public boolean update(CPoint cp) {
        cp.add(setName, repeat);
        return true;
    }

    public String toString() {
        return "Add set" + setName;
    }
}

class CmdRemove implements Command {

    public boolean update(CPoint cp) {
        cp.remove();
        return true;
    }

    public String toString() {
        return "removed";
    }
}

class CmdSloppy implements Command {

    public boolean update(CPoint cp) {
        cp.getMotionController().setCoeffs(10f, 1f);
        return true;
    }

    public String toString() {
        return "sloppy";
    }
}

class CmdStiff implements Command {

    public boolean update(CPoint cp) {
        cp.getMotionController().setCoeffs(10, 3f);
        return true;
    }

    public String toString() {
        return "stiff";
    }
}

class CmdVeryStiff implements Command {

    public boolean update(CPoint cp) {
        cp.getMotionController().setCoeffs(20, 12);
        return true;
    }

    public String toString() {
        return "very stiff";
    }
}
