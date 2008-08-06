/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.ProcessManager;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.RemoteException;

/**
 *
 */
public class GridProcessManager extends Aura implements ProcessManager {

    @Override
    public String serviceName() {
        return "GridProcessManager";
    }
    
    public void start() {
    }

    public void stop() {
    }

    public PartitionCluster createPartitionCluster(DSBitSet prefix) throws AuraException, RemoteException {
        try {
            ProcessConfiguration pcConfig = getPartitionClusterConfig(prefix.
                    toString());
            ProcessRegistration pcReg = gu.createProcess(getPartitionName(
                    prefix.toString()), pcConfig);
            while(pcReg.getRunState() != RunState.RUNNING) {
                pcReg.waitForStateChange(1000000L);
            }
            
            //
            // Now it should be registered with the Jini server, let's look up the partition clusters
            // and get the one with the right prefix.
            Component[] components = cm.getComponentRegistry().lookup(com.sun.labs.aura.datastore.impl.PartitionCluster.class, Integer.MAX_VALUE);
            for(Component c : components) {
                if(((PartitionCluster) c).getPrefix().equals(prefix)) {
                    return (PartitionCluster) c;
                }
            }
            return null;
        } catch(Exception ex) {
            throw new AuraException("Error getting partition cluster for prefix " +
                    prefix, ex);
        }
    }

    public Replicant createReplicant(DSBitSet prefix) throws AuraException, RemoteException {
        try {
            ProcessConfiguration repConfig = getReplicantConfig(replicantConfig,
                    prefix.toString());
            ProcessRegistration pcReg = gu.createProcess(getReplicantName(
                    prefix.toString()), repConfig);
            while(pcReg.getRunState() != RunState.RUNNING) {
                pcReg.waitForStateChange(1000000L);
            }
            //
            // Now it should be registered with the Jini server, let's look up the replicants
            // and get the one with the right prefix.
            Component[] components = cm.getComponentRegistry().lookup(
                    com.sun.labs.aura.datastore.impl.Replicant.class,
                    Integer.MAX_VALUE);
            for(Component c : components) {
                if(((Replicant) c).getPrefix().equals(prefix)) {
                    return (Replicant) c;
                }
            }
            return null;
        } catch(Exception ex) {
            throw new AuraException("Error getting partition cluster for prefix " +
                    prefix, ex);
        }
    }

}
