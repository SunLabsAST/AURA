/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.aura;

import java.util.logging.Level;

/**
 * An aura service that can be used to bring down Aura.
 */
public class StopAIOVM extends Aura {

    public String serviceName() {
        return "StopAIOVM";
    }
    
    public void start() {
        logger.info("Stopping registrations");
        try {
            gu.stopProcess(getAIOVMName());
            logger.info("Stopped AIOVM data store");
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping AIOVM data store", e);
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
