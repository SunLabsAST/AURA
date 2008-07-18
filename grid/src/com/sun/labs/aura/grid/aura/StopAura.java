/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessRegistration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

/**
 * An aura service that can be used to bring down Aura.
 */
public class StopAura extends Aura {

    public String serviceName() {
        return "StopAura";
    }
    
    public void start() {
        logger.info("Stopping registrations");
        Queue<ProcessRegistration> q = new LinkedList<ProcessRegistration>();
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
    }

    public void stop() {
    }
}
