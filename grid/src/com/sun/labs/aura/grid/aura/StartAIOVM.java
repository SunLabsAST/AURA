/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessRegistration;
import java.util.logging.Level;

/**
 *
 */
public class StartAIOVM extends Aura {

    @Override
    public String serviceName() {
        return "StartAIOVM";
    }

    public void start() {
        try {
            //
            // Get a reggie started up first thing
            ProcessRegistration regReg =
                    gu.createProcess(getReggieName(), getReggieConfig());
            gu.startRegistration(regReg);

            //
            // Next, get a data store head and start it
            ProcessRegistration dsReg =
                    gu.createProcess(getAIOVMName(), getAIOVMConfig());
            gu.startRegistration(dsReg);
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Erorr starting AIOVM data store", ex);
        }
    }

    public void stop() {
    }

}
