/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.logging.Level;

/**
 *
 */
public class StartDataStore extends Aura {
    
    @ConfigInteger(defaultValue=2)
    public static final String PROP_INSTANCE_NUMBER = "instanceNumber";
    private int instanceNumber;

    @Override
    public String serviceName() {
        return "StartDataStore";
    }

    public void start() {
        try {
            ProcessRegistration dsHeadReg =
                    gu.createProcess(
                    getDataStoreHeadName(instanceNumber),
                    getDataStoreHeadConfig(instanceNumber));
            gu.startRegistration(dsHeadReg);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error starting data store", ex);
        }

    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        instanceNumber = ps.getInt(PROP_INSTANCE_NUMBER);
    }

}
