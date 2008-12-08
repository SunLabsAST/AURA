
package com.sun.labs.aura.service;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.service.persist.SessionKey;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.logging.Level;
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
    public SessionKey newUserSessionKey(String userKey, String appKey)
            throws RemoteException, AuraException {
        SessionKey existing = getSK(userKey, appKey);
        if (existing != null) {
            if (existing.isExpired()) {
                deleteSK(existing);
            } else {
                throw new AuraException("Key already exists for user/app combo");
            }
        }
        SessionKey newKey = new SessionKey(userKey, appKey);
        newKey = putSK(newKey);
        return newKey;
    }

    @Override
    public SessionKey getUserSessionKey(String userKey, String appKey) throws RemoteException {
        return getSK(userKey, appKey);
    }

    @Override
    public SessionKey getUserSessionKey(String sessionKey) throws RemoteException {
        return getSK(sessionKey);
    }

    protected SessionKey getSK(String userKey, String appKey) {
        EntityJoin<String, SessionKey> join = new EntityJoin(sessionKeyByKey);
        join.addCondition(sessionKeyByUserKey, userKey);
        join.addCondition(sessionKeyByAppKey, appKey);

        try {
            ForwardCursor<SessionKey> cur = null;
            Transaction txn = null;
            try {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                cur = join.entities(txn, CursorConfig.READ_UNCOMMITTED);
                Iterator<SessionKey> it = cur.iterator();
                if (it.hasNext()) {
                    return it.next();
                } else {
                    return null;
                }
            } finally {
                if(cur != null) {
                    cur.close();
                }
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch (DatabaseException e) {
            logger.log(Level.WARNING, "Failed to look up session key for " + userKey + ", " + appKey, e);
        }
        return null;
    }

    protected SessionKey getSK(String sessionKey) {
        SessionKey ret = null;
        try {
            Transaction txn = null;
            try {
                TransactionConfig conf = new TransactionConfig();
                conf.setReadUncommitted(true);
                txn = dbEnv.beginTransaction(null, conf);
                ret = sessionKeyByKey.get(txn, sessionKey, LockMode.READ_UNCOMMITTED);
            } finally {
                if (txn != null) {
                    txn.commitNoSync();
                }
            }
        } catch (DatabaseException e) {
            logger.log(Level.WARNING, "Failed to look up session key " + sessionKey, e);
        }
        return ret;
    }

    protected SessionKey putSK(SessionKey newKey) throws AuraException {
        SessionKey ret = null;
        int numRetries = 0;
        while(numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                ret = sessionKeyByKey.put(txn, newKey);
                txn.commit();
                return ret;
            } catch(DeadlockException e) {
                try {
                    txn.abort();
                    logger.finest("Deadlock detected in putting " + newKey.getSessionKey() + ": " + e.getMessage());
                    numRetries++;
                } catch(DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch(Exception e) {
                try {
                    if(txn != null) {
                        txn.abort();
                    }
                } catch(DatabaseException ex) {
                }
                throw new AuraException("putItem transaction failed", e);
            }
        }
        throw new AuraException("putItem failed for " +
                newKey.getUserKey() + ":" + newKey.getAppKey() +
                " after " + numRetries + " retries");
    }

    protected void deleteSK(SessionKey sk) throws AuraException {
        int numRetries = 0;
        while (numRetries < MAX_DEADLOCK_RETRIES) {
            Transaction txn = null;
            try {
                txn = dbEnv.beginTransaction(null, null);
                sessionKeyByKey.delete(sk.getSessionKey());
                txn.commit();
                return;
            } catch (DeadlockException e) {
                try {
                    txn.abort();
                    numRetries++;
                } catch (DatabaseException ex) {
                    throw new AuraException("Txn abort failed", ex);
                }
            } catch (Exception e) {
                try {
                    if (txn != null) {
                        txn.abort();
                    }
                } catch (DatabaseException ex) {
                }
                throw new AuraException("deleteSK transaction failed", e);
            }
        }
        throw new AuraException("delete SessionKey failed for " +
                sk + " after " + numRetries + " retries");
    }
}
