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
