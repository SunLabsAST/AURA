/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.input.KeyBindingManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class KeyboardHandler {
    private Map<String, HandlerTracker> map = new HashMap<String, HandlerTracker>();

    public void addKeyHandler(int key, String opName, KeyActionHandler handler) {
        KeyBindingManager.getKeyBindingManager().add(opName, key);
        map.put(opName, new HandlerTracker(opName, handler));
    }

    public void update(float time) {
        KeyBindingManager kbm = KeyBindingManager.getKeyBindingManager();
        for (HandlerTracker t : map.values()) {
            if (kbm.isValidCommand(t.opName, true)) {
                if (t.ticks == 0) {
                    System.out.println("key " + t.opName);
                    t.handler.onKey(t.opName);
                }
                t.ticks++;
            } else {
                t.ticks = 0;
            }
        }
    }

    class HandlerTracker {
        String opName;
        KeyActionHandler handler;
        int ticks;

        public HandlerTracker(String opName, KeyActionHandler handler) {
            this.opName = opName;
            this.handler = handler;
        }
        
    }
}