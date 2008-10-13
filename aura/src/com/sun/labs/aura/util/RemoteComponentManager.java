/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.util;

import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigurationManager;
import java.util.Collections;
import java.util.List;

/**
 * Manages connecting a reconnecting to a component
 */
public class RemoteComponentManager implements ComponentListener {
    private final static long DEFAULT_TIMEOUT =  10 * 60 * 1000L;

    private ConfigurationManager cm;
    private Class clazz;
    private final static long CONNECTION_DELAY = 10000;
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

    /**
     * Gets the component with the given name
     * @param name te name of the component
     * @param tmo the amount of time to wait for a component (in ms)
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after tmo milliseconds
     */
    public Component getComponent(long tmo) throws AuraException {
        try {
            if (component == null) {
                Component c = null;
                long endTime = System.currentTimeMillis() + tmo;
                while (c == null && System.currentTimeMillis() < endTime) {
                    c = lookup();
                    if (c == null) {
                        long remainingTime = endTime - System.currentTimeMillis();
                        long delay = CONNECTION_DELAY > remainingTime ? remainingTime : CONNECTION_DELAY;
                        Thread.sleep(delay);
                    }
                }
                component = c;
            }
        } catch (InterruptedException ex) {
        }

        if (component == null) {
            throw new AuraException("Can't connect to component of type " + clazz.getName());
        }
        return component;
    }


    private Component lookup() {
        List<Component> clist = cm.lookupAll(clazz, this);
        if (clist.size() > 0) {
            Collections.shuffle(clist);
            return clist.get(0);
        } else {
            return null;
        }
    }


    /**
     * Gets the component with the given name
     * @return the component
     * @throws com.sun.labs.aura.util.AuraException if the component could not
     *   be found after 10 minutes
     */
    public Component getComponent() throws AuraException {
        return getComponent(DEFAULT_TIMEOUT);
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
}
