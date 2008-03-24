/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;


/**
 *
 * @author plamere
 */
public class ControlledPhysicsNode extends Controller {

    private DynamicPhysicsNode dpn;
  //  private float kp = 50.0f;
   // private float kd = 10;

    // sloppy
    //private float kp = 500.0f;
    //private float kd = 100;

    // stiff
    private float kp = 500.0f;
    private float kd = 300;

    private Vector3f maxForce = new Vector3f(100, 100, 100);
    private Vector3f setPoint = new Vector3f();
//    private Vector3f lastError = new Vector3f();

    private Vector3f force = new Vector3f();
    private Vector3f error = new Vector3f();
    private Vector3f deltaError = new Vector3f();

    private int lastTick = 0;
    private float totTime = 0;
    private boolean isSettled = false;
    private float minPosError = .03f;
    private float minForce = .03f;

    private boolean enable = true;
    private boolean jiggleEnabled;
    private final static int JIGGLE_PERIOD = 20;
    private Vector3f jiggleVector = new Vector3f(0, .25f, 0);

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public ControlledPhysicsNode(DynamicPhysicsNode dpn) {
        setDynamicPhysicsNode(dpn);
        dpn.setAffectedByGravity(false);
    }

    private Vector3f getCurPosition() {
        dpn.updateWorldVectors();
        return dpn.getWorldTranslation();
    }

    public DynamicPhysicsNode getDynamicPhysicsNode() {
        return dpn;
    }

    public void setDynamicPhysicsNode(DynamicPhysicsNode dpn) {
        this.dpn = dpn;
        PTickManager.install(dpn.getSpace());
        dpn.addController(this);
        setPoint.set(getCurPosition());
    }

    public Vector3f getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(Vector3f setPoint) {
        isSettled = false;
        this.setPoint = setPoint;
    }

    public void setCoeffs(float kp, float kd) {
        this.kp = kp;
        this.kd = kd;
    }

    public void setJiggle(boolean enable) {
        this.jiggleEnabled = enable;
    }

    @Override
    public void update(float time) {
        totTime += time;
        if (enable && PTickManager.ptick != lastTick && totTime > 0) {
            float freq = 1f / totTime;
            lastTick = PTickManager.ptick;
            Vector3f cur = getCurPosition();

            error.set(setPoint);
           
            if (jiggleEnabled) {
                boolean upPhase = (lastTick / JIGGLE_PERIOD) % 2 == 1;
                if (upPhase) {
                    error.addLocal(jiggleVector);
                } else {
                    error.subtractLocal(jiggleVector);
                }
            }
            error.subtractLocal(cur);
            deltaError.subtractLocal(error);

            force.x = kp * error.x - freq * kd * deltaError.x;
            force.y = kp * error.y - freq * kd * deltaError.y;
            force.z = kp * error.z - freq * kd * deltaError.z;

            force.multLocal(totTime);
            clamp(force, maxForce);

            dpn.clearForce();
            dpn.addForce(force);

            deltaError.set(error);
            totTime = 0;
           
            // quick and dirty settled calculation - manhattan distance check on
            // error and force vector
            if (FastMath.abs(error.x) + FastMath.abs(error.y) + FastMath.abs(error.z) < minPosError) {
                isSettled = FastMath.abs(force.x) + FastMath.abs(force.y) + FastMath.abs(force.z) < minForce;
            } else {
                isSettled = false;
            }
        } 
    }

    public boolean isSettled() {
        return isSettled;
    }

    void dump(String m, Vector3f v) {
        System.out.println(m + v.x + "," + v.y + "," + v.z);
    }

    /**
     * Clamp the values
     * @param v the values to clamp
     * @param range the ranges
     */
    private void clamp(Vector3f v, Vector3f range) {
        v.x = v.x > range.x ? range.x : v.x < -range.x ? -range.x : v.x;
        v.y = v.y > range.y ? range.y : v.y < -range.y ? -range.y : v.y;
        v.z = v.z > range.z ? range.z : v.z < -range.z ? -range.z : v.z;
    }
}
class PTickManager implements PhysicsUpdateCallback {

    static int ptick = 0;

    public void beforeStep(PhysicsSpace space, float time) {
    }

    public void afterStep(PhysicsSpace space, float time) {
        ptick++;
    }

    static void install(PhysicsSpace space) {
        if (ptick == 0) {
            ptick++;
            space.addToUpdateCallbacks(new PTickManager());
        }
    }
}
