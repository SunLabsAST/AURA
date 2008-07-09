/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import asquare.gwt.tk.client.ui.SimpleHyperLink;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public abstract class TagDisplayLib {

    public static void showTagCloud(String title, ItemInfo[] tags) {
        final DialogBox d = Popup.getDialogBox();
        Panel p = getTagsInPanel(tags, d);
        if (p!=null) {
            Popup.showPopup(p,"WebMusicExplaura :: "+title,d);
        }
    }

    private static int scoreToFontSize(double score) {
        int min = 12;
        int max = 60;
        int range = max - min;
        return (int) Math.round(range * score + min);
    }

    public static Panel getTagsInPanel(ItemInfo[] tags) {
        return getTagsInPanel(tags,null);
    }

    /**
     * Return a panel containing the tags cloud passed in parameter. If panel
     * will be used in pop-up, pass the DialogBox that will contain it in d to add
     * so the pop-up can be closed when a tag is clicked on
     * @param tags
     * @param d
     * @return
     */
    public static Panel getTagsInPanel(ItemInfo[] tags, DialogBox d) {
        Panel p = new FlowPanel();
        if (d!=null) {
            p.setWidth("600px");
        }
        HorizontalPanel innerP = new HorizontalPanel();
        innerP.setSpacing(4);

        if (tags != null && tags.length > 0) {

            double max = 0;
            double min = 1;
            for (ItemInfo tag : tags) {
                if (tag.getScore()>max) {
                    max = tag.getScore();
                } else if (tag.getScore()<min) {
                    min = tag.getScore();
                }
            }
            double range = max - min;

            tags = shuffle(tags);

            for (int i = 0; i < tags.length; i++) {
                int color = (i % 2) + 1;
                int fontSize = scoreToFontSize((tags[i].getScore() - min) / range);

                String s = "<span style='font-size:" + fontSize + "px;'>" + tags[i].getItemName() + " </span>   ";
                SimpleHyperLink sH = new SimpleHyperLink();
                sH.setHTML(s);
                sH.setStyleName("tag"+color);
                sH.addClickListener(new DataEmbededClickListener<ItemInfo>(tags[i]) {

                    public void onClick(Widget arg0) {
                        //invokeGetTagInfo(data.getId(), false)
                        History.newItem("artist-tag:"+ClientDataManager.nameToKey(data.getId()));
                    }
                });
                if (d!=null) {
                    sH.addClickListener(new DataEmbededClickListener<DialogBox>(d) {

                        public void onClick(Widget arg0) {
                            data.hide();
                        }
                    });
                }

                p.add(sH);
            }

            return p;
        } else {
            return null;
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

    public static void invokeGetCommonTags(Map<String, Double> tagMap, String artistID,
            MusicSearchInterfaceAsync musicServer, ClientDataManager cdm, CommonTagsAsyncCallback callback) {

        try {
            musicServer.getCommonTags(tagMap, artistID, 30, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }

    }

    private static ItemInfo[] shuffle(ItemInfo[] itemInfo) {

        ItemInfo[] ii = new ItemInfo[itemInfo.length];

        for (int i = 0; i < itemInfo.length; i++) {
            ii[i] = itemInfo[i];
        }

        Arrays.sort(ii, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (Random.nextBoolean()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return ii;
    }

    public abstract class AsyncCallbackWithCommand implements AsyncCallback {

        protected Command cmd;

        public AsyncCallbackWithCommand(Command cmd) {
            this.cmd = cmd;
        }

    }

}
