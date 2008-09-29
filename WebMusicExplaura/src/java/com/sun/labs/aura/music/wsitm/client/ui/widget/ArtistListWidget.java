/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.*;
import com.extjs.gxt.ui.client.Style.Direction;
import com.google.gwt.user.client.History;
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
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.PerformanceTimer;
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
            ClientDataManager cdm, ArtistCompact[] aDArray, boolean fetchRatings) {

        this.musicServer = musicServer;
        this.cdm = cdm;

        artistWidgetList = new LinkedList<CompactArtistWidget>();

        g = new Grid(1,1);
        this.aDArray=aDArray;
        initWidget(g);
        setWidth("300px");

        if (fetchRatings) {
            invokeFetchRatings();
        } else {
            ratingMap = new HashMap<String, Integer>();
            g.setWidget(0, 0, getUpdatedPanel());
        }
    }

    public abstract void openWhyPopup(SwapableTxtButton why);
    public abstract void openDiffPopup(DiffButton diff);

    /**
     * Called on tag click in the CompactArtistWidget. Overwrite to change default
     * behavior
     * @param tag
     */
    public void onTagClick(ItemInfo tag) {
        History.newItem("tag:"+tag.getId());
    }

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
        artistWidgetList.clear();
    }

    private Panel getUpdatedPanel() {

        doRemoveListeners();

        VerticalPanel vP = new VerticalPanel();

        PerformanceTimer.start("  alw - GUP");
        if (aDArray != null && aDArray.length > 0) {
            for (ArtistCompact aC : aDArray) {

                // Add artist to oracle
                cdm.getArtistOracle().add(aC.getName());

                Image img = new Image("not-interested-vert.jpg");
                img.getElement().setAttribute("style", "vertical-align:top; display:none;");

                int rating;
                if (ratingMap.containsKey(aC.getId())) {
                    rating = ratingMap.get(aC.getId());
                } else {
                    rating = 0;
                }

                PerformanceTimer.start("  alw - single artist widget");
                CompactArtistWidget caw = new OverWroteOnClickCompactArtistWidget(aC, cdm,
                        musicServer, new WhyButton(aC.getId(), aC.getName()),
                        new DiffButton(aC), rating, null, this);
                PerformanceTimer.stop("  alw - single artist widget");

                artistWidgetList.add(caw);

                DeletableWidget dW = new DeletableWidget<CompactArtistWidget>(caw, new HorizontalPanel()) {

                    public void onDelete() {
                        invokeAddNotInterested(getWidget().getArtistId());
                        this.getWidget().doRemoveListeners();
                        this.slideOut(Direction.UP,
                                new DualDataEmbededCommand<VerticalPanel, DeletableWidget>(((VerticalPanel) g.getWidget(0, 0)), this) {

                            public void execute() {
                                data.remove(sndData);
                            }
                        });
                    }
                };
                if (cdm.isLoggedIn()) {
                    dW.addRemoveButton();
                }
                vP.add(dW);
            }
        } else {
            vP.add(new Label("No artists found."));
        }

        PerformanceTimer.stop("  alw - GUP");
        return vP;
    }

    private void invokeAddNotInterested(String artistId) {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {}
            public void onFailure(Throwable caught) {
                Window.alert("Error adding not interested attention."+caught.toString());
            }
        };

        if (cdm.isLoggedIn()) {
            try {
                musicServer.addNotInterestedAttention(artistId, callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        }
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

        if (cdm.getListenerDetails().isLoggedIn()) {

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

    public class WhyButton extends SwapableTxtButton {

        public WhyButton(String id, String artistName) {
            super("why?", id, artistName);
        }
        
        @Override
        protected void addClickListener() {
            
            button.addClickListener(new DataEmbededClickListener<SwapableTxtButton>(this) {

                public void onClick(Widget arg0) {
                    openWhyPopup(data);
                }
            });
        }
    }

    public class DiffButton extends SwapableTxtButton {

        private ArtistCompact aC;

        public DiffButton(ArtistCompact aC) {
            super("diff", aC.getId(), aC.getName());
            this.aC = aC;
        }
        
        @Override
        protected void addClickListener() {
            
            button.addClickListener(new DataEmbededClickListener<DiffButton>(this) {

                public void onClick(Widget arg0) {
                    openDiffPopup(data);
                }
            });
        }

        public ItemInfo[] getDistinctiveTags() {
            return aC.getDistinctiveTags();
        }
        
        public void displayIdenticalArtistMsg() {
            Window.alert("Cannot display difference tag cloud between the same artist.");
        }

    }

    public abstract class SwapableTxtButton extends SwapableWidget<SpannedLabel, Image> {

        protected SpannedLabel button;

        private String artistName;
        private String id;

        public SwapableTxtButton(String linkName, String id, String artistName) {

            super(new SpannedLabel(linkName), new Image("ajax-loader-small.gif"));
            
            this.id = id;
            this.artistName = artistName;

            setWidth("30px");

            button = getWidget1();
            button.getElement().setAttribute("style", "font-size: 11px");
            button.addStyleName("pointer");

            addClickListener();
        }
        
        protected abstract void addClickListener();

        public String getId() {
            return id;
        }

        public String getName() {
            return artistName;
        }

        public void showButton() {
            showWidget(LoadableWidget.W1);
        }

        public void showLoad() {
            showWidget(LoadableWidget.W2);
        }
    }

    public class OverWroteOnClickCompactArtistWidget extends CompactArtistWidget {

        private ArtistListWidget aLW;

        public OverWroteOnClickCompactArtistWidget(ArtistCompact aD, ClientDataManager cdm,
                MusicSearchInterfaceAsync musicServer, SwapableTxtButton whyB,
                SwapableTxtButton diffB, int currentRating, Set<String> userTags,
                ArtistListWidget aLW) {
            super(aD, cdm, musicServer, whyB, diffB, currentRating, userTags);
            this.aLW = aLW;
        }

        @Override
        public void onTagClick(ItemInfo tag) {
            aLW.onTagClick(tag);
        }
    }
}