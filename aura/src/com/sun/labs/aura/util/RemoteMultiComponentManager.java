/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
