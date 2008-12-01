/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetItems extends StandardService {

    private enum Format {
        FULL, COMPACT
    };

    public void initParams() {
        addParam("key", "the key to the item of interest");
        addParam("outputType", OutputType.Tiny.name(), "the type of output");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        ItemFormatterManager formatter = getItemFormatterManager();
        String itemID = getParam(request, "key");
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());

        String[] keys = itemID.split(",");
        for (String key : keys) {
            key = key.trim();
            long fetchStart = System.currentTimeMillis();
            Item item = mdb.getDataStore().getItem(key);
            long delta = System.currentTimeMillis() - fetchStart;
            if (item != null) {
                out.println(formatter.toXML(item, outputType));
                out.println("<!-- item fetch in " + delta + " ms -->");
            } else {
                throw new ParameterException(ErrorCode.NotFound, key);
            }
        }
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Get items from the database";
    }
}
