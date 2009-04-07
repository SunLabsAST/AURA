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

import com.sun.labs.aura.aardvark.dashboard.*;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;

/**
 *
 * @author plamere
 */
public class MotionController extends Controller {

    private Node controlledNode;
    private float kp = 500.0f;
    private float kd = 300;
    private Vector3f maxForce = new Vector3f(2, 2, 2);
    private Vector3f maxVel = new Vector3f(100, 100, 100);
    private Vector3f setPoint = new Vector3f();
    private Vector3f vel = new Vector3f();
    private Vector3f force = new Vector3f();
    private Vector3f error = new Vector3f();
    private Vector3f deltaError = new Vector3f();
    private float totTime = 0;
    private boolean isSettled = false;
    private float minPosError = .03f;
    private float minVel = .03f;
    private boolean enable = false;
    private boolean gravityEnabled = false;
    private float gravityForce = -1.0f;
    private final static float JIGGLE_PERIOD = .3f;
    private final static float JIGGLE_RANGE = .1f;
    private final static float JIGGLE_OFFSET = .2f;
    private boolean jiggleEnabled;
    private float jiggleTime = 0;

    public MotionController(Node node, float initX, float initY, float initZ) {
        this.controlledNode = node;
        controlledNode.setLocalTranslation(initX, initY, initZ);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
        if (!enable) {
            force.zero();
        } else {
            setPoint.set(getCurPosition());

        }
    }

    public void setAffectedByGravity(boolean enable) {
        gravityEnabled = enable;

    }

    public boolean isEnable() {
        return enable;
    }

    private Vector3f getCurPosition() {
        //return controlledNode.getLocalTranslation();
        return controlledNode.getWorldTranslation();
    }

    public Node getNode() {
        return controlledNode;
    }

    public Vector3f getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(Vector3f setPoint) {
        isSettled = false;
        this.setPoint.set(setPoint);
    }

    public void setLinearVelocity(Vector3f v) {
        vel.set(v);
    }

    public void setCoeffs(float kp, float kd) {
        this.kp = kp;
        this.kd = kd;
    }

    public void stop() {
        force.zero();
        vel.zero();
    }

    public void setJiggle(boolean enable) {
        this.jiggleEnabled = enable;
    }

    @Override
    public void update(float time) {
        totTime += time;
        if (totTime > 0) {
            Vector3f cur = getCurPosition();
            if (enable) {
                float freq = 1f / totTime;

                error.set(setPoint);

                if (jiggleEnabled) {
                    jiggleTime += time;

                    if (jiggleTime > JIGGLE_PERIOD * 2) {
                        jiggleTime -= JIGGLE_PERIOD * 2;
                    }

                    float t = jiggleTime <= JIGGLE_PERIOD ? jiggleTime : 
                            JIGGLE_PERIOD - (jiggleTime - JIGGLE_PERIOD);
                    float jigScale = 1 - (2 * t / JIGGLE_PERIOD);
                    error.z += jigScale * JIGGLE_RANGE + JIGGLE_OFFSET;
                }
                error.subtractLocal(cur);
                deltaError.subtractLocal(error);

                force.x = kp * error.x - freq * kd * deltaError.x;
                force.y = kp * error.y - freq * kd * deltaError.y;
                force.z = kp * error.z - freq * kd * deltaError.z;

                force.multLocal(totTime);
                clamp(force, maxForce);


                deltaError.set(error);

            }

            if (gravityEnabled) {
                force.addLocal(0, gravityForce * totTime, 0);
            }

            vel.addLocal(force);
            clamp(vel, maxVel);
            dump("f ", force);
            dump("v ", vel);

            // quick and dirty settled calculation - manhattan distance check on
            // error and force vector
            if (FastMath.abs(error.x) + FastMath.abs(error.y) + FastMath.abs(error.z) < minPosError) {
                isSettled = FastMath.abs(vel.x) + FastMath.abs(vel.y) + FastMath.abs(vel.z) < minVel;
            } else {
                isSettled = false;
            }
            controlledNode.setLocalTranslation(cur.x + vel.x * totTime, cur.y + vel.y * totTime, cur.z + vel.z * totTime);
            controlledNode.updateWorldVectors();
            totTime = 0;
        }
    //controlledNode.setWorldTranslation(cur.x + vel.x * totTime, cur.y + vel.y * totTime, cur.z + vel.z * totTime);
    }

    public boolean isSettled() {
        return isSettled;
    }

    void dump(String m, Vector3f v) {
        if (false) System.out.println(m + v.x + "," + v.y + "," + v.z);
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
