/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.input.action.InputActionEvent;

/**
 *
 * @author plamere
 */
interface ActionHandler {
    public void performAction(CPoint cp, InputActionEvent evt);
}
