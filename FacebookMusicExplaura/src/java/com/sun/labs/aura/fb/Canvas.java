
package com.sun.labs.aura.fb;

import com.sun.labs.aura.fb.util.Util;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.FacebookWebappHelper;
import com.google.code.facebookapi.ProfileField;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.minion.util.StopWatch;
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
        logger.info("path is " + canvasPath);
        if (canvasPath == null || canvasPath.equals("/")) {
            response.setContentType("text/html;charset=UTF-8");

            FacebookWebappHelper helper = FacebookWebappHelper.newInstanceJson(request, response, apiKey, secretKey);
            if (helper.requireLogin(Util.getRootPath(request, false) + "/canvas")) {
                logger.info("sending redirect");
                return;
            }
            FacebookJsonRestClient client = (FacebookJsonRestClient)helper.getFacebookRestClient();
            String fbSession = client.getCacheSessionKey();

            //
            // We should now have an active client session.
            String userName = "";
            try {
                StopWatch sw = new StopWatch();
                sw.start();
                long uid = client.users_getLoggedInUser();
                FBUserInfo info = getUserInfo(apiKey, secretKey, fbSession, uid);
                String music = info.getMusicString();
                if (music == null || music.isEmpty()) {
                    request.setAttribute("nomusic", Boolean.TRUE);
                    music = "Coldplay";
                } else {
                    request.setAttribute("nomusic", Boolean.FALSE);
                }
                //
                // Assume music is a comma-delimited list of artist names
                StopWatch asw = new StopWatch();
                asw.start();
                List<Artist> artists = getArtistsFromFBString(music, dm);
                asw.stop();

                request.setAttribute("server", Util.getRootPath(request, false));
                request.setAttribute("fbSession", fbSession);
                request.setAttribute("artists", artists);
                request.setAttribute("time", sw.getTime() + "ms");
                request.setAttribute("auraTime", asw.getTime() + "ms");
            } catch (FacebookException e) {
                userName = e.getMessage();
            }
            
            request.setAttribute("user", userName);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/canvas/main.jsp");
            dispatcher.forward(request, response);
        } else if (canvasPath.equals("/updateCloudFromArtistIDs")) {
            String artistsParam = request.getParameter("artists");
            String fbSession = request.getParameter("fbSession");
            String[] artists = artistsParam.split(",");

            StopWatch tsw = new StopWatch();
            tsw.start();
            //
            // Get the merged cloud
            ItemInfo[] cloud = dm.getMergedCloud(artists, DataManager.CLOUD_SIZE);
            tsw.stop();

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
                Element rootDiv = doc.createElement("div");
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
                    rootDiv.appendChild(span);
                    rootDiv.appendChild(
                            doc.createTextNode(" "));
                }
                frag.appendChild(rootDiv);
                String fbml = Util.xmlToString(frag);
                logger.info("setting profile to " + fbml);
                
                if (!client.profile_setFBML(null, fbml, null, null, fbml)) {
                    logger.info("failed to set profile FBML");
                }
            } catch (ParserConfigurationException e) {
                logger.log(Level.WARNING,
                        "Failed to construct DOM for FBML", e);
            } catch (FacebookException e) {
                logger.log(Level.WARNING, "Failed to set Profile FBML", e);
            }

            //
            // Finally, send the answer (this is done after the above call
            // so that there will be profile fbml to set)
            HashMap<String,String> props = new HashMap<String,String>();
            props.put("fbml_profile", getAddToProfileFBML());
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
            String artistsParam = request.getParameter("artists");
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
            String[] artists = artistsParam.split(",");

            StopWatch tsw = new StopWatch();
            tsw.start();
            //
            // Get the merged cloud
            ItemInfo[] cloud = dm.getMergedCloud(artists, DataManager.CLOUD_SIZE);
            tsw.stop();

            //
            // Dig up the artist info for the FBUID
            String friendMusic = null;
            FBUserInfo friend = null;
            try {
                friend = getUserInfo(apiKey, secretKey, fbSession, friendID);
            } catch (FacebookException e) {
                logger.log(Level.WARNING, "Failed to talk to FB", e);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
            
            friendMusic = friend.getMusicString();
            if (friendMusic == null || friendMusic.isEmpty()) {
                //
                // Nothing to compare to... send back an error
                JSONArray err = getJSONError("Sorry, " + friend.getName() +
                        " has not entered any favorite music.");
                sendJSON(err, response);
                return;
            }

            List<Artist> friendArtists = getArtistsFromFBString(friendMusic, dm);
            ItemInfo[] friendCloud = dm.getMergedCloud(friendArtists, DataManager.CLOUD_SIZE);
            ItemInfo[] compareCloud = dm.getComparisonCloud(cloud, friendCloud);
            
            HashMap<String,String> props = new HashMap<String,String>();
            props.put("isAppUser", friend.isAppUser().toString());
            props.put("friendName", friend.getName());
            if (!friend.isAppUser()) {
                props.put("fbml_invite", getInviteFBML(friend.getName(), friendID));
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

            String musicStr = null;
            FBUserInfo friend = null;
            try {
                friend = getUserInfo(apiKey, secretKey, fbSession, friendID);
            } catch (FacebookException e) {
                logger.log(Level.WARNING, "Failed to talk to FB", e);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }

            musicStr = friend.getMusicString();
            if (musicStr == null || musicStr.isEmpty()) {
                //
                // Nothing to compare to... send back an error
                JSONArray err = getJSONError("Sorry, " + friend.getName() +
                        " has not entered any favorite music.");
                sendJSON(err, response);
                return;
            }

            //
            // Retrieve the artists, then build the cloud
            List<Artist> artists = getArtistsFromFBString(musicStr, dm);
            ItemInfo[] friendCloud = dm.getMergedCloud(artists, DataManager.CLOUD_SIZE);
            //
            // And sort by name
            Arrays.sort(friendCloud, ItemInfo.getNameSorter());

            HashMap<String,String> props = new HashMap<String,String>();
            props.put("isAppUser", friend.isAppUser().toString());
            props.put("friendName", friend.getName());
            if (!friend.isAppUser()) {
                props.put("fbml_invite", getInviteFBML(friend.getName(), friendID));
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
            response.setContentType("application/json");
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
        return new FBUserInfo(friendName, friendMusic, isAppUser);
    }

    private String getInviteFBML(String fbUserName, Long fbUserId) {
        String result =
                "<table><tr><td>It looks like " + fbUserName +
                " hasn't used the Music Explaura.</td><td>" +
                "<fb:request-form action=\"http://apps.facebook.com/musicexplaura\" type=\"Music Explaura\" content=\"" +
                "Use the Music Explaura to add your personal music tag cloud to " +
                "your profile and compare your taste in music with your friends' taste. " +
                "<fb:req-choice url=&quot;http://apps.facebook.com/musicexplaura&quot; label=&quot;Explore!&quot;>\"> " +
                "<fb:request-form-submit uid=" + fbUserId + " label=\"Share with %n\" />" +
                "</fb:request-form></td></tr></table>";
        logger.info("Invite FBML:" + result);
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
