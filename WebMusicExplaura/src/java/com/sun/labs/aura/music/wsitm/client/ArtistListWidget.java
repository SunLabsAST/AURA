/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
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

/**
 *
 * @author mailletf
 */
public class ArtistListWidget extends Composite {

    private Grid g;

    public ArtistListWidget(ArtistCompact[] aDArray) {
        g = new Grid(1,1);
        g.setWidget(0, 0, getUpdatedPanel(aDArray));
        initWidget(g);
    }

    public void updateWidget(ArtistCompact[] aDArray) {
        g.setWidget(0, 0, getUpdatedPanel(aDArray));
    }

    private Panel getUpdatedPanel(ArtistCompact[] aDArray) {

        VerticalPanel vP = new VerticalPanel();

        for (ArtistCompact aD : aDArray) {
            HorizontalPanel artistPanel = new HorizontalPanel();
            artistPanel.setStyleName("artistPanel");
            artistPanel.setSpacing(5);

            Image img = aD.getBestArtistImage(true);
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
            aNamePanel.add(aName);
            aNamePanel.add(WebLib.getSpotifyListenWidget(aD, 20));

            txtPanel.add(aNamePanel);

            Label tagsLabel = new Label(getNDistinctiveTags(aD, 4));
            tagsLabel.setStyleName("recoTags");
            txtPanel.add(tagsLabel);

            StarRatingWidget star = new StarRatingWidget(0, StarRatingWidget.Size.SMALL);
            txtPanel.add(star);

            artistPanel.add(txtPanel);

            vP.add(artistPanel);
        }

        return vP;
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
