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

package com.sun.labs.aura.music.wsitm.server;

import com.google.gwt.user.client.rpc.RemoteService;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * User: aviadbendov
 * Date: Apr 18, 2008
 * Time: 9:27:03 PM
 *
 * A servlet incharge of the OpenID authentication process, from authentication to verification.
 * @author Aviad Bendov
 * @see Original version : http://chaoticjava.com/posts/using-openid-within-gwt/
 */
@SuppressWarnings({"GwtInconsistentSerializableClass"})
public class OpenIDServlet extends HttpServlet implements RemoteService {

    public static interface Callback {
        String getOpenIdServletURL(HttpServletRequest request);
        String getLoginURL(HttpServletRequest request);
        String getMainPageURL(HttpServletRequest request);

        String createUniqueIdForUser(String user);
        void saveIdentifierForUniqueId(HttpServletRequest request, String uniqueId, Identifier identifier);
    }

    public static final String ATTR_NAME = "name";
    public static final String ATTR_NICKNAME = "nickname";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_BIRTHDATE = "birthdate";
    public static final String ATTR_GENDER = "gender";
    public static final String ATTR_STATE = "state";
    public static final String ATTR_COUNTRY = "state";
    public static final String ATTR_LANGUAGE = "lang";

    public static final String authParameter = "app-openid-auth";
    public static final String nameParameter = "app-openid-name";
    public static final String openIdCookieName = "app-openid-identifier";
    public static final String uniqueIdCookieName = "app-openid-uniqueid";


    private static Callback callback = new SitmCallback();

    private ConsumerManager manager;
/*
    public OpenIDServlet() {
        try {
            manager = new ConsumerManager();
        } catch (ConsumerException e) {
            throw new RuntimeException("Error creating consumer manager", e);
        }
    }
  */  
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            this.manager = new ConsumerManager();
            //Logger.getLogger("org.openid4java").setLevel(Level.ALL);
            manager.setAssociations(new InMemoryConsumerAssociationStore());
            manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        } catch (ConsumerException e) {
            throw new ServletException(e);
        }

    }

    /**
     * <b>Note</b>: In a normal servlet environment, this method would probably
     * redirect the response itself. However, since GWT servlets do not allow for
     * such behavior, a path to this servlet is returned and the redirection is done
     * on the client side.
     * @param openIdName The OpenID identifier the user provided.
     * @return The URL the browser should be redirected to.
     */
    public static String getAuthenticationURL(String openIdName) {
        // This is where a redirect for the response was supposed to occur; however, since GWT doesn't allow that
        // on responses coming from a GWT servlet, only a redirect via the web page is made.
        return null;
        //return MessageFormat.format("{3}?{1}=true&{2}={0}", openIdName, authParameter, nameParameter, callback.getOpenIdServletURL());
    }

    /**
     * Returns the unique cookie and the OpenID identifier saved on the user's
     * browser.
     *
     * The servlet should be the only entity accessing and manipulating these cookies,
     * so it is also in-charge of fetching them when needed.
     * @param request The user's request to extract the cookies from.
     * @return Array containing { UniqueId, OpenID-Identifier }
     */
    public static String[] getRequestUserInfo(HttpServletRequest request) {
                    
        return new String[] {
                getCookieValue(request,openIdCookieName),
                getCookieValue(request,uniqueIdCookieName)
        };
    }

    /**
     * Implements the GET method by either sending it to an OpenID authentication or verification
     * mechanism.
     *
     * Checks the parameters of the GET method; if they contain the "authParameter" set to true,
     * the authentication process is performed. If not, the verification process is performed
     * (the parameters in the verification process are controlled by the OpenID provider).
     *
     * @param request The request sent to the servlet. Might come from the GWT application or
     * the OpenID provider.
     *
     * @param response The response sent to the user. Generally used to redirect the user to the next
     * step in the OpenID process.
     *
     * @throws ServletException Usually wrapping an OpenID process exception.
     * @throws IOException Usually when redirection could not be performed.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
        PrintWriter pw = response.getWriter();
        for (Object s :request.getParameterMap().keySet()) {
            pw.write((String)s+":"+request.getParameter((String)s));
        }
        pw.close();
        return;
         **/
        if (Boolean.valueOf(request.getParameter(authParameter))) {
            authenticate(request, response);
        } else {
            verify(request, response);

        }
    }

    /**
     * Discovers the OpenID provider from the provided user string, and starts an authentication process
     * against it.
     *
     * This is done in three steps:
     * <ol>
     * <li>Discover the OpenID provider URL</li>
     * <li>Create a unique cookie and send it to the user, so that after
     * the provider redirects the user back we'll know what to do with him.</li>
     * <li>Redirect the user to the provider URL, supplying the verification URL as a return point.</li>
     * </ol>
     *
     * @param request The request for the OpenID authentication.
     * @param response The response, used to redirect the user.
     *
     * @throws IOException Occurs when a redirection is not successful.
     * @throws ServletException Wrapping an OpenID exception.
     */
    private void authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final String loginString = request.getParameter(nameParameter);

        try {
            /*
            String openIdCookie = Cookies.getCookie(openIdCookieName);
            String uniqueIdCookie = Cookies.getCookie(uniqueIdCookieName);
            */
            String uuid = callback.createUniqueIdForUser(loginString);
            response.addCookie(new Cookie(uniqueIdCookieName, uuid));
            //Cookies.setCookie(uniqueIdCookieName, uuid);

            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(loginString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);

            // store the discovery information in the user's session
            request.getSession().setAttribute("openid-disc", discovered);
            
            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, callback.getOpenIdServletURL(request), null);

            Logger.getLogger("").info("Redirecting user to : "+authReq.getDestinationUrl(true));

            // Attribute Exchange - List of schemas : http://www.axschema.org/types
            FetchRequest fetch = FetchRequest.createFetchRequest();

            fetch.addAttribute(ATTR_NAME,"http://schema.openid.net/namePerson", true);
            fetch.addAttribute(ATTR_NICKNAME,"http://schema.openid.net/namePerson/friendly", true);
            fetch.addAttribute(ATTR_EMAIL, "http://schema.openid.net/contact/email",  false);
            fetch.addAttribute(ATTR_BIRTHDATE, "http://schema.openid.net/birthDate", false);
            fetch.addAttribute(ATTR_GENDER, "http://schema.openid.net/person/gender", false);
            fetch.addAttribute(ATTR_STATE, "http://schema.openid.net/contact/state/home", false);
            fetch.addAttribute(ATTR_COUNTRY, "http://schema.openid.net/contact/country/home", false);
            fetch.addAttribute(ATTR_LANGUAGE, "http://schema.openid.net/pref/language", false);
            
            // we only want one email address
            fetch.setCount(ATTR_EMAIL,1);

            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            // redirect to OpenID for authentication
            response.sendRedirect(authReq.getDestinationUrl(true));
        } catch (DiscoveryException de) {
            response.sendRedirect("./#loginMsg:username");
        } catch (OpenIDException e) {
            throw new ServletException("Login string probably caused an error. loginString = " + loginString, e);
        }
    }

    /**
     * Checks the response received by the OpenID provider, and saves the user identifier for later use
     * if the authentication was sucesssful.
     *
     * <b>Note</b>: While confusing, the OpenID provider's response is in fact encapsulated within
     * the request; this is because it is the provider who requested the page, and sent the response
     * as parameters.
     *
     * This is done in three steps:
     * <ol>
     * <li>Verify the OpenID resposne.</li>
     * <li>If verification was successful, retrieve the OpenID identifier of the user
     * and save it for later use.</li>
     * <li>Redirect the user back to the main page of the application, together with a cookie containing
     * his OpneID identifier.</li>
     * </ol>

     * @param request The request, containing the OpenID provider's response as parameters.
     * @param response The response, used to redirect the user back to the application page.
     * @throws IOException Occurs when redirection is not successful.
     * @throws ServletException Wrapping an OpenID exception.
     */
    private void verify(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {

            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList responseParams =
                    new ParameterList(request.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("openid-disc");
                
            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(request.getQueryString());
            
            Logger.getLogger("").info("Received back "+receivingURL.toString());
            
            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    responseParams, discovered);
            
            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {

                response.addCookie(new Cookie(openIdCookieName, verified.getIdentifier()));

                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

                // Extract user information
                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    request.getSession().setAttribute(openIdCookieName, verified.getIdentifier());
                    request.getSession().setAttribute(ATTR_NAME, fetchResp.getAttributeValue(ATTR_NAME));
                    request.getSession().setAttribute(ATTR_NICKNAME, fetchResp.getAttributeValue(ATTR_NICKNAME));
                    request.getSession().setAttribute(ATTR_BIRTHDATE, fetchResp.getAttributeValue(ATTR_BIRTHDATE));
                    request.getSession().setAttribute(ATTR_COUNTRY, fetchResp.getAttributeValue(ATTR_COUNTRY));
                    request.getSession().setAttribute(ATTR_STATE, fetchResp.getAttributeValue(ATTR_STATE));
                    request.getSession().setAttribute(ATTR_LANGUAGE, fetchResp.getAttributeValue(ATTR_LANGUAGE));
                    request.getSession().setAttribute(ATTR_EMAIL, fetchResp.getAttributeValue(ATTR_EMAIL));
                    request.getSession().setAttribute(ATTR_GENDER, fetchResp.getAttributeValue(ATTR_GENDER));

                } else {
                    Logger.getLogger("").info("Did not receive any details for the user.");
                }
                Logger.getLogger("").info("Login successfull");
                callback.saveIdentifierForUniqueId(request, getCookieValue(request,uniqueIdCookieName), verified);
            } else {
                Logger.getLogger("").info("Login failed");
            }

            response.sendRedirect(callback.getMainPageURL(request));
        }
        catch (OpenIDException e) {
            throw new ServletException("Could not verify identity", e);
        }
    }

    private static String getCookieValue(HttpServletRequest request, String key) {
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(key)) {
                return c.getValue();
            }
        }
        return "";
    }
    
    public static class SitmCallback implements Callback {

        private String getURL(String s, String tail) {
            return s.substring(0, s.lastIndexOf("/"))+tail;
        }
        
        public String getOpenIdServletURL(HttpServletRequest request) {
            return getURL(request.getRequestURL().toString(),"/Login");
        }

        public String getLoginURL(HttpServletRequest request) {
            return getURL(request.getRequestURL().toString(),"/Login");
        }

        public String getMainPageURL(HttpServletRequest request) {
            return getURL(request.getRequestURL().toString(),"/");
        }

        public String createUniqueIdForUser(String user) {
            Logger.getLogger("").info("creating unique id for user "+user);
            if (user.startsWith("http://")) {
                return user.substring(7);
            } else {
                return user;
            }
        }

        public void saveIdentifierForUniqueId(HttpServletRequest request, String uniqueId, Identifier identifier) {
            request.getSession().setAttribute("openid-disc", identifier);
            Logger.getLogger("").info("Saving identifier for user. unique id :"+uniqueId+" :: "+identifier.getIdentifier());
        }

        
    }
}

