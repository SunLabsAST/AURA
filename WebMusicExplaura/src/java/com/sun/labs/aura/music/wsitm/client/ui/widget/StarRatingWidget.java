/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;


import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededMouseListener;
import com.sun.labs.aura.music.wsitm.client.event.RatingListener;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.*;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;

/**
 *
 * @author mailletf
 */
public class StarRatingWidget extends Composite implements RatingListener, LoginListener {

    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;
    private String artistID;

    private int nbrStars = 5;
    private int nbrSelectedStars = 0;
    private int oldNbrSelectedStars = nbrSelectedStars; // save to revert in case RPC call fails

    private String STAR_LID = "";
    private String STAR_NOTLID = "";
    private String STAR_WHITE = "";
    private String NOT_INTERESTED = "";
    private String NOT_INTERESTED_HOVER = "";

    private static final String STAR_LID_M = "star-lid.png";
    private static final String STAR_NOTLID_M = "star-notlid.png";
    private static final String STAR_WHITE_M = "star-white.png";
    private static final String NOT_INTERESTED_M = "not-interested2.png";
    private static final String NOT_INTERESTED_HOVER_M = "not-interested2-hover.png";

    private static final String STAR_LID_S = "star-lid-s.png";
    private static final String STAR_NOTLID_S = "star-notlid-s.png";
    private static final String STAR_WHITE_S = "star-white-s.png";
    private static final String NOT_INTERESTED_S = "not-interested2-s.png";
    private static final String NOT_INTERESTED_HOVER_S = "not-interested2-s-hover.png";

    private Image[] images;

    private Grid g;

    public enum Size {
        SMALL,
        MEDIUM
    }

    public StarRatingWidget(MusicSearchInterfaceAsync musicServer, ClientDataManager cdm,
            String artistID, Size size) {

        g = new Grid(1, 1);
        initWidget(g);

        initializeRatingWidget(musicServer, cdm, artistID, -1, size);
        if (cdm.isLoggedIn()) {
            invokeFetchRating();
        } else {
            drawRatingWidget();
        }

    }

    public StarRatingWidget(MusicSearchInterfaceAsync musicServer, ClientDataManager cdm,
            String artistID, int initialSelection, Size size) {

        g = new Grid(1, 1);
        initWidget(g);

        initializeRatingWidget(musicServer, cdm, artistID, initialSelection, size);
        drawRatingWidget();
    }

    private void initializeRatingWidget(MusicSearchInterfaceAsync musicServer, ClientDataManager cdm,
            String artistID, int initialSelection, Size size) {

        this.nbrSelectedStars = initialSelection;
        this.cdm = cdm;
        this.artistID = artistID;
        this.musicServer = musicServer;

        cdm.getRatingListenerManager().addListener(artistID, this);

        if (size == Size.SMALL) {
            STAR_LID = STAR_LID_S;
            STAR_NOTLID = STAR_NOTLID_S;
            STAR_WHITE = STAR_WHITE_S;
            NOT_INTERESTED = NOT_INTERESTED_S;
            NOT_INTERESTED_HOVER = NOT_INTERESTED_HOVER_S;
        } else {
            STAR_LID = STAR_LID_M;
            STAR_NOTLID = STAR_NOTLID_M;
            STAR_WHITE = STAR_WHITE_M;
            NOT_INTERESTED = NOT_INTERESTED_M;
            NOT_INTERESTED_HOVER = NOT_INTERESTED_HOVER_M;
        }
    }

    public void onDelete() {
        cdm.getRatingListenerManager().removeListener(artistID, this);
        cdm.getLoginListenerManager().removeListener(this);
    }

    private void drawRatingWidget() {

        FlowPanel p = new FlowPanel();

        images = new Image[nbrStars];
        for (int i=0; i<nbrStars; i++) {
            if (i<=nbrSelectedStars-1) {
                images[i] = new Image(STAR_NOTLID);
            } else {
                images[i] = new Image(STAR_WHITE);
            }
            images[i].addClickListener(new DataEmbededClickListener<Integer>(i) {

                public void onClick(Widget arg0) {
                    invokeSaveRating(data);
                }
            });
            images[i].addMouseListener(new DataEmbededMouseListener<Integer>(i) {

                public void onMouseEnter(Widget arg0) {
                    for (int i = 0; i <= data; i++) {
                        images[i].setUrl(STAR_LID);
                    }
                }

                public void onMouseLeave(Widget arg0) {
                    redrawStars();
                }

                public void onMouseDown(Widget arg0, int arg1, int arg2) {}
                public void onMouseMove(Widget arg0, int arg1, int arg2) {}
                public void onMouseUp(Widget arg0, int arg1, int arg2) {}

            });
            p.add(images[i]);
        }
        Image noInterest = new Image(NOT_INTERESTED);
        noInterest.addMouseListener(new MouseListener() {

            public void onMouseDown(Widget arg0, int arg1, int arg2) {}
            public void onMouseMove(Widget arg0, int arg1, int arg2) {}
            public void onMouseUp(Widget arg0, int arg1, int arg2) {}

            public void onMouseEnter(Widget arg0) {
                ((Image)arg0).setUrl(NOT_INTERESTED_HOVER);
            }

            public void onMouseLeave(Widget arg0) {
                ((Image)arg0).setUrl(NOT_INTERESTED);
            }
        });
        //p.add(noInterest);
        g.setWidget(0, 0, p);
    }

    private void invokeSaveRating(int index) {

        if (!cdm.isLoggedIn()) {
            Window.alert("Message from the happy tag : you must be logged in to access this feature. I should redirect you to another page so you can create an account, but I'd rather keep you here so we can be friends.");
            return;
        }

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                cdm.getRatingListenerManager().triggerOnRate(artistID, nbrSelectedStars);
            }

            public void onFailure(Throwable caught) {
                Window.alert("Unable to save your rating for artistID "+artistID);
                nbrSelectedStars = oldNbrSelectedStars;
                redrawStars();
            }
        };

        oldNbrSelectedStars = nbrSelectedStars;
        nbrSelectedStars = index + 1;
        redrawStars();

        try {
            musicServer.updateUserSongRating(index + 1, artistID, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }

    }

    private void redrawStars() {
        for (int i = 0; i < nbrStars; i++) {
            if (i <= nbrSelectedStars - 1) {
                images[i].setUrl(STAR_NOTLID);
            } else {
                images[i].setUrl(STAR_WHITE);
            }
        }
    }

    private void invokeFetchRating() {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                nbrSelectedStars = (Integer) result;
                drawRatingWidget();
            }

            public void onFailure(Throwable caught) {
                Window.alert("Error fetching rating.");
            }
        };

        try {
            musicServer.fetchUserSongRating(artistID, callback);
        } catch (WebException ex) {
            Window.alert(ex.getMessage());
        }
    }

    public void onLogin(ListenerDetails lD) {
        invokeFetchRating();
    }

    public void onLogout() {
        nbrSelectedStars = 0;
        redrawStars();
    }

    public void onRate(String itemId, int rating) {
        nbrSelectedStars = rating;
        drawRatingWidget();
    }
}
