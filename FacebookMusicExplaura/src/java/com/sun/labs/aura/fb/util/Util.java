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

package com.sun.labs.aura.fb.util;

import com.sun.labs.aura.fb.ItemInfo;
import com.sun.labs.aura.util.Scored;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

/**
 *
 */
public class Util {
    protected static final DecimalFormat scoreFormat = new DecimalFormat("0.0#####");
    /**
     * Generates the root path URL of the application
     *
     * @param req the request
     * @param relative if this should return a relative or absolute url
     *
     * @return the root path URL
     */
    public static String getRootPath(HttpServletRequest req,
            boolean relative) {
        String context = "";
        if (!relative) {
            int port = req.getServerPort();
            context = req.getScheme() + "://" + req.getServerName() +
                    (port != 80 ? ":" + port : "") +
                    req.getContextPath();
        }
        return context;
    }

    public static String getWMELink(ItemInfo[] tags) {
        String url = "http://music.tastekeeper.com/#steering:cloud:";
        for (ItemInfo tag : tags) {
            url += "(" + tag.getItemName() + "," +
                    scoreFormat.format(tag.getScore()) + ")";
        }
        return url;
    }

    public static String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ItemInfo[] normalize(ItemInfo[] itemInfo) {
        ItemInfo[] retItemInfo = new ItemInfo[itemInfo.length];
        double max = findMax(itemInfo);
        for (int i = 0; i < itemInfo.length; i++) {
            retItemInfo[i] = new ItemInfo(itemInfo[i].getId(), itemInfo[i].getItemName(),
                    itemInfo[i].getScore() / max, itemInfo[i].getPopularity());
        }
        return retItemInfo;
    }

    public static ItemInfo[] normalize(Collection<Scored<String>> tags) {
        ItemInfo[] retItemInfo = new ItemInfo[tags.size()];
        double max = findMax(tags);
        int i = 0;
        for (Scored<String> sstr : tags) {
            retItemInfo[i++] = new ItemInfo("", sstr.getItem(),
                    sstr.getScore() / max, 0);
        }
        return retItemInfo;
    }

    public static double findMax(ItemInfo[] itemInfos) {
        double max = -Double.MAX_VALUE;

        for (ItemInfo ii : itemInfos) {
            if (ii.getScore() > max) {
                max = ii.getScore();
            }
        }
        return max;
    }

    public static <V> double findMax(Collection<Scored<V>> values) {
        double max = -Double.MAX_VALUE;

        for (Scored<V> scored : values) {
            if (scored.getScore() > max) {
                max = scored.getScore();
            }
        }
        return max;
    }

    public static List<ItemInfo> negative(List<ItemInfo> infos) {
        List<ItemInfo> retList = new ArrayList<ItemInfo>();
        for (ItemInfo ii : infos) {
            retList.add(new ItemInfo(ii.getId(), ii.getItemName(),
                    -ii.getScore(), ii.getPopularity()));
        }
        return retList;
    }

    public static List<ItemInfo> getTopUniqueInfo(ItemInfo[] infos, Set<String> ignoreNames, int maxSize) {
        Arrays.sort(infos, ItemInfo.getScoreSorter());

        List<ItemInfo> list = new ArrayList<ItemInfo>();
        for (ItemInfo ii : infos) {
            if (!ignoreNames.contains(ii.getItemName())) {
                list.add(ii);
            }
        }
        if (list.size() > maxSize) {
            list = list.subList(0, maxSize);
        }
        return list;
    }

}
