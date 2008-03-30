/*
 * Copyright (c) 2003-2007 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.math.Ray;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

/**
 * <code>MousePick</code>
 * @author Mark Powell
 * @version
 */
public class MousePick extends MouseInputAction {
    private Camera camera;
    private Node scene;
    private float shotTime = 0;

    public MousePick(Camera camera, Node scene) {
        this.camera = camera;
        this.scene = scene;
    }
    /* (non-Javadoc)
     * @see com.jme.input.action.MouseInputAction#performAction(float)
     */
    public void performAction(InputActionEvent evt) {
        shotTime += evt.getTime();
        if( MouseInput.get().isButtonDown(0) && shotTime > 0.1f) {
            shotTime = 0;
            Ray ray = new Ray(camera.getLocation(), camera.getDirection()); // camera direction is already normalized
            PickResults results = new BoundingPickResults();
            results.setCheckDistance(true);
            scene.findPick(ray,results);

            if(results.getNumber() > 0) {
                Spatial spatial = results.getPickData(0).getTargetMesh().getParentGeom();
                CPoint cpoint = (CPoint) spatial.getUserData("cpoint");
                if (cpoint != null) {
                    cpoint.performAction(evt);
                }
            }
            results.clear();
        }
    }
}
