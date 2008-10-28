/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.CommonTagsAsyncCallback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import java.util.HashMap;
import java.util.Arrays;

/**
 *
 * @author mailletf
 */
public abstract class TagDisplayLib {

    public enum ORDER {
        ALPHABETICAL,
        DESC,
        SHUFFLE
    }

    public static void showDifferenceCloud(String title, ItemInfo[] tags1, ItemInfo[] tags2, ClientDataManager cdm) {
        
        HashMap<String, Double> normTags1 = normaliseItemInfoArray(tags1);
        HashMap<String, Double> normTags2 = normaliseItemInfoArray(tags2);
        
        HashMap<String, Double> subTags = new HashMap<String, Double>();
        for (String s : normTags1.keySet()) {
            if (normTags2.containsKey(s)) {
                subTags.put(s, normTags1.get(s) - normTags2.get(s));
            } else {
                subTags.put(s, normTags1.get(s));
            }
        }
        // All tags from normTags2 that are not in subTags are not contained
        // in normTags1 so we can add them as negative valued tags
        for (String s : normTags2.keySet()) {
            if (!subTags.containsKey(s)) {
                subTags.put(s, -normTags2.get(s));
            }
        }

        showTagCloud(title, subTags, ORDER.DESC, cdm);
    }
    
    private static HashMap<String, Double> normaliseItemInfoArray(ItemInfo[] iI) {
        
        HashMap<String, Double> normMap = new HashMap<String, Double>();
        
        double maxVal = 0;
        for (ItemInfo i : iI) {
            if (i.getScore() > maxVal) {
                maxVal = i.getScore();
            }
        }
        
        for (ItemInfo i : iI) {
            normMap.put(i.getItemName(), i.getScore() / maxVal);
        }
        
        return normMap;
    }
   
    public static void showTagCloud(String title, HashMap<String, Double> tags, ORDER order, ClientDataManager cdm) {
        ItemInfo[] iI = new ItemInfo[tags.size()];
        int index = 0;
        for (String name : tags.keySet()) {
            double val = tags.get(name);
            iI[index++] = new ItemInfo(ClientDataManager.nameToKey(name), name, val, val);
        }
        showTagCloud(title, iI, order, cdm);
    }
        
    public static void showTagCloud(String title, ItemInfo[] tags, ORDER order, ClientDataManager cdm) {
        //final DialogBox d = Popup.getDialogBox();
        final PopupPanel d = Popup.getPopupPanel();
        Panel p = getTagsInPanel(tags, d, order, cdm);
        if (p!=null) {
        //    Popup.showPopup(p,title,d);
            Popup.showRoundedPopup(p, title, d);
        }
    }

    private static int scoreToFontSize(double score) {
        int min = 12;
        int max = 60;
        int range = max - min;
        return (int) Math.round(range * score + min);
    }

    public static Panel getTagsInPanel(ItemInfo[] tags, ORDER order, ClientDataManager cdm) {
        return getTagsInPanel(tags, null, order, cdm);
    }
       
    /**
     * Return a panel containing the tags cloud passed in parameter. If panel
     * will be used in pop-up, pass the DialogBox that will contain it in d to add
     * so the pop-up can be closed when a tag is clicked on
     * @param tags
     * @param d
     * @return
     */
    public static Panel getTagsInPanel(ItemInfo[] tags, PopupPanel d, ORDER order, ClientDataManager cdm) {
        Panel p = new FlowPanel();
        if (d != null) {
            p.setWidth("600px");
        }
        HorizontalPanel innerP = new HorizontalPanel();
        innerP.setSpacing(4);

        if (tags != null && tags.length > 0) {

            double max = 0;
            double min = 1;
            double tempScore;
            for (ItemInfo tag : tags) {
                tempScore = Math.abs(tag.getScore());
                if (tempScore > max) {
                    max = tempScore;
                } else if (tempScore < min) {
                    min = tempScore;
                }
            }
            double range = max - min;

            if (order == ORDER.SHUFFLE) {
                Arrays.sort(tags, ItemInfo.getRandomSorter());
            } else if (order == ORDER.DESC) {
                Arrays.sort(tags, ItemInfo.getScoreSorter());
            } else if (order == ORDER.ALPHABETICAL) {
                Arrays.sort(tags, ItemInfo.getNameSorter());
            }

            for (int i = 0; i < tags.length; i++) {
                int colorId = i % 2;
                int fontSize;
                if (tags.length == 1 || range == 0) {
                    fontSize = scoreToFontSize(1);
                } else {
                    fontSize = scoreToFontSize(( Math.abs(tags[i].getScore()) - min) / range);
                }

                ContextMenuTagLabel sL = new ContextMenuTagLabel(tags[i], cdm);
                //sL.getElement().getStyle().setPropertyPx("font-size", fontSize);
                sL.getElement().setAttribute("style", "font-size:" + fontSize + "px;");
                setColorToElem(sL, colorId, tags[i].getScore());
                sL.addStyleName("pointer");
                sL.addClickListener(new DataEmbededClickListener<ItemInfo>(tags[i]) {

                    public void onClick(Widget arg0) {
                        String tagLink = data.getId();
                        if (!tagLink.startsWith("artist-tag:")) {
                            tagLink = ClientDataManager.nameToKey(tagLink);
                        }
                        History.newItem("tag:"+tagLink);
                    }
                });
                if (d!=null) {
                    sL.addClickListener(new DataEmbededClickListener<PopupPanel>(d) {

                        public void onClick(Widget arg0) {
                            data.hide();
                        }
                    });
                }
                p.add(sL);
                p.add(new SpannedLabel("    "));
            }
            return p;
        } else {
            return null;
        }
    }

    /**
     * Set the right stylesheet to a tag label
     * @param sL label
     * @param index {0,1}
     * @param size Size of label; will determine if colored positive or negative
     */
    public static void setColorToElem(Label sL, int index, double size) {
        if (index == 0) {
            if (size < 0) {
                sL.addStyleName("tag1neg");
            } else {
                sL.addStyleName("tag1");
            }
        } else if (index == 1) {
            if (size < 0) {
                sL.addStyleName("tag2neg");
            } else {
                sL.addStyleName("tag2");
            }
        } else {
            Window.alert("Invalid color");
        }        
    }
    
    public static void invokeGetCommonTags(String artistID1, String artistID2,
            MusicSearchInterfaceAsync musicServer, ClientDataManager cdm, CommonTagsAsyncCallback callback) {

        try {
            musicServer.getCommonTags(artistID1, artistID2, 30, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    public static void invokeGetCommonTags(HashMap<String, Double> tagMap, String artistID,
            MusicSearchInterfaceAsync musicServer, ClientDataManager cdm, CommonTagsAsyncCallback callback) {

        try {
            musicServer.getCommonTags(tagMap, artistID, 30, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    public abstract class AsyncCallbackWithCommand implements AsyncCallback {

        protected Command cmd;

        public AsyncCallbackWithCommand(Command cmd) {
            this.cmd = cmd;
        }
    }
}
