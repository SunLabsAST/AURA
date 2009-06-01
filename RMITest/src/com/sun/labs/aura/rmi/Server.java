package com.sun.labs.aura.rmi;

import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.util.props.Component;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface for a server.
 */
public interface Server extends Remote, Component {

    public long get() throws RemoteException;

    public void reset() throws RemoteException;

    public long simple(long ns) throws RemoteException;

    public long intCall(long ns, int x) throws RemoteException;

    public long stringCall(long ns, String s) throws RemoteException;

    public long dvCall(long ns, DocumentVector dv, SimilarityConfig config) throws RemoteException;
}
