/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetItems extends StandardService {

    protected final static Logger logger = Logger.getLogger("GetItems");
    
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
        long funcStart = System.currentTimeMillis();
        ItemFormatterManager formatter = getItemFormatterManager();
        String itemID = getParam(request, "key");
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());
        String[] keys = itemID.split(",");
        if (keys.length == 0) {
            throw new ParameterException(ErrorCode.MissingArgument, "key");
        }
        //
        // Put all the keys in a list
        List keysToFetch = new ArrayList<String>();
        for (String key : keys) {
            key = key.trim();
            keysToFetch.add(key);
        }
        //
        // Get the items from the datastore
        Collection<Item> items = mdb.getDataStore().getItems(keysToFetch);
        if (items.isEmpty()) {
            throw new ParameterException(ErrorCode.NotFound, itemID);
        }
        long delta = System.currentTimeMillis() - funcStart;
        //
        // Print 'em out
        for (Item item : items) {
            out.println(formatter.toXML(item, outputType));
        }
        out.println("<!-- item fetch in " + delta + " ms -->");
        //logger.severe(String.format("GetItems for %d no out %d", keys.length, delta));
        //logger.severe(String.format("GetItems for %d took   %d", keys.length, System.currentTimeMillis() - funcStart));
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Get items from the database";
    }
}
