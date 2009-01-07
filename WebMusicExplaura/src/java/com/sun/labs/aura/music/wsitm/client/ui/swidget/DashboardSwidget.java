/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.TaggingListener;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.event.RatingListener;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.CompactArtistWidget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DDEClickHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.PlayedListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistRecommendation;
import com.sun.labs.aura.music.wsitm.client.items.AttentionItem;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuImage;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuSpannedLabel;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.ui.UpdatablePanel;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ContextMenuArtistLabel;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ContextMenuSteeringWheelWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PlayButton;
import com.sun.labs.aura.music.wsitm.client.ui.widget.StarRatingWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.SteeringWheelWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class DashboardSwidget extends Swidget implements LoginListener {

    private enum AttentionType {
        PLAYED,
        TAGGED,
        RATED
    }

    private Grid dashBoardWindow;
    private MainPanel mP;
    private RecentPlayedList rpl;
    private RecentRatedList rrl;
    private RecentTaggedList rtl;
    
    private Grid recPanel;
    private UpdatablePanel uP;

    private String currHistoryToken="";

    public DashboardSwidget(ClientDataManager cdm) {
        super("Dashboard", cdm);
        dashBoardWindow = new Grid(1,1);
        dashBoardWindow.getCellFormatter().setVerticalAlignment(0, 0, VerticalPanel.ALIGN_TOP);
        dashBoardWindow.setWidget(0, 0, new Label(""));
        cdm.getLoginListenerManager().addListener(this);
        initWidget(dashBoardWindow);
    }

    @Override
    public ArrayList<String> getTokenHeaders() {
        
        ArrayList<String> l = new ArrayList<String>();
        l.add("dashboard:");
        l.add("viewRecentTagged:");
        l.add("viewRecentRated:");
        l.add("viewRecentPlayed:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        menuItem = new MenuItem("Dashboard", MenuItem.getDefaultTokenClickHandler("dashboard:"), true, 3);
    }

    @Override
    public void update(String historyToken) {
        if (cdm.isLoggedIn()) {
            if (mP == null) {
                mP = new MainPanel();
            }
            dashBoardWindow.setWidget(0, 0, mP);
            if (historyToken.equals("dashboard:")) {
                mP.setCenterWidget();
            } else if (historyToken.equals("viewRecentPlayed:")) {
                if (rpl==null) {
                    rpl = new RecentPlayedList();
                }
                mP.setCenterWidget(rpl);
            } else if (historyToken.equals("viewRecentRated:")) {
                if (rrl==null) {
                    rrl = new RecentRatedList();
                }
                mP.setCenterWidget(rrl);
            } else if (historyToken.equals("viewRecentTagged:")) {
                if (rtl==null) {
                    rtl = new RecentTaggedList();
                }
                mP.setCenterWidget(rtl);
            }
        } else {
            dashBoardWindow.setWidget(0, 0, getMustBeLoggedInWidget());
        }
        currHistoryToken = historyToken;
    }

    public void onLogin(ListenerDetails lD) {
        update(currHistoryToken);
    }

    public void onLogout() {
        onDelete();
        update(currHistoryToken);
    }

    public void doRemoveListeners() {
        onDelete();
    }

    public void onDelete() {
        if (mP != null) {
            mP.doRemoveListeners();
            mP = null;
        }
        if (rpl != null) {
            rpl.doRemoveListeners();
            rpl = null;
        }
        if (rrl != null) {
            rrl.doRemoveListeners();
            rrl = null;
        }
        if (rtl != null) {
            rtl.doRemoveListeners();
            rtl = null;
        }
    }

    private class RecentPlayedList extends ListPanel implements PlayedListener {

        public RecentPlayedList() {
            super("Recently played artists");
            cdm.getPlayedListenerManager().addListener(this);
            super.invokeFetchRecentAttentions(AttentionType.PLAYED);
        }

        public void onPlay(String artistId) {
            invokeFetchArtistCompact(artistId);
        }

        public void onDelete() {
            cdm.getPlayedListenerManager().removeListener(this);
            deleteSuperElems();
        }

    }

    private class RecentRatedList extends ListPanel implements RatingListener {

        public RecentRatedList() {
            super("Recently rated artists");
            cdm.getRatingListenerManager().addListener(this);
            super.invokeFetchRecentAttentions(AttentionType.RATED);
        }

        public void onDelete() {
            cdm.getRatingListenerManager().removeListener(this);
            deleteSuperElems();
        }

        public void onRate(String itemId, int rating) {
            invokeFetchArtistCompact(itemId);
        }

    }

    private class RecentTaggedList extends ListPanel implements TaggingListener {

        public RecentTaggedList() {
            super("Recently tagged artists");
            cdm.getTaggingListenerManager().addListener(this);
            super.invokeFetchRecentAttentions(AttentionType.TAGGED);
        }

        @Override
        public void onDelete() {
            cdm.getTaggingListenerManager().removeListener(this);
            deleteSuperElems();
        }

        public void onTag(String itemId, HashSet<String> tags) {
            invokeFetchArtistCompact(itemId);
        }

    }

    private abstract class ListPanel extends Composite implements HasListeners {

        private VerticalPanel mainList;
        private ArrayList<PlayButton> playList;
        private ArrayList<StarRatingWidget> starList;

        public ListPanel(String title) {

            playList = new ArrayList<PlayButton>();
            starList = new ArrayList<StarRatingWidget>();

            mainList = new VerticalPanel();
            mainList.setWidth("100%");

            HorizontalPanel rateHp = new HorizontalPanel();
            rateHp.setWidth("100%");
            rateHp.setStyleName("h2");
            rateHp.add(new Label(title));
            rateHp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            Label featMore = new Label("Back to dashboard");
            featMore.addStyleName("headerMenuMedItem");
            featMore.addClickListener(new ClickListener() {

                public void onClick(Widget sender) {
                    History.newItem("dashboard:");
                }
            });
            rateHp.add(featMore);

            Grid g = new Grid(2,1);
            g.setWidget(0, 0, rateHp);
            g.getCellFormatter().setVerticalAlignment(1, 0, VerticalPanel.ALIGN_TOP);
            g.setWidget(1, 0, mainList);

            initWidget(g);
        }

        protected void deleteSuperElems() {
            for (PlayButton pB : playList) {
                pB.onDelete();
            }
            for (StarRatingWidget srw : starList) {
                srw.onDelete();
            }
            playList = null;
            starList = null;
        }

        protected void initElements(ArrayList<AttentionItem<ArtistCompact>> aIL) {
            for (AttentionItem<ArtistCompact> i : aIL) {
                addElement(i.getItem(), i.getDate(), false);
            }
        }

        protected void addElement(ArtistCompact aC, Date date, boolean onTopOfList) {
            PlayButton pB = new PlayButton(cdm, aC,
                    PlayButton.PLAY_ICON_SIZE.SMALL, musicServer);
            playList.add(pB);

            StarRatingWidget srw = new StarRatingWidget(musicServer, cdm,
                    aC.getId(), StarRatingWidget.InitialRating.FETCH, StarRatingWidget.Size.SMALL);
            starList.add(srw);

            Grid g = new Grid(1, 4);
            g.setWidth("600px");
            g.addStyleName("headerMenuMed");

            SteeringWheelWidget steerButton = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.SMALL,
                    new DDEClickHandler<ClientDataManager, ArtistCompact>(cdm, aC) {
                @Override
                public void onClick(ClickEvent ce) {
                    data.setSteerableReset(true);
                    History.newItem("steering:" + sndData.getId());
                }
            });
            steerButton.setTitle("Steerable recommendations starting with " + aC.getName() + "'s tag cloud");
            steerButton.addStyleName("largeMarginRight");

            HorizontalPanel buttonsPanel = new HorizontalPanel();
            buttonsPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            buttonsPanel.add(pB);
            buttonsPanel.add(new ContextMenuSteeringWheelWidget(cdm, steerButton, aC));

            g.setWidget(0, 0, buttonsPanel);
            g.setWidget(0, 1, new ContextMenuArtistLabel(aC, cdm));
            g.getColumnFormatter().setWidth(1, "200px");
            g.setWidget(0, 2, srw);
            g.getCellFormatter().setHorizontalAlignment(0, 3, HorizontalPanel.ALIGN_RIGHT);
            g.setWidget(0, 3, new Label(date.toString().substring(0, 16)));
            if (onTopOfList) {
                mainList.insert(g, 0);
            } else {
                mainList.add(g);
            }
        }

        protected void invokeFetchArtistCompact(String id) {

            AsyncCallback callback = new AsyncCallback() {

                public void onFailure(Throwable arg0) {
                    Window.alert(arg0.toString());
                }

                public void onSuccess(Object arg0) {
                    addElement((ArtistCompact)arg0, new Date(), true);
                }
            };

            try {
                musicServer.getArtistCompact(id, callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        }

        public void doRemoveListeners() {
            onDelete();
        }

        public abstract void onDelete();


        protected void invokeFetchRecentAttentions(AttentionType aT) {

            DEAsyncCallback<AttentionType, ArrayList<AttentionItem<ArtistCompact>>> callback =
                    new DEAsyncCallback<AttentionType, ArrayList<AttentionItem<ArtistCompact>>>(aT) {

                public void onFailure(Throwable arg0) {
                    Window.alert(arg0.toString());
                }

                public void onSuccess(ArrayList<AttentionItem<ArtistCompact>> arg0) {

                    if (arg0.size() > 0) {
                        initElements(arg0);
                    } else {
                        mainList.add(new Label("No activity"));
                    }
                }
            };

            try {
                if (aT == AttentionType.PLAYED) {
                    musicServer.getLastPlayedArtists(100, false, callback);
                } else if (aT == AttentionType.RATED) {
                    musicServer.getLastRatedArtists(100, true, callback);
                } else {
                    musicServer.getLastTaggedArtists(100, false, callback);
                }
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        }

    }

    private class MainPanel extends Composite implements RatingListener,
            TaggingListener, PlayedListener, HasListeners {

        private Grid centerWidget;
        private Grid defaultCenter = null;
        private Grid featArtist;
        private Grid recentRating;
        private ArrayList<HasListeners> recentRatingListeners;
        private Grid recentTagged;
        private ArrayList<HasListeners> recentTaggingListeners;
        private Grid recentPlayed;
        private ArrayList<HasListeners> recentPlayedListeners;

        private PlayButton playButton;

        public MainPanel() {

            cdm.getRatingListenerManager().addListener(this);
            cdm.getTaggingListenerManager().addListener(this);
            cdm.getPlayedListenerManager().addListener(this);

            recentRatingListeners = new ArrayList<HasListeners>();
            recentTaggingListeners = new ArrayList<HasListeners>();
            recentPlayedListeners = new ArrayList<HasListeners>();

            centerWidget = new Grid(1,1);
            centerWidget.getCellFormatter().setVerticalAlignment(0, 0, VerticalPanel.ALIGN_TOP);
            initWidget(getDashboard());
        }

        private void formatRecentBox(Grid g, String title, ClickListener cL) {
            HorizontalPanel rateHp = new HorizontalPanel();
            rateHp.setWidth("100%");
            rateHp.setStyleName("h2");
            rateHp.add(new Label(title));
            rateHp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            Label featMore = new Label("See more");
            featMore.addStyleName("headerMenuMedItem");
            featMore.addClickListener(cL);
            rateHp.add(featMore);
            g.setWidget(0, 0, rateHp);
            g.setWidget(1, 0, new Image("ajax-bar.gif"));
        }

        public void setCenterWidget(Widget w) {
            centerWidget.setWidget(0, 0, w);
        }

        public void setCenterWidget() {
            if (defaultCenter == null) {
                defaultCenter = new Grid(1,1);
                defaultCenter.setWidget(0, 0, getMainPagePanel());
            }
            centerWidget.setWidget(0, 0, defaultCenter);
        }

        private Widget getDashboard() {

            DockPanel dP = new DockPanel();
            
            recPanel = new Grid(1,1);
            recPanel.setWidget(0, 0, new Label("Loading..."));
            if (cdm.getRecTypes() == null || cdm.getRecTypes().isEmpty()) {
                invokeFetchRecType();
            } else {
                createRecPanel();
            }           
            dP.add(recPanel, DockPanel.WEST);

            centerWidget.setWidget(0, 0, getMainPagePanel());
            Label titleLbl = new Label("Dashboard");
            titleLbl.setStyleName("h1");

            dP.add(titleLbl, DockPanel.NORTH);
            dP.add(centerWidget, DockPanel.NORTH);
            return dP;
        }

        private Widget getMainPagePanel() {

            //
            // Featured artist
            featArtist = new Grid(2,1);
            featArtist.setWidget(0, 0, new HTML("<h2>Featured Artist</h2>"));
            featArtist.setWidget(1, 0, new Image("ajax-bar.gif"));
            invokeFetchFeaturedArtist();

            recentRating = new Grid(2,1);
            formatRecentBox(recentRating, "Recently rated artists", new ClickListener() {

                public void onClick(Widget sender) {
                    History.newItem("viewRecentRated:");
                }
            });
            invokeFetchRecentAttentions(AttentionType.RATED);

            recentTagged = new Grid(2,1);
            formatRecentBox(recentTagged, "Recently tagged artists", new ClickListener() {

                public void onClick(Widget sender) {
                    History.newItem("viewRecentTagged:");
                }
            });
            invokeFetchRecentAttentions(AttentionType.TAGGED);

            recentPlayed = new Grid(2,1);
            formatRecentBox(recentPlayed, "Recently played artists", new ClickListener() {

                public void onClick(Widget sender) {
                    History.newItem("viewRecentPlayed:");
                }
            });
            invokeFetchRecentAttentions(AttentionType.PLAYED);

            ItemInfo[] trimTags = null;
            if (cdm.getListenerDetails().getUserTagCloud() != null && cdm.getListenerDetails().getUserTagCloud().length > 0) {
                int max = cdm.getListenerDetails().getUserTagCloud().length;
                if (max > 20) {
                    max = 20;
                }
                ArrayList<ItemInfo> liI = ItemInfo.arrayToList(cdm.getListenerDetails().getUserTagCloud());
                Collections.sort(liI,ItemInfo.getScoreSorter());
                trimTags = new ItemInfo[max];
                for (int i=0; i<max; i++) {
                    trimTags[i] = liI.get(i);
                }
            }

            VerticalPanel centerPanel = new VerticalPanel();

            if (trimTags != null) {
                centerPanel.add(TagDisplayLib.getTagsInPanel(trimTags,
                        TagDisplayLib.ORDER.SHUFFLE, cdm));
            }
            centerPanel.add(featArtist);
            centerPanel.add(recentRating);
            centerPanel.add(recentTagged);
            centerPanel.add(recentPlayed);
            return centerPanel;
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
                featArtTitle.setWidget(0, 0, new Label("Featured artist : " + aD.getName()));

                if (playButton != null) {
                    playButton.onDelete();
                }

                StarRatingWidget srw = new StarRatingWidget(musicServer, cdm, aD.getId(), StarRatingWidget.InitialRating.FETCH, StarRatingWidget.Size.MEDIUM);
                featArtTitle.setWidget(0, 1, srw);

                HorizontalPanel hP = new HorizontalPanel();
                hP.setWidth("100%");
                hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
                hP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

                ArtistCompact aC = aD.toArtistCompact();
                playButton = new PlayButton(cdm, aC, PlayButton.PLAY_ICON_SIZE.MEDIUM, musicServer);
                cdm.getMusicProviderSwitchListenerManager().addListener(playButton);
                hP.add(playButton);

                SteeringWheelWidget steerButton = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.BIG,
                        new DDEClickHandler<ClientDataManager, ArtistCompact>(cdm, aC) {
                    @Override
                    public void onClick(ClickEvent ce) {
                        data.setSteerableReset(true);
                        History.newItem("steering:" + sndData.getId());
                    }
                });
                steerButton.setTitle("Steerable recommendations starting with "+aC.getName()+"'s tag cloud");
                hP.add(steerButton);

                featArtTitle.setWidget(0, 2, hP);

                featArtist.setWidget(0, 0, featArtTitle);

                HorizontalPanel featHp = new HorizontalPanel();
                featHp.setSpacing(5);
                featHp.add(aD.getBestArtistImage(false));

                VerticalPanel featVp = new VerticalPanel();
                featVp.setSpacing(4);
                featVp.add(new HTML(aD.getBiographySummary().substring(0, 300) + " [...]"));
                //featVp.add(TagDisplayLib.getTagsInPanel(aD.getDistinctiveTags(), TagDisplayLib.ORDER.SHUFFLE, cdm));
                featVp.add(new HTML("<b>Tags</b> : "+getNDistinctiveTags(aD, 10)));

                featHp.add(featVp);

                featArtist.setWidget(1, 0,  featHp);

            }
        }

        private void invokeFetchRecentAttentions(AttentionType aT) {

            DEAsyncCallback<AttentionType, ArrayList<AttentionItem<ArtistCompact>>> callback =
                    new DEAsyncCallback<AttentionType, ArrayList<AttentionItem<ArtistCompact>>>(aT) {

                public void onFailure(Throwable arg0) {
                    Window.alert(arg0.toString());
                }

                public void onSuccess(ArrayList<AttentionItem<ArtistCompact>> arg0) {

                    Grid sucessGrid = null;
                    ArrayList<HasListeners> sucessAL = null;

                    if (data == AttentionType.PLAYED) {
                        sucessGrid = recentPlayed;
                        sucessAL = recentPlayedListeners;
                    } else if (data == AttentionType.RATED) {
                        sucessGrid = recentRating;
                        sucessAL = recentRatingListeners;
                    } else {
                        sucessGrid = recentTagged;
                        sucessAL = recentTaggingListeners;
                    }

                    if (arg0.size() > 0) {
                        int numLines = (int)Math.ceil(arg0.size() / 2.0);
                        Grid artists = new Grid(numLines, 2);

                        int lineIndex = 0;
                        int colIndex = 0;

                        for (AttentionItem<ArtistCompact> aI : arg0) {

                            CompactArtistWidget caw = new CompactArtistWidget(aI.getItem(), cdm,
                                    musicServer, null, null, StarRatingWidget.intToRatingEnum(aI.getRating()), aI.getTags());
                            sucessAL.add(caw);
                            artists.setWidget(lineIndex, (colIndex++)%2, caw);

                            if (colIndex%2 == 0) {
                                lineIndex++;
                            }
                        }
                        sucessGrid.setWidget(1, 0, artists);
                    } else {
                        sucessGrid.setWidget(1, 0, new Label("No recent activity"));
                    }
                }
            };

            try {
                if (aT == AttentionType.PLAYED) {
                    musicServer.getLastPlayedArtists(6, true, callback);
                } else if (aT == AttentionType.RATED) {
                    musicServer.getLastRatedArtists(6, true, callback);
                } else {
                    musicServer.getLastTaggedArtists(6, true, callback);
                }
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        }

        /**
         * Create the left recommendation pannel
         */
        private void createRecPanel() {

            HorizontalPanel hP = new HorizontalPanel();
            hP.setWidth("100%");
            SpannedLabel title = new SpannedLabel("Recommendations");
            hP.add(title);
          
            Label currShowing = new Label("");
            
            //
            // Populate context menu
            ContextMenu cM = new ContextMenu();
            String[] keyArray = cdm.getRecTypes().keySet().toArray(new String[0]);
            for (int i = keyArray.length - 1; i >= 0; i--) {
                cM.addElement(keyArray[i], new DualDataEmbededClickListener<String, Label>(keyArray[i], currShowing) {
                    @Override
                    public void onClick(Widget sender) {
                        String newSelectName = data;

                        // If the selection has changed
                        if (!cdm.getCurrRecTypeName().equals(newSelectName)) {
                            cdm.setCurrRecTypeName(newSelectName);
                            sndData.setText("Showing "+newSelectName);
                            invokeFetchRecommendations();
                        }
                    }
                });
            }
            cdm.setCurrRecTypeName(keyArray[0]);
            
            //
            // Create click listener
            ClickListener cL = new DataEmbededClickListener<ContextMenu>(cM) {
                public void onClick(Widget sender) {
                    data.showAt(DOM.eventGetCurrentEvent());
                }
            };
            
            ContextMenuImage menuImg = new ContextMenuImage("customize.png");
            menuImg.addClickListener(cL);
            menuImg.addRightClickListener(cL);
            hP.add(menuImg);
            
            VerticalPanel vP = new VerticalPanel();
            vP.setWidth("100%");
            currShowing.setText("Showing "+cdm.getCurrRecTypeName());
            currShowing.setStyleName("smallItalicExplanation");
            vP.add(hP);
            vP.add(currShowing);
            
            uP = new UpdatablePanel(vP, new Image("ajax-loader-small.gif"), cdm);
            recPanel.setWidget(0, 0, uP);
            invokeFetchRecommendations();
        }

        private void invokeFetchRecType() {

            AsyncCallback<HashMap<String, String>> callback = new AsyncCallback<HashMap<String, String>>() {

                public void onFailure(Throwable arg0) {
                    Window.alert(arg0.toString());
                }

                public void onSuccess(HashMap<String, String> recTypes) {
                    if (recTypes != null) {
                        cdm.setRecTypes(recTypes);
                        createRecPanel();
                    } else {
                        Window.alert("Recommendation types are not available.");
                    }
                }
            };

            musicServer.getArtistRecommendationTypes(callback);
        }
        
        private void invokeFetchRecommendations() {

            AsyncCallback<ArrayList<ArtistRecommendation>> callback =
                    new AsyncCallback<ArrayList<ArtistRecommendation>>() {

                public void onFailure(Throwable arg0) {
                    Window.alert(arg0.toString());
                }

                public void onSuccess(ArrayList<ArtistRecommendation> rec) {
                    uP.setNewContent(new UserCloudArtistListWidget(musicServer,
                            cdm, ArtistRecToArtistCompact(rec),rec));
                    uP.setWaitIconVisible(false);
                }
            };

            uP.setWaitIconVisible(true);
            
            try {
                musicServer.getRecommendations(cdm.getCurrRecTypeName(), 15, callback);
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
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

                ArtistCompact[] aC = cdm.getListenerDetails().getRecommendations();
                if (aC.length > 0) {
                    int itemIndex = Random.nextInt(aC.length);
                    musicServer.getArtistDetails(aC[itemIndex].getId(), false, cdm.getCurrSimTypeName(), cdm.getCurrPopularity(), callback);
                }
            } catch (WebException ex) {
                Window.alert(ex.getMessage());
            }
        }

        public void onDelete() {
            defaultCenter = null;
        }
        
        public void doRemoveListeners() {
            onDelete();
            if (playButton != null) {
                playButton.onDelete();
            }
            clearListeners(recentRatingListeners);
            clearListeners(recentTaggingListeners);
            clearListeners(recentPlayedListeners);
        }

        public void onRate(String itemId, int rating) {
            clearListeners(recentRatingListeners);
            recentRating.setWidget(1, 0, new Image("ajax-bar.gif"));
            invokeFetchRecentAttentions(AttentionType.RATED);
        }

        public void onTag(String itemId, HashSet<String> tags) {
            clearListeners(recentTaggingListeners);
            recentTagged.setWidget(1, 0, new Image("ajax-bar.gif"));
            invokeFetchRecentAttentions(AttentionType.TAGGED);
        }

        private void clearListeners(ArrayList<HasListeners> hLL) {
            for (HasListeners hL : hLL) {
                hL.doRemoveListeners();
            }
            hLL.clear();
        }

        public void onPlay(String artistId) {
            clearListeners(recentPlayedListeners);
            recentPlayed.setWidget(1, 0, new Image("ajax-bar.gif"));
            invokeFetchRecentAttentions(AttentionType.PLAYED);
        }

    }

    /**
     * Extract the ArtistCompacts from a list of artist recommenation
     * @param aR
     * @return
     */
    public ArtistCompact[] ArtistRecToArtistCompact(ArrayList<ArtistRecommendation> aR) {
        ArtistCompact[] aC = new ArtistCompact[aR.size()];
        for (int i = 0; i < aR.size(); i++) {
            aC[i] = aR.get(i).getArtist();
        }
        return aC;
    }
    
    private class UserCloudArtistListWidget extends ArtistListWidget {

        private HashMap<String, ArtistRecommendation> mapAR;

        public UserCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
                ClientDataManager cdm, ArtistCompact[] aDArray, ArrayList<ArtistRecommendation> aR) {

            super(musicServer, cdm, aDArray, false);
            mapAR = new HashMap<String, ArtistRecommendation>();
            for (ArtistRecommendation a : aR) {
                mapAR.put(a.getArtist().getId(), a);
            }
        }

        public void openWhyPopup(SwapableTxtButton why) {
            TagDisplayLib.showTagCloud(mapAR.get(why.getId()).getDescription(), mapAR.get(why.getId()).getExplanation(), TagDisplayLib.ORDER.SHUFFLE, cdm);
        }

        @Override
        public void openDiffPopup(DiffButton diff) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
