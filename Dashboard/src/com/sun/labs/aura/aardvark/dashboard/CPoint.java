/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard;

import com.jme.bounding.BoundingBox;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.InputActionInterface;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import com.jmex.physics.DynamicPhysicsNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;



/**
 *
 * @author plamere
 */
public class CPoint extends Controller implements InputActionInterface {
    private DynamicPhysicsNode dnp;
    private ControlledPhysicsNode cpn;
    private Spatial spatial;
    private final static float defaultTick = .1f;
    private float curTick;
    private float curTime;
    private float lifeTime;
    private Queue<Command> commandQueue = new ConcurrentLinkedQueue<Command>();
    private Map<String, Command[]> cmdSetMap = new HashMap<String, Command[]>();
    private List<ActionHandler> actionHandlers = new ArrayList<ActionHandler>();

    public CPoint(DynamicPhysicsNode dnp, Spatial spatial) {
        this.dnp = dnp;
        this.cpn = new ControlledPhysicsNode(dnp);
        this.spatial = spatial;
        dnp.addController(this);
        dnp.attachChild(spatial);
        dnp.generatePhysicsGeometry();
        spatial.setModelBound(new BoundingBox());
        spatial.updateModelBound();
        spatial.setUserData("cpoint", this);
    }

    public DynamicPhysicsNode getDNP() {
        return dnp;
    }

    public void remove() {
        dnp.removeFromParent();
    }

    public ControlledPhysicsNode getCPN() {
        return cpn;
    }

    public void poke() {
        cpn.setJiggle(true);
    }

    public void unpoke() {
        cpn.setJiggle(false);
    }

    public void add(Command[] cmds) {
        for (Command cmd : cmds) {
            add(cmd);
        }
    }

    public void add(Command cmd) {
        commandQueue.add(cmd);
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
                commandQueue.remove();
                curTime = 0f;
            }
        }
    }

    @Override
    public void update(float time) {
        lifeTime += time;
        curTime += time;
        curTick += time;
        if (curTick > defaultTick) {
            curTick = 0;
            checkQueue();
        }
    }


    public synchronized void performAction(InputActionEvent evt) {
        for  (ActionHandler ah : actionHandlers) {
            ah.performAction(this, evt);
        }
    }

    public synchronized void addActionHandler(ActionHandler ah) {
        actionHandlers.add(ah);
    }

    public synchronized void removeActionHandler(ActionHandler ah) {
        actionHandlers.remove(ah);
    }


}

class CmdWait implements Command {
    private float delay;

    CmdWait(float delay) {
        this.delay = delay;
    }

    public boolean update(CPoint cp) {
        return (cp.getCurTime() > delay);
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
        cp.getCPN().setSetPoint(v);
        return true;
    }
}

class WaitSettled implements Command {
    public boolean update(CPoint cp) {
        return cp.getCPN().isSettled();
    }
}

class CmdGravity implements Command {
    private boolean gravityOn;

    CmdGravity(boolean on) {
        this.gravityOn = on;
    }

    public boolean update(CPoint cp) {
        cp.getDNP().setAffectedByGravity(gravityOn);
        return true;
    }
}

class CmdControl implements Command {
    private boolean cmdControl;

    CmdControl(boolean on) {
        this.cmdControl = on;
    }

    public boolean update(CPoint cp) {
        cp.getCPN().setEnable(cmdControl);
        return true;
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
}

class CmdRemove implements Command {
    public boolean update(CPoint cp) {
        cp.remove();
        return true;
    }
}

class CmdSloppy implements Command {
    public boolean update(CPoint cp) {
        cp.getCPN().setCoeffs(500f, 100f);
        return true;
    }
}

class CmdStiff implements Command {
    public boolean update(CPoint cp) {
        cp.getCPN().setCoeffs(500f, 300f);
        return true;
    }
}