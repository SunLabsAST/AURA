/*
 * SimpleSearchWidget.java
 *
 * Created on March 7, 2007, 5:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.CommonTagsAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ui.widget.StarRatingWidget;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.ui.widget.TagInputWidget;
import com.sun.labs.aura.music.wsitm.client.ui.Updatable;
import com.sun.labs.aura.music.wsitm.client.ui.widget.SteeringWheelWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.items.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.items.ArtistVideo;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DDEClickHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.searchTypes;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.ui.PerformanceTimer;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib.TagColorType;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ContextMenuSteeringWheelWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PlayButton;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PopularitySelect;
import java.util.ArrayList;
import java.util.Map;
import org.adamtacy.client.ui.NEffectPanel;
import org.adamtacy.client.ui.effects.impl.Fade;

/**
 *
 * @author plamere
 */
public class SimpleSearchSwidget extends Swidget implements HasListeners {

    private Widget curResult;
    private String curResultToken = "";

    private DockPanel mainPanel;
    private Label message;
    private Image icon;

    private PopularitySelect popSelect;
    
    // Widgets that contain listeners that need to be removed to prevent leaks
    private ArtistListWidget leftRecList;
    private ArtistListWidget leftSimList;
    private ArtistListWidget leftRelList;
    private StarRatingWidget artistStar;
    private TagInputWidget tagInputWidget;
    private PlayButton playButton;

    public SimpleSearchSwidget(ClientDataManager cdm) {
        super("Simple Search", cdm);
        try {
            initWidget(getWidget());
            showResults(History.getToken());
        } catch (Exception e) {
            Window.alert("Server problem. Please try again later.");
        }
    }

    /** Creates a new instance of SimpleSearchWidget */
    @Override
    public Widget getWidget() {

        //searchBoxContainerPanel = new FlowPanel();
        
        //search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel);
        //search.updateSuggestBox(Oracles.ARTIST);

        message = new Label();
        message.setHeight("20px");
        message.setStyleName("message");

        icon = new Image();
        icon.setVisible(false);
        icon.setStyleName("img");

        VerticalPanel msgPanel = new VerticalPanel();
        msgPanel.setWidth("100%");
        msgPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        msgPanel.add(message);
        msgPanel.add(icon);
        msgPanel.setCellHeight(message, "28px");
        msgPanel.setCellHorizontalAlignment(icon, VerticalPanel.ALIGN_CENTER);
        msgPanel.setCellHeight(icon, "20px");
        msgPanel.setCellHorizontalAlignment(message, VerticalPanel.ALIGN_CENTER);

        VerticalPanel topPanel = new VerticalPanel();
        topPanel.setWidth("100%");
        topPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

        //topPanel.add(search);
        topPanel.add(msgPanel);

        mainPanel = new DockPanel();
        mainPanel.setWidth("100%");
        mainPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
        mainPanel.add(topPanel, DockPanel.NORTH);

        return mainPanel;
    }

    /**
     * Display a message with possibly an icon
     * @param msg Message to display
     * @param iconPath URL to icon. Set to null to not use and icon
     */
    private void showMessage(String msg, String iconPath) {
        if (message!=null) {
            if (iconPath!=null && !iconPath.equals("")) {
                icon.setUrl(iconPath);
                icon.setVisible(true);
            } else {
                icon.setVisible(false);
            }

            message.setStyleName("messageNormal");
            message.setText(msg);
        }
    }

    private void showMessage(String msg) {
        showMessage(msg,null);
    }

    private void showError(String msg) {
        message.setStyleName("messageError");
        message.setText(msg);
        icon.setVisible(false);
    }

    private void clearMessage() {
        message.setStyleName("messageNormal");
        message.setText("");
        icon.setVisible(false);
    }

    private void setResults(String historyName, Widget result) {
        if (curResult == result || curResultToken.equals(historyName)) {
            return;
        } 

        if (!History.getToken().equals(historyName)) {
            curResultToken = historyName;
            History.newItem(historyName, false);
        }
        
        if (curResult != null) {
            mainPanel.remove(curResult);
            curResult = null;
        }

        if (result != null) {
            cdm.setCurrSearchWidgetToken(historyName);
            mainPanel.add(result, DockPanel.CENTER);
            curResult = result;
            curResultToken = historyName;
        }// else {
        //    search.setText("");
        //}
    }

    private void clearResults() {
        setResults("artist:",new Label(""));
    }

    @Override
    public ArrayList<String> getTokenHeaders() {

        ArrayList<String> l = new ArrayList<String>();
        l.add("artist:");
        l.add("tag:");
        l.add("artistSearch:");
        l.add("artistSearchByTag:");
        l.add("tagSearch:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        menuItem = new MenuItem("Exploration",new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    History.newItem(cdm.getCurrSearchWidgetToken());
                }
            },false,0);
    }

    @Override
    public void doRemoveListeners() {

        if (leftRelList != null) {
            leftRelList.doRemoveListeners();
        }
        if (leftSimList != null) {
            leftSimList.doRemoveListeners();
        }
        if (leftRelList != null) {
            leftRelList.doRemoveListeners();
        }

        if (artistStar != null) {
            artistStar.onDelete();
        }

        if (tagInputWidget != null) {
            tagInputWidget.onDelete();
        }
        
        if (playButton != null) {
            playButton.onDelete();
        }
    }

    private void showResults(String resultName) {

        // Reset current artistID. Will be updated in invokeGetArtistInfo
        cdm.setCurrArtistInfo("", "");

        // Clear all listeners
        doRemoveListeners();

        // Reset the search
        cdm.getSearchAttentionManager().resetSearch();

        //  resultName = URL.decodeComponent(resultName);
        if (resultName.startsWith("artist:")) {
            //search.updateSuggestBox(Oracles.ARTIST);
            invokeGetArtistInfo(resultName, false);
        } else if (resultName.startsWith("tag:")) {
            //search.updateSuggestBox(Oracles.TAG);
            invokeGetTagInfo(resultName, false);
        } else if (resultName.startsWith("artistSearch:")) {
            //search.updateSuggestBox(Oracles.ARTIST);
            String query = resultName.replaceAll("artistSearch:", "");
            invokeArtistSearchService(query, searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST, 0);
        } else if (resultName.startsWith("artistSearchByTag:")) {
            //search.updateSuggestBox(Oracles.TAG);
            String query = resultName.replaceAll("artistSearchByTag:", "");
            invokeArtistSearchService(query, searchTypes.SEARCH_FOR_ARTIST_BY_TAG, 0);
        } else if (resultName.startsWith("tagSearch:")) {
            //search.updateSuggestBox(Oracles.TAG);
            String query = resultName.replaceAll("tagSearch:", "");
            invokeTagSearchService(query, 0);
        } else if (resultName.startsWith("searchHome:")) {
            cdm.setCurrSearchWidgetToken("searchHome:");
            History.newItem("searchHome:");
        }
    }

    @Override
    public void update(String historyToken) {
        // Only update results if the history token is different than the
        // currently loaded page
        if (!curResultToken.equals(historyToken)) {
            showResults(historyToken);
        }
    }

    private void invokeTagSearchService(String searchText, int page) {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                SearchResults sr = (SearchResults) result;
                if (sr != null && sr.isOK()) {
                    ItemInfo[] results = sr.getItemResults(cdm);
                    if (results.length == 0) {
                        showError("No Match for " + sr.getQuery());
                        clearResults();
                    } else if (results.length == 1) {
                        WebLib.trackPageLoad("#tagSearch:" + sr.getQuery());
                        ItemInfo ar = results[0];
                        invokeGetTagInfo(ar.getId(), false);
                    } else {
                        showMessage("Found " + sr.getItemResults(cdm).length + " matches");
                        setResults(sr.toString(), getItemInfoList("Pick one: ", sr.getItemResults(cdm), null, false, true, cdm.getTagOracle()));
                    }
                } else {
                    if (sr == null) {
                        showError("Error. Resultset is null. There were probably no tags found.");
                        clearResults();
                    } else {
                        showError("Whoops " + sr.getStatus());
                        clearResults();
                    }
                }
            }

            public void onFailure(Throwable caught) {
                failureAction(caught);
            }
        };

        showMessage("Searching for " + searchText,WebLib.ICON_WAIT);

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        try {
            musicServer.tagSearch(searchText, 100, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void invokeArtistSearchService(String searchText, searchTypes sT, int page) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {

                SearchResults sr = (SearchResults) result;
                if (sr != null && sr.isOK()) {
                    ItemInfo[] results = sr.getItemResults(cdm);
                    if (results.length == 0) {
                        showError("No Match for " + sr.getQuery());
                        clearResults();
                    } else if (results.length == 1) {
                        ItemInfo ar = results[0];
                        WebLib.trackPageLoad("#artistSearch:" + sr.getQuery());
                        cdm.getSearchAttentionManager().processUserClick(ar.getId());
                        invokeGetArtistInfo(ar.getId(), false);
                    } else {
                        showMessage("Found " + sr.getItemResults(cdm).length + " matches");
                        Widget searchResults = getItemInfoList("Pick one: ", sr.getItemResults(cdm), null, true, true, cdm.getArtistOracle());
                        searchResults.setStyleName("searchResults");
                        searchResults.setWidth("300px");
                        setResults(sr.toString(), searchResults);
                    }
                } else {
                    if (sr == null) {
                        showError("Error. Can't find tag specified.");
                        clearResults();
                    } else {
                        showError("Very Whooops " + sr.getStatus());
                        clearResults();
                    }
                }
            }

            public void onFailure(Throwable caught) {
                failureAction(caught);
            }
        };

        showMessage("Searching for " + searchText,WebLib.ICON_WAIT);
        try {
            if (sT == searchTypes.SEARCH_FOR_ARTIST_BY_TAG) {
                musicServer.artistSearchByTag(searchText, 100, callback);
            } else if (sT == searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST) {
                musicServer.artistSearch(searchText, 100, callback);
            } else {
                Popup.showInformationPopup("Error. Invalid search type.");
            }
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void failureAction(Throwable caught) {
        Window.alert("Whoops! It looks like the server is down. Time to get a spot of tea.");
        showMessage(caught.getMessage());
    }

    private void invokeGetArtistInfo(String artistID, boolean refresh) {
        PerformanceTimer.start("invokeGetArtistInfo");
        //
        // If we are currently fetching the similarity type, we can't fetch the
        // artist's info yet so let's try again in 250ms
        if (cdm.getCurrSimTypeName() == null || cdm.getCurrSimTypeName().equals("")) {
            Timer t = new TimerWithArtist(artistID, refresh);
            t.schedule(250);
        } else {
            if (artistID.startsWith("artist:")) {
                artistID = artistID.replaceAll("artist:", "");
            }

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    PerformanceTimer.stop("getArtistDetails");
                    // do some UI stuff to show success
                    ArtistDetails artistDetails = (ArtistDetails) result;
                    if (artistDetails != null && artistDetails.isOK()) {
                        PerformanceTimer.start("createArtistPanel");
                        cdm.setCurrArtistInfo(artistDetails.getId(), artistDetails.getName());
                        Widget artistPanel = createArtistPanel(artistDetails);
                        //search.setText(artistDetails.getName(), searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);
                        //search.updateSuggestBox(Oracles.ARTIST);
                        setResults("artist:" + artistDetails.getId(), artistPanel);
                        clearMessage();
                        PerformanceTimer.stop("createArtistPanel");
                    } else {
                        if (artistDetails == null) {
                            showError("Sorry. The details for the artist don't seem to be in our database.");
                            clearResults();
                        } else {
                            showError("Whooops " + artistDetails.getStatus());
                            clearResults();
                        }
                    }
                    PerformanceTimer.stop("invokeGetArtistInfo");
                }

                public void onFailure(Throwable caught) {
                    PerformanceTimer.stop("getArtistDetails");
                    failureAction(caught);
                    PerformanceTimer.stop("invokeGetArtistInfo");
                }
            };

            showMessage("Getting info for artist", WebLib.ICON_WAIT);

            try {
                PerformanceTimer.start("getArtistDetails");
                musicServer.getArtistDetails(artistID, refresh, cdm.getCurrSimTypeName(), 
                        cdm.getCurrPopularity(), callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }
    }

    private void invokeGetTagInfo(String tagID, boolean refresh) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                TagDetails tagDetails = (TagDetails) result;
                if (tagDetails != null && tagDetails.isOK()) {
                    Widget tagPanel = createTagPanel(tagDetails);
                    //search.setText(tagDetails.getName(), searchTypes.SEARCH_FOR_TAG_BY_TAG);
                    //search.updateSuggestBox(Oracles.TAG);
                    setResults("tag:"+tagDetails.getId(), tagPanel);
                    clearMessage();
                } else {
                    if (tagDetails == null) {
                        showError("Sorry. The details for the tag don't seem to be in our database.");
                        clearResults();
                    } else {
                        showError("Whooops " + tagDetails.getStatus());
                        clearResults();
                    }
                }
            }

            public void onFailure(Throwable caught) {
                failureAction(caught);
            }
        };

        showMessage("Getting info for tag", WebLib.ICON_WAIT);

        try {
            musicServer.getTagDetails(tagID.substring(tagID.indexOf(":")+1), refresh, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private final void addCompactArtistToOracle(ArrayList<ScoredC<ArtistCompact>> aCList) {
        for (ScoredC<ArtistCompact> aC : aCList) {
            cdm.getArtistOracle().add(aC.getItem().getName(), aC.getItem().getPopularity());
        }
    }
    
    private final void addCompactArtistToOracle(ArtistCompact[] aCArray) {
        for (ArtistCompact aC : aCArray) {
            cdm.getArtistOracle().add(aC.getName(), aC.getPopularity());
        }
    }

    private Widget createArtistPanel(ArtistDetails artistDetails) {
        ArtistCompact aC = artistDetails.toArtistCompact();

        VerticalPanel main = new VerticalPanel();
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        main.add(getBioWidget(artistDetails));
        main.add(WebLib.createSection("Videos", new VideoScrollWidget(artistDetails.getVideos())));
        main.add(WebLib.createSection("Photos", new ImageScrollWidget(artistDetails.getPhotos())));
        main.add(WebLib.createSection("Albums", new AlbumScrollWidget(artistDetails.getAlbums())));
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        main.add(getEventsWidget(artistDetails));
        main.setStyleName("center");

        VerticalPanel left = new VerticalPanel();
        left.setSpacing(4);
        left.setWidth("300px");
        
        // Add similar artists
        ArtistCompact[] aCArray;
        aCArray = artistDetails.getSimilarArtistsAsArray();
        addCompactArtistToOracle(aCArray);

        if (leftSimList != null) {
            leftSimList.doRemoveListeners();
        }
        leftSimList = new ArtistCloudArtistListWidget(musicServer, cdm, artistDetails.getSimilarArtists(), aC);

        HorizontalPanel hP = new HorizontalPanel();
        hP.add(new Label("Similar artists"));
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        popSelect = new PopularitySelectAD(artistDetails);
        hP.add(popSelect);
        hP.setWidth("300px");
        hP.setStyleName("h2");

        left.add(
                new Updatable<ArtistDetails>(hP, leftSimList, cdm, artistDetails) {

                    public void update(ArrayList<ScoredC<ArtistCompact>> aCList) {
                        addCompactArtistToOracle(aCList);
                        leftSimList.doRemoveListeners();
                        leftSimList = new ArtistCloudArtistListWidget(musicServer, cdm, aCList, data.toArtistCompact());
                        setNewContent(leftSimList);
                    }
                });

        // Add recommended artists
        if (artistDetails.getRecommendedArtists().length > 0) {
            aCArray = artistDetails.getRecommendedArtists();
            addCompactArtistToOracle(aCArray);
            
            if (leftRecList != null) {
                leftRecList.doRemoveListeners();
            }
            leftRecList = new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, aC);
            left.add(WebLib.createSection("Recommendations", leftRecList));
        }

        // Add related artists
        if (artistDetails.getCollaborations().length > 0) {
            aCArray = artistDetails.getCollaborations();
            addCompactArtistToOracle(aCArray);

            if (leftRelList != null) {
                leftRelList.doRemoveListeners();
            }
            leftRelList = new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, aC);
            left.add(WebLib.createSection("Related", leftRelList));
        }
        left.add(getMoreInfoWidget(artistDetails));
        left.setStyleName("left");

        DockPanel artistPanel = new DockPanel();
        artistPanel.add(main, DockPanel.CENTER);
        artistPanel.add(left, DockPanel.WEST);
        artistPanel.setWidth("100%");
        artistPanel.setStyleName("resultpanel");
        return artistPanel;
    }

    private Widget createTagPanel(TagDetails tagDetails) {

        VerticalPanel main = new VerticalPanel();
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        main.add(getTagHeaderWidget(tagDetails));
        main.add(WebLib.createSection("Videos", new VideoScrollWidget(tagDetails.getVideos())));
        main.add(WebLib.createSection("Photos", new ImageScrollWidget(tagDetails.getPhotos())));
        main.setStyleName("center");

        VerticalPanel left = new VerticalPanel();
        left.setSpacing(4);
        left.setWidth("300px");
        
        // Add similar artists
        ArtistCompact[] aCArray;
        aCArray = tagDetails.getRepresentativeArtists();
        addCompactArtistToOracle(aCArray);

        if (leftSimList != null) {
            leftSimList.doRemoveListeners();
        }
        leftSimList = new TagCloudArtistListWidget(musicServer, cdm, aCArray);

        HorizontalPanel hP = new HorizontalPanel();
        hP.add(new Label("Representative artists"));
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        hP.setWidth("300px");
        hP.setStyleName("h2");

        left.add(hP);
        left.add(leftSimList);

        DockPanel artistPanel = new DockPanel();
        artistPanel.add(main, DockPanel.CENTER);
        artistPanel.add(left, DockPanel.WEST);
        artistPanel.setWidth("100%");
        artistPanel.setStyleName("resultpanel");
        return artistPanel;
    }

    private Widget getTagHeaderWidget(TagDetails tagDetails) {
        
        HTML html = new HTML();
        html.setHTML(getBestTagImageAsHTML(tagDetails) + tagDetails.getDescription());
        html.setStyleName("bio");

        HorizontalPanel hP = new HorizontalPanel();
        hP.add(WebLib.getListenWidget(tagDetails));
        
        return createMainSection(tagDetails.getName(), html,
                hP, tagDetails.getSimilarTags(), null, false);
    }
    
    private Widget getBioWidget(ArtistDetails artistDetails) {
        HTML html = new HTML();
        html.setHTML(artistDetails.getBestArtistImageAsHTML() + artistDetails.getBiographySummary());
        html.setStyleName("bio");

        artistStar = new StarRatingWidget(musicServer, cdm, artistDetails.getId(), StarRatingWidget.InitialRating.FETCH, StarRatingWidget.Size.MEDIUM);
        cdm.getLoginListenerManager().addListener(artistStar);

        HorizontalPanel hP = new HorizontalPanel();
        hP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        playButton = new PlayButton(cdm, artistDetails.toArtistCompact(),
            PlayButton.PLAY_ICON_SIZE.MEDIUM, musicServer);
        if (playButton!=null) {
            cdm.getMusicProviderSwitchListenerManager().addListener(playButton);
            playButton.addStyleName("pointer");
            hP.add(playButton);
        }
        
        ArtistCompact aC = artistDetails.toArtistCompact();
        SteeringWheelWidget steerButton = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.BIG, 
                new DDEClickHandler<ClientDataManager, ArtistCompact>(cdm, aC) {

            @Override
            public void onClick(ClickEvent ce) {
                data.setSteerableReset(true);
                History.newItem("steering:" + sndData.getId());
            }
        });
        steerButton.setTitle("Steerable recommendations starting with "+aC.getName()+"'s tag cloud");
        hP.add(new ContextMenuSteeringWheelWidget(cdm, steerButton, aC));

        return createMainSection(artistDetails.getName(), html,
                hP, artistDetails.getDistinctiveTags(), artistStar, true);
    }

    private Widget getMoreInfoWidget(ArtistDetails artistDetails) {
        Map<String, String> urls = artistDetails.getUrls();

        if (urls != null && urls.size() > 0) {
            Grid grid = new Grid(urls.size(), 1);
            int index = 0;
            for (String key : urls.keySet()) {
                String url =  urls.get(key);
                HTML html = new HTML(WebLib.createAnchor(key, url));
                grid.setWidget(index++, 0, html);
            }
            return WebLib.createSection("More info", grid);
        } else {
            return new Label("");
        }
    }
/*
    Widget getTastAuraMeterPanel(ArtistDetails aD) {

        double currArtistScore = 1; //cdm.computeTastauraMeterScore(aD);
        double realMaxScore;    // max between currArtist and user's fav artists' max score

        if (currArtistScore>cdm.getMaxScore()) {
            realMaxScore = currArtistScore;
        } else {
            realMaxScore = cdm.getMaxScore();
        }

        VerticalPanel vPanel = new VerticalPanel();

        for (String key : cdm.getFavArtist().keySet()) {
            vPanel.add(WebLib.getPopularityWidget(key, cdm.getFavArtist().get(key)/realMaxScore, false, null));
        }

        vPanel.add(WebLib.getPopularityWidget(aD.getName(),
                currArtistScore/realMaxScore, false, "itemInfoHighlight"));

        return WebLib.createSection("Tast-aura-meter", vPanel);
    }
    */

    private Widget getPopularityPanel(ArtistDetails artistDetails) {

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(WebLib.getPopularityWidget("The Beatles",1,true,null));
        vPanel.add(WebLib.getPopularityWidget(artistDetails.getName(),
                artistDetails.getNormPopularity(),true,null));

        return WebLib.createSection("Popularity", vPanel);
    }

    private Widget getTastAuraMeterWidget(String name, double normPopularity, boolean log) {

        /**
        Widget popWidget = getPopularityWidget(name, normPopularity, log);

        Label why = new Label("why?");
        why.setStyleName("tinyInfo");
        why.addClickListener(new CommonTagsClickListener(highlightID, itemInfo[i].getId()));
        **/
        return null;

    }

    private Widget getEventsWidget(ArtistDetails artistDetails) {
        ArtistEvent[] events = artistDetails.getEvents();
        Panel widget = new VerticalPanel();
        if (events.length == 0) {
            widget.add(new Label("No events found"));
        } else {
            String introMessage;
            if (artistDetails.isActive()) {
                introMessage = "Some upcoming events related to " + artistDetails.getName();
            } else {
                introMessage = "Although we haven't heard from " + artistDetails.getName() + " since " + artistDetails.getEndYear() + ", you might find these related events to be of interest.";
            }
            widget.add(new HTML(introMessage + "<br/>"));
            Grid grid = new Grid(events.length, 3);
            for (int i = 0; i < events.length; i++) {
                ArtistEvent event = events[i];
                grid.setWidget(i, 0, new Label(events[i].getDate()));
                grid.setWidget(i, 1, new HTML(WebLib.createAnchor(event.getName(), event.getEventURL())));
                String venue = event.getVenue();
                grid.setWidget(i, 2, new HTML(venue));
            }
            widget.add(grid);
        }
        return WebLib.createSection("Upcoming Events", widget);
    }

    private Widget createMainSection(String title, Widget widget, Widget adornment, boolean addTagInputWidget) {
        return createMainSection(title, widget, adornment, null, null, addTagInputWidget);
    }

    private Widget createMainSection(String title, Widget widget, Widget adornment, 
            ItemInfo[] tagCloud, StarRatingWidget starWidget, boolean addTagInputWidget) {
        Panel panel = new VerticalPanel();
        DockPanel h = new DockPanel();
        h.add(new Label(title), DockPanel.WEST);
        if (adornment != null) {
            h.add(adornment, DockPanel.EAST);
            h.setCellHorizontalAlignment(adornment, HorizontalPanel.ALIGN_RIGHT);
        }
        if (starWidget != null) {
            h.add(starWidget, DockPanel.NORTH);
        }

        h.setWidth("100%");
        h.setStyleName("h1");
        panel.add(h);
        if (tagCloud != null) {
            if (tagInputWidget != null) {
                tagInputWidget.onDelete();
            }
            if (addTagInputWidget) {
                tagInputWidget = new TagInputWidget(musicServer, cdm, "artist", cdm.getCurrArtistID());
                cdm.getLoginListenerManager().addListener(tagInputWidget);
                panel.add(tagInputWidget);
            }
            
            Panel p = TagDisplayLib.getTagsInPanel(tagCloud, TagDisplayLib.ORDER.SHUFFLE, cdm, TagColorType.TAG);
            // If there are not tags, this will be null
            if (p != null) {
                p.addStyleName("tagCloudMargin");
                panel.add(p);
            } else {
                panel.add(new HTML("<br /<br />"));
            }
        }
        panel.add(widget);
        return panel;
    }

    private String getBestTagImageAsHTML(TagDetails td) {
        String imgHtml = "";
        ArtistPhoto[] photos = td.getPhotos();
        if (photos.length > 0) {
            imgHtml = photos[0].getHtmlWrapper();
        }
        return imgHtml;
    }

    private String getEmbeddedVideo(ArtistVideo video, boolean autoplay) {
        String url = video.getUrl();
        String autostring = autoplay ? "&autoplay=1" : "";
        url = url.replaceAll("\\?v=", "/v/");
        //String title = "<span style=\"text-align:center\">" + video.getTitle() + "</span><br/>";
        String obj = "<object width=\"425\" height=\"350\"><param name=\"movie\" value=\"" + url + "\"></param><param name=\"wmode\" value=\"transparent\"></param>" + "<embed src=\"" + url + autostring + "\" type=\"application/x-shockwave-flash\"" + " wmode=\"transparent\" width=\"425\" height=\"350\"></embed></object>";
        //return title + obj;
        return obj;
    }

    private VerticalPanel getItemInfoList(final String title, final ItemInfo[] itemInfo, 
            String highlightID, boolean getArtistOnClick, boolean displayPopularity, 
            PopSortedMultiWordSuggestOracle oracle) {

        Grid artistGrid;
        if (displayPopularity) {
            artistGrid = new Grid(itemInfo.length + 1, 2);
            artistGrid.setCellSpacing(5);
            artistGrid.setWidget(0, 0, new HTML("<b>Name</b>"));
            artistGrid.setWidget(0, 1, new HTML("<b>Popularity</b>"));
        } else {
            artistGrid = new Grid(itemInfo.length, 1);
        }

        // Find the maximum values for score and popularity
        double maxPopularity = 0;
        for (ItemInfo iI : itemInfo) {
            if (iI.getPopularity() > maxPopularity) {
                maxPopularity = iI.getPopularity();
            }
        }

        for (int i = 0; i < itemInfo.length; i++) {

            if (oracle != null) {
                oracle.add(itemInfo[i].getItemName(), itemInfo[i].getPopularity());
            }

            Label label = new Label(itemInfo[i].getItemName());
            label.addClickHandler(new DEClickHandler<String>(itemInfo[i].getId()) {
                @Override
                public void onClick(ClickEvent event) {
                    // Add search attention if necessary
                    cdm.getSearchAttentionManager().processUserClick(data);
                }
            });
            label.addClickHandler(new ItemInfoClickHandler(itemInfo[i], getArtistOnClick));
            label.setTitle("Score: " + itemInfo[i].getScore() + " Popularity:" + itemInfo[i].getPopularity());
            if (highlightID != null && highlightID.equals(itemInfo[i].getId())) {
                label.setStyleName("itemInfoHighlight");
            } else {
                label.setStyleName("itemInfo");
            }
            
            if (displayPopularity) {
                artistGrid.setWidget(i + 1, 0, label);
                artistGrid.setWidget(i + 1, 1, WebLib.getPopularityHisto( itemInfo[i].getPopularity() / maxPopularity, false, 10, 100));
            } else {
                artistGrid.setWidget(i, 0, label);
            }
        }

        VerticalPanel w;
        if (!getArtistOnClick) {
            Grid titleWidget = new Grid(1, 2);
            titleWidget.setWidget(0, 0, new HTML("<h2>" + title + "</h2>"));
            Label l = new Label(" Cloud");
            l.setStyleName("tinyInfo");
            l.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent ce) {
                    TagDisplayLib.showTagCloud(title, itemInfo, TagDisplayLib.ORDER.SHUFFLE, cdm);
                }
            });
            titleWidget.setWidget(0, 1, l);
            w = WebLib.createSection(titleWidget, artistGrid);
        } else {
            w = WebLib.createSection(title, artistGrid);
        }
        w.setStyleName("infoList");
        if (displayPopularity) {
            w.setWidth("325px");
        } else {
            w.setWidth("200px");
        }
        return w;
    }

    private class TimerWithArtist extends Timer {

        private String artistID;
        private boolean refresh;

        public TimerWithArtist(String artistID, boolean refresh) {
            super();
            this.artistID=artistID;
            this.refresh=refresh;
        }

        @Override
        public void run() {
            History.newItem("artist:"+artistID);
        }
    }

    private class ItemInfoClickHandler implements ClickHandler {

        private ItemInfo info;
        private boolean getArtistOnClick;

        ItemInfoClickHandler(ItemInfo info, boolean getArtistOnClick) {
            this.info = info;
            this.getArtistOnClick = getArtistOnClick;
        }

        @Override
        public void onClick(ClickEvent ce) {
            if (getArtistOnClick) {
                History.newItem("artist:"+info.getId());
            } else {
                History.newItem("tag:"+info.getId());
            }
        }
    }

    private class ArtistCloudArtistListWidget extends ArtistListWidget {

        private ArtistCompact currArtist;

        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArrayList<ScoredC<ArtistCompact>> aC, ArtistCompact currArtist) {

            super(musicServer, cdm, aC, cdm.isLoggedIn(), true);
            this.currArtist = currArtist;
        }
        
        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aC, ArtistCompact currArtist) {

            super(musicServer, cdm, aC, cdm.isLoggedIn(), true);
            this.currArtist = currArtist;
        }

        @Override
        public void openWhyPopup(SwapableTxtButton why) {
            why.showLoad();
            TagDisplayLib.invokeGetCommonTags(currArtist.getId(), why.getId(),
                    musicServer, cdm, new CommonTagsAsyncCallback(why, "Common tags between "+currArtist.getName()+" and "+why.getName(), cdm) {});
        }

        @Override
        public void openDiffPopup(DiffButton diff) {
            if (diff.getId().equals(currArtist.getId())) {
                diff.displayIdenticalArtistMsg();
            } else {
                TagDisplayLib.showDifferenceCloud("Difference cloud between "+currArtist.getName()+" and "+diff.getName(),
                    currArtist.getDistinctiveTags(), diff.getDistinctiveTags(), cdm);
            }
        }
    }
    
    private class TagCloudArtistListWidget extends ArtistListWidget {

        private ArtistCompact[] aC;

        public TagCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray) {
            
            super(musicServer, cdm, aDArray, cdm.isLoggedIn(), false);
            this.aC = aDArray;
        }

        @Override
        public void openWhyPopup(SwapableTxtButton why) {

            for (ArtistCompact t : aC) {
                if (t.getId().equals(why.getId())) {
                    TagDisplayLib.showTagCloud(t.getName()+"'s tag cloud",
                        t.getDistinctiveTags(), TagDisplayLib.ORDER.SHUFFLE, cdm);
                    break;
                }
            }
        }

        @Override
        public void openDiffPopup(DiffButton diff) {
            Window.alert("Not implemented");
        }
        
    }

    private abstract class ScrollWidget extends Composite {

        protected ScrollItem[] items;

        private final int NBR_ITEM_ON_PREVIEW=12;
        private int NBR_ITEM_PER_LINE=3;

        protected int maxImgHeight = 0;
        protected int maxImgWidth = 0;

        protected Grid mainPanel = new Grid(2,1);
        protected Grid topPanel = new Grid(1,3);

        protected Panel currPreview;
        protected Panel nextPreview;

        protected int currIndex = 0; // index of the first preview item we're showing
        /**
         * when we've seen the last item, start over right away (=true) or display
         * empty elements (=false)
         */
        protected boolean wrapAround = false;

        abstract protected void triggerAction(int index);
        abstract protected String getSectionName();

        protected Widget init() {

            // If number of elements smaller than the available grid size, don't warp around
            if (items.length<=NBR_ITEM_ON_PREVIEW) {
                wrapAround=false;
            }

            if (Window.getClientWidth()>1024) {
                NBR_ITEM_PER_LINE=4;
            }

            topPanel.addStyleName("center");
            topPanel.setWidth("100%");
            topPanel.setCellPadding(4);

            Image prev = new Image("Prev_Button.jpg");
            prev.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    setPreviewPanel(getNextElements(-NBR_ITEM_ON_PREVIEW));
                }
            });

            Image next = new Image("Next_Button.jpg");
            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    setPreviewPanel(getNextElements(NBR_ITEM_ON_PREVIEW));
                }
            });

            if (items.length>NBR_ITEM_ON_PREVIEW) {
                topPanel.setWidget(0, 0, prev);
                topPanel.setWidget(0, 2, next);
            }
            topPanel.setWidget(0, 1, new Label(items.length+" "+getSectionName()));

            for (int j=0; j<3; j++) {
                topPanel.getCellFormatter().setAlignment(0, j, HorizontalPanel.ALIGN_CENTER, VerticalPanel.ALIGN_MIDDLE);
            }

            if (items.length>0) {
                setPreviewPanel(getNextElements(NBR_ITEM_ON_PREVIEW));
                mainPanel.setWidget(0, 0, topPanel);
            } else {
                mainPanel.setWidget(1, 0, new Label("No "+getSectionName()));
            }
            mainPanel.setWidth("100%");

            return mainPanel;
        }

        /**
         * Returns the n next elements
         * @param n number of elements to return
         * @return n next scrollitems to return
         */
        protected ArrayList<ScrollItem> getNextElements(int n) {
            // If we want previous elements
            if (n<0) {
                currIndex+=(2*n); // which will be a substraction
                if (currIndex<0) {
                    currIndex=items.length+currIndex;
                }
            }

            n = Math.abs(n);
            ArrayList<ScrollItem> sI = new ArrayList<ScrollItem>();
            for (int i=0; i<n; i++) {
                sI.add(new ScrollItem(items[currIndex].title,
                        items[currIndex].thumb, currIndex));
                if (++currIndex>=items.length) {
                    currIndex=0;
                    if (!wrapAround) {
                        break;
                    }
                }
            }
            return sI;
        }

        private void setPreviewPanel(ArrayList<ScrollItem> sI) {
            nextPreview = new VerticalPanel();
            ArrayList<HorizontalPanel> topPreviewArray = new ArrayList<HorizontalPanel>();
            HorizontalPanel topPreview=null;

            int index=0;
            for (ScrollItem i : sI) {
                if (topPreview==null || ++index>=NBR_ITEM_PER_LINE) {
                    if (topPreview!=null) {
                        topPreviewArray.add(topPreview);
                        topPreview=null;
                    }

                    index = 0;
                    topPreview = new HorizontalPanel();
                    topPreview.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
                    topPreview.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
                    topPreview.setWidth("100%");
                    topPreview.setSpacing(8);
                }

                Grid g = new Grid(1, 1);
                g.setSize(maxImgWidth+"px", maxImgHeight+"px");
                g.getCellFormatter().getElement(0, 0).setAttribute("valign", "middle");
                g.getCellFormatter().getElement(0, 0).setAttribute("align", "center");
                g.setTitle(i.title);

                NEffectPanel theEffectPanel = new NEffectPanel();
                Fade f = new Fade();
                f.getProperties().setStartOpacity(0);
                f.getProperties().setEndOpacity(100);
                theEffectPanel.addEffect(f);

                Image img = new Image(i.thumb);
                //img.setTitle(i.title);
                theEffectPanel.add(img);
                img.setVisible(false);
                img.addLoadListener(new LoadListenerPanelContainer(theEffectPanel));
                img.addClickHandler(new IndexClickHandler(i.index));

                // Crop if necessary
                if (maxImgHeight>0 && maxImgWidth>0) {
                    img.setVisibleRect(0, 0, maxImgWidth, maxImgHeight);
                }

                g.setWidget(0, 0, theEffectPanel);
                topPreview.add(g);
            }

            if (topPreview!=null) {

                while (index<NBR_ITEM_PER_LINE) {
                    topPreview.add(new Label(""));
                    index++;
                }

                topPreviewArray.add(topPreview);
                topPreview=null;
            }

            nextPreview = new VerticalPanel();
            for (HorizontalPanel p : topPreviewArray) {
                nextPreview.add(p);
            }
            mainPanel.setWidget(1, 0, nextPreview);
        }

        protected class IndexClickHandler implements ClickHandler {
            
            protected int index;

            public IndexClickHandler(int index) {
                super();
                this.index=index;
            }

            public void onClick(ClickEvent ce) {
                triggerAction(index);
            }
        }

        private class LoadListenerPanelContainer implements LoadListener {

            private NEffectPanel theEffectPanel;

            /**
             * @param w widget we want the effect applied to
             */
            public LoadListenerPanelContainer(NEffectPanel theEffectPanel) {
                super();
                this.theEffectPanel=theEffectPanel;
            }

            public void onError(Widget arg0) {
            }

            public void onLoad(Widget arg0) {
                theEffectPanel.playEffects();
            }
        }

        protected class ScrollItem {
            public String title;
            public String thumb;
            public int index;

            public ScrollItem(String title, String thumb, int index) {
                this.title=title;
                this.thumb=thumb;
                this.index=index;
            }
        }

    }

    private class ImageScrollWidget extends ScrollWidget {

        private ArtistPhoto[] aP;


        public ImageScrollWidget(ArtistPhoto[] aPArray) {
            this.aP = aPArray;
            ArrayList<ScrollItem> sIList = new ArrayList<ScrollItem>();
            int i = 0;
            for (ArtistPhoto a : aPArray) {
                if (a != null) {
                    sIList.add(new ScrollItem(a.getTitle(),
                        a.getSmallImageUrl(), i++));
                }
            }
            items = sIList.toArray(new ScrollItem[0]);

            maxImgHeight = 130;
            maxImgWidth = 130;

            initWidget(init());
        }

        protected String getSectionName() {
            return "photos";
        }

        @Override
        protected void triggerAction(int index) {
            HTML html = new HTML(aP[index].getRichHtmlWrapper());
            //Popup.showPopup(html,"WebMusicExplaura :: Flickr Photo");
            //Popup.showRoundedPopup(html, "WebMusicExplaura :: Flickr Photo");
            Popup.showRoundedPopup(html, aP[index].getTitle());
        }

    }

    private class VideoScrollWidget extends ScrollWidget {

        private ArtistVideo[] aV;

        public VideoScrollWidget(ArtistVideo[] aVArray) {
            this.aV = aVArray;
            ArrayList<ScrollItem> sIList = new ArrayList<ScrollItem>();
            int i = 0;
            for (ArtistVideo a : aVArray) {
                if (a != null) {
                    sIList.add(new ScrollItem(a.getTitle(),
                        a.getThumbnail(), i++));
                }
            }
            items = sIList.toArray(new ScrollItem[0]);

            maxImgHeight = 97;
            maxImgWidth = 130;

            initWidget(init());
        }

        protected String getSectionName() {
            return "videos";
        }

        protected void triggerAction(int index) {
            HTML html = new HTML(getEmbeddedVideo(aV[index], true));
            //Popup.showPopup(html,"WebMusicExplaura :: YouTube Video");
            //Popup.showRoundedPopup(html, "WebMusicExplaura :: YouTube Video");
            Popup.showRoundedPopup(html, aV[index].getTitle());
        }

    }

    private class AlbumScrollWidget extends ScrollWidget {

        private AlbumDetails[] aD;

        public AlbumScrollWidget(AlbumDetails[] aDArray) {
            this.aD = aDArray;
            ArrayList<ScrollItem> sIList = new ArrayList<ScrollItem>();
            int i = 0;
            for (AlbumDetails a : aDArray) {
                if (a != null) {
                    sIList.add(new ScrollItem(a.getTitle(),
                        a.getAlbumArt(), i++));
                }
            }
            items = sIList.toArray(new ScrollItem[0]);
            
            maxImgHeight = 130;
            maxImgWidth = 130;
            
            initWidget(init());
        }

        protected void triggerAction(int index) {
            Window.open(aD[index].getAmazonLink(), "Window1", "");
        }

        protected String getSectionName() {
            return "albums";
        }

    }
    
    private class PopularitySelectAD extends PopularitySelect {

        private ArtistDetails aD;

        public PopularitySelectAD(ArtistDetails aD) {
            super(cdm.getCurrPopularity());
            this.aD = aD;
        }

        @Override
        public void onSelectionChange(String newPopularity) {
            cdm.setCurrPopularity(newPopularity);
            cdm.displayWaitIconUpdatableWidgets();
            invokeGetArtistInfo(aD.getId());
        }

        /**
         * Fetch new similar artists. Used when similarity type is updated
         * @param artistID
         * @param refresh
         */
        private void invokeGetArtistInfo(String artistID) {

            if (artistID.startsWith("artist:")) {
                artistID = artistID.replaceAll("artist:", "");
            }

            AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback = new AsyncCallback<ArrayList<ScoredC<ArtistCompact>>>() {

                public void onSuccess(ArrayList<ScoredC<ArtistCompact>> aC) {
                    // do some UI stuff to show success
                    if (aC != null) {
                        cdm.updateUpdatableWidgets(aC);
                    } else {
                        Window.alert("An error occured while fetching the new recommendations.");
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert("An error occured while fetching the new recommendations.");
                }
            };

            try {
                musicServer.getSimilarArtists(artistID, cdm.getCurrSimTypeName(), cdm.getCurrPopularity(), callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }
    };
}