
package com.sun.labs.aura.service;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.impl.store.BerkeleyDataWrapper;
import com.sun.labs.aura.service.persist.SessionKey;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * Implementation of the login service for tracking user sessions.  The service
 * is persistent across reboots, using a berkeley database as its backing
 * store.
 */
public class LoginServiceImpl implements LoginService, AuraService, Configurable {
    /**
     * The location of the BDB/JE Database Environment
     */
    @ConfigString(defaultValue = "/tmp/aura")
    public final static String PROP_DB_ENV = "dbEnv";
    protected String dbEnvDir;

    /**
     * A logger for messages/debug info
     */
    protected Logger logger;

    /**
     * The max number of times to retry a deadlocked transaction before
     * admitting failure.
     */
    protected final static int MAX_DEADLOCK_RETRIES = 10;

    /**
     * The actual database environment.
     */
    protected Environment dbEnv;

    /**
     * The store inside the environment where our index will live
     */
    protected EntityStore store;

    /**
     * The primary index, accessible by session key string
     */
    protected PrimaryIndex<String,SessionKey> sessionKeyByKey;

    /**
     * The index of sessions keys, accessible by user
     */
    protected SecondaryIndex<String,String,SessionKey> sessionKeyByUserKey;

    /**
     * The index of session keys, accessible by app
     */
    protected SecondaryIndex<String,String,SessionKey> sessionKeyByAppKey;

    /**
     * The index of session keys, accessible by expiration date
     */
    protected SecondaryIndex<Long,String,SessionKey> sessionKeyByExpDate;

    public LoginServiceImpl() {

    }

    @Override
    public void start() {
        
    }

    @Override
    public void stop() {
        
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();

        //
        // Get or create the database environment
        dbEnvDir = ps.getString(PROP_DB_ENV);
        File f = new File(dbEnvDir);
        if(!f.exists() && !f.mkdirs()) {
            throw new PropertyException(ps.getInstanceName(), PROP_DB_ENV,
                    "Unable to create new directory for db");
        }

        //
        // Configure and open the environment and entity store
        try {
            logger.info("Opening DB: " + dbEnvDir);
            EnvironmentConfig econf = new EnvironmentConfig();
            StoreConfig sconf = new StoreConfig();

            econf.setAllowCreate(true);
            econf.setTransactional(true);

            sconf.setAllowCreate(true);
            sconf.setTransactional(true);

            dbEnv = new Environment(f, econf);
            store = new EntityStore(dbEnv, "Login", sconf);
            sessionKeyByKey =
                    store.getPrimaryIndex(String.class, SessionKey.class);
            sessionKeyByUserKey =
                    store.getSecondaryIndex(sessionKeyByKey,
                                            String.class,
                                            "userKey");
            sessionKeyByAppKey =
                    store.getSecondaryIndex(sessionKeyByKey,
                                            String.class,
                                            "appKey");
            sessionKeyByExpDate =
                    store.getSecondaryIndex(sessionKeyByKey,
                                            Long.class,
                                            "expDate");

            logger.info("Finished opening DB");
        } catch(DatabaseException e) {
            logger.severe("Failed to load the database environment at " +
                    dbEnvDir + ": " + e);
        }
    }

    @Override
    public SessionKey newUserSessionKey(String userKey, String appKey) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SessionKey getUserSessionKey(String userKey, String appKey) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SessionKey getUserSessionKey(String sessionKey) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
