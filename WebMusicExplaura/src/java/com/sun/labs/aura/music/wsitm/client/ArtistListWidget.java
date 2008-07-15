/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public abstract class ArtistListWidget extends Composite {

    private Grid g;
    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private ArtistCompact[] aDArray;
    private Map<String,Integer> ratingMap;

    public ArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray) {

        this.musicServer = musicServer;
        this.cdm = cdm;

        g = new Grid(1,1);
        this.aDArray=aDArray;
        initWidget(g);
        setWidth("300px");
        invokeFetchRatings();
    }

    public abstract void openWhyPopup(WhyButton why);
    
    public void onTagClick(ItemInfo tag) {
        History.newItem("tag:"+tag.getId());
    }

    public void updateWidget(ArtistCompact[] aDArray) {
        if (this.aDArray!=aDArray) {
            this.aDArray = aDArray;
        }
        invokeFetchRatings();
    }

    private Panel getUpdatedPanel() {

        VerticalPanel vP = new VerticalPanel();

        if (aDArray != null && aDArray.length > 0) {
            for (ArtistCompact aD : aDArray) {
                Image img = new Image("not-interested-vert.jpg");
                img.getElement().setAttribute("style", "vertical-align:top; display:none;");
                vP.add(new DeletableWidget(new ArtistPanel(aD), new HorizontalPanel()) {
                    public void onDelete() {
                        ((VerticalPanel) g.getWidget(0, 0)).remove(this);
                    }
                });
            }
        } else {
            vP.add(new Label("No artists found."));
        }

        return vP;
    }

    private void invokeFetchRatings() {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                Map<String,Integer> map = (Map<String,Integer>)result;
                if (map!=null) {
                   ratingMap = map;
                } else {
                   ratingMap = new HashMap<String, Integer>();
                }
                g.setWidget(0, 0, getUpdatedPanel());
            }

            public void onFailure(Throwable caught) {
                Window.alert("Error fetching ratings.");
            }
        };

        if (cdm.getListenerDetails().loggedIn) {

            g.setWidget(0, 0, new Image("ajax-loader.gif"));

            Set<String> artistIDs = new HashSet<String>();
            for (ArtistCompact aC : aDArray) {
                artistIDs.add(aC.getId());
            }

            try {
                musicServer.fetchUserSongRating(cdm.getListenerDetails(), artistIDs, callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        } else {
            ratingMap = new HashMap<String, Integer>();
            g.setWidget(0, 0, getUpdatedPanel());
        }
    }

    /**
     * Stores the n first distinctive tags for an artist in a comma seperated string
     * @param aD artist's details
     * @param n number of tags
     * @return comma seperated string
     */
    private Panel getNDistinctiveTags(ArtistCompact aD, int n) {

        List<ItemInfo> tagList = new ArrayList<ItemInfo>();
        for (ItemInfo i : aD.getDistinctiveTags()) {
            tagList.add(i);
        }
        Collections.sort(tagList, ItemInfo.getScoreSorter());

        FlowPanel tagPanel = new FlowPanel();
        for (int i = 0; i < tagList.size(); i++) {
            SpannedLabel t = new SpannedLabel(tagList.get(i).getItemName());
            t.addStyleName("pointer");
            t.addClickListener(new DataEmbededClickListener<ItemInfo>(tagList.get(i)) {

                public void onClick(Widget arg0) {
                    onTagClick(data);
                }
            });
            tagPanel.add(t);
            if (i == n) {
                break;
            } else {
                tagPanel.add(new SpannedLabel(", "));
            }
        }
        return tagPanel;
    }

    public class ArtistPanel extends Composite {

        public ArtistPanel(ArtistCompact aD) {

            HorizontalPanel artistPanel = new HorizontalPanel();
            artistPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            artistPanel.setStyleName("artistPanel");
            artistPanel.setSpacing(5);

            ClickListener cL = new DataEmbededClickListener<String>("artist:" + aD.getId()) {

                public void onClick(Widget arg0) {
                    History.newItem(data);
                }
            };

            Image img = aD.getBestArtistImage(true);
            if (img==null) {
                img = new Image("nopic.gif");
            }
            img.setStyleName("image");
            img.addClickListener(cL);
            artistPanel.add(img);

            VerticalPanel txtPanel = new VerticalPanel();


            HorizontalPanel aNamePanel = new HorizontalPanel();
            aNamePanel.setWidth("210px");
            aNamePanel.setSpacing(5);
            Label aName = new Label(aD.getName());
            aName.addClickListener(cL);
            aName.addStyleName("image");
            aNamePanel.add(aName);

            HorizontalPanel buttonPanel = new HorizontalPanel();
            buttonPanel.setSpacing(5);
            Widget spotify = WebLib.getSpotifyListenWidget(aD, 20);
            spotify.getElement().setAttribute("style", "align : right;");
            buttonPanel.add(spotify);

            SteeringWheelWidget steerButton = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.SMALL,
                    new DataEmbededClickListener<String>(aD.getId()) {

                public void onClick(Widget arg0) {
                    cdm.setSteerableReset(true);
                    History.newItem("steering:"+data);
                }
            });
            buttonPanel.add(steerButton);

            WhyButton why = new WhyButton(aD.getId());
            buttonPanel.add(why);

            aNamePanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            aNamePanel.add(buttonPanel);

            txtPanel.add(aNamePanel);

            Panel tagsLabel = getNDistinctiveTags(aD, 4);
            tagsLabel.setStyleName("recoTags");
            txtPanel.add(tagsLabel);

            int rating;
            if (ratingMap.containsKey(aD.getId())) {
                rating = ratingMap.get(aD.getId());
            } else {
                rating = 0;
            }

            StarRatingWidget star = new StarRatingWidget(musicServer, cdm.getListenerDetails(), aD.getId(),
                    rating, StarRatingWidget.Size.SMALL);
            Label starLbl = new Label("Your rating: ");
            starLbl.setStyleName("recoTags");
            starLbl.addStyleName("marginRight");
            HorizontalPanel starHP = new HorizontalPanel();
            starHP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            starHP.add(starLbl);
            starHP.add(star);
            txtPanel.add(starHP);

            txtPanel.add(WebLib.getSmallPopularityWidget(aD.getNormPopularity(), true, true));

            artistPanel.add(txtPanel);

            initWidget(artistPanel);
        } 
    }

    public class WhyButton extends Composite {

        private Grid g;
        private SpannedLabel why;
        private Image load;

        private String id;

        public WhyButton(String id) {

            this.id = id;

            g = new Grid(1,1);
            g.setWidth("30px");
            g.getCellFormatter().getElement(0, 0).setAttribute("style", "align: center;");

            why = new SpannedLabel("why?");
            why.getElement().setAttribute("style", "font-size: 12px");
            why.addStyleName("pointer");

            g.setWidget(0, 0, why);
            initWidget(g);

            why.addClickListener(new DataEmbededClickListener<WhyButton>(this) {

                public void onClick(Widget arg0) {
                    openWhyPopup(data);
                }
            });

            load = new Image("ajax-loader-small.gif");
        }

        public String getId() {
            return id;
        }

        public void showWhy() {
            g.setWidget(0, 0, why);
        }

        public void showLoad() {
            g.setWidget(0, 0, load);
        }

    }
}
