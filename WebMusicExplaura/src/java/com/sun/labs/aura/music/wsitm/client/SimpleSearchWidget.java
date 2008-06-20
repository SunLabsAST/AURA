/*
 * SimpleSearchWidget.java
 *
 * Created on March 7, 2007, 5:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.items.AlbumDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ArtistEvent;
import com.sun.labs.aura.music.wsitm.client.items.ArtistVideo;
import asquare.gwt.tk.client.ui.SimpleHyperLink;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
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
public class SimpleSearchWidget extends Swidget implements HistoryListener {

    private Widget curResult;
    private DockPanel mainPanel;
    private FlowPanel searchBoxContainerPanel;
    private Label message;
    private boolean debug;
    private SearchWidget search;
    private Image icon;

    private static MultiWordSuggestOracle artistOracle;
    private static MultiWordSuggestOracle tagOracle;
    private Oracles currLoadedOracle;
    private Oracles fetchOracle;    // Oracle we are currently fetching

    private String curToken = null;

    public static enum Oracles {
        ARTIST,
        TAG
    }

    private static final String ICON_WAIT = "ajax-bar.gif";

    public SimpleSearchWidget(ClientDataManager cdm) {
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

        search = new SearchWidget();
        updateSuggestBox(Oracles.ARTIST);

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
        if (curResult == result) {
            return;
        }

        if (!History.getToken().equals(historyName)) {
            History.newItem(historyName);
            curToken = historyName; //History.getToken();
        }
        if (curResult != null) {
            mainPanel.remove(curResult);
            curResult = null;
        }
        if (result != null) {
            cdm.setCurrSearchWidgetToken(historyName);
            mainPanel.add(result, DockPanel.CENTER);
            curResult = result;
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


    private void showResults(String resultName) {

        // Reset current artistID. Will be updated in invokeGetArtistInfo
        cdm.setCurrArtistID("");

        //  resultName = URL.decodeComponent(resultName);
        if (resultName.startsWith("artist:")) {
            updateSuggestBox(Oracles.ARTIST);
            invokeGetArtistInfo(resultName, false);
        } else if (resultName.startsWith("tag:")) {
            updateSuggestBox(Oracles.TAG);
            invokeGetTagInfo(resultName, false);
        } else if (resultName.startsWith("artistSearch:")) {
            updateSuggestBox(Oracles.ARTIST);
            String query = resultName.replaceAll("artistSearch:", "");
            invokeArtistSearchService(query, false, 0);
        } else if (resultName.startsWith("artistSearchByTag:")) {
            updateSuggestBox(Oracles.TAG);
            String query = resultName.replaceAll("artistSearchByTag:", "");
            invokeArtistSearchService(query, true, 0);
        } else if (resultName.startsWith("tagSearch:")) {
            updateSuggestBox(Oracles.TAG);
            String query = resultName.replaceAll("tagSearch:", "");
            invokeTagSearchService(query, 0);
        } else if (resultName.startsWith("searchHome:")) {
            cdm.setCurrSearchWidgetToken("searchHome:");
            setResults("searchHome", null);
        }
    }

    public void onHistoryChanged(String historyToken) {
        if (!historyToken.equals(curToken)) {
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
                        setResults(sr.toString(), getItemInfoList("Pick one: ", sr.getItemResults(), null, false,tagOracle));
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

        showMessage("Searching for " + searchText,ICON_WAIT);

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

    private void invokeOracleFetchService(Oracles type) {

        AsyncCallbackWithType callback = new AsyncCallbackWithType(type) {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                List<String> callBackList = (List<String>) result;
                MultiWordSuggestOracle newOracle = new MultiWordSuggestOracle();
                newOracle.addAll(callBackList);
                swapSuggestBox(newOracle,this.type);
            }

            public void onFailure(Throwable caught) {
                failureAction(caught);
            }
        };

        searchBoxContainerPanel.clear();
        searchBoxContainerPanel.add(getLoadingBarWidget());

        try {
            if (type==Oracles.ARTIST) {
                musicServer.getArtistOracle(callback);
            } else {
                musicServer.getTagOracle(callback);
            }
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
                        Widget searchResults = getItemInfoList("Pick one: ", sr.getItemResults(), null, true, artistOracle);
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

        showMessage("Searching for " + searchText,ICON_WAIT);
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
            //
            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    // do some UI stuff to show success
                    ArtistDetails artistDetails = (ArtistDetails) result;
                    if (artistDetails != null && artistDetails.isOK()) {
                        Widget artistPanel = createArtistPanel("Artists", artistDetails);
                        search.setText(artistDetails.getName(), SearchResults.SEARCH_FOR_ARTIST_BY_ARTIST);
                        updateSuggestBox(Oracles.ARTIST);
                        setResults("artist:" + artistDetails.getId(), artistPanel);
                        cdm.setCurrArtistID(artistDetails.getId());
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

            showMessage("Getting info for artist", ICON_WAIT);

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
                // do some UI stuff to show success
                TagDetails tagDetails = (TagDetails) result;
                if (tagDetails != null && tagDetails.isOK()) {
                    Widget tagPanel = createTagPanel("Tags", tagDetails);
                    search.setText(tagDetails.getName(), SearchResults.SEARCH_FOR_TAG_BY_TAG);
                    updateSuggestBox(Oracles.TAG);
                    setResults(tagDetails.getId(), tagPanel);
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

        showMessage("Getting info for tag",ICON_WAIT);

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        try {
            musicServer.getTagDetails(tagID, refresh, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void invokeGetCommonTags(String artistID1, String artistID2) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                clearMessage();
                showTagCloud("Common tags", (ItemInfo[]) result);
            }

            public void onFailure(Throwable caught) {
                failureAction(caught);
            }
        };

        showMessage("Getting common tags",ICON_WAIT);
        try {
            musicServer.getCommonTags(artistID1, artistID2, 30, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private Widget createArtistPanel(String title, ArtistDetails artistDetails) {
        VerticalPanel main = new VerticalPanel();
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        main.add(getBioWidget(artistDetails));
        main.add(createSection("Videos", new VideoScrollWidget(artistDetails.getVideos())));
        main.add(createSection("Photos", new ImageScrollWidget(artistDetails.getPhotos())));
        main.add(createSection("Albums", new AlbumScrollWidget(artistDetails.getAlbums())));
        main.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        main.add(getEventsWidget(artistDetails));
        main.setStyleName("center");

        VerticalPanel right = new VerticalPanel();
        String id = artistDetails.getId();
        right.add(getItemInfoList("Distinctive Tags", artistDetails.getDistinctiveTags(), null, false, tagOracle));
        right.add(getItemInfoList("Frequent Tags", artistDetails.getFrequentTags(), null, false, tagOracle));
        right.add(getPopularityPanel(artistDetails));
        /*
        if (cdm.isLoggedIn()) {
            right.add(getTastAuraMeterPanel(artistDetails));
        }
         * */
        right.setStyleName("right");

        VerticalPanel left = new VerticalPanel();
        if (artistDetails.getSimilarArtists().length > 0) {
            left.add(
                    new Updatable(new HTML("<H2>"+cdm.getCurrSimTypeName()+"-omendations</H2>"),
                    getItemInfoList2(artistDetails.getSimilarArtists(), id, true, artistOracle), cdm, id) {

                        public void update(ArtistDetails aD) {
                            setNewContent(new HTML("<H2>"+cdm.getCurrSimTypeName()+"-omendations</H2>"),
                                    getItemInfoList2(aD.getSimilarArtists(), extraParam, true, artistOracle));
                        }
                    }
           );
        }
        if (artistDetails.getRecommendedArtists().length > 0) {
            left.add(getItemInfoList("Recommendations", artistDetails.getRecommendedArtists(), id, true, artistOracle));
        }
        if (artistDetails.getCollaborations().length > 0) {
            left.add(getItemInfoList("Related", artistDetails.getCollaborations(), id, true, artistOracle));
        }
        left.add(getMoreInfoWidget(artistDetails));
        left.setWidth("150px");
        left.setStyleName("left");

        DockPanel artistPanel = new DockPanel();
        artistPanel.add(main, DockPanel.CENTER);
        artistPanel.add(right, DockPanel.EAST);
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
        Widget w = getItemInfoList(tagDetails.getName() + " artists", tagDetails.getRepresentativeArtists(), null, true, tagOracle);
        left.add(w);
        main.add(left, DockPanel.WEST);

        VerticalPanel v = new VerticalPanel();
        v.add(getTagWidget(tagDetails));
        v.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        v.add(createSection("Videos", new VideoScrollWidget(tagDetails.getVideos())));
        v.add(createSection("Photos", new ImageScrollWidget(tagDetails.getPhotos())));
        v.setStyleName("center");
        main.add(v, DockPanel.CENTER);

        VerticalPanel right = new VerticalPanel();
        w = getItemInfoList("Similar tags", tagDetails.getSimilarTags(), tagDetails.getId(), false, tagOracle);
        w.setStyleName("right");
        right.add(w);
        main.add(right, DockPanel.EAST);

        main.setStyleName("resultpanel");
        return main;
    }

    Widget getBioWidget(ArtistDetails artistDetails) {
        HTML html = new HTML();
        html.setHTML(getBestArtistImageAsHTML(artistDetails) + artistDetails.getBiographySummary());
        html.setStyleName("bio");

        StarRatingWidget starWidget = null;
        if (cdm.isLoggedIn()) {
            starWidget = new StarRatingWidget(0);
        }

        return createMainSection(artistDetails.getName(), html, getSpotifyListenWidget(artistDetails), starWidget);
    }

    Widget getTagWidget(TagDetails tagDetails) {
        HTML html = new HTML();
        html.setHTML(getBestTagImageAsHTML(tagDetails) + tagDetails.getDescription());
        html.setStyleName("bio");
        return createMainSection(tagDetails.getName(), html, getListenWidget(tagDetails), null);
    }

    Widget getLastFMListenWidget(final ArtistDetails artistDetails) {
        Image image = new Image("play-icon30.jpg");
        //image.setSize("22px", "22px");
        image.setTitle("Play music like " + artistDetails.getName() + " at last.fm");
        image.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                popupSimilarArtistRadio(artistDetails, true);
            }
        });
        return image;
    }

    Widget getSpotifyListenWidget(final ArtistDetails artistDetails) {
        String musicURL = artistDetails.getSpotifyId();
        if (musicURL != null && !musicURL.equals("")) {
            HTML html = new HTML("<a href=\"" + musicURL + "\"><img src=\"play-icon30.jpg\"/></a>");
            html.setTitle("Play " + artistDetails.getName() + " with Spotify");
            return html;
        } else {
            return getLastFMListenWidget(artistDetails);
        }
    }

    Widget getListenWidget(final TagDetails tagDetails) {
        Image image = new Image("play-icon30.jpg");
        image.setTitle("Play music like " + tagDetails.getName() + " at last.fm");
        image.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                popupTagRadio(tagDetails, true);
            }
        });
        return image;
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
                HTML html = new HTML(createAnchor(key, url));
                grid.setWidget(index++, 0, html);
            }
            return createSection("More info", grid);
        } else {
            return new Label("");
        }
    }

    Widget getTastAuraMeterPanel(ArtistDetails aD) {

        double currArtistScore = cdm.computeTastauraMeterScore(aD);
        double realMaxScore;    // max between currArtist and user's fav artists' max score

        if (currArtistScore>cdm.getMaxScore()) {
            realMaxScore = currArtistScore;
        } else {
            realMaxScore = cdm.getMaxScore();
        }

        VerticalPanel vPanel = new VerticalPanel();

        for (String key : cdm.getFavArtist().keySet()) {
            vPanel.add(getPopularityWidget(key, cdm.getFavArtist().get(key)/realMaxScore, false, null));
        }

        vPanel.add(getPopularityWidget(aD.getName(),
                currArtistScore/realMaxScore, false, "itemInfoHighlight"));

        return createSection("Tast-aura-meter", vPanel);
    }

    Widget getPopularityPanel(ArtistDetails artistDetails) {

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(getPopularityWidget("The Beatles",1,true,null));
        vPanel.add(getPopularityWidget(artistDetails.getName(),
                artistDetails.getNormPopularity(),true,null));

        return createSection("Popularity", vPanel);
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

    /**
     * Build a horizontal bar representing the popularity
     * @param name name of the artist or concept
     * @param normPopularity popularity as a number between 0 and 1
     * @param log plot on a log scale
     * @param style style to apply to the name
     */
    Widget getPopularityWidget(String name, double normPopularity, boolean log, String style) {

        if (log) {
            normPopularity=Math.log(normPopularity+1)/Math.log(2); // get the base 2 log
        }
        int leftWidth = (int)(normPopularity*100);
        if (leftWidth<1) {
            leftWidth=1;
        } else if (leftWidth>100) {
            leftWidth=100;
        }
        int rightWidth = 100-leftWidth;

        HorizontalPanel table = new HorizontalPanel();
        table.setWidth("100px");
        table.setBorderWidth(0);
        table.setSpacing(0);

        Widget left = new Label("");
        left.setStyleName("popLeft");
        left.setWidth(leftWidth+"");
        left.setHeight("15px");

        Widget right = new Label("");
        right.setStyleName("popRight");
        right.setWidth(rightWidth+"");
        left.setHeight("15px");

        table.add(left);
        table.add(right);


        VerticalPanel vPanel = new VerticalPanel();
        Label lbl = new Label(name);
        if (style!=null && !style.equals("")) {
            lbl.addStyleName(style);
        }
        vPanel.add(lbl);
        vPanel.add(table);
        return vPanel;
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
                grid.setWidget(i, 1, new HTML(createAnchor(event.getName(), event.getEventURL())));
                String venue = event.getVenue();
                grid.setWidget(i, 2, new HTML(venue));
            }
            widget.add(grid);
        }
        return createSection("Upcoming Events", widget);
    }

    Widget createSection(String title, Widget widget) {
        return createSection(new HTML("<h2>" + title + "</h2>"), widget);
    }

    Widget createSection(Widget title, Widget widget) {
        Panel panel = new VerticalPanel();
        panel.add(title);
        panel.add(widget);
        return panel;
    }

    Widget createMainSectionOld(String title, Widget widget) {
        Panel panel = new VerticalPanel();
        panel.add(new HTML("<h1>" + title + "</h1>"));
        panel.add(widget);
        return panel;
    }

    Widget createMainSection(String title, Widget widget, Widget adornment, StarRatingWidget starWidget) {
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
        panel.add(widget);
        return panel;
    }

    /**
     * Creates a link
     * @param text link description
     * @param url link url
     * @return html formated link
     */
    String createAnchor(String text, String url) {
//        if (url!=null && url.compareTo(null)!=0 && url.compareTo("null")!=0 && url.compareTo("http://upcoming.org/venue/null/")!=0) {
            return "<a href=\"" + url + "\" target=\"window1\">" + text + "</a>";
  //      } else {
    //        return text;
    //    }
    }

    String createAnchoredImage(String imageURL, String url, String style) {
        String styleSpec = "";
        if (style != null) {
            styleSpec = "style=\"" + style + "\"";
        }
        return createAnchor("<img class=\"inlineAlbumArt\" " + styleSpec + " src=\"" + imageURL + "\"/>", url);
    }

    private String getBestArtistImageAsHTML(ArtistDetails ad) {
        String imgHtml = "";
        ArtistPhoto[] photos = ad.getArtistPhotos();
        if (photos.length > 0) {
            imgHtml = photos[0].getHtmlWrapper();
        } else if (ad.getAlbums().length > 0) {
            AlbumDetails album = ad.getAlbums()[0];
            imgHtml = createAnchoredImage(album.getAlbumArt(), album.getAmazonLink(), "margin-right: 10px; margin-bottom: 10px");
        }
        return imgHtml;
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
        url = url.replaceAll("\\?v=", "v/");
        String title = "<span style=\"text-align:center\">" + video.getTitle() + "</span><br/>";
        String obj = "<object width=\"425\" height=\"350\"><param name=\"movie\" value=\"" + url + "\"></param><param name=\"wmode\" value=\"transparent\"></param>" + "<embed src=\"" + url + autostring + "\" type=\"application/x-shockwave-flash\"" + " wmode=\"transparent\" width=\"425\" height=\"350\"></embed></object>";
        return title + obj;
    }

    private Widget getSimilarArtistRadio(ArtistDetails artist) {
        String embeddedObject = "<object width=\"340\" height=\"123\">" + "<param name=\"movie\" value=\"http://panther1.last.fm/webclient/50/defaultEmbedPlayer.swf\" />" + "<param name=FlashVars value=\"viral=true&lfmMode=radio&amp;radioURL=lastfm://artist/ARTIST_NAME/similarartists&amp;" + "restTitle= ARTIST_NAME’s Similar Artists \" />" + "<param name=\"wmode\" value=\"transparent\" />" + "<embed src=\"http://panther1.last.fm/webclient/50/defaultEmbedPlayer.swf\" width=\"340\" " + "FlashVars=\"viral=true&lfmMode=radio&amp;radioURL=" + "lastfm://artist/ARTIST_NAME/similarartists&amp;restTitle= ARTIST_NAME’s Similar Artists \" height=\"123\" " + "type=\"application/x-shockwave-flash\" wmode=\"transparent\" />" + "</object>";
        embeddedObject = embeddedObject.replaceAll("ARTIST_NAME", artist.getEncodedName());
        return new HTML(embeddedObject);
    }

    private String getSimilarArtistRadioLink(ArtistDetails artist, boolean useTags) {
        if (useTags) {
            ItemInfo tag = getBestTag(artist);
            if (tag != null) {
                return getTagRadioLink(tag.getItemName());
            } else {
                return getSimilarArtistRadioLink(artist, false);
            }
        } else {
            String link = "http://www.last.fm/webclient/popup/?radioURL=" + "lastfm://artist/ARTIST_REPLACE_ME/similarartists&resourceID=undefined" + "&resourceType=undefined&viral=true";
            return link.replaceAll("ARTIST_REPLACE_ME", artist.getEncodedName());
        }
    }

    private String getTagRadioLink(String tagName) {
        tagName = tagName.replaceAll("\\s+", "%20");
        String link = "http://www.last.fm/webclient/popup/?radioURL=" + "lastfm://globaltags/TAG_REPLACE_ME/&resourceID=undefined" + "&resourceType=undefined&viral=true";
        return link.replaceAll("TAG_REPLACE_ME", tagName);
    }

    private ItemInfo getBestTag(ArtistDetails artist) {
        ItemInfo tag = null;
        ItemInfo[] tags = artist.getDistinctiveTags();
        if (tags == null && tags.length == 0) {
            tags = artist.getFrequentTags();
        }
        if (tags != null && tags.length > 0) {
            tag = tags[0];
        }
        return tag;
    }

    private void popupSimilarArtistRadio(ArtistDetails artist, boolean useTags) {
        Window.open(getSimilarArtistRadioLink(artist, useTags), "lastfm_popup", "width=400,height=170,menubar=no,toolbar=no,directories=no," + "location=no,resizable=no,scrollbars=no,status=no");
    }

    private void popupTagRadio(TagDetails tagDetails, boolean useTags) {
        Window.open(getTagRadioLink(tagDetails.getName()), "lastfm_popup", "width=400,height=170,menubar=no,toolbar=no,directories=no," + "location=no,resizable=no,scrollbars=no,status=no");
    }

    private Widget getItemInfoList(final String title, final ItemInfo[] itemInfo, String highlightID, boolean getArtistOnClick, MultiWordSuggestOracle oracle) {
        /*
         Listener listener = new Listener<ComponentEvent>() {
       public void handleEvent(ComponentEvent ce) {
         ContentPanel cp = (ContentPanel) ce.component;
         String n = cp.getTitleText();
         if (ce.type == Events.Expand) {
           Info.display("Panel Change", "The '{0}' panel was expanded", n);
         } else {
           Info.display("Panel Change", "The '{0}' panel was collapsed", n);
         }
       }
     };

        ContentPanel cp = new ContentPanel();
        cp.setCollapsible(true);
        cp.setWidth(200);
        cp.setBodyStyle("fontSize: 12px");
        cp.setHeading("Collapsible");
        cp.addListener(Events.Expand, listener);
        cp.addListener(Events.Collapse, listener);
        */

        Grid artistGrid = new Grid(itemInfo.length, 1);
        for (int i = 0; i < itemInfo.length; i++) {

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
        }

        Widget w;
        if (!getArtistOnClick) {
            Grid titleWidget = new Grid(1, 2);
            titleWidget.setWidget(0, 0, new HTML("<h2>" + title + "</h2>"));
            Label l = new Label(" Cloud");
            l.setStyleName("tinyInfo");
            l.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    showTagCloud(title, itemInfo);
                }
            });
            titleWidget.setWidget(0, 1, l);
            w = createSection(titleWidget, artistGrid);
        } else {
            w = createSection(title, artistGrid);
        }
        w.setStyleName("infoList");
        w.setWidth("200px");
        return w;
    }

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

    public Widget getLoadingBarWidget() {
        FlowPanel panel = new FlowPanel();
        panel.add(new HTML("<img src='"+ICON_WAIT+"'/>"));
        return panel;
    }

    private SuggestBox createSuggestBox(MultiWordSuggestOracle oracle) {
        SuggestBox sbox = new SuggestBox(oracle);

        sbox.setStyleName("searchText");
        sbox.ensureDebugId ("cwSuggestBox");
        sbox.setLimit(20);

        sbox.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                if (keyCode == KEY_ENTER) {

                    /* Hack to go around the bug of the suggestbox which wasn't
                     * using the highlighted element of the suggetions popup
                     * when submitting the form
                     * */
                    DeferredCommand.addCommand(new Command(){ public void execute(){
                        search.search();
                    }});
                } else if (keyCode == KEY_ESCAPE) {
                    //Window.alert("escape!!");
                    //MouseListenerCollection a = new MouseListenerCollection();
                    //DOM.
                    //a.fireMouseEvent(sender, new Event(Event.ONCLICK));
                    //a.fireMouseDown(sender, sender.getAbsoluteLeft(), sender.getAbsoluteTop());
                    //a.fireMouseUp(sender, sender.getAbsoluteLeft(), sender.getAbsoluteTop());
                }
            }
        });

        return sbox;
    }

    /**
     * Update suggest box with new oracle if necessary. Will fetch oracle if it
     * is currently null
     * @param type artist or tag
     */
    private void updateSuggestBox(Oracles type) {
        if (currLoadedOracle!=null && currLoadedOracle == type) {
            return;
        } else {
            if (type == Oracles.ARTIST) {
                if (artistOracle == null) {
                    invokeOracleFetchService(type);
                } else {
                    swapSuggestBox(artistOracle, fetchOracle);
                    fetchOracle = null;
                }
            } else {
                if (tagOracle == null) {
                    invokeOracleFetchService(type);
                } else {
                    swapSuggestBox(tagOracle, fetchOracle);
                    fetchOracle = null;
                }
            }
        }
    }

    /**
     * Does the actual swapping of the suggest box with the provided oracle
     * @param newOracle
     *
     */
    private void swapSuggestBox(MultiWordSuggestOracle newOracle, Oracles newOracleType) {

        String oldTxt;
        if (search.getSearchBox()!=null) {
            oldTxt = search.getSearchBox().getText();
        } else {
            oldTxt="";
        }

        searchBoxContainerPanel.clear();
        SuggestBox textBox = createSuggestBox(newOracle);
        textBox.setText(oldTxt);
        search.setSearchBox(textBox);
        searchBoxContainerPanel.add(search.getSearchBox());

        if (newOracleType==Oracles.ARTIST) {
            artistOracle = newOracle;
            currLoadedOracle = Oracles.ARTIST;
        } else {
            tagOracle = newOracle;
            currLoadedOracle = Oracles.TAG;
        }
    }

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

    abstract class AsyncCallbackWithType implements AsyncCallback {

        public Oracles type;

        public AsyncCallbackWithType(Oracles type) {
            super();
            this.type=type;
        }

        public abstract void onFailure(Throwable arg0);
        public abstract void onSuccess(Object arg0);

    }

    class PopupHiderClickListener implements ClickListener {

        DialogBox d;

        public PopupHiderClickListener(DialogBox d) {
            this.d=d;
        }

        public void onClick(Widget arg0) {
            d.hide();
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

    class LinkClickListener implements ClickListener {

        private String url;

        LinkClickListener(String url) {
            this.url = url;
        }

        public void onClick(Widget sender) {
            Window.open(url, "Window1", "");
        }
    }

    class CommonTagsClickListener implements ClickListener {

        private String id1;
        private String id2;

        CommonTagsClickListener(String id1, String id2) {
            this.id1 = id1;
            this.id2 = id2;
        }

        public void onClick(Widget sender) {
            invokeGetCommonTags(id1, id2);
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
            Popup.showPopup(html,"WebMusicExplaura :: Flick Photo");
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

    class SearchWidget extends Composite {

        private SuggestBox textBox;
        private RadioButton[] searchButtons;

        SearchWidget() {

            textBox = new SuggestBox();
            textBox.setTabIndex(0);

            searchBoxContainerPanel = new FlowPanel();
            searchBoxContainerPanel.add(getLoadingBarWidget());

            Panel searchType = new FlowPanel();
            searchButtons = new RadioButton[3];
            searchButtons[0] = new RadioButton("searchType", "For Artist");
            searchButtons[1] = new RadioButton("searchType", "By Tag");
            searchButtons[2] = new RadioButton("searchType", "For Tag");

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

            setText("", SearchResults.SEARCH_FOR_ARTIST_BY_ARTIST);
            updateSuggestBox(Oracles.ARTIST);

            for (int i = 0; i < searchButtons.length; i++) {
                searchType.add(searchButtons[i]);
                searchButtons[i].setStyleName("searchTypeButton");
            }
            searchType.setWidth("100%");
            searchType.setStyleName("searchPanel");

            FlowPanel searchPanel = new FlowPanel();
            searchPanel.setStyleName("searchPanel");

            Button searchButton = new Button("Search", new ClickListener() {
                public void onClick(Widget sender) {
                    search();
                }
            });
            searchButton.addStyleName("main");
            searchButton.setTabIndex(1);

            searchPanel.add(searchBoxContainerPanel);
            searchPanel.add(searchButton);
            searchPanel.add(searchType);
            this.initWidget(searchPanel);
        }

        private int getSearchType() {
            for (int i = 0; i < searchButtons.length; i++) {
                if (searchButtons[i].isChecked()) {
                    return i;
                }
            }
            return 0;
        }

        void setText(String text, int which) {
            textBox.setText(text);
            for (int i = 0; i < searchButtons.length; i++) {
                searchButtons[i].setChecked(i == which);
            }
        }

        public void search() {
            if (cdm.getCurrSimTypeName() == null || cdm.getCurrSimTypeName().equals("")) {
                Window.alert("Error. Cannot search without the similarity types.");
            } else {
                String query = textBox.getText().toLowerCase();
                if (getSearchType() == SearchResults.SEARCH_FOR_TAG_BY_TAG) {
                    invokeTagSearchService(query, 0);
                } else {
                    invokeArtistSearchService(query, getSearchType() == SearchResults.SEARCH_FOR_ARTIST_BY_TAG, 0);
                }
            }
        }

        public void setSearchBox(SuggestBox box) {
            this.textBox=box;
        }

        public SuggestBox getSearchBox() {
            return textBox;
        }
    }

    public void showTagCloud(String title, ItemInfo[] tags) {
        final DialogBox d = Popup.getDialogBox();
        Panel p = new FlowPanel();
        p.setWidth("600px");
        HorizontalPanel innerP = new HorizontalPanel();
        innerP.setSpacing(4);

        if (tags.length > 0) {
            //StringBuffer sb = new StringBuffer();
            double max = tags[0].getScore();
            double min = tags[tags.length - 1].getScore();
            double range = max - min;
            tags = shuffle(tags);
            int index=0;
            for (int i = 0; i < tags.length; i++) {
                int color = (i % 2) + 1;
                int fontSize = scoreToFontSize((tags[i].getScore() - min) / range);

                String s = "<span style='font-size:" + fontSize + "px;'>" + tags[i].getItemName() + " </span>   ";
                SimpleHyperLink sH = new SimpleHyperLink();
                sH.setHTML(s);
                sH.setStyleName("tag"+color);
                sH.addClickListener(new ItemInfoClickListener(tags[i], false));
                sH.addClickListener(new PopupHiderClickListener(d));

                p.add(sH);
            }
            Popup.showPopup(p,"WebMusicExplaura :: "+title,d);
        }
    }

    private int scoreToFontSize(double score) {
        int min = 12;
        int max = 60;
        int range = max - min;
        return (int) Math.round(range * score + min);
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

    private ItemInfo[] shuffle(ItemInfo[] itemInfo) {

        ItemInfo[] ii = new ItemInfo[itemInfo.length];

        for (int i = 0; i < itemInfo.length; i++) {
            ii[i] = itemInfo[i];
        }

        Arrays.sort(ii, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (Random.nextBoolean()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return ii;
    }
}