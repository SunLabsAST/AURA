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

package com.sun.labs.aura.grid;

import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface for a Jini service that will be used to start up other Aura
 * services on the same machine.  This is meant to be run on an EC2-style machine
 * instance.
 */
public interface ServiceStarter extends Component, Remote {

    /**
     * Starts another JVM with the <code>AuraService</code> defined by the
     * given configuration file and starter name.
     *
     * @param configFile the name of the configuration file for the service
     * @param starter the name of the starter to use from the configuration file
     * @return <code>true</code> if the JVM started, false otherwise.
     */
    public boolean start(String configFile, String starter) throws RemoteException;

}
