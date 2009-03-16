/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.fb.util;

import com.sun.labs.aura.fb.ItemInfo;
import com.sun.labs.aura.util.Scored;
import java.io.StringWriter;
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
