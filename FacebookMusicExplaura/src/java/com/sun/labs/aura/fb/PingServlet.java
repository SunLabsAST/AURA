/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.fb;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.FacebookParam;
import com.google.code.facebookapi.FacebookWebappHelper;
import com.google.code.facebookapi.ProfileField;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ja151348
 */
public class PingServlet extends HttpServlet {
    protected Logger logger = Logger.getLogger("");
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        ServletContext context = request.getSession().getServletContext();
        String apiKey = context.getInitParameter("apiKey");
        String secretKey = context.getInitParameter("secretKey");
        MusicDatabase mdb = (MusicDatabase)context.getAttribute("mdb");

        String function = request.getPathInfo();

        if (function.equals("/auth")) {
            //
            // We know the UID, get an email address too
            FacebookWebappHelper helper = FacebookWebappHelper.newInstanceJson(request, response, apiKey, secretKey);
            FacebookJsonRestClient client = (FacebookJsonRestClient)helper.getFacebookRestClient();
            try {
                long uid = client.users_getLoggedInUser();
                List<Long> list = Collections.singletonList(uid);
                JSONArray info = (JSONArray)client.users_getInfo(list,
                    EnumSet.of(ProfileField.PROXIED_EMAIL));
                JSONObject user = info.getJSONObject(0);
                String email = user.getString(ProfileField.PROXIED_EMAIL.toString());

                DataStore ds = mdb.getDataStore();
                User u = ds.getUser(getUserKey(uid));
                if (u == null) {
                    u = StoreFactory.newUser(getUserKey(uid), "Facebook User");
                    u.setEmail(email);
                    ds.putUser(u);
                }
            } catch (FacebookException e) {
                logger.log(Level.INFO, "Failed to get info for authorize ping", e);
            } catch (JSONException e) {
                logger.log(Level.INFO, "Error parsing JSON on authorize", e);
            } catch (AuraException e) {
                logger.log(Level.INFO, "Failed to put user into datastore", e);
            } catch (RemoteException e) {
                logger.log(Level.INFO, "Failed to communicate with datastore", e);
            }

        } else if (function.equals("/remove")) {
            //
            // Use the API client to  verify the facebook signature in the
            // request (so that third parties can't hit this URL to remove users.
            FacebookWebappHelper helper = FacebookWebappHelper.newInstanceJson(request, response, apiKey, secretKey);
            Map<String,String> fbParams = getFBParams(request.getParameterMap());
            if (helper.verifySignature(fbParams,
                    request.getParameter(FacebookParam.SIGNATURE.toString()))) {
                String uidStr = request.getParameter(FacebookParam.USER.toString());
                try {
                    Long uid = Long.parseLong(uidStr);
                    DataStore ds = mdb.getDataStore();
                    ds.deleteUser(getUserKey(uid));
                } catch (AuraException e) {
                    logger.log(Level.INFO, "Failed to delete user from datastore", e);
                } catch (RemoteException e) {
                    logger.log(Level.INFO, "Failed to communicate with datastore", e);
                } catch (NumberFormatException e) {
                    logger.log(Level.INFO, "Failed to parse UID " + uidStr);
                }
            } else {
                logger.log(Level.WARNING, "Failed to authorize remove ping from " + request.getRemoteAddr());
            }
        }
        return;
    }

    private Map<String,String> getFBParams(Map<String,String[]> httpParams) {
        String prefix = "fb_sig_";
        int prefix_len = prefix.length();
        Map<String,String> fb_params = new HashMap<String,String>();
        for ( Entry<String,String[]> requestParam : httpParams.entrySet() ) {
            if ( requestParam.getKey().indexOf( prefix ) == 0 ) {
                fb_params.put( requestParam.getKey().substring( prefix_len ), requestParam.getValue()[0] );
            }
        }
        return fb_params;
    }

    private String getUserKey(long fbUID) {
        return "facebook_" + fbUID;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
