package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.util.props.ConfigurationManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generates a feed for a user
 */
public class GetStories extends HttpServlet {

    protected Logger logger = Logger.getLogger("");

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */

//  getStories?topics=news,music,business,technology,all&time=epoch&delta=epoch&max=count
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        DataStore dataStore = (DataStore) context.getAttribute("dataStore");


        String topicString = request.getParameter("topics");
        if (topicString == null) {
            topicString = "all";
        }
        String[] topics = topicString.split(",");

        Set<String> topicSet = new HashSet<String>();
        for (String topic : topics) {
            topicSet.add(topic);
        }

        int maxCount = 1000;
        String maxCountString = request.getParameter("max");
        if (maxCountString != null) {
            maxCount = Integer.parseInt(maxCountString);
        }

        String timeString = request.getParameter("time");
        long time = System.currentTimeMillis();
        if (timeString != null && !timeString.equalsIgnoreCase("now")) {
            time = Long.parseLong(timeString);
        }

        long delta = -60 * 1000l;
        String deltaTimeString = request.getParameter("delta");
        if (deltaTimeString != null) {
            delta = Long.parseLong(deltaTimeString);
        }

        try {
            List<BlogEntry> entries = collectStories(dataStore, time, delta, maxCount, topicSet);

            response.setContentType("text/xml;charset=UTF-8");
            PrintWriter out = response.getWriter();
            StoryUtil.dumpStories(out,  dataStore, entries);
            out.close();

        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        }
    }

    private List<BlogEntry> collectStories(DataStore dataStore,
            long time, long delta, int maxCount, Set<String> topicSet) throws AuraException, RemoteException {
        List<BlogEntry> results = new ArrayList<BlogEntry>();
        long start;
        long end;

        if (delta >= 0) {
            start = time;
            if (delta > 0) {
                end = start + delta;
            } else {
                end = System.currentTimeMillis();
            }
        } else {
            end = time;
            start = end + delta;
        }

        if (end > System.currentTimeMillis()) {
            try {
                Thread.sleep(end - System.currentTimeMillis());
            } catch (InterruptedException e) {
                // if we are interruped, return an empty set
                return results;
            }
        }

        DBIterator<Item> iter = dataStore.getItemsAddedSince(ItemType.BLOGENTRY, new Date(start));
        try {
            while (iter.hasNext() && results.size() < maxCount) {
                Item item = iter.next();
                BlogEntry entry = new BlogEntry(item);
                if (entry.getTimeAdded() > end) {
                    break;
                }
                if (inTopicSet(entry, topicSet)) {
                    results.add(entry);
                }
            }
        } finally {
            iter.close();
        }
        return results;
    }

    private boolean inTopicSet(BlogEntry entry, Set<String> topicSet) {
        // TBD: Adjust this to use autoclass
        if (topicSet.contains("all")) {
            return true;
        } else {
            for (Tag tag : entry.getTags()) {
                if (topicSet.contains(tag.getName().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
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
