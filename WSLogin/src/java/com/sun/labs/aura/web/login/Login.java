
package com.sun.labs.aura.web.login;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.service.LoginService;
import com.sun.labs.aura.service.persist.SessionKey;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;

/**
 * Provides the mechanism for forwarding login requests through to OpenID
 * providers in order to establish session tokens for connecting users.
 *
 * If an error occurs, an error code will be set back to the return URL
 * provided by the client of this service.  Error codes are listed below.
 */
public class Login extends HttpServlet {

    public static final String E_MISSING_ID = "Must specify openid parameter";
    public static final String E_INTERNAL = "An internal error occurred";
    public static final String E_VERIFY = "Failed to verify identity";

    protected Logger logger = Logger.getLogger("");

    protected ConsumerManager consumer = null;

    protected LoginService loginSvc = null;
    protected DataStore dataStore = null;

    protected SimpleDateFormat openIdDateFormat =
            new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            this.consumer = new ConsumerManager();
            consumer.setAssociations(new InMemoryConsumerAssociationStore());
            consumer.setNonceVerifier(new InMemoryNonceVerifier(5000));
        } catch (ConsumerException e) {
            throw new ServletException(e);
        }

        ServletContext context = config.getServletContext();
        loginSvc = (LoginService)context.getAttribute("loginService");
        dataStore = (DataStore)context.getAttribute("dataStore");
    }
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
    throws ServletException, IOException {
        String spath = request.getServletPath();
        if (spath.equals("/wslogin")) {
            login(request, response);
        } else if (spath.equals("/wsloginreturn")) {
            loginReturn(request, response);
        }
    }

    protected void login(HttpServletRequest request,
                         HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter pw = response.getWriter();

        try {
            //
            // Get the parameters and check 'em out.
            HttpSession session = request.getSession();
            String openIdURL = request.getParameter("openid");
            String clientReturn = request.getParameter("return");
            if (openIdURL == null || openIdURL.isEmpty()) {
                returnError(request, response,
                            "E_MISSING_ID", E_MISSING_ID, null);
                return;
            }
            if (clientReturn == null || clientReturn.isEmpty()) {
                pw.println("Error: No Client Return URL provided.");
                return;
            }

            //
            // Do we know who this user is?  If not, we need to enroll them
            // in Aura for the first time.  Do we need to do anything special
            // here, or should we just create a user in the data store?
            openIdURL = cleanUpID(openIdURL);
            boolean isRegistration = false;
            try {
                User existing = dataStore.getUser(openIdURL);
                if (existing == null) {
                    isRegistration = true;
                }
            } catch (AuraException e) {
                //
                // If we failed to talk to the datastore, we're in trouble
                returnError(request, response, "E_INTERNAL", E_INTERNAL, e);
                return;
            }

            //
            // Store the client return URL and the open ID in the session
            // so we can use them when we get the open ID callback
            session.setAttribute("openid", openIdURL);
            session.setAttribute("clientReturn", clientReturn);

            //
            // Initiate the OpenID login
            authRequest(openIdURL, request, response, isRegistration);
            return;
        } finally {
            pw.close();
        }
    } 

    protected void loginReturn(HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter pw = response.getWriter();
        try {
            //
            // Verify the response that we got with the open ID provider
            Identifier identifier = verifyResponse(request);
            if (identifier == null) {
                //
                // login failed, send them back to the welcome
                returnError(request, response, "E_VERIFY",
                        E_VERIFY + ": Login failed (intendedID was " +
                        (String)session.getAttribute("intendedID") + ")", null);
                return;
            }

            //
            // If we are doing registration, pull the appropriate attributes
            // out of the request and actually make the user.
            String openid = (String)session.getAttribute("openid");
            User u = null;
            try {
                u = getOrEnrollUser(openid, request);
            } catch (AuraException e) {
                returnError(request, response, "E_INTERNAL", E_INTERNAL, e);
                return;
            } catch (RemoteException e) {
                returnError(request, response, "E_INTERNAL", E_INTERNAL, e);
                return;
            }

            //
            // The user exists and is logged in.  Create a session key that
            // can be used to identify this user by the requesting app.
            SessionKey sessionKey = loginSvc.getUserSessionKey(u.getKey(), "wslogin");
            if (sessionKey == null || sessionKey.isExpired()) {
                try {
                    sessionKey = loginSvc.newUserSessionKey(u.getKey(), "wslogin");
                } catch (Exception e) {
                    returnError(request, response, "E_INTERNAL",
                            E_INTERNAL + ": Failed getting session key", e);
                    return;
                }
            }

            //
            // Return to the client webapp now, passing the user's session
            // key.
            String clientReturn = (String)session.getAttribute("clientReturn");
            clientReturn += "?tksession=" + sessionKey.getSessionKey();
            response.sendRedirect(response.encodeRedirectURL(clientReturn));

        } finally {
            pw.close();
        }
    }

    protected void returnError(HttpServletRequest request,
                               HttpServletResponse response,
                               String errorName, String errorMessage,
                               Exception e) throws IOException {
        HttpSession session = request.getSession();
        String clientReturn = (String)session.getAttribute("clientReturn");
        if (e != null) {
            logger.log(Level.SEVERE, errorMessage, e);
        } else {
            logger.log(Level.INFO, errorMessage);
        }
        clientReturn += "?error=" + errorName;
        response.sendRedirect(response.encodeRedirectURL(clientReturn));
    }

    /**
     * Request authorization for a given user ID (redirects the http request)
     *
     * @param userSuppliedString the open ID string
     * @param httpReq the request
     * @param httpResp the response
     * @return
     * @throws java.io.IOException
     */
    protected void authRequest(String userSuppliedString,
                            HttpServletRequest httpReq,
                            HttpServletResponse httpResp,
                            boolean isRegistration)
            throws ServletException, IOException {
        try {
            //
            // We'll have the open ID provider return here with the auth
            // results, then we'll redirect as appropriate
            String fullURL = httpReq.getRequestURL().toString();
            String returnToUrl = fullURL.substring(0, fullURL.lastIndexOf('/'))
                    + "/wsloginreturn";

            // perform discovery on the user-supplied identifier
            List discoveries = consumer.discover(userSuppliedString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = consumer.associate(discoveries);

            // store the discovery information in the user's session
            httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain an AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq =
                    consumer.authenticate(discovered, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            //FetchRequest fetch = FetchRequest.createFetchRequest();
            SRegRequest sreg = SRegRequest.createFetchRequest();

            if (isRegistration) {
                sreg.addAttribute("nickname", true);
                sreg.addAttribute("email", true);
                sreg.addAttribute("fullname", true);
                sreg.addAttribute("dob", false);
                sreg.addAttribute("gender", false);
                sreg.addAttribute("postcode", false);
                sreg.addAttribute("country", false);
                sreg.addAttribute("language", false);
                sreg.addAttribute("timezone", false);
            }
            // attach the extension to the authentication request
            authReq.addExtension(sreg);

            //if (!discovered.isVersion2()) {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                logger.info("Sending redirect to "
                        + authReq.getDestinationUrl(true));
                httpResp.sendRedirect(authReq.getDestinationUrl(true));
                return;
            //} else {
            // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
                //RequestDispatcher dispatcher = getServletContext()
                //                .getRequestDispatcher("/formredirection.jsp");
                //httpReq.setAttribute("prameterMap", httpReq.getParameterMap());
                //httpReq.setAttribute("message", authReq);
                //dispatcher.forward(httpReq, httpResp);
            //}
        } catch (OpenIDException e) {
            // go to error page
            logger.log(Level.WARNING, "Error in auth", e);
        }
    }


    /**
     * Verify the response received from the open id provider
     *
     * @param httpReq the request (with the response params)
     * @return a valid identifier if verified, otherwise null
     */
    public Identifier verifyResponse(HttpServletRequest httpReq) {
        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered =
                    (DiscoveryInformation) httpReq.getSession().
                                            getAttribute("openid-disc");

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                receivingURL.append("?").append(httpReq.getQueryString());
            }

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification =
                    consumer.verify(receivingURL.toString(),
                                    response, discovered);

            // examine the verification result and extract the verified
            // identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();
                //if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                    SRegResponse fetchResp =
                            (SRegResponse) authSuccess.getExtension(
                                            SRegMessage.OPENID_NS_SREG);

                    // List emails = fetchResp.getAttributeValues("email");
                    // String email = (String) emails.get(0);

                    List aliases = fetchResp.getAttributeNames();
                    for (Iterator iter = aliases.iterator(); iter.hasNext();) {
                        String alias = (String) iter.next();
                        String value = fetchResp.getAttributeValue(alias);
                        if (value != null && !value.isEmpty()) {
                            logger.severe("Got attr " + alias + " : " + value);
                            httpReq.setAttribute(alias, value);
                        }
                    }
                }

                return verified; // success
            }
        } catch (OpenIDException e) {
            logger.log(Level.WARNING, "verify failed with exception", e);
        }
        return null;
    }

    /**
     * Get a user if the users exists or enroll the user if not.
     *
     * @param openid the id of the user
     * @param request the request with discovered attributes filled in
     * @return the user
     */
    protected User getOrEnrollUser(String openid, HttpServletRequest request)
            throws AuraException, RemoteException {

        User u = dataStore.getUser(openid);

        if (u == null) {
            //
            // Try to get each of the fields out that we asked for.
            String nickname = (String)request.getAttribute("nickname");
            String email = (String)request.getAttribute("email");
            String fullname = (String)request.getAttribute("fullname");
            // date is YYYY-MM-DD
            String dob = (String)request.getAttribute("dob");
            String gender = (String)request.getAttribute("gender");
            String postcode = (String)request.getAttribute("postcode");
            String country = (String)request.getAttribute("country");
            String language = (String)request.getAttribute("language");
            String timezone = (String)request.getAttribute("timezone");

            //
            // Figure out what we'll call the user on-screen
            String username = nickname;
            if (username == null) {
                username = fullname;
                if (username == null) {
                    username = email;
                    if (username == null) {
                        username = openid;
                    }
                }
            }

            //
            // Make the user and set all applicable fields
            u = StoreFactory.newUser(openid, username);
            u.setNickname(nickname);
            u.setEmail(email);
            u.setFullname(fullname);
            if (dob != null) {
                try {
                    Date birthday = openIdDateFormat.parse(dob);
                    u.setDob(birthday);
                } catch (ParseException e) {
                    logger.fine("Failed to parse DOB for " + openid + ": " + dob);
                }
            }
            u.setGender(gender);
            u.setPostcode(postcode);
            u.setCountry(country);
            u.setLanguage(language);
            u.setTimezone(timezone);
            dataStore.putUser(u);
        }
        return u;
    }

    /**
     * Normalize an OpenID ID to have the http://, the trailing /, etc
     * 
     * @param id the id to clean up
     * @return the normalized id
     */
    protected String cleanUpID(String id) {
        try {
            if (!id.startsWith("http://")) {
                id = "http://" + id;
            }
            return UrlIdentifier.normalize(id).toExternalForm();
        } catch (DiscoveryException e) {
            return id;
        }
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
        return "Login Redirection Servlet";
    }// </editor-fold>

}
