/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.sitm;

import java.util.logging.Level;

/**
 *
 */
public class StopSITM extends SITM {

    public void start() {
        try {
            gu.stopProcess(getArtistCrawlerName());
            gu.stopProcess(getListenerCrawlerName());
            gu.stopProcess(getTagCrawlerName());
            gu.waitForFinish(50000);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error stopping SITM", e);
        }
    }

    public void stop() {
    }

}
