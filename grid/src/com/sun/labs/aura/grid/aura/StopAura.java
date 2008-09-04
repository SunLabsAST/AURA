/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.aura;

import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.logging.Level;

/**
 * An aura service that can be used to bring down Aura.
 */
public class StopAura extends Aura {
    
    @ConfigBoolean(defaultValue=false)
    public static final String PROP_DESTROY = "destroy";
    
    private boolean destroy;

    public String serviceName() {
        return "StopAura";
    }
    
    public void start() {
        logger.info("Stopping registrations");
        try {
            gu.stopProcess(getStatServiceName());
            logger.info("Stopped stat service");
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping Stat Service", e);
        }

        try {
            gu.stopProcess(getProcessManagerName());
            logger.info("Stopped process manager");
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping Stat Service", e);
        }

        for(String prefix : repFSMap.keySet()) {
            try {
                gu.stopProcess(getPartitionName(
                        prefix));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error stopping partition " + prefix, e);
            }
        }

        for(String prefix : repFSMap.keySet()) {
            try {
                gu.stopProcess(getReplicantName(
                        prefix));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error stopping replicant " + prefix, e);
            }
        }

        try {
            gu.stopProcess(getDataStoreHeadName(1));
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping data store head", e);
        }
        
        try {
            gu.stopProcess(getReggieName());
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping reggie", e);
        }

        try {
            gu.waitForFinish();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error waiting for processes to finish", e);
        }
        
        if(destroy) {
            try {
                gu.destroyRegistration(getStatServiceName());
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error destroying stat service", e);
            }
            
            for(String prefix : repFSMap.keySet()) {
                try {
                    gu.destroyRegistration(getPartitionName(
                            prefix));
                    gu.destroyRegistration(getReplicantName(
                            prefix));
                } catch(Exception e) {
                    logger.log(Level.SEVERE, "Error destroying replicant " +
                            prefix, e);
                }
            }

            try {
                gu.destroyRegistration(getDataStoreHeadName(1));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error destroying data store head", e);
            }

            try {
                gu.destroyRegistration(getProcessManagerName());
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error destroying process manager", e);
            }

            try {
                gu.destroyRegistration(getReggieName());
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error destroying reggie", e);
            }

       }
    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        destroy = ps.getBoolean(PROP_DESTROY);
    }
    
}
