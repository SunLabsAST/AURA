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

package com.sun.labs.aura.util;

import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ConfigurationManager;
import java.util.List;
import java.util.logging.Logger;

/**
 * A component manager that will handle multiple instances of a given component
 * type and hand them out in a round-robin fashion when requested.
 */
public class RemoteMultiComponentManager extends RemoteComponentManager {

    private Component[] components;

    int p;

    Logger logger = Logger.getLogger(getClass().getName());
    
    public RemoteMultiComponentManager(ConfigurationManager cm, Class c) {
        super(cm, c);
    }

    private synchronized void getComponents() {
        List<Component> l = cm.lookupAll(clazz, this);
        if(l != null) {
            components = l.toArray(new Component[0]);
        }
    }
    
    /**
     * Gets the component with the given name
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after 10 minutes
     */
    public Component getComponent() throws AuraException {
        if(components == null) {
            getComponents();
        }
        synchronized(this) {
            p %= components.length;
            return components[p++];
        }
    }

    @Override
    public void componentAdded(Component added) {
        logger.info(String.format("Added: " + added));
        getComponents();
    }

    @Override
    public void componentRemoved(Component componentToRemove) {
        logger.info(String.format("Removed: " + componentToRemove));
        getComponents();
    }

    public void shutdown() {
        cm.shutdown();
    }
}
