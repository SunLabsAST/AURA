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

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.agentspecific.impl.CssDefsImpl;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PlayButton;

/**
 *
 * @author mailletf
 */
public abstract class WebLib {

    /**
     * @deprecated 
     */
    public static final String ICON_WAIT = "ajax-bar.gif";
    public static final String ICON_WAIT_SUN = "loader-sun.gif";
    public static final String ICON_WAIT_SUN_BACK = "loader-sun-back.gif";

    /**
     * Disables the browsers default context menu for the specified element.
     *
     * @see http://groups.google.com/group/Google-Web-Toolkit/browse_thread/thread/73c80118cb781390/3b76ad6a17a6c376
     * @param elem the element whos context menu will be disabled
     */
    public static native void disableContextMenu(Element elem) /*-{
    elem.oncontextmenu=elem.onclick;
    }-*/;

    /**
     * Selection MUST be reenabled when widget is deleted to prevent a memory leak
     * @see Taken from Gwt EXT 1.0
     * @param disable Set to true to disable the selection and to false to reenable it
     */
    public native static void disableTextSelectInternal(Element e, boolean disable)/*-{
    if (disable) {
    e.ondrag = function () { return false; };
    e.onselectstart = function () { return false; };
    } else {
    e.ondrag = null;
    e.onselectstart = null;
    }
    }-*/;
    
    public static ItemInfo getBestTag(ArtistCompact artist) {
        ItemInfo tag = null;
        ItemInfo[] tags = artist.getDistinctiveTags();
        if (tags == null && tags.length == 0) {
            tags = artist.getDistinctiveTags();
        }
        if (tags != null && tags.length > 0) {
            tag = tags[0];
        }
        return tag;
    }

    public static VerticalPanel createSection(String title, Widget widget) {
        return createSection(new HTML("<h2>" + title + "</h2>"), widget);
    }

    public static VerticalPanel createSection(Widget title, Widget widget) {
        VerticalPanel panel = new VerticalPanel();
        panel.add(title);
        panel.add(widget);
        return panel;
    }

    public static String createAnchoredImage(String imageURL, String url, String style) {
        String styleSpec = "";
        if (style != null) {
            styleSpec = "style=\"" + style + "\"";
        }
        return createAnchor("<img class=\"inlineAlbumArt\" " + styleSpec + " src=\"" + imageURL + "\"/>", url);
    }

    /**
     * Creates a link
     * @param text link description
     * @param url link url
     * @return html formated link
     */
    public static String createAnchor(String text, String url) {
            return "<a href=\"" + url + "\" target=\"window1\">" + text + "</a>";
    }
    
    public static Widget getListenWidget(final TagDetails tagDetails) {
        Image image = PlayButton.playImgBundle.playLastfm30().createImage();
        image.setTitle("Play music like " + tagDetails.getName() + " at last.fm");
        image.setStyleName("pointer");
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                //Window.open(getTagRadioLink(tagDetails.getName()), "lastfm_popup", "width=300,height=266,menubar=no,toolbar=no,directories=no," + "location=no,resizable=no,scrollbars=no,status=no");
                Window.open(getTagRadioLink(tagDetails.getName()), "lastfm_popup", "width=340,height=286");
            }
        });
        return image;
    }
    
    public static String getTagRadioLink(String tagName) {
        tagName = tagName.replaceAll("\\s+", "%20");
        //String link = "http://www.last.fm/webclient/popup/?radioURL=" + "lastfm://globaltags/TAG_REPLACE_ME/&resourceID=undefined" + "&resourceType=undefined&viral=true";
        //return link.replaceAll("TAG_REPLACE_ME", tagName);
        String link = CssDefsImpl.impl.getLastFmRadioPrefix()+"MusicPlayer?type=tag&name=" + tagName;
        return link;
    }

    public static Widget getSunLoaderWidget(boolean whiteBack) {
        HorizontalPanel hP = new HorizontalPanel();
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        if (whiteBack) {
            hP.add(new Image(WebLib.ICON_WAIT_SUN));
        } else {
            hP.add(new Image(WebLib.ICON_WAIT_SUN_BACK));
        }

        return hP;
    }

    public static Widget getSunLoaderWidget() {
        return getSunLoaderWidget(true);
    }

    public static Widget getPopularityWidget(String name, double normPopularity, boolean log, String style) {
        return getPopularityWidget(name, normPopularity, 100, log, style);
    }

    /**
     * Build a horizontal bar representing the popularity
     * @param name name of the artist or concept
     * @param normPopularity popularity as a number between 0 and 1
     * @param log plot on a log scale
     * @param style style to apply to the name
     */
    public static Widget getPopularityWidget(String name, double normPopularity, int maxWidth, boolean log, String style) {

        VerticalPanel vPanel = new VerticalPanel();
        Label lbl = new Label(name);
        if (style!=null && !style.equals("")) {
            lbl.addStyleName(style);
        }
        vPanel.add(lbl);
        vPanel.add(getPopularityHisto(normPopularity, log, 15, maxWidth));
        return vPanel;
    }

    public static Widget getSmallPopularityWidget(double normPopularity, boolean log, boolean displayName) {
        return getSmallPopularityWidget(normPopularity, 100, log, displayName);
    }

    /**
     * Get small version of horizontal bar representing the popularity
     * @param normPopularity popularity as a number between 0 and 1
     * @param maxWidth width of the popularity histo if normPopularity=1
     * @param log plot on log scale
     * @return
     */
    public static Widget getSmallPopularityWidget(double normPopularity, int maxWidth, boolean log, boolean displayName) {

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        Label lbl = new Label("Popularity: ");
        lbl.setStyleName("recoTags");
        lbl.addStyleName("marginRight");
        lbl.addStyleName("bold");
        if (displayName) {
            hPanel.add(lbl);
        }
        hPanel.add(getPopularityHisto(normPopularity, log, 8, maxWidth));
        return hPanel;
    }

    /**
     * Returns a populatiry histrogram. To get it wrapped with a title, use other
     * utility functions getSmallPopularityWidget() or getPopularityWidget()
     * @param normPopularity popularity as a number between 0 and 1
     * @param log plot on log scale
     * @param height maximum size of 15
     * @param maxWidth
     * @return
     */
    public static HorizontalPanel getPopularityHisto(double normPopularity, boolean log, int height, int maxWidth) {

        if (log) {
            normPopularity = Math.log(normPopularity + 1) / Math.log(2); // get the base 2 log
        }
        int leftWidth = (int) (normPopularity * maxWidth);
        if (leftWidth < 1) {
            leftWidth = 1;
        } else if (leftWidth > maxWidth) {
            leftWidth = maxWidth;
        }
        int rightWidth = maxWidth - leftWidth;

        HorizontalPanel table = new HorizontalPanel();
        table.setWidth(maxWidth+"px");
        table.setBorderWidth(0);
        table.setSpacing(0);

        Widget left = new Label("");
        left.setStyleName("popLeft");
        left.setWidth(leftWidth + "");
        left.setHeight(height+"px");
        left.getElement().getStyle().setPropertyPx("fontSize", height-2);

        Widget right = new Label("");
        right.setStyleName("popRight");
        right.setWidth(rightWidth + "");
        right.setHeight(height+"px");
        right.getElement().getStyle().setPropertyPx("fontSize", height-2);

        table.add(left);
        table.add(right);

        return table;
    }

    public static void trackPageLoad(String operation, String item, String detail) {
        trackPageLoad(operation + "/" + item + "/" + detail);
    }

    public static void trackPageLoad(String operation, String item) {
        trackPageLoad(operation + "/" + item);
    }

    public static void trackPageLoad(String page) {
        trackPageLoadNative("/" + page);
    }

    public static native void trackPageLoadNative(String page) /*-{
          $wnd.pageTracker._trackPageview(page);
}-*/;

    public static String traceToString(Throwable e) {
        String trace = "\n"+e.getClass()+"\n";
        for (StackTraceElement s : e.getStackTrace()) {
            trace += "    at  " + s + "\n";
        }
        return trace;
    }

}
