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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class ArtistListWidget extends Composite {

    private Grid g;
    private MusicSearchInterfaceAsync musicServer;
    private ListenerDetails lD;

    private ArtistCompact[] aDArray;
    private Map<String,Integer> ratingMap;

    public ArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ListenerDetails lD, ArtistCompact[] aDArray) {

        this.musicServer = musicServer;
        this.lD = lD;

        g = new Grid(1,1);
        this.aDArray=aDArray;
        initWidget(g);

        invokeFetchRatings();
    }

    public void updateWidget(ArtistCompact[] aDArray) {
        if (this.aDArray!=aDArray) {
            this.aDArray = aDArray;
            //invokeFetchRatings();
        }
            //else {
//            g.setWidget(0, 0, getUpdatedPanel());
//        }
        invokeFetchRatings();
    }

    private Panel getUpdatedPanel() {

        VerticalPanel vP = new VerticalPanel();

        for (ArtistCompact aD : aDArray) {
            HorizontalPanel artistPanel = new HorizontalPanel();
            artistPanel.setStyleName("artistPanel");
            artistPanel.setSpacing(5);

            Image img = aD.getBestArtistImage(true);
            img.addClickListener(new TokenClickListener("artist:" + aD.getId()));
            img.setStyleName("image");
            if (img==null) {
                artistPanel.add(new Image("nopic.gif"));
            } else {
                artistPanel.add(img);
            }

            VerticalPanel txtPanel = new VerticalPanel();


            HorizontalPanel aNamePanel = new HorizontalPanel();
            aNamePanel.setSpacing(5);
            Label aName = new Label(aD.getName());
            aName.addClickListener(new TokenClickListener("artist:" + aD.getId()));
            aName.addStyleName("image");
            aNamePanel.add(aName);
            aNamePanel.add(WebLib.getSpotifyListenWidget(aD, 20));

            txtPanel.add(aNamePanel);

            Label tagsLabel = new Label(getNDistinctiveTags(aD, 4));
            tagsLabel.setStyleName("recoTags");
            txtPanel.add(tagsLabel);

            int rating;
            if (ratingMap.containsKey(aD.getId())) {
                rating = ratingMap.get(aD.getId());
            } else {
                rating = 0;
            }

            StarRatingWidget star = new StarRatingWidget(musicServer, lD, aD.getId(),
                    rating, StarRatingWidget.Size.SMALL);
            txtPanel.add(star);

            artistPanel.add(txtPanel);

            vP.add(artistPanel);
        }

        return vP;
    }

    private void invokeFetchRatings() {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    Map<String,Integer> map = (Map<String,Integer>)result;
                    if (map!=null) {
                       ratingMap = map;
                       g.setWidget(0, 0, getUpdatedPanel());
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert("Error fetching ratings.");
                }
            };

            Set<String> artistIDs = new HashSet<String>();
            for (ArtistCompact aC : aDArray) {
                artistIDs.add(aC.getId());
            }

            try {
                musicServer.fetchUserSongRating(lD, artistIDs, callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
    }

    /**
     * Stores the n first distinctive tags for an artist in a comma seperated string
     * @param aD artist's details
     * @param n number of tags
     * @return comma seperated string
     */
    private String getNDistinctiveTags(ArtistCompact aD, int n) {
        String tags = "";
        for (int i = 0; i < aD.getDistinctiveTags().length; i++) {
            tags += aD.getDistinctiveTags()[i].getItemName() + ", ";
            if (i == n) {
                break;
            }
        }
        return tags.substring(0, tags.length() - 2);
    }

    public class TokenClickListener implements ClickListener {

        String token="";

        public TokenClickListener(String token) {
            this.token = token;
        }

        public void onClick(Widget arg0) {
            History.newItem(token);
        }

    }
}
