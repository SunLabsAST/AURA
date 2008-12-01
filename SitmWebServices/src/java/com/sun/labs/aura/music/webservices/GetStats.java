/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetStats extends StandardService {

    @Override
    public void initParams() {
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        DataStore ds = mdb.getDataStore();
        // show the number of items of each type

        Util.tag(out, "ready", "" + ds.ready());
        Util.tag(out, "replicants", Integer.toString(ds.getPrefixes().size()));
        for (ItemType t : ItemType.values()) {
            long count = ds.getItemCount(t);
            if (count > 0L) {
                Util.tag(out, t.toString(), Long.toString(count));
            }
        }
        for (Attention.Type t : Attention.Type.values()) {
            AttentionConfig ac = new AttentionConfig();
            ac.setType(t);
            long count = ds.getAttentionCount(ac);
            if (count > 0L) {
                Util.tag(out, t.toString(), Long.toString(count));
            }
        }
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
