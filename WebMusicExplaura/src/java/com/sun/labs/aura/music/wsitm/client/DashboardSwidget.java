/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mailletf
 */
public class DashboardSwidget extends Swidget {

    private MainPanel mP;

    public DashboardSwidget(ClientDataManager cdm) {
        super("Dashboard", cdm);
        mP = new MainPanel();
        registerLoginListener(mP);
        initWidget(mP);
    }

    public List<String> getTokenHeaders() {
        
        List<String> l = new ArrayList<String>();
        l.add("dashboard:");
        return l;
    }



    private class MainPanel extends LoginListener {

        private Grid g;
        private static final int IMG_SIZE = 150;

        public MainPanel() {
            g = new Grid(1,1);
            initWidget(g);
            update();
        }

        public void onLogin(ListenerDetails lD) {
            update();
        }

        public void onLogout() {
            update();
        }

        public void update() {
            
            if (cdm.isLoggedIn()) {
                g.setWidget(0, 0, getDashboard());
            } else {
                g.setWidget(0, 0, getMustBeLoggedInWidget());
            }
        }

        private Widget getDashboard() {


            DockPanel dP = new DockPanel();

            VerticalPanel vP = new VerticalPanel();
            vP.setSpacing(4);
            vP.setWidth("300px");

            for (ArtistDetails aD : cdm.getListenerDetails().recommendations) {
                HorizontalPanel artistPanel = new HorizontalPanel();
                artistPanel.setSpacing(5);

                Image img = aD.getBestArtistImage();
                artistPanel.add(img);

                VerticalPanel txtPanel = new VerticalPanel();


                HorizontalPanel aNamePanel = new HorizontalPanel();
                aNamePanel.setSpacing(5);
                Label aName = new Label(aD.getName());
                aName.addClickListener(new TokenClickListener("artist:"+aD.getId()));
                aNamePanel.add(aName);

                aNamePanel.add(WebLib.getSpotifyListenWidget(aD, 20));
                txtPanel.add(aNamePanel);

                String tags = "";
                for (int i=0; i<aD.getDistinctiveTags().length; i++) {
                    tags += aD.getDistinctiveTags()[i].getItemName()+", ";
                    if (i==4) {
                        break;
                    }
                }
                Label tagsLabel = new Label(tags.substring(0, tags.length()-2));
                tagsLabel.setStyleName("recoTags");
                txtPanel.add(tagsLabel);
                artistPanel.add(txtPanel);

                vP.add(artistPanel);
            }
            dP.add(WebLib.createSection("Artist recommendations", vP), DockPanel.WEST);

            Label titleLbl = new Label("Dashhhhboard");
            titleLbl.setStyleName("title");
            dP.add(titleLbl, DockPanel.NORTH);


            return dP;
        }

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
