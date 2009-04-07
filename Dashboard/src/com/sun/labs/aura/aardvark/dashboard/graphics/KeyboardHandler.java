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
