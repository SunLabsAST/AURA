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
import com.sun.labs.aura.recommender.Recommendation;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        } else if (op.equals("experimentalRecommendation")) {
            showExperimentalRecommendation(request, response);
        } else if (op.equals("user")) {
            showUser(request, response);
        } else if (op.equals("feedAuthority")) {
            showFeedAuthority(request, response);
        } else if (op.equals("dumpFeeds")) {
            dumpFeeds(request, response);
        } else if (op.equals("dumpHosts")) {
            dumpHosts(request, response);
        } else if (op.equals("entryAuthority")) {
            showEntryAuthority(request, response);
        } else if (op.equals("findSimilar")) {
            showSimilar(request, response);
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

    protected void showExperimentalRecommendation(HttpServletRequest request, HttpServletResponse response)
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
            ExperimentalRecommender er = new ExperimentalRecommender(dataStore);
            List<Recommendation> recommendations = er.getRecommendations(user, num);
            long total = System.currentTimeMillis() - start;

            out.println(getHeader("Experimental recommendations for " + user.getName()));
            for (Recommendation recommendation : recommendations) {
                Item item = recommendation.getItem();
                BlogEntry entry = new BlogEntry(item);
                Item feedItem = dataStore.getItem(entry.getFeedKey());
                BlogFeed feed = new BlogFeed(feedItem);
                dumpEntry(out, entry, feed);
            }
            out.printf("<p>%d recommendations in %d ms\n", recommendations.size(), total);
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
            if (item.getType() == ItemType.FEED) {
                String q = "Show?op=query&query=" + encode("feedKey=" + item.getKey());
                out.println("<p><a href=\"" + q + "\">Show entries for this feed.</a><p>");
            }

            {
                String q = "Show?op=findSimilar&item=" + encode(item.getKey());
                out.println("<p><a href=\"" + q + "\">Find similar items.</a><p>");
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
                out.printf(tr() + "<td>%s<td>%.2f<td>%d<td>%d<td>%d<td>%s\n",
                        fmtExternalLink(feed.getKey(), feed.getName()), feed.getAuthority(), storedIncomingLinks, inAttn.size(),
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

    protected void dumpFeeds(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();

        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            disableCaching(response);
            response.setContentType("text/plain");

            List<Item> feeds = dataStore.getAll(ItemType.FEED);
            for (Item feed : feeds) {
                BlogFeed bfeed = new BlogFeed(feed);
                out.printf("%.2f %d %d %s\n", bfeed.getAuthority(),
                        bfeed.getNumPulls(), bfeed.getNumErrors(),
                        bfeed.getKey());
            }
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

    protected void dumpHosts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();

        try {
            DataStore dataStore = (DataStore) context.getAttribute("dataStore");

            disableCaching(response);
            response.setContentType("text/plain");

            List<Item> feeds = dataStore.getAll(ItemType.FEED);
            Map<String, Integer> hostCount = new HashMap();
            int errorCount = 0;
            for (Item feed : feeds) {
                try {
                    URL url = new URL(feed.getKey());
                    String host = url.getHost();
                    Integer i = hostCount.get(host);
                    if (i == null) {
                        i = new Integer(0);
                    }
                    hostCount.put(host, i + 1);
                } catch (MalformedURLException e) {
                    errorCount++;
                }
            }
            out.println("# hosts: " + hostCount.size() + " badhosts: " + errorCount);
            for (String host : hostCount.keySet()) {
                out.println(hostCount.get(host) + " " + host);
            }
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
                out.printf(tr() + "<td>%s<td>%.2f<td>%d<td>%d<td>%s\n",
                        fmtExternalLink(entry.getKey(), entry.getName()), entry.getAuthority(), inAttn.size(),
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

    protected void showSimilar(HttpServletRequest request, HttpServletResponse response)
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
            String itemID = request.getParameter("item");
            Item item = dataStore.getItem(itemID);

            if (item == null) {
                Shared.forwardToError(context, request, response, "Can't find item " + itemID);
                return;
            }

            disableCaching(response);
            response.setContentType("text/html;charset=UTF-8");
            out.println(getHeader("Find Similar for " + item.getName()));

            List<Scored<Item>> scoredItems = dataStore.findSimilar(itemID, num, null);

            out.println("<table>");
            out.println(getHeadTR() + th("Name") + th("Type") + th("Score") + th("Key"));
            for (Scored<Item> scoredItem : scoredItems) {
                Item sitem = scoredItem.getItem();
                out.println(
                        tr() +
                        td(fmtExternalLink(sitem.getKey(), sitem.getName())) +
                        td(sitem.getType().toString()) +
                        td(scoredItem.getScore()) +
                        td(fmtItemLink(sitem.getKey())));

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
            Collections.sort(scoredUsers, ScoredComparator.COMPARATOR);
            Collections.reverse(scoredUsers);

            if (scoredUsers.size() > num) {
                scoredUsers = scoredUsers.subList(0, num);
            }

            out.printf("<p>Showing %d of %d users<p>\n", scoredUsers.size(), items.size());

            out.println("<table>");
            out.println(getHeadTR() + th("Name") + th("Attention") +
                    th("Key") + th(2, "Recommendation"));
            for (Scored<BlogUser> scoredUser : scoredUsers) {
                BlogUser user = scoredUser.getItem();
                out.println(tr() +
                        td(user.getName()) +
                        td(scoredUser.getScore()) +
                        td(fmtItemLink(user.getKey())) +
                        td("<a href=Show?op=recommendation&user=" + user.getKey() + "> Standard </a>") +
                        td("<a href=Show?op=experimentalRecommendation&user=" + user.getKey() + "> Experimental </a>"));
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
                out.printf(tr() + "<td>%s<td>%s<td>%.2f<td>%s", fmtExternalLink(item.getKey(), item.getName()),
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
            out.println(getHeader("Status", "bigMargin"));

            out.println("<table>");
            out.println(getHeadTR() + th("Field") + th("Count"));
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
            out.println("<h3>" + title + "</h3>");
            out.println("<table>");
            out.println(getHeadTR() + "<td>Source<td>Type<td>Target<td>Date");
            for (Attention attn : attnList) {
                out.printf(tr() + "<td>%s<td>%s<td>%s<td>%s<td>\n",
                        fmtItemLink(attn.getSourceKey()), attn.getType().toString(),
                        fmtItemLink(attn.getTargetKey()), new Date(attn.getTimeStamp()).toString());
            }
            out.println("</table>");
        }
    }

    private String fmtItemLink(String link) {
        return fmtItemLink(link, link);
    }

    private String fmtItemLink(String link, String text) {
        return fmtItemLink(link, text, 40);
    }

    private String fmtItemLink(String link, String text, int len) {
        return "<a href=/DashboardWebServices/Show?op=item&item=" + encode(link) + ">" + trim(text, len) + "</a>";
    }

    private String trim(String s) {
        trim(s, 40);
        return s;
    }

    private String trim(String s, int maxLen) {
        if (s.length() > maxLen) {
            s = s.substring(0, maxLen) + "...";
        }
        return s;
    }

    private String encode(String s) {
        try {
            s = URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return s;
    }

    private String fmtExternalLink(String link) {
        return fmtExternalLink(link, link);
    }

    private String fmtExternalLink(String link, String text) {
        return fmtExternalLink(link, text, 40);
    }

    private String fmtExternalLink(String link, String text, int len) {
        if (link != null) {
            if (text == null) {
                text = link;
            }
            return "<a href=" + link + ">" + trim(text, len) + "</a>";
        } else {
            return null;
        }
    }

    private void dumpEntry(PrintWriter out, BlogEntry entry, BlogFeed feed) {
        out.println("<div class=\"title\">" + fmtExternalLink(entry.getKey(), entry.getTitle(), 80) + "</div>");

        out.println("<div class=\"indented-table\"><table>");
        out.println(
                getHeadTR() +
                td(fmtItemLink(entry.getKey(), "Details")) +
                td(em(entry.getPublishDate().toString())) +
                td("Authority: " + Float.toString(entry.getAuthority())) +
                td("Feed: " + fmtItemLink(feed.getKey(), feed.getName())));
        out.println("</table></div><p>");

    /*
    String manualTags = collectTags(entry.getTags());
    if (manualTags.length() > 0) {
    fmtOut(out, "Manual tags", feed.getName());
    }
    String autoTags = collectAutotags(entry.getAutoTags());
    if (autoTags.length() > 0) {
    fmtOut(out, "Autotags", feed.getName());
    }
     */
    }

    private void fmtOut(PrintWriter out, String s, String v) {
        if (v != null) {
            out.printf(tr() + "<td> %s<td> %s </tr>\n", s, v);
        }
    }

    private String collectAutotags(List<Scored<String>> tags) {
        StringBuilder sb = new StringBuilder();
        if (tags != null) {
            for (Scored<String> tag : tags) {
                sb.append(tag.getItem());
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String collectTags(List<Tag> tags) {
        StringBuilder sb = new StringBuilder();
        if (tags != null) {
            for (Tag tag : tags) {
                sb.append(tag.getName());
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private void fmtTitle(PrintWriter out, String s) {
        out.printf(tr() + "<td colspan=\"2\"><b> %s </b></tr>\n", s);
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
        return "<center><div class=\"tryit\"><b>Blog search:</b><input type=\"text\" size=\"60\"" +
                "onChange=\"window.location = 'Show?op=query&query=' + this.value;\" /> </div></center><p>";
    }

    private String getHeader(String title) {
        return getHeader(title, "standard");
    }

    private String getHeader(String title, String divClass) {
        String s = "<html><head><title> " + title + "</title>";

        s += "<link rel=\"stylesheet\" type=\"text/css\" href=\"style/aardvark.css\"/>";
        s += "<link rel=\"Shortcut Icon\" href=\"/DashboardWebServices/favicon.ico\"/>";

        s += "</head><body>";
        s += "<div align=\"center\">" +
                "<a href=\"/DashboardWebServices\">" +
                "<img src=\"images/aardvark-welcome.gif\">" +
                "</a>" +
                "</div>";
        s += getQueryHTML();

        s += "<div class=\"" + divClass + "\">";
        s += "<h2 class=\"bigOrangeTxt\">" + title + "</h2>";

        return s;
    }

    private String getFooter(long ms) {
        String s = "";
        s += "</div>";
        s += "<center><em><p>Page loaded on " + new Date() + " in " + ms + " ms.</em></center>";
        s += "</body></html>";
        return s;
    }
    private boolean even = false;

    private String tr() {
        String tr = "<tr class=" + (even ? "\"even\"" : "\"odd\"") + ">";
        even = !even;
        return tr;
    }

    private String getHeadTR() {
        even = false;
        return tr();
    }

    private String th(String s) {
        return "<td><b>" + s + "</b></td>";
    }

    private String th(int colspan, String s) {
        return "<td colspan=" + colspan + "><b>" + s + "</b></td>";
    }

    private String td(String s) {
        return "<td>" + s + "</td>";
    }

    private String td(int colspan, String s) {
        return "<td colspan=" + colspan + ">" + s + "</td>";
    }

    private String td(double f) {
        return String.format("<td>%.2f</td>", f);
    }

    private String td(int f) {
        return String.format("<td>%d</td>", f);
    }

    private String em(String s) {
        return "<em>" + s + "</em>";
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
