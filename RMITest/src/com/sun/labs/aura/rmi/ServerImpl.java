/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.rmi;

import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;

/**
 *
 */
public class ServerImpl implements Server, Configurable {

    private long tot;

    public long get() throws RemoteException {
        return tot;
    }

    public void reset() throws RemoteException {
        tot = 0;
    }

    public long simple(long ns) throws RemoteException {
        tot += System.nanoTime() - ns;
        return System.nanoTime();
    }

    public long intCall(long ns, int x) throws RemoteException {
        tot += System.nanoTime() - ns;
        return System.nanoTime();
    }

    public long stringCall(long ns, String s) throws RemoteException {
        tot += System.nanoTime() - ns;
        return System.nanoTime();
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
    }

    public long dvCall(long ns, DocumentVector dv, SimilarityConfig config) throws RemoteException {
        tot += System.nanoTime() - ns;
        return System.nanoTime();
    }

}
