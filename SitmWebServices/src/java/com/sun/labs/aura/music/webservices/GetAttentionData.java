/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetAttentionData extends StandardService {

    public void initParams() {
        addParam("max", "100", "the maximum number of attention elements to return");
        addParam("srcKey", null, "the source key");
        addParam("tgtKey", null, "the destination key");
        addParam("type", null, "the type of the attention");
        addParam("svalue", null, "the optional string value associated with the attention type");
        addParam("nvalue", null, "the optional integer value associated with the attention type");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        String src = getParam(request, "srcKey");
        String target = getParam(request, "tgtKey");
        Attention.Type type = (Attention.Type) getParamAsEnum(request, "type", Attention.Type.values());
        String svalue = getParam(request, "svalue");
        String nvalue = getParam(request, "nvalue");
        int maxCount = getParamAsInt(request, "max", 1, 1000);

        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(src);
        ac.setTargetKey(target);
        ac.setType(type);

        if (svalue != null) {
            ac.setStringVal(svalue);
        }

        if (nvalue != null) {
            try {
                Long lval = Long.parseLong(nvalue);
                ac.setNumberVal(lval);
            } catch (NumberFormatException nfe) {
                throw new ParameterException(ErrorCode.BadArgument, "bad nvalue format");
            }
        }

        List<Attention> attns = mdb.getDataStore().getLastAttention(ac, maxCount);
        for (Attention attn : attns) {
            out.println(Util.toXML(attn));
        }
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Get attention data from the database";
    }// </editor-fold>
}
