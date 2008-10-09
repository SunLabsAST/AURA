/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.logging.Logger;

/**
 * A simple wrapper that starts up a shell with the aura commands
 */
public class AuraShell implements AuraService, Configurable {
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStoreHead";

    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";

    private DataStore dataStore;
    private CommandInterpreter shell;    
    @SuppressWarnings(value="URF_UNREAD_FIELD")
    private Logger logger;
    @SuppressWarnings(value="URF_UNREAD_FIELD")
    private ShellUtils sutils;
    private StatService statService;

    
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("aura% ");
        sutils = new ShellUtils(shell, dataStore, statService);
        Thread t = new Thread() {

            public void run() {
                shell.run();
                shell = null;
            }
        };
        t.start();
    }
    
    public void stop() {
        if (shell != null) {
            shell.close();
        }
    }
    
    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        logger = ps.getLogger();
    }

}
