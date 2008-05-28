/*
 * SimpleSearchWidget.java
 *
 * Created on March 7, 2007, 5:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class SimpleSearchWidget extends Swidget implements HistoryListener {

    private Widget curResult;
    private DockPanel mainPanel;
    private Label message;
    private boolean debug;
    private SearchWidget search;
    private MusicSearchInterfaceAsync musicServer;
    private Image icon;
    
    private static final String ICON_WAIT = "ajax-loader.gif";
    
    public SimpleSearchWidget() {
        super("Simple Search");
        initRPC();
        History.addHistoryListener(this);
        initWidget(getWidget());
        showResults(History.getToken());
    }

    /** Creates a new instance of SimpleSearchWidget */
    public Widget getWidget() {

        search = new SearchWidget();
        message = new Label();
        //message.setWidth("100%");
        message.setHeight("20px");
        message.setStyleName("message");

        icon = new Image();
        icon.setVisible(false);
        icon.setStyleName("icon");
        
        HorizontalPanel msgPanel = new HorizontalPanel();
        msgPanel.setWidth("100%");
        msgPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        msgPanel.add(icon);
        msgPanel.add(message);
        msgPanel.setCellWidth(icon, "32px");
        msgPanel.setCellHorizontalAlignment(icon, HorizontalPanel.ALIGN_RIGHT);
        
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

    private void showMessage(String msg) {
        message.setStyleName("messageNormal");
        message.setText(msg);
        icon.setUrl(SimpleSearchWidget.ICON_WAIT);
        icon.setVisible(true);
    }

    private void showError(String msg) {
        message.setStyleName("messageError");
        message.setText(msg);
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
    private String curToken = null;

    private void setResults(String historyName, Widget result) {
        //Window.alert("setResults with history name "+historyName);
        if (curResult == result) {
            return;
        }

        if (!History.getToken().equals(historyName)) {
            History.newItem(historyName);
            curToken = History.getToken();
        }
        if (curResult != null) {
            mainPanel.remove(curResult);
            curResult = null;
        }
        if (result != null) {
            mainPanel.add(result, DockPanel.CENTER);
            curResult = result;
        }
    }

    private void clearResults() {
        setResults("home", null);
    }

    private void showResults(String resultName) {
        //Window.alert("showResults '" + resultName +"'");
        
        //  resultName = URL.decodeComponent(resultName);
        if (resultName.startsWith("artist:")) {
            invokeGetArtistInfo(resultName, false);
        } else if (resultName.startsWith("tag:")) {
            invokeGetTagInfo(resultName, false);
        } else if (resultName.startsWith("artistSearch:")) {
            String query = resultName.replaceAll("artistSearch:", "");
            invokeArtistSearchService(query, false, 0);
        } else if (resultName.startsWith("artistSearchByTag:")) {
            String query = resultName.replaceAll("artistSearchByTag:", "");
            invokeArtistSearchService(query, true, 0);
        } else if (resultName.startsWith("tagSearch:")) {
            String query = resultName.replaceAll("tagSearch:", "");
            invokeTagSearchService(query, 0);
        } else if (resultName.equals("home")) {
            setResults("home", null);
        } else {
            GWT.log("unknown history token " + resultName, new Throwable());
            // Window.alert("unknown history token " + resultName);
            setResults("home", null);
        }
    }

    public void onHistoryChanged(String historyToken) {
        //debug("history changed token is '" + historyToken + "'");
        historyToken = decodeHistoryToken(historyToken);
        //debug("history decoded token is '" + historyToken + "'");
        
        //@todo fix this
        //if (!historyToken.equals(curToken)) {
            //showResults(historyToken);
        //}
    }

    // On Firefox, the history tokens are already decoded, but this is not
    // the case on safari, so we decode them here.
    static native String decodeHistoryToken(String historyToken) /*-{
        return decodeURIComponent(historyToken);
    }-*/;

    private void initRPC() {
        // (1) Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the async version of
        // the interface. The cast is always safe because the generated proxy
        // implements the async interface automatically.
        //
        musicServer = (MusicSearchInterfaceAsync) GWT.create(MusicSearchInterface.class);

        // (2) Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) musicServer;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "musicsearch";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
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
                        setResults(sr.toString(), getItemInfoList("Pick one: ", sr.getItemResults(), null, false));
                    }
                } else {
                    if (sr == null) {
                        showError("Error. Resultset is null. (209)");
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

        showMessage("Searching for " + searchText);

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        musicServer.tagSearch(searchText, 100, callback);
    }

    private void invokeArtistSearchService(String searchText, boolean byTag, int page) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                //Window.alert("entering on success");
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
                        Widget searchResults = getItemInfoList("Pick one: ", sr.getItemResults(), null, true);
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

        showMessage("Searching for " + searchText);
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
                    setResults("artist:" + artistDetails.getId(), artistPanel);
                    clearMessage();
                } else {
                    if (artistDetails == null) {
                        Window.alert("i am 1");
                        showError("Search trouble. You'd better call Steve.");
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

        showMessage("Getting info for artist");

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        try {
            musicServer.getArtistDetails(artistID, refresh, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
            
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
                    setResults(tagDetails.getId(), tagPanel);
                    clearMessage();
                } else {
                    if (tagDetails == null) {
                        Window.alert("i am 2");
                        showError("Search trouble. You'd better call Steve.");
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

        showMessage("Getting info for tag");

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        musicServer.getTagDetails(tagID, refresh, callback);
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

        showMessage("Getting common tags");
        try {
            musicServer.getCommonTags(artistID1, artistID2, 30, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
      
    }

    private Widget createArtistPanel(String title, ArtistDetails artistDetails) {
        VerticalPanel main = new VerticalPanel();
        main.add(getBioWidget(artistDetails));
        main.add(getVideosWidget(artistDetails.getVideos()));
        main.add(getPhotosWidget(artistDetails.getPhotos()));
        main.add(getAlbumsWidget(artistDetails));
        main.add(getEventsWidget(artistDetails));
        main.setStyleName("center");

        VerticalPanel right = new VerticalPanel();
        String id = artistDetails.getId();
        right.add(getItemInfoList("Distinctive Tags", artistDetails.getDistinctiveTags(), null, false));
        right.add(getItemInfoList("Frequent Tags", artistDetails.getFrequentTags(), null, false));
        right.add(getPopularityWidget(artistDetails));
        right.add(getRefreshWidget(artistDetails));
        right.setStyleName("right");

        VerticalPanel left = new VerticalPanel();
        if (artistDetails.getSimilarArtists().length > 0) {
            left.add(getItemInfoList2("Tagomendations", artistDetails.getSimilarArtists(), id, true));
        }
        if (artistDetails.getRecommendedArtists().length > 0) {
            left.add(getItemInfoList("Recommendations", artistDetails.getRecommendedArtists(), id, true));
        }
        if (artistDetails.getCollaborations().length > 0) {
            left.add(getItemInfoList("Related", artistDetails.getCollaborations(), id, true));
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

        {
            VerticalPanel left = new VerticalPanel();
            left.setWidth("150px");
            left.setStyleName("left");
            Widget w = getItemInfoList(tagDetails.getName() + " artists", tagDetails.getRepresentativeArtists(), null, true);
            left.add(w);
            main.add(left, DockPanel.WEST);
        }

        {
            VerticalPanel v = new VerticalPanel();
            v.add(getTagWidget(tagDetails));
            v.add(getVideosWidget(tagDetails.getVideos()));
            v.add(getPhotosWidget(tagDetails.getPhotos()));
            v.setStyleName("center");
            main.add(v, DockPanel.CENTER);
        }

        {
            VerticalPanel right = new VerticalPanel();
            Widget w = getItemInfoList("Similar tags", tagDetails.getSimilarTags(), tagDetails.getId(), false);
            w.setStyleName("right");
            right.add(w);
            right.add(getRefreshWidget(tagDetails));
            main.add(right, DockPanel.EAST);
        }

        main.setStyleName("resultpanel");
        return main;
    }

    Widget getBioWidget(ArtistDetails artistDetails) {
        HTML html = new HTML();
        html.setHTML(getBestArtistImageAsHTML(artistDetails) + artistDetails.getBiographySummary());
        html.setStyleName("bio");
        return createMainSection(artistDetails.getName(), html, getSpotifyListenWidget(artistDetails));
    }

    Widget getTagWidget(TagDetails tagDetails) {
        HTML html = new HTML();
        html.setHTML(getBestTagImageAsHTML(tagDetails) + tagDetails.getDescription());
        html.setStyleName("bio");
        return createMainSection(tagDetails.getName(), html, getListenWidget(tagDetails));
    }

    Widget getLastFMListenWidget(final ArtistDetails artistDetails) {
        Image image = new Image("play.gif");
        image.setTitle("Play music like " + artistDetails.getName() + " at last.fm");
        image.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                popupSimilarArtistRadio(artistDetails, true);
            }
        });
        return image;
    }

    Widget getSpotifyListenWidget(final ArtistDetails artistDetails) {
        String musicURL = artistDetails.getMusicURL();
        if (musicURL != null) {
            HTML html = new HTML("<a href=\"" + musicURL + "\"><img src=\"play.gif\"/></a>"); 
            html.setTitle("Play " + artistDetails.getName() + " with Spotify");
            return html;
        } else {
            return getLastFMListenWidget(artistDetails);
        }
    }

    Widget getListenWidget(final TagDetails tagDetails) {
        Image image = new Image("play.gif");
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

    Widget getVideosWidget(ArtistVideo[] videos) {
        Panel videoPanel = new FlowPanel();
        if (videos.length == 0) {
            videoPanel.add(new Label("None available"));
        } else {
            for (int i = 0; i < videos.length; i++) {
                ArtistVideo video = videos[i];
                Image image = new Image(video.getThumbnail());
                image.addClickListener(new VideoClickListener(video));
                image.setTitle(video.getTitle());
                image.setStyleName("video");
                videoPanel.add(image);
            }
        }
        videoPanel.setStyleName("videos");
        return createSection("Videos", videoPanel);
    }

    Widget getPopularityWidget(ArtistDetails artistDetails) {
        return createSection("Popularity", new Label("" + artistDetails.getPopularity()));
    }

    Widget getRefreshWidget(final ArtistDetails artistDetails) {
        Label l = new Label("Refresh");
        l.setStyleName("refresh");
        l.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                invokeGetArtistInfo(artistDetails.getId(), true);
            }
        });
        return l;
    }

    Widget getRefreshWidget(final TagDetails tagDetails) {
        Label l = new Label("Refresh");
        l.setStyleName("refresh");
        l.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                invokeGetTagInfo(tagDetails.getId(), true);
            }
        });
        return l;
    }

    Widget getPhotosWidget(ArtistPhoto[] photos) {
        Panel photoPanel = new FlowPanel();
        if (photos.length == 0) {
            photoPanel.add(new Label("None available"));
        } else {
            for (int i = 0; i < photos.length; i++) {
                ArtistPhoto photo = photos[i];
                Image image = new Image(photo.getSmallImageUrl());
                image.addClickListener(new PhotoClickListener(photo));
                image.setTitle(photo.getTitle());
                image.setStyleName("image");
                photoPanel.add(image);
            }
        }
        photoPanel.setStyleName("photos");
        return createSection("Photos", photoPanel);
    }

    private Widget getAlbumsWidget(ArtistDetails artistDetails) {
        Panel panel = new FlowPanel();
        for (int i = 0; i < artistDetails.getAlbums().length; i++) {
            AlbumDetails album = artistDetails.getAlbums()[i];
            Image image = new Image(album.getAlbumArt());
            image.addClickListener(new LinkClickListener(album.getAmazonLink()));
            image.setStyleName("albumArt");
            image.setTitle(album.getTitle());
            panel.add(image);
        }
        panel.setStyleName("albums");
        panel.setWidth("100%");
        return createSection("Albums", panel);
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
                String venue = event.getVenue() + " " + event.getVenueAddress();
                grid.setWidget(i, 2, new HTML(createAnchor(venue, event.getVenueURL())));
            }
            widget.add(grid);
        }
        return createSection("Upcoming Events", widget);
    }

    Widget createSection(String title, Widget widget) {
        return createSection(new HTML("<h2>" + title + "</H2>"), widget);
    }

    Widget createSection(Widget title, Widget widget) {
        Panel panel = new VerticalPanel();
        panel.add(title);
        panel.add(widget);
        return panel;
    }

    Widget createMainSectionOld(String title, Widget widget) {
        Panel panel = new VerticalPanel();
        panel.add(new HTML("<h1>" + title + "</H1>"));
        panel.add(widget);
        return panel;
    }

    Widget createMainSection(String title, Widget widget, Widget adornment) {
        Panel panel = new VerticalPanel();
        DockPanel h = new DockPanel();
        h.add(new Label(title), DockPanel.WEST);
        if (adornment != null) {
            h.add(adornment, DockPanel.EAST);
            h.setCellHorizontalAlignment(adornment, h.ALIGN_RIGHT);
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

    private Widget getItemInfoList(final String title, final ItemInfo[] itemInfo, String highlightID, boolean getArtistOnClick) {
        Grid artistGrid = new Grid(itemInfo.length, 1);
        for (int i = 0; i < itemInfo.length; i++) {
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

    private Widget getItemInfoList2(String title, ItemInfo[] itemInfo, String highlightID, boolean getArtistOnClick) {
        Grid artistGrid = new Grid(itemInfo.length, 2);
        for (int i = 0; i < itemInfo.length; i++) {
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
        Widget w = createSection(title, artistGrid);
        w.setStyleName("infoList");
        w.setWidth("200px");
        return w;
    }

    public void showPopup(Widget w) {
        final PopupPanel popup = new PopupPanel(true);
        DockPanel docPanel = new DockPanel();

        docPanel.setStyleName("borderpopup");
        Label closeButton = new Label("Close");
        closeButton.setStyleName("clickableLabel");
        closeButton.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                popup.hide();
            }
        });

        Panel container = new FlowPanel();
        container.setStyleName("outerpopup");
        container.add(w);

        docPanel.add(container, DockPanel.CENTER);
        docPanel.add(closeButton, DockPanel.NORTH);
        docPanel.setCellHorizontalAlignment(closeButton, DockPanel.ALIGN_RIGHT);
        popup.add(docPanel);
        popup.center();
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
            html.setStyleName("popup");
            showPopup(html);
        }
    }

    class PhotoClickListener implements ClickListener {

        private ArtistPhoto photo;

        PhotoClickListener(ArtistPhoto photo) {
            this.photo = photo;
        }

        public void onClick(Widget sender) {
            HTML html = new HTML(photo.getRichHtmlWrapper());
            html.setStyleName("popup");
            showPopup(html);
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

    class SearchWidget extends Composite {

        private TextBox textBox;
        private RadioButton[] searchButtons;

        SearchWidget() {
            textBox = new TextBox();
            textBox.setMaxLength(120);
            textBox.setVisibleLength(30);
            textBox.setStyleName("searchText");
            textBox.addKeyboardListener(new KeyboardListenerAdapter() {

                public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                    if (keyCode == KEY_ENTER) {
                        search();
                    }
                }
            });

            Panel searchType = new FlowPanel();
            searchButtons = new RadioButton[3];
            searchButtons[0] = new RadioButton("searchType", "For Artist");
            searchButtons[1] = new RadioButton("searchType", "By Tag");
            searchButtons[2] = new RadioButton("searchType", "For Tag");

            setText("", SearchResults.SEARCH_FOR_ARTIST_BY_ARTIST);

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
            searchPanel.add(textBox);
            searchPanel.add(searchButton);
            searchPanel.add(searchType);
            initWidget(searchPanel);
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

        private void search() {
            String query = textBox.getText().toLowerCase();
            if (getSearchType() == SearchResults.SEARCH_FOR_TAG_BY_TAG) {
                invokeTagSearchService(query, 0);
            } else {
                invokeArtistSearchService(query, getSearchType() == SearchResults.SEARCH_FOR_ARTIST_BY_TAG, 0);
            }
        }
    }

    private void showTagCloud(String title, ItemInfo[] tags) {
        Panel p = new FlowPanel();

        HTML titleWidget = new HTML("<h2>" + title + "</h2>");
        p.setWidth("600px");

        if (tags.length > 0) {
            StringBuffer sb = new StringBuffer();
            double max = tags[0].getScore();
            double min = tags[tags.length - 1].getScore();
            double range = max - min;
            tags = shuffle(tags);
            String color1 = "#00f8c6";
            String color2 = " #00c8f6";
            for (int i = 0; i < tags.length; i++) {
                String color = i % 2 == 1 ? color1 : color2;
                int fontSize = scoreToFontSize((tags[i].getScore() - min) / range);
                sb.append("<span style='" + "color: " + color + ";" + " font-size:" + fontSize + "px;'>" + tags[i].getItemName() + "  </span>");
            }
            HTML html = new HTML(sb.toString());
            p.add(html);
            showPopup(p);
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
