/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DDEClickHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget.SwapableTxtButton;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuSpannedLabel;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuTagLabel;
import com.sun.labs.aura.music.wsitm.client.ui.RoundedPanel;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedFlowPanel;
import com.sun.labs.aura.music.wsitm.client.ui.widget.StarRatingWidget.InitialRating;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class CompactArtistWidget extends Composite implements HasListeners {

    private ClientDataManager cdm;
    private MusicSearchInterfaceAsync musicServer;

    private StarRatingWidget star;
    private PlayButton playButton;

    private String artistId;

    public CompactArtistWidget(ArtistCompact aC, ClientDataManager cdm,
                MusicSearchInterfaceAsync musicServer, SwapableTxtButton whyB,
                SwapableTxtButton diffB, InitialRating iR, Set<String> userTags) {
        
        this(aC, cdm, musicServer, whyB, diffB, iR, userTags, null);
    }
    
    public CompactArtistWidget(ArtistCompact aC, ClientDataManager tCdm,
            MusicSearchInterfaceAsync tMusicServer, SwapableTxtButton whyB,
            SwapableTxtButton diffB, InitialRating iR, Set<String> userTags,
            String backgroundColor) {

        this.cdm = tCdm;
        this.musicServer = tMusicServer;

        artistId = aC.getId();

        HorizontalPanel artistPanel = new HorizontalPanel();
        artistPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
        artistPanel.setStyleName("artistPanel");

        ClickHandler cH = new DEClickHandler<String>("artist:" + aC.getId()) {
            @Override
            public void onClick(ClickEvent ce) {
                History.newItem(data);
            }
        };

        //ContextMenuImage img = new MouseOverRollImage(aC);
        Image.prefetch(aC.getImageURL());   // force image load. Needed for IE
        Image img = new Image(aC.getImageURL());
        if (img == null) {
            img = new Image("nopic.gif");
        }
        img.setStyleName("image");
        img.setHeight("75px");
        img.setWidth("75px");
        img.addClickHandler(cH);
        img.addStyleName("largeMarginRight");

        artistPanel.add(img);

        HorizontalPanel aNamePanel = new HorizontalPanel();
        aNamePanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        aNamePanel.setWidth("210px");
        
        aNamePanel.add(new ContextMenuArtistLabel(aC, cdm));

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        playButton = new PlayButton(cdm, aC, PlayButton.PLAY_ICON_SIZE.SMALL, musicServer);
        if (playButton != null) {
            cdm.getMusicProviderSwitchListenerManager().addListener(playButton);
            playButton.getElement().getStyle().setProperty("align", "right");
            playButton.addStyleName("largeMarginRight");
            buttonPanel.add(playButton);
        }

        SteeringWheelWidget steerButton = 
                new SteeringWheelWidget(SteeringWheelWidget.wheelSize.SMALL,
                new DDEClickHandler<ClientDataManager, ArtistCompact>(cdm, aC) {
            @Override
            public void onClick(ClickEvent ce) {
                data.setSteerableReset(true);
                History.newItem("steering:" + sndData.getId());
            }
        });
        steerButton.setTitle("Steerable recommendations starting with "+aC.getName()+"'s tag cloud");
        steerButton.addStyleName("largeMarginRight");
        buttonPanel.add(new ContextMenuSteeringWheelWidget(cdm, steerButton, aC));

        //steeringMenu
        VerticalPanel swapableButtonPanel = new VerticalPanel();
        swapableButtonPanel.setStyleName("smallTagClick");
        boolean empty = true;
        if (whyB != null) {
            swapableButtonPanel.add(whyB);
            empty = false;
        }
        if (diffB != null) {
            swapableButtonPanel.add(diffB);
            empty = false;
        }
        if (!empty) {
            buttonPanel.add(swapableButtonPanel);
        }
        
        aNamePanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        aNamePanel.add(buttonPanel);

        VerticalPanel txtPanel = new VerticalPanel();
        txtPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
        txtPanel.add(aNamePanel);

        if (userTags != null && userTags.size() > 0) {
            Panel tagsLabel = getNDistinctiveTags("Your tags: ", userTags, 4);
            tagsLabel.setStyleName("recoTags");
            txtPanel.add(tagsLabel);
        }

        Panel tagsLabel = getNDistinctiveTags("Tags: ", aC, 4);
        tagsLabel.setStyleName("recoTags");
        txtPanel.add(tagsLabel);
        /*
        star = new StarRatingWidget(musicServer, cdm, aC.getId(),
                iR, StarRatingWidget.Size.SMALL);

        cdm.getRatingListenerManager().addListener(aC.getId(), star);
        cdm.getLoginListenerManager().addListener(star);
        
        Label starLbl = new Label("Your rating: ");
        starLbl.setStyleName("recoTags");
        starLbl.addStyleName("marginRight");
        starLbl.addStyleName("bold");

        Label tagLbl = new Label("Add tags");
        tagLbl.setStyleName("recoTags");
        tagLbl.addStyleName("bold");
        tagLbl.addStyleName("pointer");
        tagLbl.addClickHandler(new DEClickHandler<ArtistCompact>(aC) {
            @Override
            public void onClick(ClickEvent event) {
                TagInputWidget.showTagInputPopup(data, musicServer, cdm);
            }
        });

        Grid starGrid = new Grid(1,3);
        starGrid.setWidth("100%");
        starGrid.getCellFormatter().setHorizontalAlignment(0, 0, HorizontalPanel.ALIGN_LEFT);
        starGrid.setWidget(0, 0, starLbl);
        starGrid.setWidget(0, 1, star);
        starGrid.getCellFormatter().setHorizontalAlignment(0, 2, HorizontalPanel.ALIGN_RIGHT);
        starGrid.setWidget(0, 2, tagLbl);
        txtPanel.add(starGrid);
        */
        Widget w = WebLib.getSmallPopularityWidget(aC.getNormPopularity(), true, true);
        w.getElement().getStyle().setPropertyPx("marginTop", 5);
        txtPanel.add(w);

        artistPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
        artistPanel.add(txtPanel);
        artistPanel.setWidth("298px");
        if (backgroundColor != null) {
            artistPanel.getElement().getStyle().setProperty("background", backgroundColor);
            artistPanel.getElement().getStyle().setProperty("backgroundColor", backgroundColor);
            RoundedPanel rP = new RoundedPanel(artistPanel);
            rP.setCornerColor(backgroundColor);
            rP.addStyleName("largeMarginBottom");
            initWidget(rP);
        } else {
            artistPanel.addStyleName("largeMarginBottom");
            initWidget(artistPanel);
        }
    }

    public void setNbrStarsSelected(int nbrStars) {
        if (star != null) {
            star.setNbrSelectedStarsWithNoDbUpdate(nbrStars);
        }
    }

    public String getArtistId() {
        return artistId;
    }

    public void setSteerableResetTrue() {
        cdm.setSteerableReset(true);
    }

    public void onTagClick(ItemInfo tag) {
        History.newItem("tag:"+tag.getId());
    }

    public void doRemoveListeners() {
        if (star != null) {
            star.onDelete();
        }
        if (playButton != null) {
            playButton.onDelete();
        }
    }

    private Panel getNDistinctiveTags(String header, ArtistCompact aD, int n) {
        List<ItemInfo> tagList = new ArrayList<ItemInfo>();
        for (ItemInfo i : aD.getDistinctiveTags()) {
            tagList.add(i);
        }
        return getNDistinctiveTags(header, tagList, n);
    }

    private Panel getNDistinctiveTags(String header, Set<String> tagSet, int n) {
        List<ItemInfo> tagList = new ArrayList<ItemInfo>();
        for (String s : tagSet) {
            tagList.add(new ItemInfo(ClientDataManager.nameToKey(s), s, Random.nextDouble(), Random.nextDouble()));
        }
        return getNDistinctiveTags(header, tagList, n);
    }

    /**
     * Stores the n first distinctive tags for an artist in a comma seperated string
     * @param aD artist's details
     * @param n number of tags
     * @return comma seperated string
     */
    private FlowPanel getNDistinctiveTags(String header, List<ItemInfo> tagList, int n) {

        Collections.sort(tagList, ItemInfo.getScoreSorter());

        FlowPanel tagPanel = new FlowPanel();
        SpannedFlowPanel addTagPanel = new SpannedFlowPanel();

        SpannedLabel title = new SpannedLabel(header);
        title.addStyleName("pointer");
        title.addClickHandler(new DEClickHandler<Panel>(addTagPanel) {
            @Override
            public void onClick(ClickEvent ce) {
                if (data.isVisible()) {
                    data.setVisible(false);
                } else {
                    data.setVisible(true);
                }
            }
        });
        title.addStyleName("bold");
        tagPanel.add(title);
        for (int i = 0; i < tagList.size(); i++) {
            ContextMenuSpannedLabel t = new ContextMenuTagLabel(tagList.get(i), cdm);
            t.addStyleName("pointer");

            // Add main click listener
            t.addClickHandler(new DEClickHandler<ItemInfo>(tagList.get(i)) {
                @Override
                public void onClick(ClickEvent event) {
                    onTagClick(data);
                }
            });

            if (i < n) {
                tagPanel.add(t);
                // If we're not on the last tag
                if (i != n - 1) {
                    tagPanel.add(new SpannedLabel(", "));
                }
            } else {
                addTagPanel.add(new SpannedLabel(", "));
                addTagPanel.add(t);
            }
        }

        addTagPanel.setVisible(false);
        tagPanel.add(addTagPanel);
        return tagPanel;
    }
    
    /*private class MouseOverRollImage extends ContextMenuImage {

        private ArtistPhoto[] photos;
        private int index = 0;
        private boolean allLoaded = false;
        private int lastX = 0;
        private int lastY = 0;

        public MouseOverRollImage(ArtistCompact aC) {
            super();

            // @todo improve this
            // cheaply hardcode the width and height to deal with albums images
            // being a lot bigger than flickr thumbnails
            super.setHeight("75px");
            super.setWidth("75px");

            index = 0;
            ArtistPhoto[] aCphotos = aC.getPhotos();
            AlbumDetails[] albumDetails = aC.getAlbums();

            if (aCphotos != null && aCphotos.length > 0) {
                photos = aCphotos;
            } else if (albumDetails != null && albumDetails.length > 0) {
                for (AlbumDetails aD : albumDetails) {
                    if (aD.getAlbumArt() != null && aD.getAlbumArt().length() > 0) {
                        photos = new ArtistPhoto[1];
                        photos[0] = new ArtistPhoto();
                        photos[0].setThumbNailImageUrl(aD.getAlbumArt());
                    }
                }
                if (photos == null) {
                    setUrl("nopic.gif");
                }
            } else {
                setUrl("nopic.gif");
            }

            this.addMouseListener(new MouseListener() {

                public void onMouseMove(Widget arg0, int arg1, int arg2) {
                    if (Math.abs(lastX - arg1) + Math.abs(lastY - arg2) > 5) {
                        showNextImage();
                        lastX = arg1;
                        lastY = arg2;
                    }
                }

                public void onMouseDown(Widget arg0, int arg1, int arg2) {}
                public void onMouseEnter(Widget arg0) {}
                public void onMouseLeave(Widget arg0) {}
                public void onMouseUp(Widget arg0, int arg1, int arg2) {}
            });
            showNextImage();

        }

        public void showNextImage() {
            if (photos != null) {
                setUrl(photos[index++].getThumbNailImageUrl());
                if (index >= photos.length) {
                    index = 0;
                    allLoaded = true;
                } else if (!allLoaded) {
                    Image.prefetch(photos[index].getThumbNailImageUrl());
                }
            }
        }
    }*/

}
