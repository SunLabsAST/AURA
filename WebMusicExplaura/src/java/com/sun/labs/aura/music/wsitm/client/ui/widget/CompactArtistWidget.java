/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget.WhyButton;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ArtistPhoto;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuImage;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuSpannedLabel;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuTagLabel;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedFlowPanel;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
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

    private String artistId;

    public CompactArtistWidget(ArtistCompact aC, ClientDataManager cdm,
            MusicSearchInterfaceAsync musicServer, WhyButton whyB, 
            int currentRating, Set<String> userTags) {

        this.cdm = cdm;
        this.musicServer = musicServer;

        artistId = aC.getId();

        HorizontalPanel artistPanel = new HorizontalPanel();
        artistPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
        artistPanel.setStyleName("artistPanel");
        artistPanel.setSpacing(5);

        ClickListener cL = new DataEmbededClickListener<String>("artist:" + aC.getId()) {

            public void onClick(Widget arg0) {
                History.newItem(data);
            }
        };

        ContextMenuImage img = new MouseOverRollImage(aC.getPhotos()); //aD.getBestArtistImage(true);
        if (img == null) {
            img = new ContextMenuImage("nopic.gif");
        }
        img.setStyleName("image");
        img.addClickListener(cL);

        artistPanel.add(img);

        VerticalPanel txtPanel = new VerticalPanel();


        HorizontalPanel aNamePanel = new HorizontalPanel();
        aNamePanel.setWidth("210px");
        aNamePanel.setSpacing(5);
        ContextMenuSpannedLabel aName = new ContextMenuSpannedLabel(aC.getName());
        aName.addClickListener(cL);
        aName.addStyleName("image");
        //
        //  Create context menu
        aName.getContextMenu().addItem("View artist details", new DataEmbededCommand<String>("artist:" + aC.getId()) {

            public void execute() {
                History.newItem(data);
            }
        });
        aName.getContextMenu().addItem("View tag cloud",
                new DualDataEmbededCommand<ArtistCompact, ClientDataManager>(aC, cdm) {

            public void execute() {
                TagDisplayLib.showTagCloud("Tag cloud for "+data.getName(), data.getDistinctiveTags(), sndData);
            }
        });
        //aName.getContextMenu().addSeperator();
        aName.getContextMenu().addItem("Start new steerable from artist's top tags",
                new DualDataEmbededCommand<String,ClientDataManager>(aC.getId(), cdm) {

            public void execute() {
                sndData.setSteerableReset(true);
                History.newItem("steering:" + data);
            }
        });
        aName.getContextMenu().addItem("Add artist to steerable",
                new DualDataEmbededCommand<ArtistCompact,ClientDataManager>(aC, cdm) {

            public void execute() {
                sndData.getSteerableTagCloudExternalController().addArtist(data);
            }
        });
        aName.getContextMenu().addItem("Add artist's top tags to steerable",
                new DualDataEmbededCommand<ItemInfo[],ClientDataManager>(aC.getDistinctiveTags(), cdm) {

            public void execute() {
                sndData.getSteerableTagCloudExternalController().addTags(data);
            }
        });
        
        aNamePanel.add(aName);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        Widget spotify = WebLib.getSpotifyListenWidget(aC, WebLib.PLAY_ICON_SIZE.SMALL,
                musicServer, cdm.isLoggedIn(), new DualDataEmbededClickListener<String, ClientDataManager>(aC.getId(), cdm) {

            public void onClick(Widget arg0) {
                sndData.getPlayedListenerManager().triggerOnPlay(data);
            }
        });
        spotify.getElement().setAttribute("style", "align : right;");
        buttonPanel.add(spotify);

        SteeringWheelWidget steerButton = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.SMALL, aC, cdm.getSharedSteeringMenu());
        buttonPanel.add(steerButton);

        if (whyB != null) {
            buttonPanel.add(whyB);
        }

        aNamePanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        aNamePanel.add(buttonPanel);

        txtPanel.add(aNamePanel);

        if (userTags != null && userTags.size() > 0) {
            Panel tagsLabel = getNDistinctiveTags("Your tags: ", userTags, 4);
            tagsLabel.setStyleName("recoTags");
            txtPanel.add(tagsLabel);
        }

        Panel tagsLabel = getNDistinctiveTags("Tags: ", aC, 4);
        tagsLabel.setStyleName("recoTags");
        txtPanel.add(tagsLabel);

        star = new StarRatingWidget(musicServer, cdm, aC.getId(),
                currentRating, StarRatingWidget.Size.SMALL);

        cdm.getRatingListenerManager().addListener(aC.getId(), star);
        cdm.getLoginListenerManager().addListener(star);

        Label starLbl = new Label("Your rating: ");
        starLbl.setStyleName("recoTags");
        starLbl.addStyleName("marginRight");
        starLbl.addStyleName("bold");
        HorizontalPanel starHP = new HorizontalPanel();
        starHP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        starHP.add(starLbl);
        starHP.add(star);
        txtPanel.add(starHP);

        txtPanel.add(WebLib.getSmallPopularityWidget(aC.getNormPopularity(), true, true));

        artistPanel.add(txtPanel);

        initWidget(artistPanel);
        setWidth("300px");
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
        star.onDelete();
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
    private Panel getNDistinctiveTags(String header, List<ItemInfo> tagList, int n) {

        Collections.sort(tagList, ItemInfo.getScoreSorter());

        FlowPanel tagPanel = new FlowPanel();
        SpannedFlowPanel addTagPanel = new SpannedFlowPanel();

        SpannedLabel title = new SpannedLabel(header);
        title.addStyleName("pointer");
        title.addClickListener(new DataEmbededClickListener<Panel>(addTagPanel) {

            public void onClick(Widget arg0) {
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
            t.addClickListener(new DataEmbededClickListener<ItemInfo>(tagList.get(i)) {
                public void onClick(Widget arg0) {
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
    
    private class MouseOverRollImage extends ContextMenuImage {

        private ArtistPhoto[] photos;
        private int index = 0;
        private boolean allLoaded = false;
        private int lastX = 0;
        private int lastY = 0;

        public MouseOverRollImage(ArtistPhoto[] photos) {
            super();
            if (photos != null && photos.length > 0) {

                this.photos = photos;
                index = 0;

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
            } else {
                setUrl("nopic.gif");
            }
        }

        public void showNextImage() {
            setUrl(photos[index++].getThumbNailImageUrl());
            if (index >= photos.length) {
                index = 0;
                allLoaded = true;
            } else if (!allLoaded) {
                Image.prefetch(photos[index].getThumbNailImageUrl());
            }
        }
    }
}