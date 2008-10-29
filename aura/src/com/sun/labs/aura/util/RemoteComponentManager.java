/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.util;

import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigurationManager;
import java.util.logging.Logger;

/**
 * Manages connecting and reconnecting to a component
 */
public class RemoteComponentManager implements ComponentListener {
    private ConfigurationManager cm;
    private Class clazz;
    private Component component = null;

    /**
     * Creates a RemoteComponentManager
     * @param cm the configuration manager to use to fetch components
     * @param logger the logger to use (or null, in which case an anonymous logger will be used)
     */
    public RemoteComponentManager(ConfigurationManager cm, Class c) {
        this.cm = cm;
        this.clazz = c;
    }

    private Component lookup() {
        return cm.lookup(clazz, this);
    }

    /**
     * Gets the component with the given name
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after 10 minutes
     */
    public Component getComponent() throws AuraException {
        if(component == null) {
            component = cm.lookup(clazz, this);
            Logger.getLogger("").info("Got component: " + component);
        }
        return component;
    }


    @Override
    public void componentAdded(Component arg0) {
    }

    @Override
    public void componentRemoved(Component componentToRemove) {
        if (component == componentToRemove) {
            component = null;
        }
    }

    public void shutdown() {
        cm.shutdown();
    }
}
