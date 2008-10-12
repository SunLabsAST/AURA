/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.util;

import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigurationManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages connecting a reconnecting to a component
 */
public class RemoteComponentManager implements ComponentListener {
    private final static long DEFAULT_TIMEOUT =  10 * 60 * 1000L;
    private ConfigurationManager cm;
    private final static long CONNECTION_DELAY = 10000;
    private Map<String, Component> componentMap;

    /**
     * Creates a RemoteComponentManager
     * @param cm the configuration manager to use to fetch components
     * @param logger the logger to use (or null, in which case an anonymous logger will be used)
     */
    public RemoteComponentManager(ConfigurationManager cm) {
        this.cm = cm;
        componentMap = Collections.synchronizedMap(new HashMap<String, Component>());
    }

    /**
     * Gets the component with the given name
     * @param name te name of the component
     * @param tmo the amount of time to wait for a component (in ms)
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after tmo milliseconds
     */
    public Component getComponent(String name, long tmo) throws AuraException {
        Component component = null;
        try {
            component = componentMap.get(name);
            if (component == null) {
                long endTime = System.currentTimeMillis() + tmo;
                while (component == null && System.currentTimeMillis() < endTime) {
                    component = cm.lookup(name, this);
                    if (component == null) {
                        long remainingTime = endTime - System.currentTimeMillis();
                        long delay = CONNECTION_DELAY > remainingTime ? remainingTime : CONNECTION_DELAY;
                        Thread.sleep(delay);
                    }
                }

                if (component != null) {
                    componentMap.put(name, component);
                }
            }

        } catch (InterruptedException ex) {
        }

        if (component == null) {
            throw new AuraException("Can't connect to " + name);
        }
        return component;
    }


    /**
     * Gets the component with the given name
     * @param name te name of the component
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after 10 minutes
     */
    public Component getComponent(String name) throws AuraException {
        return getComponent(name, DEFAULT_TIMEOUT);
    }


    @Override
    public void componentAdded(Component arg0) {
    }

    @Override
    public void componentRemoved(Component componentToRemove) {
        for (String key : componentMap.keySet()) {
            Component c = componentMap.get(key);
            if (c == componentToRemove) {
                componentMap.remove(key);
            }
        }
    }
}
