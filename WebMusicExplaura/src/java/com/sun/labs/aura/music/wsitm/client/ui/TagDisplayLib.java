/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.sun.labs.aura.music.wsitm.client.*;
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
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
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

    public enum TagColorType {
        TAG,
        TAG_POPUP,
        ARTIST,
        STICKY_TAG,
        STICKY_ARTIST
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
        
    public static void showTagCloud(String title, ItemInfo[] tags, ORDER order,
            ClientDataManager cdm) {
        //final DialogBox d = Popup.getDialogBox();
        final PopupPanel d = Popup.getPopupPanel();
        Panel p = getTagsInPanel(tags, d, order, cdm, TagColorType.TAG_POPUP);
        if (p!=null) {
        //    Popup.showPopup(p,title,d);
            Popup.showRoundedPopup(p, title, d);
        } else {
            Popup.showRoundedPopup(new Label("Sorry, no tags to display"), title, d);
        }
    }

    private static int scoreToFontSize(double score) {
        int min = 12;
        int max = 60;
        int range = max - min;
        return (int) Math.round(range * score + min);
    }

    public static Panel getTagsInPanel(ItemInfo[] tags, ORDER order,
            ClientDataManager cdm, TagColorType colorTheme) {
        return getTagsInPanel(tags, null, order, cdm, colorTheme);
    }
       
    /**
     * Return a panel containing the tags cloud passed in parameter. If panel
     * will be used in pop-up, pass the DialogBox that will contain it in d to add
     * so the pop-up can be closed when a tag is clicked on
     * @param tags
     * @param d
     * @return
     */
    public static Panel getTagsInPanel(ItemInfo[] tags, PopupPanel d, ORDER order,
            ClientDataManager cdm, TagColorType colorTheme) {
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
                setColorToElem(sL, colorId, tags[i].getScore(), null, colorTheme);
                sL.addStyleName("pointer");
                sL.addClickHandler(new DEClickHandler<ItemInfo>(tags[i]) {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        String tagLink = data.getId();
                        if (!tagLink.startsWith("artist-tag:")) {
                            tagLink = ClientDataManager.nameToKey(tagLink);
                        }
                        History.newItem("tag:"+tagLink);
                    }
                });
                if (d!=null) {
                    sL.addClickHandler(new DEClickHandler<PopupPanel>(d) {

                        @Override
                        public void onClick(ClickEvent event) {
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
     * Set the right stylesheet to a tag label based on its color and index
     * @param sL label
     * @param index {0,1}
     * @param size Size of label; will determine if colored positive or negative
     */
    public static String setColorToElem(Label sL, int index, double size, String previousColor, TagColorType colorType) {
        String newColor = "";
        String negStr = "";
        if (size<0) {
            negStr="neg";
        }
        if (colorType == TagColorType.TAG) {
            newColor = "tag" + (index + 1) + negStr;

        } else if (colorType == TagColorType.TAG_POPUP) {
            newColor = "tagPop" + (index + 1) + negStr;

        } else if (colorType == TagColorType.ARTIST) {
            newColor = "artistTag" + (index + 1) + negStr;

        } else if (colorType==TagColorType.STICKY_TAG) {
            if (size<0) {
                return setColorToElem(sL, index, size, previousColor, TagColorType.TAG);
            } else {
                newColor = "tag"+(index+1)+"Sticky";
            }
        } else if (colorType==TagColorType.STICKY_ARTIST) {
            if (size<0) {
                return setColorToElem(sL, index, size, previousColor, TagColorType.ARTIST);
            } else {
                newColor = "artistTag"+(index+1)+"Sticky";
            }
        }

        if (previousColor != null && previousColor.length() > 0) {
            sL.removeStyleName(previousColor);
        }
        sL.addStyleName(newColor);
        return newColor;

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
