/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogUser;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
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
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;

/**
 * This servlet handles both the sending of open id auth and the receiving of
 * the response.
 */
public class Login extends HttpServlet {
    protected Logger logger = Logger.getLogger("");

    protected ConsumerManager consumer;
    
    protected boolean bypassAuth = false;
    
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

    }
    
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        ServletContext context = getServletContext();
        String servletPath = request.getServletPath();
        Aardvark aardvark = (Aardvark)context.getAttribute("aardvark");
        try {
            Shared.fillPageHeader(request, aardvark);
        } catch (AuraException e) {
            logger.log(Level.SEVERE, "Failed to talk to aura", e);
        }
        //
        // Are we getting a login request, or an auth response?
        if (servletPath.equals("/Login")) {
            //
            // This is an initial login request.  See if we have this user,
            // then redirect them to log in if we do.  If not, register them.
            String openid_url = request.getParameter("openid_url");
            openid_url = openid_url.toLowerCase();
            User u = null;
            try {
                u = aardvark.getUser(openid_url);
                if (u == null) {
                    //
                    // Add or remove a trailing slash to see if we can get
                    // the user
                    if (openid_url.endsWith("/")) {
                        openid_url = openid_url.substring(0, openid_url.length());
                    } else {
                        openid_url = openid_url + "/";
                    }
                    u = aardvark.getUser(openid_url);
                }
            } catch (AuraException e) {
                // do error page?
            }
            if (u != null) {
                if (bypassAuth) {
                    request.getSession().setAttribute("loggedInUser", u);
                    //UserBean ub = new UserBean(new BlogUser(u), intendedFeed);
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/Home"));
                    return;
                } else {
                    //
                    // Do authentication redirect
                    authRequest(openid_url, request, response, false);
                    return;
                }
            } else {
                //
                // Forward to registration page
                request.setAttribute("openid_url", openid_url);
                RequestDispatcher rd =
                        context.getRequestDispatcher("/register.jsp");
                rd.forward(request, response);
                return;
            }
        } else if (servletPath.equals("/LoginReturn")) {
            HttpSession session = request.getSession();
            //
            // verify that they are who they say they are, then put their
            // user in a session and direct them to their home page
            Identifier identifier = verifyResponse(request);
            if (identifier == null) {
                //
                // login failed, send them back to the welcome
                logger.log(Level.WARNING, "Login failed (intendedID was " +
                        (String)session.getAttribute("intendedID") + ")");
                context.getRequestDispatcher("/welcome.jsp")
                        .forward(request, response);
                return;
            }
            
            //
            // Is this a registration?  It is if it has an intendedID
            String intendedID = (String)session.getAttribute("intendedID");
            String intendedFeed = (String)session.getAttribute("intendedFeed");
            session.removeAttribute("intendedID");
            session.removeAttribute("intendedFeed");
            
            if (intendedID != null) {
                if (!intendedID.equals(identifier.getIdentifier())) {
                    //
                    // identities didn't match!  that can't be good
                    logger.log(Level.WARNING, "Identity confirmed, but " +
                            "returned " + identifier.getIdentifier() +
                            " and intended " + intendedID + " don't match!");
                    //context.getRequestDispatcher("/welcome.jsp")
                    //        .forward(request, response);
                    //return;
                }
                //
                // Make double sure we don't already have this user
                User u = null;
                try {
                    u = aardvark.getUser(identifier.getIdentifier());
                } catch (AuraException e) {
                    Shared.forwardToError(context, request, response, e);
                }
                
                if (u != null) {
                    // error, they were already registered!
                    request.setAttribute("alreadyRegistered", "true");
                    RequestDispatcher rd =
                            context.getRequestDispatcher("/register.jsp");
                    rd.forward(request, response);
                    return;
                }
                
                //
                // Now it is safe to make the user
                try {
                    u = aardvark.enrollUser(identifier.getIdentifier());
                    fillRegistration(u, request);
                    u = aardvark.updateUser(u);
                    aardvark.addUserFeed(u, intendedFeed, Attention.Type.STARRED_FEED);
                    //
                    // direct them home!
                    session.setAttribute("loggedInUser", u);
                    //UserBean ub = new UserBean(new BlogUser(u), intendedFeed);
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/Home"));
                    return;
                } catch (AuraException e) {
                    Shared.forwardToError(context, request, response, e);
                }
                
            } else {
                //
                // This is a simple login, redirect to home
                try {
                    User u = aardvark.getUser(identifier.getIdentifier());
                    session.setAttribute("loggedInUser", u);
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/Home"));
                    return;
                } catch (AuraException e) {
                    Shared.forwardToError(context, request, response, e);
                }
            }
        } else if (servletPath.equals("/Logout")) {
            HttpSession session = request.getSession();
            session.removeAttribute("loggedInUser");
            session.invalidate();
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/Welcome"));
            return;
        } else if (servletPath.equals("/Register")) {
            //
            // We're trying to register a user.  1) Make sure they don't already
            // exist.  2) Store away their registration info in their session
            // and 3) auth them.
            String openid_url = request.getParameter("openid_url");
            openid_url = openid_url.toLowerCase();
            String defaultFeed = request.getParameter("default_feed");
            User u = null;
            try {
                u = aardvark.getUser(openid_url);
            } catch (AuraException e) {
                // do error page?
            }
            if (u != null) {
                // error, they were already registered!
                request.setAttribute("alreadyRegistered", "true");
                RequestDispatcher rd =
                        context.getRequestDispatcher("/register.jsp");
                rd.forward(request, response);
                return;
            }
            
            //
            // Things are looking good.  Store the ID and Feed they want to use
            // so that if their registration is successful, we'll make a user
            // for them.
            HttpSession session = request.getSession();
            session.setAttribute("intendedID", openid_url);
            session.setAttribute("intendedFeed", defaultFeed);
            
            if (bypassAuth) {
                try {
                    u = aardvark.enrollUser(openid_url);
                    fillRegistration(u, request);
                    u = aardvark.updateUser(u);
                    aardvark.addUserFeed(u, defaultFeed, Attention.Type.STARRED_FEED);
                    //
                    // direct them home!
                    session.setAttribute("loggedInUser", u);
                } catch (AuraException e) {
                    logger.log(Level.SEVERE, "Error enrolling", e);
                }
                //UserBean ub = new UserBean(new BlogUser(u), intendedFeed);
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/Home"));
                return;

            } else {
                //
                // Now send them in to get authed
                authRequest(openid_url, request, response, true);
                return;
            }
        }
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
    public void authRequest(String userSuppliedString,
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
                    + "/LoginReturn";
            
            // perform discovery on the user-supplied identifier
            List discoveries = consumer.discover(userSuppliedString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = consumer.associate(discoveries);

            // store the discovery information in the user's session
            httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = consumer.authenticate(discovered, returnToUrl);
            
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
                logger.info("Sending redirect to " + authReq.getDestinationUrl(true));
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
            ParameterList response = new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute("openid-disc");

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                receivingURL.append("?").append(httpReq.getQueryString());
            }

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = consumer.verify(receivingURL.toString(), response, discovered);

            // examine the verification result and extract the verified
            // identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
                //if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                    SRegResponse fetchResp = (SRegResponse) authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);

                    // List emails = fetchResp.getAttributeValues("email");
                    // String email = (String) emails.get(0);

                    List aliases = fetchResp.getAttributeNames();
                    for (Iterator iter = aliases.iterator(); iter.hasNext();) {
                        String alias = (String) iter.next();
                        String value = fetchResp.getAttributeValue(alias);
                        if (value != null) {
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
    
    
    protected void fillRegistration(User u, HttpServletRequest req) {
        //
        // Try to get each of the fields out that we asked for.
        String nickname = (String)req.getAttribute("nickname");
        String email = (String)req.getAttribute("email");
        String fullname = (String)req.getAttribute("fullname");
        String dob = (String)req.getAttribute("dob");
        String gender = (String)req.getAttribute("gender");
        String postcode = (String)req.getAttribute("postcode");
        String country = (String)req.getAttribute("country");
        String language = (String)req.getAttribute("language");
        String timezone = (String)req.getAttribute("timezone");
        
        BlogUser bu = new BlogUser(u);
        bu.setNickname(nickname);
        bu.setEmailAddress(email);
        bu.setFullname(fullname);
        bu.setDob(dob);
        bu.setGender(gender);
        bu.setPostcode(postcode);
        bu.setCountry(country);
        bu.setLanguage(language);
        bu.setTimezone(timezone);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
