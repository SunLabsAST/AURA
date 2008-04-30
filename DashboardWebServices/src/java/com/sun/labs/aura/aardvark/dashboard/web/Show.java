/*
 * Project Aura,
 * 
 * Copyright (c) 2008,  Sun Microsystems Inc
 * See license.txt for licensing info.
 */
package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.aardvark.BlogUser;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.aura.util.Tag;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import java.io.*;
import java.net.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class Show extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String defaultOp = "status";
        String op = request.getParameter("op");

        if (op == null) {
            op = defaultOp;
        }

        if (op.equals("status")) {
            showStatus(request, response);
        } else if (op.equals("recommendation")) {
            showRecommendation(request, response);
        } else if (op.equals("user")) {
            showUser(request, response);
        } else if (op.equals("feedAuthority")) {
            showFeedAuthority(request, response);
        } else if (op.equals("entryAuthority")) {
            showEntryAuthority(request, response);
        } else if (op.equals("item")) {
            showItem(request, response);
        } else if (op.equals("users")) {
            showUsers(request, response);
        } else if (op.equals("query")) {
            showQuery(request, response);
        } else {
            ServletContext context = getServletContext();
            Shared.forwardToError(context, request, response, "Unknown op " + op);
        }
    }

    protected void showRecommendation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();
        try {
            String userID = request.getParameter("user");
            String snum = request.getParameter("num");
            int num = 10;
            if (snum != null) {
                num = Integer.parseInt(snum);
            }

            Aardvark aardvark = (Aardvark) context.getAttribute("aardvark");
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");
            User user = aardvark.getUser(userID);
            if (user == null) {
                Shared.forwardToError(context, request, response, "Can't find user with ID " + userID);
                return;
            }

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");

            long start = System.currentTimeMillis();
            SyndFeed syndFeed = aardvark.getRecommendedFeed(user, num);
            long total = System.currentTimeMillis() - start;

            out.println(getHeader(syndFeed.getTitle()));
            List entries = syndFeed.getEntries();
            for (Object o : entries) {
                SyndEntry syndEntry = (SyndEntry) o;
                Item item = dataStore.getItem(syndEntry.getLink());
                BlogEntry entry = new BlogEntry(item);
                Item feedItem = dataStore.getItem(entry.getFeedKey());
                BlogFeed feed = new BlogFeed(feedItem);
                dumpEntry(out, entry, feed);
            }
            out.printf("<p>%d recommendations in %d ms\n", entries.size(), total);
            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showItem(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();
        try {
            String itemID = request.getParameter("item");
            Aardvark aardvark = (Aardvark) context.getAttribute("aardvark");
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            long start = System.currentTimeMillis();
            Item item = aardvark.getItem(itemID);
            if (item == null) {
                Shared.forwardToError(context, request, response, "Can't find item with ID " + itemID);
                return;
            }
            long total = System.currentTimeMillis() - start;

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Information for " + item.getName()));

            out.println("<h2>" + item.getName() + "</h2>");
            if (item.getType() == ItemType.FEED) {
                String q = "Show?op=query&query=feedKey=" + item.getKey();
                out.println("<p><a href=\"" + q + "\">Show entries for this feed.</a><p>");
            }
            out.println("<table>");
            fmtOut(out, "Key", fmtExternalLink(item.getKey()));
            fmtOut(out, "Type", item.getType().toString());
            fmtOut(out, "Added", new Date(item.getTimeAdded()).toString());

            for (String key : item.getMap().keySet()) {
                Object val = item.getMap().get(key);
                if (val != null) {
                    String sval = val.toString();
                    if (sval.startsWith("http://")) {
                        sval = fmtItemLink(sval);
                    }
                    // special case, see if any fields that end with 'time'
                    // are long values, of so convert them to dates
                    if (key.toLowerCase().endsWith("time")) {
                        if (val instanceof Long) {
                            Long lval = (Long) val;
                            sval = new Date(lval.longValue()).toString();
                        }
                    }
                    fmtOut(out, key, sval);
                }
            }
            out.println("</table>");
            dumpAttention(out, "Incoming attention", dataStore.getAttentionForTarget(itemID));
            dumpAttention(out, "Outgoing attention", dataStore.getAttentionForSource(itemID));
            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();
        try {
            String userID = request.getParameter("user");
            Aardvark aardvark = (Aardvark) context.getAttribute("aardvark");
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            long start = System.currentTimeMillis();
            User user = aardvark.getUser(userID);
            if (user == null) {
                Shared.forwardToError(context, request, response, "Can't find user with ID " + userID);
                return;
            }
            long total = System.currentTimeMillis() - start;

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");

            out.println(getHeader("Information for " + user.getName()));

            BlogUser buser = new BlogUser(user);
            out.println("<h2>" + user.getName() + "</h2>");
            out.println("<table>");
            fmtOut(out, "Country", buser.getCountry());
            fmtOut(out, "DOB", buser.getDob());
            fmtOut(out, "Email", buser.getEmailAddress());
            fmtOut(out, "Fullname", buser.getFullname());
            fmtOut(out, "Gender", buser.getGender());
            fmtOut(out, "Key", buser.getKey());
            fmtOut(out, "Language", buser.getLanguage());
            fmtOut(out, "Name", buser.getName());
            fmtOut(out, "Nick", buser.getNickname());
            fmtOut(out, "Postcode", buser.getPostcode());
            fmtOut(out, "Rand", buser.getRandString());
            fmtOut(out, "TZ", buser.getTimezone());
            out.println("</table>");

            dumpAttention(out, "Incoming attention", dataStore.getAttentionForTarget(userID));
            dumpAttention(out, "Outgoing attention", dataStore.getAttentionForSource(userID));

            out.printf("<p>User info loaded in %d ms\n", total);

            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showFeedAuthority(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();

        String snum = request.getParameter("num");
        int num = 100;
        if (snum != null) {
            num = Integer.parseInt(snum);
        }

        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Feed Authority"));

            // this way takes too long
            if (false) {
                List<Item> items = dataStore.getAll(ItemType.FEED);
                List<Scored<BlogFeed>> scoredFeeds = new ArrayList();
                for (Item item : items) {
                    BlogFeed feed = new BlogFeed(item);
                    //int incoming = dataStore.getAttentionForTarget(feed.getKey()).size();
                    float authority = feed.getAuthority();
                    scoredFeeds.add(new Scored<BlogFeed>(feed, authority));
                }
                Collections.sort(scoredFeeds);
                Collections.reverse(scoredFeeds);

                if (scoredFeeds.size() > num) {
                    scoredFeeds = scoredFeeds.subList(0, num);
                }
            }

            long total = dataStore.getItemCount(ItemType.FEED);
            //List<Scored<Item>> scoredItems = dataStore.query("aura-type=FEED", "-lastPullTime", num, null);
            List<Scored<Item>> scoredItems = dataStore.query("aura-type=FEED", "-numIncomingLinks", num, null);

            out.printf("<p>Showing %d of %d feeds<p>\n", scoredItems.size(), total);

            out.println("<table>");
            out.printf(getHeadTR() + "<td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b>\n",
                    "Name", " Authority", "In*", "In", "Out", "Key");
            for (Scored<Item> scoredItem : scoredItems) {
                BlogFeed feed = new BlogFeed(scoredItem.getItem());
                int storedIncomingLinks = feed.getNumIncomingLinks();
                List<Attention> inAttn = dataStore.getAttentionForTarget(feed.getKey());
                List<Attention> outAttn = dataStore.getAttentionForSource(feed.getKey());
                out.printf(getTR() + "<td>%s<td>%.2f<td>%d<td>%d<td>%d<td>%s\n",
                        feed.getName(), feed.getAuthority(), storedIncomingLinks, inAttn.size(),
                        outAttn.size(), fmtItemLink(feed.getKey()));
            }
            out.println("</table>");

            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showEntryAuthority(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();

        String snum = request.getParameter("num");
        int num = 100;
        if (snum != null) {
            num = Integer.parseInt(snum);
        }

        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Entry Authority"));

            // this way takes too long

            long total = dataStore.getItemCount(ItemType.BLOGENTRY);
            List<Scored<Item>> scoredItems = dataStore.query("aura-type=BLOGENTRY", "-authority", num, null);

            out.printf("<p>Showing %d of %d entries<p>\n", scoredItems.size(), total);

            out.println("<table>");
            out.printf(getHeadTR() + "<td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b>\n",
                    "Name", " Authority", "In", "Out", "Key");
            for (Scored<Item> scoredItem : scoredItems) {
                BlogEntry entry = new BlogEntry(scoredItem.getItem());
                List<Attention> inAttn = dataStore.getAttentionForTarget(entry.getKey());
                List<Attention> outAttn = dataStore.getAttentionForSource(entry.getKey());
                out.printf(getTR() + "<td>%s<td>%.2f<td>%d<td>%d<td>%s\n",
                        entry.getName(), entry.getAuthority(), inAttn.size(),
                        outAttn.size(), fmtItemLink(entry.getKey()));
            }
            out.println("</table>");

            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();

        String snum = request.getParameter("num");
        int num = 100;
        if (snum != null) {
            num = Integer.parseInt(snum);
        }

        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Users"));
            List<Item> items = dataStore.getAll(ItemType.USER);
            List<Scored<BlogUser>> scoredUsers = new ArrayList();
            for (Item item : items) {
                BlogUser user = new BlogUser((User) item);
                int attn = dataStore.getAttentionForSource(user.getKey()).size();
                scoredUsers.add(new Scored<BlogUser>(user, attn));
            }
            Collections.sort(scoredUsers);
            Collections.reverse(scoredUsers);

            if (scoredUsers.size() > num) {
                scoredUsers = scoredUsers.subList(0, num);
            }

            out.printf("<p>Showing %d of %d users<p>\n", scoredUsers.size(), items.size());

            out.println("<table>");
            out.printf(getHeadTR() + "<td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b>\n",
                    "Name", " Attention", "Key", "Recommend");
            for (Scored<BlogUser> scoredUser : scoredUsers) {
                BlogUser user = scoredUser.getItem();
                out.printf(getTR() +"<td>%s<td>%.0f<td>%s<td>%s\n",
                        user.getName(), scoredUser.getScore(), fmtItemLink(user.getKey()),
                        "<a href=Show?op=recommendation&user=" + user.getKey() + "> Recommend </a>");
            }
            out.println("</table>");

            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showQuery(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();

        String query = request.getParameter("query");
        if (query == null) {
            Shared.forwardToError(context, request, response, "missing query");
        }

        int num = 100;
        String snum = request.getParameter("num");
        if (snum != null) {
            num = Integer.parseInt(snum);
        }

        String sortSpec = request.getParameter("sort");
        if (sortSpec == null) {
            sortSpec = "-score";
        }

        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Query '" + query + "'"));
            out.println("<table>");
            out.printf(getHeadTR() + "<td><b>%s</b><td><b>%s</b><td><b>%s</b><td><b>%s</b>",
                    "Name", "Type", "Score", "Key");
            List<Scored<Item>> items = dataStore.query(query, sortSpec, num, null);
            for (Scored<Item> scoredItem : items) {
                Item item = scoredItem.getItem();
                double score = scoredItem.getScore();
                out.printf(getTR() + "<td>%s<td>%s<td>%.2f<td>%s", item.getName(),
                        item.getType().toString(), score, fmtItemLink(item.getKey()));
            }
            out.println("</table>");
            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void showStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();
        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");
            StatService ss = (StatService) context.getAttribute("statService");

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Status"));

            out.println("<table>");
            out.printf(getHeadTR() + "<td>Field<td>Count\n");
            fmtOut(out, "Users", Long.toString(dataStore.getItemCount(ItemType.USER)));
            fmtOut(out, "Feeds", Long.toString(dataStore.getItemCount(ItemType.FEED)));
            fmtOut(out, "Entries", Long.toString(dataStore.getItemCount(ItemType.BLOGENTRY)));
            fmtOut(out, "Attn", Long.toString(dataStore.getAttentionCount()));

            if (ss != null) {
                String[] counterNames = ss.getCounterNames();
                for (String name : counterNames) {
                    fmtOut(out, name, Long.toString(ss.get(name)));
                }
            }
            out.println("</table>");
            out.println(getFooter(System.currentTimeMillis() - entryTime));
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        } catch (RemoteException ex) {
            Shared.forwardToError(context, request, response, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void disableCaching(HttpServletResponse response) {
        if (false) {
            response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
        }
    }

    private void dumpAttention(PrintWriter out, String title, List<Attention> attnList) {
        if (attnList.size() > 0) {
            out.printf("<h3>" + title + "</h3>");
            out.printf("<table>");
            out.println(getHeadTR() + "<td>Source<td>Type<td>Target<td>Date");
            for (Attention attn : attnList) {
                out.printf(getTR() + "<td>%s<td>%s<td>%s<td>%s<td>\n",
                        fmtItemLink(attn.getSourceKey()), attn.getType().toString(),
                        fmtItemLink(attn.getTargetKey()), new Date(attn.getTimeStamp()).toString());
            }
            out.printf("</table>");
        }
    }

    private String fmtItemLink(String link) {
        return fmtItemLink(link, link);
    }

    private String fmtItemLink(String link, String text) {
        int MAX_LEN = 40;
        if (text.length() > MAX_LEN) {
            text = link.substring(0, MAX_LEN) + "...";
        }
        String encodedLink = link;
        try {
            encodedLink = URLEncoder.encode(link, "utf-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return "<a href=/DashboardWebServices/Show?op=item&item=" + encodedLink + ">" + text + "</a>";
    }

    private String fmtExternalLink(String link) {
        if (link != null) {
            return "<a href=" + link + ">" + link + "</a>";
        } else {
            return null;
        }
    }

    private void dumpEntry(PrintWriter out, BlogEntry entry, BlogFeed feed) {
        out.printf("<h2><a target=\"other\" href=\"%s\">%s</a></h2>", entry.getKey(), entry.getTitle());
        out.println("<p>" + fmtItemLink(entry.getKey(), "Show details for this entry.") + "</p>");
        out.println("<table>");
        fmtOut(out, "Author", entry.getAuthor());
        fmtOut(out, "Authority", Float.toString(entry.getAuthority()));
        fmtOut(out, "Publish Date", entry.getPublishDate().toString());
        fmtTitle(out, "Feed Info");
        fmtOut(out, "Feed Name", feed.getName());
        fmtOut(out, "Feed Authority", Float.toString(feed.getAuthority()));
        fmtOut(out, "Feed Incoming Links", Integer.toString(feed.getNumIncomingLinks()));
        fmtOut(out, "Feed Last Pull ", new Date(feed.getLastPullTime()).toString());
        fmtOut(out, "Feed URL", fmtItemLink(feed.getCannonicalURL()));

        {
            List<Scored<String>> tags = entry.getAutoTags();
            if (tags != null && tags.size() > 0) {
                fmtTitle(out, "Autotags");
                for (Scored<String> tag : tags) {
                    fmtOut(out, tag.getItem(), Double.toString(tag.getScore()));
                }
            }
        }

        {
            List<Tag> plaintags = entry.getTags();
            if (plaintags != null && plaintags.size() > 0) {
                fmtTitle(out, "Manual Tags");
                for (Tag tag : plaintags) {
                    fmtOut(out, tag.getName(), Integer.toString(tag.getCount()));
                }
            }
        }
        out.println("</table>");
    }

    private void fmtOut(PrintWriter out, String s, String v) {
        if (v != null) {
            out.printf(getTR() + "<td> %s<td> %s </tr>\n", s, v);
        }
    }

    private void fmtTitle(PrintWriter out, String s) {
        out.printf(getTR() + "<td colspan=\"2\"><b> %s </b></tr>\n", s);
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

    private String getQueryHTML() {
        return "<center><div class=\"tryit\"><b>Item Query:</b><input type=\"text\" size=\"60\"" +
                "onChange=\"window.location = 'Show?op=query&query=' + this.value;\" /> </div></center><p>";
    }

    private String getHeader(String title) {
        String s = "<html><head><title> " + title + "</title>";

        s += "<link rel=\"stylesheet\" type=\"text/css\" href=\"style/aardvark.css\"/>";
        s += "<link rel=\"Shortcut Icon\" href=\"/favicon.ico\"/>";

        s += "</head><body>";
        s += "<div align=\"center\">" +
                "<a href=\"/DashboardWebServices\">" +
                "<img src=\"images/aardvark-welcome.gif\">" +
                "</a>" +
                "</div>";
        s += getQueryHTML();
        s += "<h2>" + title + "</h2>";
        return s;
    }

    private String getFooter(long ms) {
        String s = "<center><em><p>Page loaded on " + new Date() + " in " + ms + " ms.</em></center>";
        s += "</body></html>";
        return s;
    }

    private boolean even = false;
    private String getTR() {
        String tr = "<tr class=" + (even ? "\"even\"" : "\"odd\"") + ">";
        even = !even;
        return tr;
    }

    private String getHeadTR() {
        even = false;
        return getTR();
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
