/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public abstract class ArtistListWidget extends Composite implements HasListeners {

    private Grid g;
    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private ArtistCompact[] aDArray;
    private Map<String,Integer> ratingMap;

    private List<CompactArtistWidget> artistWidgetList;

    public ArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray) {

        this.musicServer = musicServer;
        this.cdm = cdm;

        artistWidgetList = new LinkedList<CompactArtistWidget>();

        g = new Grid(1,1);
        this.aDArray=aDArray;
        initWidget(g);
        setWidth("300px");
        invokeFetchRatings();
    }

    public abstract void openWhyPopup(WhyButton why);
    
    public void updateWidget(ArtistCompact[] aDArray) {
        if (this.aDArray!=aDArray) {
            this.aDArray = aDArray;
        }
        invokeFetchRatings();
    }

    public void doRemoveListeners() {
        for (CompactArtistWidget caw : artistWidgetList) {
            caw.doRemoveListeners();
        }
    }

    private Panel getUpdatedPanel() {

        doRemoveListeners();

        VerticalPanel vP = new VerticalPanel();

        if (aDArray != null && aDArray.length > 0) {
            for (ArtistCompact aD : aDArray) {
                Image img = new Image("not-interested-vert.jpg");
                img.getElement().setAttribute("style", "vertical-align:top; display:none;");

                int rating;
                if (ratingMap.containsKey(aD.getId())) {
                    rating = ratingMap.get(aD.getId());
                } else {
                    rating = 0;
                }

                CompactArtistWidget caw = new CompactArtistWidget(aD, cdm,
                        musicServer, new WhyButton(aD.getId(), aD.getName()), rating, null);

                vP.add(new DeletableWidget<CompactArtistWidget>(caw, new HorizontalPanel()) {

                    public void onDelete() {
                        this.getWidget().doRemoveListeners();
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
                Window.alert(caught.toString());
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
                musicServer.fetchUserSongRating(artistIDs, callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        } else {
            ratingMap = new HashMap<String, Integer>();
            g.setWidget(0, 0, getUpdatedPanel());
        }
    }

    public class WhyButton extends Composite {

        private Grid g;
        private SpannedLabel why;
        private Image load;

        private String artistName;
        private String id;

        public WhyButton(String id, String artistName) {

            this.id = id;
            this.artistName = artistName;

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

        public String getName() {
            return artistName;
        }

        public void showWhy() {
            g.setWidget(0, 0, why);
        }

        public void showLoad() {
            g.setWidget(0, 0, load);
        }
    }
}
