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

import com.sun.labs.aura.fb.util.Util;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.FacebookWebappHelper;
import com.google.code.facebookapi.ProfileField;
import com.sun.labs.aura.fb.util.ExpiringLRACache;
import com.sun.labs.aura.music.Artist;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Handles the Canvas landing page requests.  Shows the canvas or directs
 * users to log in if not logged in.
 *
 * @author ja151348
 */
public class Canvas extends HttpServlet {
    protected Logger logger = Logger.getLogger("");
    protected ExpiringLRACache<Long,List<Artist>> uidToArtists =
            new ExpiringLRACache<Long,List<Artist>>(5000, 24 * 60 * 60 * 1000);
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
        DataManager dm = (DataManager)context.getAttribute("dm");
        
        String canvasPath = request.getPathInfo();
        if (canvasPath == null || canvasPath.equals("/")) {
            response.setContentType("text/html;charset=UTF-8");

            FacebookWebappHelper helper = FacebookWebappHelper.newInstanceJson(request, response, apiKey, secretKey);
            if (helper.requireLogin(Util.getRootPath(request, false) + "/canvas")) {
                return;
            }
            FacebookJsonRestClient client = (FacebookJsonRestClient)helper.getFacebookRestClient();
            String fbSession = client.getCacheSessionKey();

            try {
                long uid = client.users_getLoggedInUser();

                String compareTo = request.getParameter("compareTo");
                if (compareTo != null && !compareTo.isEmpty()) {
                    request.setAttribute("compareTo", compareTo);
                }
                request.setAttribute("server", Util.getRootPath(request, false));
                request.setAttribute("fbSession", fbSession);
                request.setAttribute("fbUID", uid);
            } catch (FacebookException e) {

            }
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("/canvas/main.jsp");
            dispatcher.forward(request, response);
        } else if (canvasPath.equals("/updateCloud")) {
            String fbSession = request.getParameter("fbSession");
            String uidStr = request.getParameter("fbUID");
            Long uid = Long.valueOf(uidStr);

            List<Artist> artists = new ArrayList<Artist>();
            FBUserInfo currUser = null;
            //
            // Get the music taste from facebook - when displaying the cloud,
            // we never use the cache.  This way if the user updates their
            // music taste, we'll always find it.
            try {
                currUser = getUserInfo(apiKey, secretKey, fbSession, uid);
            } catch (FacebookException e) {
                logger.log(Level.WARNING, "Failed to get FB music taste", e);
                JSONArray err = getJSONError("Sorry, we were unable to read " +
                        "your musical taste from Facebook");
                sendJSON(err, response);
                return;
            }
            artists = getArtistsFromFBString(currUser.getMusicString(), dm);
            uidToArtists.put(uid, artists);

            //
            //
            if (artists.isEmpty()) {
                //
                // We didn't recognize any artists, so set hasMusic to false,
                // then ask for the default music and go on from there
                currUser.setHasMusic(false);
                artists = getArtistsFromFBString(currUser.getMusicString(), dm);

                //
                // If we still didn't get any artists, the datastore is
                // probably unresponsive.  Send back an error.
                if (artists.isEmpty()) {
                    logger.log(Level.WARNING,
                            "No artists after Coldplay lookup!  DS down?");
                    JSONArray err = getJSONError("Sorry, we're having trouble " +
                            "communicating with our servers.  Please check back " +
                            "later.");
                    sendJSON(err, response);
                    return;
                }
                uidToArtists.put(uid, artists);
            }

            //
            // Get the merged cloud
            ItemInfo[] cloud = dm.getMergedCloud(artists, DataManager.CLOUD_SIZE);

            //
            // And sort by name
            Arrays.sort(cloud, ItemInfo.getNameSorter());

            //
            // Build and update the user's cloud in their profile
            FacebookJsonRestClient client =
                    new FacebookJsonRestClient(apiKey, secretKey, fbSession);
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().
                        newDocumentBuilder();
                Document doc = db.newDocument();
                DocumentFragment frag = doc.createDocumentFragment();
                //
                // Draw the cloud
                Element cloudDiv = doc.createElement("div");
                for (int i = 0; i < cloud.length; i++) {
                    ItemInfo item = cloud[i];
                    Element span = doc.createElement("span");
                    String color = (i % 2 == 0) ? "#f8981d" : "#5382a1";
                    span.setAttribute("style",
                            "color: " + color +
                                "; font-size: "
                                + Math.round(
                                          (item.getScore() * 3.0 + 1) * 8)
                                + "px;");
                    span.appendChild(
                            doc.createTextNode(item.getItemName()));
                    cloudDiv.appendChild(span);
                    cloudDiv.appendChild(
                            doc.createTextNode(" "));
                }
                frag.appendChild(cloudDiv);
                //
                // Add some links
                Element launch = doc.createElement("a");
                launch.setAttribute("href",
                        "http://apps.facebook.com/musicexplaura");
                launch.setTextContent("Launch");
                Element compare = doc.createElement("a");
                compare.setAttribute("href",
                        "http://apps.facebook.com/musicexplaura/?compareTo=" +
                        client.users_getLoggedInUser());
                compare.setTextContent("Compare");
                Element ldata = doc.createElement("td");
                ldata.appendChild(launch);
                ldata.setAttribute("style", "text-align: center;");
                Element tdata = doc.createElement("td");
                tdata.appendChild(compare);
                tdata.setAttribute("style", "text-align: center;");
                Element row = doc.createElement("tr");
                row.appendChild(ldata).appendChild(tdata);
                Element table = doc.createElement("table");
                table.appendChild(row);
                table.setAttribute("style", "width: 100%; text-align: center;");
                Element buttons = doc.createElement("div");
                buttons.setAttribute("style", "margin-top: 4px; border: 1px solid #d8dfea;");
                buttons.appendChild(table);
                frag.appendChild(buttons);

                String fbml = Util.xmlToString(frag);
                
                if (!client.profile_setFBML(null, fbml, null, null, fbml)) {
                    logger.info("failed to set profile FBML to " + fbml);
                }
            } catch (ParserConfigurationException e) {
                logger.log(Level.WARNING,
                        "Failed to construct DOM for FBML", e);
            } catch (FacebookException e) {
                logger.log(Level.WARNING, "Failed to set Profile FBML", e);
            }

            //
            // Make a string representation of the artists we used so we can
            // show it in the UI
            String artistStr = "";
            for (int i = 0; i < artists.size(); i++) {
                artistStr += artists.get(i).getName();
                if (i < artists.size() - 1) {
                    artistStr += ", ";
                }
            }

            //
            // Finally, send the answer (this is done after the above call
            // so that there will be profile fbml to set)
            HashMap<String,String> props = new HashMap<String,String>();
            props.put("fbml_profile", getAddToProfileFBML());
            props.put("hasmusic", Boolean.toString(currUser.hasMusic()));
            props.put("artists", artistStr);
            props.put("fbml_steerLink", "<span style=\"font-size: 14px\">View in the <a href=\"" +
                    Util.getWMELink(cloud) +
                    "\">full Music Explaura</a></span>");
            JSONArray result = getJSONResponse(props);
            result = addCloudJSON(result, cloud);
            sendJSON(result, response);

        } else if (canvasPath.equals("/clearProfile")) {
            String fbSession = request.getParameter("fbSession");
            FacebookJsonRestClient client =
                    new FacebookJsonRestClient(apiKey, secretKey, fbSession);
            try {
                 client.profile_setFBML(null, "", null, null, "");
            } catch (FacebookException e) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        } else if (canvasPath.equals("/getCompareCloud")) {
            String uidStr = request.getParameter("fbUID");
            String fbSession = request.getParameter("fbSession");
            String friendIDStr = request.getParameter("friendUID");
            Long uid = Long.valueOf(uidStr);
            Long friendID = null;
            try {
                friendID = Long.valueOf(friendIDStr);
            } catch (NumberFormatException e) {
                //
                // Nothing to compare to... send back an error
                JSONArray err = getJSONError("Sorry, we didn't understand that friend name.");
                sendJSON(err, response);
                return;
            }

            List<Artist> artists = null;
            try {
                artists = getArtistsForUser(apiKey, secretKey, fbSession, dm, uid);
            } catch (FacebookException e) {
                //
                // Nothing to compare to... send back an error
                JSONArray err = getJSONError("Sorry, we couldn't retrieve your musical taste.");
                sendJSON(err, response);
                return;
            }

            //
            // Get my merged cloud
            ItemInfo[] cloud = dm.getMergedCloud(artists, DataManager.CLOUD_SIZE);

            FBUserInfo friend = null;
            List<Artist> friendArtists = null;
            try {
                friend = getUserInfo(apiKey, secretKey, fbSession, friendID);
                //
                // If the friend has no music, stop now
                if (!friend.hasMusic()) {
                    //
                    // Nothing to compare to... send back an error
                    JSONArray json = getJSONErrorWithInvite("Sorry, " + friend.getName() +
                            " has not entered any favorite music.", friend);
                    sendJSON(json, response);
                    return;
                }
            } catch (FacebookException e) {
                //
                // Nothing to compare to... send back an error
                JSONArray err = getJSONError("Sorry, we couldn't retrieve your friend's musical taste.");
                sendJSON(err, response);
                return;
            }

            //
            // Get the friend's artists
            friendArtists = getArtistsForUser(dm, friend);
            if (friendArtists.isEmpty()) {
                //
                // Still no artists, send back an error
                JSONArray json = getJSONErrorWithInvite("Sorry, we were " +
                        "unable to recognize any music from " + friend.getName() +
                        ".", friend);
                sendJSON(json, response);
                return;
            }

            //
            // Get my friend's merged cloud and the comparison cloud
            ItemInfo[] friendCloud = dm.getMergedCloud(friendArtists, DataManager.CLOUD_SIZE);
            ItemInfo[] compareCloud = dm.getComparisonCloud(cloud, friendCloud);
            
            HashMap<String,String> props = new HashMap<String,String>();
            props.put("isAppUser", friend.isAppUser().toString());
            props.put("friendName", friend.getName());
            if (!friend.isAppUser()) {
                props.put("fbml_invite", getInviteFBML(friend.getName(),
                        friendID,
                        false));
            }
            JSONArray result = getJSONResponse(props);
            result = addCloudJSON(result, compareCloud);
            sendJSON(result, response);
        } else if (canvasPath.equals("/getOtherCloud")) {
            //
            // Build a cloud for a different user than the one logged in
            String fbSession = request.getParameter("fbSession");
            String friendIDStr = request.getParameter("friendUID");
            Long friendID = null;
            try {
                friendID = Long.valueOf(friendIDStr);
            } catch (NumberFormatException e) {
                //
                // Nothing to compare to... send back an error
                JSONArray err = getJSONError("Sorry, we didn't understand that friend name.");
                sendJSON(err, response);
                return;
            }

            FBUserInfo friend = null;
            List<Artist> friendArtists = null;
            try {
                friend = getUserInfo(apiKey, secretKey, fbSession, friendID);
                if (!friend.hasMusic()) {
                    JSONArray json = getJSONErrorWithInvite("Sorry, " + friend.getName() +
                            " has not entered any favorite music.", friend);
                    sendJSON(json, response);
                    return;
                }
                friendArtists = getArtistsForUser(dm, friend);
            } catch (FacebookException e) {
                logger.log(Level.WARNING, "Failed to talk to FB", e);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }

            if (friendArtists.isEmpty()) {
                //
                // Still no artists, send back an error
                JSONArray json = getJSONErrorWithInvite("Sorry, we were " +
                        "unable to recognize any music from " + friend.getName() +
                        ".", friend);
                sendJSON(json, response);
                return;
            }

            //
            // Retrieve the artists, then build the cloud
            ItemInfo[] friendCloud = dm.getMergedCloud(friendArtists, DataManager.CLOUD_SIZE);
            //
            // And sort by name
            Arrays.sort(friendCloud, ItemInfo.getNameSorter());

            //
            // Make a string representation of the artists we used so we can
            // show it in the UI
            String artistStr = "";
            for (int i = 0; i < friendArtists.size(); i++) {
                artistStr += friendArtists.get(i).getName();
                if (i < friendArtists.size() - 1) {
                    artistStr += ", ";
                }
            }


            HashMap<String,String> props = new HashMap<String,String>();
            props.put("isAppUser", friend.isAppUser().toString());
            props.put("friendName", friend.getName());
            props.put("friendArtists", artistStr);
            if (!friend.isAppUser()) {
                props.put("fbml_invite", getInviteFBML(friend.getName(),
                        friendID,
                        false));
            }
            JSONArray result = getJSONResponse(props);
            result = addCloudJSON(result, friendCloud);
            sendJSON(result, response);
        } else {
            logger.warning("No code to handle " + canvasPath);
        }
    } 

    private JSONArray addCloudJSON(JSONArray result, ItemInfo[] tags) {
        try {
            //
            // Now construct the JSON objects to return
            for (ItemInfo i : tags) {
                JSONObject curr = new JSONObject();
                curr.put("name", i.getItemName());
                double size = i.getScore();
                if (size < 0) {
                    size = (size * -3.0 + 1.0) * -14.0;
                } else {
                    size = (size * 3.0 + 1.0) * 14.0;
                }
                curr.put("size", Math.round(size));
                result.put(curr);
            }
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Error making JSON cloud", e);
        }
        return result;
    }

    private JSONArray getJSONError(String msg) {
        JSONArray result = new JSONArray();
        try {
            JSONObject err = new JSONObject();
            err.put("error", msg);
            result.put(err);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Error encoding JSON", e);
        }
        return result;
    }

    /**
     * Get a JSON error response that includes info about inviting a friend
     * @param msg
     * @param friend
     * @return
     */
    private JSONArray getJSONErrorWithInvite(String msg, FBUserInfo friend) {
        JSONArray result = new JSONArray();
        try {
            JSONObject err = new JSONObject();
            err.put("isAppUser", friend.isAppUser().toString());
            if (!friend.isAppUser()) {
                err.put("fbml_invite", getInviteFBML(friend.getName(),
                        friend.getUID(),
                        true));
            }
            err.put("error", msg);
            result.put(err);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Error encoding JSON", e);
        }
        return result;
    }

    private JSONArray getJSONResponse(Map<String,String> props) {
        JSONArray result = new JSONArray();
        try {
            JSONObject data = new JSONObject();
            for (Entry<String,String> ent : props.entrySet()) {
                data.put(ent.getKey(), ent.getValue());
            }
            result.put(data);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Error encoding JSON", e);
        }
        return result;
    }

    private void sendJSON(JSONArray data, HttpServletResponse response)
        throws IOException {
        try {
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            try {
                data.write(out);
                out.println();
            } finally {
                out.close();
            }
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Failed to write JSON response", e);
        }
    }

    protected List<Artist> getArtistsFromFBString(String favMusic, DataManager dm) {
        String[] artistNames = favMusic.split("[;,\n]");
        ArrayList<Artist> artists = new ArrayList<Artist>();
        for (String artistName : artistNames) {
            artistName = artistName.trim().toLowerCase();
            Artist a = dm.guessArtist(artistName);
            if (a != null) {
                artists.add(a);
            }
        }
        return artists;
    }

    protected FBUserInfo getUserInfo(String apiKey,
                                     String secretKey,
                                     String fbSession,
                                     long fbUserId)
        throws FacebookException {
        //
        // Dig up the artist info for the FBUID
        FacebookJsonRestClient client =
                new FacebookJsonRestClient(apiKey, secretKey, fbSession);
        String friendMusic = null;
        String friendName = null;
        boolean isAppUser = false;
        try {
            List<Long> list = Collections.singletonList(fbUserId);
            client.beginBatch();
            client.users_getInfo(list,
                    EnumSet.of(ProfileField.FIRST_NAME,
                               ProfileField.MUSIC));
            client.users_isAppUser(fbUserId);
            List results = client.executeBatch(true);

            JSONArray res = (JSONArray)results.get(0);
            JSONObject user = res.getJSONObject(0);
            friendName = user.getString(ProfileField.FIRST_NAME.toString());
            friendMusic = user.getString(ProfileField.MUSIC.toString());

            isAppUser = (Boolean)results.get(1);
        } catch (FacebookException e) {
            logger.log(Level.WARNING, "Failed to talk to FB", e);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Failed to read response", e);
            throw new FacebookException(1,
                    "JSON Exception in client, see server log for details");
        }
        return new FBUserInfo(fbUserId, friendName, friendMusic, isAppUser);
    }


    protected List<Artist> getArtistsForUser(String apiKey, String secretKey,
            String fbSession, DataManager dm, Long uid)
            throws FacebookException {
        List<Artist> artists = uidToArtists.get(uid);
        if (artists != null) {
            return artists;
        }
        
        //
        // Get the music taste from facebook.
        FBUserInfo user = getUserInfo(apiKey, secretKey, fbSession, uid);
        return getArtistsForUser(dm, user);
    }

    protected List<Artist> getArtistsForUser(DataManager dm, FBUserInfo user) {
        List<Artist> artists = uidToArtists.get(user.getUID());
        if (artists != null) {
            return artists;
        }
        //
        // Get the list of artists based on the music defined in the user
        String music = user.getMusicString();
        artists = getArtistsFromFBString(user.getMusicString(), dm);
        uidToArtists.put(user.getUID(), artists);
        return artists;
    }

    private String getInviteFBML(String fbUserName, Long fbUserId, boolean pleaForMusic) {
        String result =
                "<table><tr><td>It looks like " + fbUserName +
                " hasn't used the Music Explaura.</td><td>" +
                "<fb:request-form action=\"http://apps.facebook.com/musicexplaura\" type=\"Music Explaura\" content=\"" +
                (pleaForMusic?
                "Please add some bands into your &quot;Favorite Music&quot; in your profile then use "
                : "Use ") +
                "the Music Explaura to add your personal music tag cloud to " +
                "your profile and compare your taste in music with your friends' taste. " +
                "<fb:req-choice url=&quot;http://apps.facebook.com/musicexplaura&quot; label=&quot;Explore!&quot;>\"> " +
                "<fb:request-form-submit uid=" + fbUserId + " label=\"Share with %n\" />" +
                "</fb:request-form></td></tr></table>";
        return result;
    }

    private String getAddToProfileFBML() {
        return "<fb:if-section-not-added section=\"profile\">" +
                "<table><tr><td>Add this tag cloud to your profile page!</td>" +
                "<td><fb:add-section-button section=\"profile\" /></td>" +
                "</tr></table></fb:if-section-not-added>";
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
