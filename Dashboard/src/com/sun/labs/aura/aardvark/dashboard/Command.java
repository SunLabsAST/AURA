/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard;

/**
 *
 * A command that can be sent to a CPoint
 */

public interface Command {
    /**
     * Command that manipulates a CPoint
     * @param cp the point to be manipulated
     * @return true if we are done with this update
     */
    public boolean update(CPoint cp);
}