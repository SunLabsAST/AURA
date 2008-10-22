/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.labs.aura.datastore.impl.DataStoreHead;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.ProcessManager;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * An aura service that can be used to bring down Aura.
 */
public class StopAura extends Aura {
    
    @ConfigBoolean(defaultValue=false)
    public static final String PROP_DESTROY = "destroy";

    @ConfigBoolean(defaultValue=false)
    public static final String PROP_STOP_REGGIE = "stopReggie";

    private boolean stopReggie;
    
    private boolean destroy;
    
    private static String[] stopOrder = {
        ProcessManager.class.getName(),
        Replicant.class.getName(),
        PartitionCluster.class.getName(),
        DataStoreHead.class.getName(),
        StatService.class.getName(),
        "reggie",
        "other"
    };
    
    public String serviceName() {
        return "StopAura";
    }
    
    public void start() {
        Collection<ProcessRegistration> regs;
        String ourName = GridFactory.getProcessContext().getProcessRegistrationName();
        try {
             regs = gu.getGrid().
                    findProcessRegistrations(new ProcessRegistrationFilter.IncarnationMetaMatch(
                    Pattern.compile("instance"), Pattern.compile(instance)));
        } catch(RemoteException ex) {
            logger.log(Level.SEVERE, "Error getting registrations from grid", ex);
            return;
        }
        
        Map<String,List<ProcessRegistration>> m = new HashMap();
        for(ProcessRegistration reg : regs) {
            try {
                //
                // Don't stop ourself!
                if(reg.getName().equals(ourName)) {
                    continue;
                }
                Map<String,String> md = reg.getIncarnationConfiguration().getMetadata();
                String type = md == null ? "other" : md.get("type");
                List<ProcessRegistration> l = m.get(type);
                if(l == null) {
                    l = new ArrayList();
                    m.put(type, l);
                }
                l.add(reg);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error stopping registration " + reg.getName(), ex);
            }
        }
        
        List<ProcessRegistration> stopped = new ArrayList();
        
        for(String name : stopOrder) {
            List<ProcessRegistration> l = m.get(name);
            if(l == null) {
                continue;
            }

            if(name.equals("reggie") && !stopReggie) {
                continue;
            }
            
            for(ProcessRegistration reg : l) {
                try {
                    gu.stopProcess(reg);
                    stopped.add(reg);
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, "Error stopping registration " + reg.getName(), ex);
                }
            }
        }

        try {
            logger.info("Waiting for processes to finish");
            gu.waitForFinish();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error waiting for processes to finish", e);
        }
        
        if(destroy) {
            for(ProcessRegistration reg : stopped) {
                try {
                    logger.info("Destroying " + reg.getName());
                    reg.destroy(100000);
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, "Error destroying registration " +
                            reg.getName(), ex);
                }
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
