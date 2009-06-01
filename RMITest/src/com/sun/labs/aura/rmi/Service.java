package com.sun.labs.aura.rmi;

import com.sun.labs.aura.AuraService;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.logging.Logger;

/**
 *
 */
public class Service implements AuraService, Configurable {

    @ConfigComponent(type=com.sun.labs.aura.rmi.ServerImpl.class)
    public static final String PROP_SERVER="server";
    
    private Server server;

    private ConfigurationManager cm;
    
    public void start() {
    }

    public void stop() {
        cm.shutdown();
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        Logger logger = ps.getLogger();
        logger.info(String.format("In newProps"));
        cm = ps.getConfigurationManager();
        server = (Server) ps.getComponent(PROP_SERVER);
    }

}
