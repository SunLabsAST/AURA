/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
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

    public MenuItem getMenuTitle() {
        return new MenuItem("Dashboard",MenuItem.getDefaultTokenClickListener("dashboard:"),true,3);
    }

    private class MainPanel extends LoginListener {

        private Grid g;
        private static final int IMG_SIZE = 150;

        private Grid featArtist;

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

//            ArtistCloudArtistListWidget alp = new ArtistCloudArtistListWidget(musicServer, cdm, cdm.getListenerDetails().recommendations), cdm.get;
  //          dP.add(WebLib.createSection("Artist recommendations", alp), DockPanel.WEST);

            Label titleLbl = new Label("Dashhhhboard");
            titleLbl.setStyleName("h1");
            dP.add(titleLbl, DockPanel.NORTH);

            //
            // Featured artist
            
            featArtist = new Grid(2,1);
            featArtist.setWidget(0, 0, new HTML("<h2>Featured Artist</h2>"));
            featArtist.setWidget(1, 0, new Image("ajax-bar.gif"));
            invokeFetchFeaturedArtist();

            dP.add(featArtist, DockPanel.NORTH);

            return dP;
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
            return tags.substring(0, tags.length()-2);
        }

        private void setFeaturedArtist(ArtistDetails aD) {
            if (aD == null) {
                featArtist.setWidget(1, 0, new Label("Unable to load artist."));
            } else {

                Grid featArtTitle = new Grid(1,3);
                featArtTitle.setStyleName("h2");
                featArtTitle.setWidth("100%");
                featArtTitle.setWidget(0, 0, new Label("Featured artist :: "+aD.getName()));
                //featArtTitle.setWidget(0, 1, new StarRatingWidget(0,StarRatingWidget.Size.MEDIUM));
                featArtTitle.setWidget(0, 2, WebLib.getSpotifyListenWidget(aD, 30));

                featArtist.setWidget(0, 0, featArtTitle);

                HorizontalPanel featHp = new HorizontalPanel();
                featHp.setSpacing(5);
                featHp.add(aD.getBestArtistImage(false));

                VerticalPanel featVp = new VerticalPanel();
                featVp.setSpacing(4);
                featVp.add(new HTML(aD.getBiographySummary().substring(0, 300) + " [...]"));
                featVp.add(new HTML("<b>Tags</b> : "+getNDistinctiveTags(aD, 10)));

                featHp.add(featVp);


                featArtist.setWidget(1, 0,  featHp);

            }
        }

        private void invokeFetchFeaturedArtist() {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    setFeaturedArtist((ArtistDetails) result);
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            try {
                musicServer.getArtistDetails("24762087-34ce-4f65-b743-7d8402cf30dd", false, cdm.getCurrSimTypeName(), callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
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

    public class ArtistCloudArtistListWidget extends ArtistListWidget {

        private String currArtistId;

        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray, String currArtistId) {

            super(musicServer, cdm, aDArray);
            this.currArtistId = currArtistId;
        }

        public void openWhyPopup(String artistID) {
            TagDisplayLib.invokeGetCommonTags(currArtistId, artistID,
                    musicServer, cdm, new CommonTagsAsyncCallback() {});
        }
    }
}
