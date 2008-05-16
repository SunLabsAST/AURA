/*
 *   Copyright 2008 Sun Microsystems, Inc. All rights reserved
 *   Use is subject to license terms.
 */

package com.sun.labs.aura;

import com.sun.labs.util.props.Component;

/**
 * An interface for starting and stopping Aardvark services.
 * 
 * @see AardvarkServiceStarter
 */
public interface AuraService extends Component {
    
    /**
     * Starts the service.
     */
    public void start();
    
    /**
     * Stops the service.
     */
    public void stop();

}
