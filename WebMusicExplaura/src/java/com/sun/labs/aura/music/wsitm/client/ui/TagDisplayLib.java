/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.CommonTagsAsyncCallback;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public abstract class TagDisplayLib {

    public static void showTagCloud(String title, ItemInfo[] tags, ClientDataManager cdm) {
        final DialogBox d = Popup.getDialogBox();
        Panel p = getTagsInPanel(tags, d, cdm);
        if (p!=null) {
            Popup.showPopup(p,title,d);
        }
    }

    private static int scoreToFontSize(double score) {
        int min = 12;
        int max = 60;
        int range = max - min;
        return (int) Math.round(range * score + min);
    }

    public static Panel getTagsInPanel(ItemInfo[] tags, ClientDataManager cdm) {
        return getTagsInPanel(tags, null, cdm);
    }

    /**
     * Return a panel containing the tags cloud passed in parameter. If panel
     * will be used in pop-up, pass the DialogBox that will contain it in d to add
     * so the pop-up can be closed when a tag is clicked on
     * @param tags
     * @param d
     * @return
     */
    public static Panel getTagsInPanel(ItemInfo[] tags, DialogBox d, ClientDataManager cdm) {
        Panel p = new FlowPanel();
        if (d!=null) {
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

            tags = ItemInfo.shuffle(tags);

            for (int i = 0; i < tags.length; i++) {
                int colorId = i % 2;
                int fontSize = scoreToFontSize(( Math.abs(tags[i].getScore()) - min) / range);

                ContextMenuTagLabel sL = new ContextMenuTagLabel(tags[i], cdm);
                sL.getElement().setAttribute("style", "font-size:" + fontSize + "px; color:" + getColor(colorId, tags[i].getScore()) +";");
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
                    sL.addClickListener(new DataEmbededClickListener<DialogBox>(d) {

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

    private static String getColor(int index, double size) {
        if (index == 0) {
            if (size < 0) {
                return "#D49090";
            } else {
                return "#D4C790";
            }
        } else if (index == 1) {
            if (size < 0) {
                return "#AD7676";
            } else {
                return "#ADA376";
            }
        } else {
            Window.alert("Invalid color");
            return "#000000";
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
