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

        for(String prefixCode : prefixCodeList) {
            try {
                gu.stopProcess(getPartitionName(
                        prefixCode));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error stopping partition " + prefixCode, e);
            }
        }

        for(String prefixCode : prefixCodeList) {
            try {
                gu.stopProcess(getReplicantName(
                        prefixCode));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error stopping replicant " + prefixCode, e);
            }
        }

        try {
            gu.stopProcess(getDataStoreHeadName());
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
            
            for(String prefixCode : prefixCodeList) {
                try {
                    gu.destroyRegistration(getPartitionName(
                            prefixCode));
                    gu.destroyRegistration(getReplicantName(
                            prefixCode));
                } catch(Exception e) {
                    logger.log(Level.SEVERE, "Error destroying replicant " +
                            prefixCode, e);
                }
            }

            try {
                gu.destroyRegistration(getDataStoreHeadName());
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
