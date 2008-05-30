/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid;

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
        Queue<ProcessRegistration> q = new LinkedList<ProcessRegistration>();
        try {
            q.add(GridUtil.stopProcess(grid, getStatServiceName()));
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping Stat Service", e);
        }

        for(String prefixCode : prefixCodeList) {
            try {
                q.add(GridUtil.stopProcess(grid, getPartitionName(
                        prefixCode)));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error stopping partition " + prefixCode, e);
            }
        }

        for(String prefixCode : prefixCodeList) {
            try {
                q.add(GridUtil.stopProcess(grid, getReplicantName(
                        prefixCode)));
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error stopping replicant " + prefixCode, e);
            }
        }

        try {
            q.add(GridUtil.stopProcess(grid, getDataStoreHeadName()));
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping data store head", e);
        }
        
        try {
            q.add(GridUtil.stopProcess(grid, getReggieName()));
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping reggie", e);
        }

        try {
            GridUtil.waitForFinish(q);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error waiting for processes to finish", e);
        }
    }

    public void stop() {
    }
}
