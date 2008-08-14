/*
 * SimpleSearchWidget.java
 *
 * Created on March 7, 2007, 5:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.swidget;

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
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.items.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.items.ArtistVideo;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.searchTypes;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.adamtacy.client.ui.EffectPanel;
import org.adamtacy.client.ui.effects.impl.Fade;

/**
 *
 * @author plamere
 */
public class SimpleSearchSwidget extends Swidget implements HistoryListener {

    private Widget curResult;
    private String curResultToken = "";

    private DockPanel mainPanel;
    private FlowPanel searchBoxContainerPanel;
    private Label message;
    private boolean debug;
    private SearchWidget search;
    private Image icon;

    private String curToken = null;

    // Widgets that contain listeners that need to be removed to prevent leaks
    private ArtistListWidget leftRecList;
    private ArtistListWidget leftSimList;
    private ArtistListWidget leftRelList;
    private StarRatingWidget artistStar;
    private TagInputWidget tagInputWidget;

    public SimpleSearchSwidget(ClientDataManager cdm) {
        super("Simple Search", cdm);
        try {
            History.addHistoryListener(this);
            initWidget(getWidget());
            showResults(History.getToken());
        } catch (Exception e) {
            Window.alert("Server problem. Please try again later.");
        }
    }

    /** Creates a new instance of SimpleSearchWidget */
    public Widget getWidget() {

        searchBoxContainerPanel = new FlowPanel();
        
        search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel);
        search.updateSuggestBox(Oracles.ARTIST);

        message = new Label();
        //message.setWidth("100%");
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

        topPanel.add(search);
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

    private void debugMessage(String msg) {
        if (debug) {
            showMessage(msg);
        }
    }

    private void setResults(String historyName, Widget result) {
        if (curResult == result || curResultToken.equals(historyName)) {
            return;
        } 

        if (!History.getToken().equals(historyName)) {
            History.newItem(historyName);
            curToken = historyName;
        }
        if (curResult != null) {
            mainPanel.remove(curResult);
            curResult = null;
            curResultToken = "";
        }
        if (result != null) {
            cdm.setCurrSearchWidgetToken(historyName);
            mainPanel.add(result, DockPanel.CENTER);
            curResult = result;
            curResultToken = historyName;
        }
    }

    private void clearResults() {
        setResults("home", null);
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("artist:");
        l.add("tag:");
        l.add("artistSearch:");
        l.add("artistSearchByTag:");
        l.add("tagSearch:");
        l.add("searchHome:");
        return l;
    }

    protected void initMenuItem() {
        menuItem = new MenuItem("Search",new ClickListener() {

                public void onClick(Widget arg0) {
                    History.newItem(cdm.getCurrSearchWidgetToken());
                }
            },false,0);
    }

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
    }

    private void showResults(String resultName) {

        // Reset current artistID. Will be updated in invokeGetArtistInfo
        cdm.setCurrArtistInfo("", "");

        // Clear all listeners
        doRemoveListeners();

        //  resultName = URL.decodeComponent(resultName);
        if (resultName.startsWith("artist:")) {
            search.updateSuggestBox(Oracles.ARTIST);
            invokeGetArtistInfo(resultName, false);
        } else if (resultName.startsWith("tag:")) {
            search.updateSuggestBox(Oracles.TAG);
            invokeGetTagInfo(resultName, false);
        } else if (resultName.startsWith("artistSearch:")) {
            search.updateSuggestBox(Oracles.ARTIST);
            String query = resultName.replaceAll("artistSearch:", "");
            invokeArtistSearchService(query, false, 0);
        } else if (resultName.startsWith("artistSearchByTag:")) {
            search.updateSuggestBox(Oracles.TAG);
            String query = resultName.replaceAll("artistSearchByTag:", "");
            invokeArtistSearchService(query, true, 0);
        } else if (resultName.startsWith("tagSearch:")) {
            search.updateSuggestBox(Oracles.TAG);
            String query = resultName.replaceAll("tagSearch:", "");
            invokeTagSearchService(query, 0);
        } else if (resultName.startsWith("searchHome:")) {
            cdm.setCurrSearchWidgetToken("searchHome:");
            setResults("searchHome", null);
        }
    }

    public void onHistoryChanged(String historyToken) {
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
                    ItemInfo[] results = sr.getItemResults();
                    if (results.length == 0) {
                        showError("No Match for " + sr.getQuery());
                        clearResults();
                    } else if (results.length == 1) {
                        ItemInfo ar = results[0];
                        invokeGetTagInfo(ar.getId(), false);
                    } else {
                        showMessage("Found " + sr.getItemResults().length + " matches");
                        setResults(sr.toString(), getItemInfoList("Pick one: ", sr.getItemResults(), null, false, cdm.getTagOracle()));
                    }
                } else {
                    if (sr == null) {
                        showError("Error. Resultset is null. There were probably no tags foud.s");
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

    private void invokeArtistSearchService(String searchText, boolean byTag, int page) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                SearchResults sr = (SearchResults) result;
                if (sr != null && sr.isOK()) {
                    ItemInfo[] results = sr.getItemResults();
                    if (results.length == 0) {
                        showError("No Match for " + sr.getQuery());
                        clearResults();
                    } else if (results.length == 1) {
                        ItemInfo ar = results[0];
                        invokeGetArtistInfo(ar.getId(), false);
                    } else {
                        showMessage("Found " + sr.getItemResults().length + " matches");
                        Widget searchResults = getItemInfoList("Pick one: ", sr.getItemResults(), null, true, cdm.getArtistOracle());
                        searchResults.setStyleName("searchResults");
                        searchResults.setWidth("300px");
                        setResults(sr.toString(), searchResults);
                    }
                } else {
                    if (sr == null) {
                        showError("Error. Resultset is null. (256)");
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
            if (byTag) {
                musicServer.artistSearchByTag(searchText, 100, callback);
            } else {
                musicServer.artistSearch(searchText, 100, callback);
            }
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }

    }

    private void failureAction(Throwable caught) {
        Window.alert("Whoops! It looks like the server is down. Time to get a spot of tea.");
        showMessage(caught.getMessage());
    }

    private void debug(String msg) {
        Window.alert(msg);
    }

    private void invokeGetArtistInfo(String artistID, boolean refresh) {

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
                    // do some UI stuff to show success
                    ArtistDetails artistDetails = (ArtistDetails) result;
                    if (artistDetails != null && artistDetails.isOK()) {
                        cdm.setCurrArtistInfo(artistDetails.getId(), artistDetails.getName());
                        Widget artistPanel = createArtistPanel("Artists", artistDetails);
                        search.setText(artistDetails.getName(), searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);
                        search.updateSuggestBox(Oracles.ARTIST);
                        setResults("artist:" + artistDetails.getId(), artistPanel);
                        clearMessage();
                    } else {
                        if (artistDetails == null) {
                            showError("Sorry. The details for the artist don't seem to be in our database.");
                            clearResults();
                        } else {
                            showError("Whooops " + artistDetails.getStatus());
                            clearResults();
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    failureAction(caught);
                }
            };

            showMessage("Getting info for artist", WebLib.ICON_WAIT);

            try {
                musicServer.getArtistDetails(artistID, refresh, cdm.getCurrSimTypeName(), callback);
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
                    Widget tagPanel = createTagPanel("Tags", tagDetails);
                    search.setText(tagDetails.getName(), searchTypes.SEARCH_FOR_TAG_BY_TAG);
                    search.updateSuggestBox(Oracles.TAG);
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

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        try {
            musicServer.getTagDetails(tagID.substring(tagID.indexOf(":")+1), refresh, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private final void addCompactArtistToOracle(ArtistCompact[] aCArray) {
        for (ArtistCompact aC : aCArray) {
            cdm.getArtistOracle().add(aC.getName());
        }
    }

    private Widget createArtistPanel(String title, ArtistDetails artistDetails) {
        VerticalPanel main = new VerticalPanel();
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        main.add(getBioWidget(artistDetails));
        main.add(WebLib.createSection("Videos", new VideoScrollWidget(artistDetails.getVideos())));
        main.add(WebLib.createSection("Photos", new ImageScrollWidget(artistDetails.getPhotos())));
        main.add(WebLib.createSection("Albums", new AlbumScrollWidget(artistDetails.getAlbums())));
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        main.add(getEventsWidget(artistDetails));
        main.setStyleName("center");

        String id = artistDetails.getId();
        VerticalPanel left = new VerticalPanel();
        left.setSpacing(4);
        left.setWidth("300px");

        if (artistDetails.getSimilarArtists().length > 0) {
            ArtistCompact[] aCArray = artistDetails.getSimilarArtists();
            addCompactArtistToOracle(aCArray);

            if (leftSimList != null) {
                leftSimList.doRemoveListeners();
            }
            leftSimList = new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, cdm.getCurrArtistID(), artistDetails.getName());
            left.add(
                    new Updatable(new HTML("<H2>Similar artists</H2>"), leftSimList, cdm, id) {

                        public void update(ArtistDetails aD) {
                            ArtistCompact[] aCArray = aD.getSimilarArtists();
                            addCompactArtistToOracle(aCArray);
                            leftSimList.doRemoveListeners();
                            leftSimList = new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, cdm.getCurrArtistID(), cdm.getCurrArtistName());
                            setNewContent(new HTML("<H2>Similar artists</H2>"), leftSimList);
                        }
                    }
           );
        }
        
        if (artistDetails.getRecommendedArtists().length > 0) {
            ArtistCompact[] aCArray = artistDetails.getRecommendedArtists();
            addCompactArtistToOracle(aCArray);
            
            if (leftRecList != null) {
                leftRecList.doRemoveListeners();
            }
            leftRecList = new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, cdm.getCurrArtistID(), cdm.getCurrArtistName());
            left.add(WebLib.createSection("Recommendations", leftRecList));
        }
        if (artistDetails.getCollaborations().length > 0) {
            ArtistCompact[] aCArray = artistDetails.getCollaborations();
            addCompactArtistToOracle(aCArray);

            if (leftRelList != null) {
                leftRelList.doRemoveListeners();
            }
            leftRelList = new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, cdm.getCurrArtistID(), cdm.getCurrArtistName());
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

    private Widget createTagPanel(String title, TagDetails tagDetails) {
        DockPanel main = new DockPanel();
        main.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        main.setWidth("100%");

        VerticalPanel left = new VerticalPanel();
        left.setWidth("150px");
        left.setStyleName("left");
        Widget w = getItemInfoList(tagDetails.getName() + " artists", tagDetails.getRepresentativeArtists(), null, true, cdm.getTagOracle());
        left.add(w);
        main.add(left, DockPanel.WEST);

        VerticalPanel v = new VerticalPanel();
        v.add(getTagWidget(tagDetails));
        v.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        v.add(WebLib.createSection("Videos", new VideoScrollWidget(tagDetails.getVideos())));
        v.add(WebLib.createSection("Photos", new ImageScrollWidget(tagDetails.getPhotos())));
        v.setStyleName("center");
        main.add(v, DockPanel.CENTER);

        VerticalPanel right = new VerticalPanel();
        w = getItemInfoList("Similar tags", tagDetails.getSimilarTags(), tagDetails.getId(), false, cdm.getTagOracle());
        w.setStyleName("right");
        right.add(w);
        main.add(right, DockPanel.EAST);

        main.setStyleName("resultpanel");
        return main;
    }

    Widget getBioWidget(ArtistDetails artistDetails) {
        HTML html = new HTML();
        html.setHTML(artistDetails.getBestArtistImageAsHTML() + artistDetails.getBiographySummary());
        html.setStyleName("bio");

        artistStar = new StarRatingWidget(musicServer, cdm, artistDetails.getId(), StarRatingWidget.Size.MEDIUM);
        cdm.getLoginListenerManager().addListener(artistStar);

        HorizontalPanel hP = new HorizontalPanel();
        Widget spotify = WebLib.getSpotifyListenWidget(artistDetails, WebLib.PLAY_ICON_SIZE.MEDIUM, 
                musicServer, cdm.isLoggedIn(), new DualDataEmbededClickListener<String, ClientDataManager>(artistDetails.getId(), cdm) {

            public void onClick(Widget arg0) {
                sndData.getPlayedListenerManager().triggerOnPlay(data);
            }
        });
        if (spotify!=null) {
            spotify.addStyleName("pointer");
            hP.add(spotify);
        }
        SteeringWheelWidget steerButton = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.BIG, artistDetails.toArtistCompact(), cdm.getSharedSteeringMenu());
        hP.add(steerButton);

        return createMainSection(artistDetails.getName(), html,
                hP,
                artistDetails.getDistinctiveTags(), artistStar);
    }

    Widget getTagWidget(TagDetails tagDetails) {
        HTML html = new HTML();
        html.setHTML(getBestTagImageAsHTML(tagDetails) + tagDetails.getDescription());
        html.setStyleName("bio");
        return createMainSection(tagDetails.getName(), html, WebLib.getListenWidget(tagDetails));
    }

    Widget getMoreInfoWidget(ArtistDetails artistDetails) {
        String text = "";
        Map urls = artistDetails.getUrls();

        if (urls != null && urls.size() > 0) {
            Grid grid = new Grid(urls.size(), 1);
            int index = 0;
            for (Iterator i = urls.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String url = (String) urls.get(key);
                HTML html = new HTML(WebLib.createAnchor(key, url));
                grid.setWidget(index++, 0, html);
            }
            return WebLib.createSection("More info", grid);
        } else {
            return new Label("");
        }
    }

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

    Widget getPopularityPanel(ArtistDetails artistDetails) {

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(WebLib.getPopularityWidget("The Beatles",1,true,null));
        vPanel.add(WebLib.getPopularityWidget(artistDetails.getName(),
                artistDetails.getNormPopularity(),true,null));

        return WebLib.createSection("Popularity", vPanel);
    }

    Widget getTastAuraMeterWidget(String name, double normPopularity, boolean log) {

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

    Widget createMainSectionOld(String title, Widget widget) {
        Panel panel = new VerticalPanel();
        panel.add(new HTML("<h1>" + title + "</h1>"));
        panel.add(widget);
        return panel;
    }

    Widget createMainSection(String title, Widget widget, Widget adornment) {
        return createMainSection(title, widget, adornment, null, null);
    }

    Widget createMainSection(String title, Widget widget, Widget adornment, ItemInfo[] tagCloud, StarRatingWidget starWidget) {
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
            tagInputWidget = new TagInputWidget(musicServer, cdm, "artist", cdm.getCurrArtistID());
            cdm.getLoginListenerManager().addListener(tagInputWidget);
            panel.add(tagInputWidget);
            
            Panel p = TagDisplayLib.getTagsInPanel(tagCloud, cdm);
            p.addStyleName("tagCloudMargin");
            panel.add(p);
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

    private ArtistVideo selectRandomVideo(ArtistVideo[] av) {
        if (av.length > 0) {
            int index = Random.nextInt(av.length);
            return av[index];
        } else {
            return null;
        }
    }

    private String getEmbeddedVideo(ArtistVideo video, boolean autoplay) {
        String url = video.getUrl();
        String autostring = autoplay ? "&autoplay=1" : "";
        url = url.replaceAll("\\?v=", "/v/");
        String title = "<span style=\"text-align:center\">" + video.getTitle() + "</span><br/>";
        String obj = "<object width=\"425\" height=\"350\"><param name=\"movie\" value=\"" + url + "\"></param><param name=\"wmode\" value=\"transparent\"></param>" + "<embed src=\"" + url + autostring + "\" type=\"application/x-shockwave-flash\"" + " wmode=\"transparent\" width=\"425\" height=\"350\"></embed></object>";
        return title + obj;
    }

    private Widget getItemInfoList(final String title, final ItemInfo[] itemInfo, String highlightID, boolean getArtistOnClick, UniqueStore oracle) {

        Grid artistGrid = new Grid(itemInfo.length, 1);
        for (int i = 0; i < itemInfo.length; i++) {

            if (oracle != null) {
                oracle.add(itemInfo[i].getItemName());
            }

            Label label = new Label(itemInfo[i].getItemName());
            label.addClickListener(new ItemInfoClickListener(itemInfo[i], getArtistOnClick));
            label.setTitle("Score: " + itemInfo[i].getScore() + " Popularity:" + itemInfo[i].getPopularity());
            if (highlightID != null && highlightID.equals(itemInfo[i].getId())) {
                label.setStyleName("itemInfoHighlight");
            } else {
                label.setStyleName("itemInfo");
            }
            artistGrid.setWidget(i, 0, label);
        }

        Widget w;
        if (!getArtistOnClick) {
            Grid titleWidget = new Grid(1, 2);
            titleWidget.setWidget(0, 0, new HTML("<h2>" + title + "</h2>"));
            Label l = new Label(" Cloud");
            l.setStyleName("tinyInfo");
            l.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    TagDisplayLib.showTagCloud(title, itemInfo, cdm);
                }
            });
            titleWidget.setWidget(0, 1, l);
            w = WebLib.createSection(titleWidget, artistGrid);
        } else {
            w = WebLib.createSection(title, artistGrid);
        }
        w.setStyleName("infoList");
        w.setWidth("200px");
        return w;
    }
/*
    private Widget getItemInfoList2(ItemInfo[] itemInfo,
            String highlightID, boolean getArtistOnClick, MultiWordSuggestOracle oracle) {
        Grid artistGrid = new Grid(itemInfo.length, 2);
        for (int i = 0; i < itemInfo.length; i++) {

            // Add name to oracle as we only populated it with most popular
            if (oracle!=null) {
                oracle.add(itemInfo[i].getItemName());
            }

            Label label = new Label(itemInfo[i].getItemName());
            label.addClickListener(new ItemInfoClickListener(itemInfo[i], getArtistOnClick));
            label.setTitle("Score: " + itemInfo[i].getScore() + " Popularity:" + itemInfo[i].getPopularity());
            if (highlightID != null && highlightID.equals(itemInfo[i].getId())) {
                label.setStyleName("itemInfoHighlight");
            } else {
                label.setStyleName("itemInfo");
            }
            artistGrid.setWidget(i, 0, label);
            Label why = new Label("why?");
            why.setStyleName("tinyInfo");

            why.addClickListener(new CommonTagsClickListener(highlightID, itemInfo[i].getId()));
            artistGrid.setWidget(i, 1, why);
        }
        //Widget w = createSection(title, artistGrid);
        //w.setStyleName("infoList");
        //w.setWidth("200px");
        //return w;
        return artistGrid;
    }
*/
    class TimerWithArtist extends Timer {

        private String artistID;
        private boolean refresh;

        public TimerWithArtist(String artistID, boolean refresh) {
            super();
            this.artistID=artistID;
            this.refresh=refresh;
        }

        @Override
        public void run() {
            invokeGetArtistInfo(artistID, refresh);
        }

    }

    class ItemInfoClickListener implements ClickListener {

        private ItemInfo info;
        private boolean getArtistOnClick;

        ItemInfoClickListener(ItemInfo info, boolean getArtistOnClick) {
            this.info = info;
            this.getArtistOnClick = getArtistOnClick;
        }

        public void onClick(Widget sender) {
            if (getArtistOnClick) {
                invokeGetArtistInfo(info.getId(), false);
            } else {
                invokeGetTagInfo(info.getId(), false);
            }
        }
    }

    class VideoClickListener implements ClickListener {

        private ArtistVideo video;

        VideoClickListener(ArtistVideo video) {
            this.video = video;
        }

        public void onClick(Widget sender) {
            HTML html = new HTML(getEmbeddedVideo(video, true));
            Popup.showPopup(html,"WebMusicExplaura :: YouTube Video");
        }
    }

    class PhotoClickListener implements ClickListener {

        private ArtistPhoto photo;

        PhotoClickListener(ArtistPhoto photo) {
            this.photo = photo;
        }

        public void onClick(Widget sender) {
            HTML html = new HTML(photo.getRichHtmlWrapper());
            Popup.showPopup(html,"WebMusicExplaura :: Flickr Photo");
        }
    }

    public class ArtistCloudArtistListWidget extends ArtistListWidget {

        private String currArtistId;
        private String currArtistName;

        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, ArtistCompact[] aDArray, String currArtistId, String currArtistName) {

            super(musicServer, cdm, aDArray, cdm.isLoggedIn());
            this.currArtistId = currArtistId;
            this.currArtistName = currArtistName;
        }

        public void openWhyPopup(WhyButton why) {
            why.showLoad();
            TagDisplayLib.invokeGetCommonTags(currArtistId, why.getId(),
                    musicServer, cdm, new CommonTagsAsyncCallback(why, "Common tags between "+currArtistName+" and "+why.getName(), cdm) {});
        }
    }

    abstract class ScrollWidget extends Composite {

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
            prev.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    setPreviewPanel(getNextElements(-NBR_ITEM_ON_PREVIEW));
                }
            });

            Image next = new Image("Next_Button.jpg");
            next.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
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

            n=Math.abs(n);
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

                EffectPanel theEffectPanel = new EffectPanel();
                Fade f = new Fade();
                f.getProperties().setStartOpacity(0);
                f.getProperties().setEndOpacity(100);
                theEffectPanel.addEffect(f);

                Image img = new Image(i.thumb);
                //img.setTitle(i.title);
                theEffectPanel.add(img);
                img.setVisible(false);
                img.addLoadListener(new LoadListenerPanelContainer(theEffectPanel));
                img.addClickListener(new IndexClickListener(i.index));

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

        protected class IndexClickListener implements ClickListener {
            protected int index;

            public IndexClickListener(int index) {
                super();
                this.index=index;
            }

            public void onClick(Widget arg0) {
                        triggerAction(index);
            }
        }


        private class LoadListenerPanelContainer implements LoadListener {

            private EffectPanel theEffectPanel;

            /**
             * @param w widget we want the effect applied to
             */
            public LoadListenerPanelContainer(EffectPanel theEffectPanel) {
                super();
                this.theEffectPanel=theEffectPanel;
            }

            public void onError(Widget arg0) {
            }

            public void onLoad(Widget arg0) {
                theEffectPanel.startEffects();
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

    class ImageScrollWidget extends ScrollWidget {

        private ArtistPhoto[] aP;


        public ImageScrollWidget(ArtistPhoto[] aP) {
            this.aP=aP;

            maxImgHeight = 130;
            maxImgWidth = 130;

            items = new ScrollItem[aP.length];
            for (int i=0; i<aP.length; i++) {
                items[i] = new ScrollItem(aP[i].getTitle(),
                        aP[i].getSmallImageUrl(), i);
            }

            initWidget(init());
        }

        protected String getSectionName() {
            return "photos";
        }

        @Override
        protected void triggerAction(int index) {
            HTML html = new HTML(aP[index].getRichHtmlWrapper());
            Popup.showPopup(html,"WebMusicExplaura :: Flickr Photo");
        }

    }

    class VideoScrollWidget extends ScrollWidget {

        private ArtistVideo[] aV;

        public VideoScrollWidget(ArtistVideo[] aV) {
            this.aV=aV;
            items = new ScrollItem[aV.length];
            for (int i=0; i<aV.length; i++) {
                items[i] = new ScrollItem(aV[i].getTitle(),
                        aV[i].getThumbnail(), i);
            }

            maxImgHeight = 97;
            maxImgWidth = 130;

            initWidget(init());
        }

        protected String getSectionName() {
            return "videos";
        }

        protected void triggerAction(int index) {
            HTML html = new HTML(getEmbeddedVideo(aV[index], true));
            Popup.showPopup(html,"WebMusicExplaura :: YouTube Video");
        }

    }

    class AlbumScrollWidget extends ScrollWidget {

        private AlbumDetails[] aD;

        public AlbumScrollWidget(AlbumDetails[] aD) {
            this.aD=aD;

            maxImgHeight = 130;
            maxImgWidth = 130;

            items = new ScrollItem[aD.length];
            for (int i=0; i<aD.length; i++) {
                items[i] = new ScrollItem(aD[i].getTitle(),
                        aD[i].getAlbumArt(), i);
            }

            initWidget(init());
        }

        protected void triggerAction(int index) {
            Window.open(aD[index].getAmazonLink(), "Window1", "");
        }

        protected String getSectionName() {
            return "albums";
        }

    }

    public class SearchWidget extends AbstractSearchWidget {

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, Panel searchBoxContainerPanel) {

            super(musicServer, cdm, searchBoxContainerPanel);

            searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

            Panel searchType = new VerticalPanel();
            searchButtons = new SearchTypeRadioButton[3];
            searchButtons[0] = new SearchTypeRadioButton("searchType", "For Artist", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);
            searchButtons[1] = new SearchTypeRadioButton("searchType", "By Tag", searchTypes.SEARCH_FOR_ARTIST_BY_TAG);
            searchButtons[2] = new SearchTypeRadioButton("searchType", "For Tag", searchTypes.SEARCH_FOR_TAG_BY_TAG);

            searchButtons[0].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.ARTIST);
                }
            });
            searchButtons[1].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.TAG);
                }
            });
            searchButtons[2].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.TAG);
                }
            });

            updateSuggestBox(Oracles.ARTIST);
            setText("", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);

            for (int i = 0; i < searchButtons.length; i++) {
                searchType.add(searchButtons[i]);
                searchButtons[i].setStyleName("searchTypeButton");
            }
            searchType.setWidth("100%");
            searchType.setStyleName("searchPanel");

            HorizontalPanel searchPanel = new HorizontalPanel();
            searchPanel.setStyleName("searchPanel");
            searchPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            searchPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

            Button searchButton = new Button("Search", new ClickListener() {
                public void onClick(Widget sender) {
                    search();
                }
            });
            searchButton.addStyleName("main");
            searchButton.setTabIndex(2);

            VerticalPanel leftP = new VerticalPanel();
            leftP.setHeight("100%");
            leftP.setWidth("100%");
            leftP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            leftP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            leftP.add(searchBoxContainerPanel);
            leftP.add(searchButton);
            searchPanel.add(leftP);
            searchPanel.add(searchType);
            this.initWidget(searchPanel);
        }

        public void search() {
            if (cdm.getCurrSimTypeName() == null || cdm.getCurrSimTypeName().equals("")) {
                Window.alert("Error. Cannot search without the similarity types.");
            } else {
                String query = textBox.getText().toLowerCase();
                if (getSearchType() == searchTypes.SEARCH_FOR_TAG_BY_TAG) {
                    invokeTagSearchService(query, 0);
                } else {
                    invokeArtistSearchService(query, getSearchType() == searchTypes.SEARCH_FOR_ARTIST_BY_TAG, 0);
                }
            }
        }
    }

    private ItemInfo[] alphaSort(ItemInfo[] itemInfo) {

        ItemInfo[] ii = new ItemInfo[itemInfo.length];

        for (int i = 0; i < itemInfo.length; i++) {
            ii[i] = itemInfo[i];
        }

        Arrays.sort(ii, new Comparator() {

            public int compare(Object o1, Object o2) {
                ItemInfo i1 = (ItemInfo) o1;
                ItemInfo i2 = (ItemInfo) o2;
                return i1.getItemName().compareTo(i2.getItemName());
            }
        });
        return ii;
    }
}