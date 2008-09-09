package com.sun.labs.aura.grid.aura;

import java.util.logging.Level;

/**
 * An on-grid class to deploy Aura.
 */
public class CreateAura extends Aura {

    public String serviceName() {
        return "CreateAura";
    }

    public void start() {
        try {
            createReplicantFileSystems();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error starting Aura", e);
        }
    }

    public void stop() {
    }
}
