/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.*;
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
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.ui.PerformanceTimer;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.widget.StarRatingWidget.InitialRating;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public abstract class ArtistListWidget extends Composite implements HasListeners {

    private Grid g;
    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private HashMap<String, CompactArtistWidget> artistWidgetMap;

    private ArtistCompact[] aDArray;
    private Double[] similarity;
    
    private boolean displayDiff;

    public ArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray,
            boolean fetchRatings) {

        this(musicServer, cdm, aDArray, fetchRatings, true);
    }

    public ArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArrayList<ScoredC<ArtistCompact>> aC,
            boolean fetchRatings, boolean displayDiff) {
    
        processArtistSimValues(aC);
        doInit(musicServer, cdm, fetchRatings, displayDiff);
    }
    
    public ArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray, 
            boolean fetchRatings, boolean displayDiff) {
        

        // Force similarity to 1 for each item since none is provided
        similarity = new Double[aDArray.length];
        for (int i=0; i<similarity.length; i++) {
            similarity[i]=1.0;
        }

        this.aDArray = aDArray;
        doInit(musicServer, cdm, fetchRatings, displayDiff);
    }
    
    private void doInit(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, boolean fetchRatings, boolean displayDiff) {
        this.musicServer = musicServer;
        this.cdm = cdm;
        
        this.displayDiff = displayDiff;
        
        artistWidgetMap = new HashMap<String, CompactArtistWidget>();

        g = new Grid(1,1);
        initWidget(g);
        setWidth("300px");

        if (fetchRatings) {
            g.setWidget(0, 0, getUpdatedPanel(null));
            invokeFetchRatings();
        } else {
            g.setWidget(0, 0, getUpdatedPanel(new HashMap<String, Integer>()));
        }
    }
    
    private void processArtistSimValues(ArrayList<ScoredC<ArtistCompact>> aCList) {
        similarity = new Double[aCList.size()];
        aDArray = new ArtistCompact[aCList.size()];
        int i = 0;
        for (ScoredC<ArtistCompact> saC : aCList) {
            aDArray[i] = saC.getItem();
            if (saC.getScore() > 1) {
                similarity[i] = 1.0;
            } else {
                similarity[i] = saC.getScore();
            }
            i++;
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

    @Override
    public void doRemoveListeners() {
        for (CompactArtistWidget caw : artistWidgetMap.values()) {
            caw.doRemoveListeners();
        }
        artistWidgetMap.clear();
    }

    private Panel getUpdatedPanel(HashMap<String, Integer> ratingMap) {

        doRemoveListeners();

        VerticalPanel vP = new VerticalPanel();

        PerformanceTimer.start("  alw - GUP");
        if (aDArray != null && aDArray.length > 0) {
            int aCIndex = 0;
            for (ArtistCompact aC : aDArray) {

                // Add artist to oracle
                cdm.getArtistOracle().add(aC.getName(), aC.getPopularity());

                //Image img = new Image("not-interested-vert.jpg");
                //img.getElement().getStyle().setProperty("vertical-align", "top");
                //img.getElement().getStyle().setProperty("display", "none");

                InitialRating rating;
                if (ratingMap == null) {
                    rating = InitialRating.DISPLAY_LOAD;
                } else {
                    if (ratingMap.containsKey(aC.getId())) {
                        rating = StarRatingWidget.intToRatingEnum(ratingMap.get(aC.getId()));
                    } else {
                        rating = InitialRating.R0;
                    }
                }

                PerformanceTimer.start("  alw - single artist widget");
                
                DiffButton dB = null;
                if (this.displayDiff) {
                    dB = new DiffButton(aC);
                }

                // MUST DETERMINE BACKGROUND COLOR AND PASS IT
                String backColor = null;
                if (similarity != null && similarity.length >= aCIndex) {
                    backColor = simToColor(similarity[aCIndex++]);
                }
                
                CompactArtistWidget caw = new OverWroteOnClickCompactArtistWidget(aC, cdm,
                        musicServer, new WhyButton(aC.getId(), aC.getName()),
                        dB, rating, null, this, backColor);
                PerformanceTimer.stop("  alw - single artist widget");

                artistWidgetMap.put(caw.getArtistId(), caw);

                DeletableWidget dW = new DeletableWidget<CompactArtistWidget>(caw, new HorizontalPanel()) {

                    @Override
                    public void onDelete() {
                        invokeAddNotInterested(getWidget().getArtistId());
                        artistWidgetMap.remove(this.w.getArtistId());
                        this.getWidget().doRemoveListeners();
                        /*
                        this.slideOut(AnimatedComposite.SlideDirection.UP,
                                new DualDataEmbededCommand<VerticalPanel, DeletableWidget>(((VerticalPanel) g.getWidget(0, 0)), this) {

                            public void execute() {
                                data.remove(sndData);
                            }
                        });
                        */
                        ((VerticalPanel) g.getWidget(0, 0)).remove(this);
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

    public static String simToColor(double sim) {
       
        String col = "";
        String tcol = "";
        
        Integer[] highColor = new Integer[3];
        Integer[] lowColor = new Integer[3];
        /* vert clair
         highColor[0] = 111;
        highColor[1] = 221;
        highColor[2] = 129;
         * */
        /** rouge
        highColor[0] = 255;
        highColor[1] = 123;
        highColor[2] = 109;
         * */

        /** For Beige CSS
        highColor[0] = 185;
        highColor[1] = 255;
        highColor[2] = 109;
        lowColor[0] = 240;
        lowColor[1] = 248;
        lowColor[2] = 198;
        */

        highColor[0] = 203;
        highColor[1] = 217;
        highColor[2] = 226;
        lowColor[0] = 244;
        lowColor[1] = 248;
        lowColor[2] = 251;

        for (int i=0; i<3; i++) {
            // linear mapping
            //tcol = Integer.toHexString((int)( (highColor[i]-lowColor[i])*sim + lowColor[i] ));
            // exp mapping
            tcol = Integer.toHexString((int)( (highColor[i]-lowColor[i])*Math.pow((sim+.5)/1.5,3) + lowColor[i] ));
            if (tcol.length() == 1) {
                tcol = "0" + tcol;
            } else if (tcol.length() == 0) {
                tcol = "00";
            }
            col += tcol;
        }
        
        return "#"+col;
    }

    private void invokeAddNotInterested(String artistId) {

        AsyncCallback callback = new AsyncCallback() {

            @Override
            public void onSuccess(Object result) {}

            @Override
            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "add 'not interested' attention.", Popup.ERROR_LVL.NORMAL, null);
            }
        };

        if (cdm.isLoggedIn()) {
            try {
                musicServer.addNotInterestedAttention(artistId, callback);
            } catch (WebException ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "add 'not interested' attention.", Popup.ERROR_LVL.NORMAL, null);
            }
        }
    }

    private void invokeFetchRatings() {

        if (cdm.getListenerDetails().isLoggedIn()) {

            HashSet<String> artistIDs = new HashSet<String>();
            for (ArtistCompact aC : aDArray) {
                int rating = cdm.getRatingFromCache(aC.getId());
                // If we have artist rating in cache
                if (rating > -1) {
                    artistWidgetMap.get(aC.getId()).setNbrStarsSelected(rating);
                // If not, we need to fetch it
                } else {
                    artistIDs.add(aC.getId());
                }
            }

            DEAsyncCallback<HashSet<String>, HashMap<String, Integer>> callback =
                    new DEAsyncCallback<HashSet<String>, HashMap<String, Integer>>(artistIDs) {

                @Override
                public void onSuccess(HashMap<String, Integer> map) {

                    cdm.setRatingInCache(map);

                    // Go through list of ids we asked to get a rating for. If
                    // nothing got returned, we don't have a rating for the artist
                    for (String id : data) {
                        if (map.containsKey(id)) {
                            artistWidgetMap.get(id).setNbrStarsSelected(map.get(id));
                        // If it wasn't returned, we don't have a rating for it so
                        // set it to zero in the cache
                        } else {
                            artistWidgetMap.get(id).setNbrStarsSelected(0);
                            cdm.setRatingInCache(id, 0);
                        }
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve artist ratings.", Popup.ERROR_LVL.NORMAL, new Command() {
                            @Override
                            public void execute() {
                                invokeFetchRatings();
                            }
                    });
                }
            };

            try {
                if (!artistIDs.isEmpty()) {
                    musicServer.fetchUserSongRating(artistIDs, callback);
                }
            } catch (WebException ex) {
                    Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve artist ratings.", Popup.ERROR_LVL.NORMAL, new Command() {
                            @Override
                            public void execute() {
                                invokeFetchRatings();
                            }
                    });
            }

        } else {
            g.setWidget(0, 0, getUpdatedPanel(new HashMap<String, Integer>()));
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
            Popup.showInformationPopup("Cannot display difference tag cloud " +
                    "between the same artist.");
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
            button.getElement().getStyle().setPropertyPx("font-size", 11);
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
                SwapableTxtButton diffB, InitialRating iR, Set<String> userTags,
                ArtistListWidget aLW, String backColor) {
            super(aD, cdm, musicServer, whyB, diffB, iR, userTags, backColor);
            this.aLW = aLW;
        }

        @Override
        public void onTagClick(ItemInfo tag) {
            aLW.onTagClick(tag);
        }
    }
}
