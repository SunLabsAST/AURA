/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.aardvark;

import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.*;
import com.sun.caroline.platform.ProcessRegistration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

/**
 *
 */
public class StopAardvark extends Aardvark {

    @Override
    public String serviceName() {
        return "StopAardvark";
    }

    private void stopAardvarkProcesses() {
        Queue<ProcessRegistration> q = new LinkedList<ProcessRegistration>();

        try {
            q.add(gu.stopProcess( getAAName()));
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error stopping Aardvark", ex);
        }
        for(int i = 0; i < 6; i++) {
            try {
                q.add(gu.stopProcess( getFMName(i)));
            } catch(Exception ex) {
                logger.log(Level.SEVERE, "Error stopping feed manager " + i, ex);
            }
        }

        try {

            q.add(gu.stopProcess( getSchedName()));
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error stopping feed scheduler", ex);
        }

        try {
            q.add(gu.stopProcess( getRecName()));
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error stopping recommender manager", ex);
        }

        try {
            gu.waitForFinish(q);
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error waiting for processes to finish", ex);
        }

    }

    public void start() {
        stopAardvarkProcesses();
    }

    public void stop() {
    }
}
