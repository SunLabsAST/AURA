/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import java.util.ArrayList;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author mailletf
 */
public class HelpPopup {

    public enum HELP_SECTIONS {
        INTRO,
        TAG_CLOUD,
        STEERING,
        ICONS
    }

    private boolean isInit = false;
    private final PopupPanel popup;
    private Grid contentPanel;

    private ArrayList<HelpSection> sections;

    public HelpPopup() {

        popup = Popup.getPopupPanel();
        contentPanel = new Grid(1,1);
        contentPanel.setWidth("775px");

        sections = new ArrayList<HelpSection>();
        sections.add(new HelpSection(HELP_SECTIONS.INTRO, "Introduction"));
        sections.add(new HelpSection(HELP_SECTIONS.TAG_CLOUD, "Tag Cloud"));
        sections.add(new HelpSection(HELP_SECTIONS.STEERING, "Steerable recommendations"));
        //sections.add(new HelpSection(HELP_SECTIONS.ICONS, "Icons"));

    }

    /**
     * Build the menu of the pannel and create the rounded popup
     */
    private void buildPanel() {

        HorizontalPanel menu = new HorizontalPanel();
        menu.setStyleName("helpPopupMenu");
        menu.setWidth("460px");
        menu.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        for (HelpSection hS : sections) {
            Label sL = hS.lbl;
            sL.addClickHandler(new DEClickHandler<HELP_SECTIONS>(hS.section) {
                @Override
                public void onClick(ClickEvent event) {
                    showHelp(data);
                }
            });
            menu.add(sL);
            menu.add(new Label(" - "));
        }
        menu.remove(menu.getWidgetCount()-1);

        RoundedPanel rp = new RoundedPanel(menu, RoundedPanel.ALL, 3);
        rp.setCornerStyleName("helpPopupMenu");
        rp.setWidth("460px");
        rp.getElement().getStyle().setPropertyPx("marginBottom", 8);

        VerticalPanel vP = new VerticalPanel();
        vP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        vP.add(rp);
        vP.add(contentPanel);
        Popup.showRoundedPopup(vP, "Music Explaura Help", popup, 800);
        isInit = true;

    }

    /**
     * Goes through all the menu labels and sets only the active on to bold
     * @param activeSection
     */
    private void resetMenuBoldStyle(HELP_SECTIONS activeSection) {
        for (HelpSection hS : sections) {
            if (hS.section==activeSection) {
                hS.lbl.getElement().getStyle().setProperty("fontWeight", "bold");
            } else {
                hS.lbl.getElement().getStyle().setProperty("fontWeight", "normal");
            }
        }
    }

    private void buildSection(HelpSection hS) {
        if (hS.section == HELP_SECTIONS.STEERING) {
            hS.w = buildSectionSteering();
        } else if (hS.section == HELP_SECTIONS.INTRO) {
            hS.w = buildSectionIntro();
        } else if (hS.section == HELP_SECTIONS.TAG_CLOUD) {
            hS.w = buildSectionTagCloud();
        } else {
            hS.w = buildSectionNA();
        }

    }

    public void showHelp(HELP_SECTIONS sectionToShow) {
        if (!isInit) {
            buildPanel();
        }
        if (!popup.isShowing()) {
            popup.show();
        }
        // Find section to display
        for (HelpSection hS : sections) {
            if (hS.section==sectionToShow) {
                if (hS.w==null) {
                    buildSection(hS);
                }
                resetMenuBoldStyle(hS.section);
                contentPanel.setWidget(0, 0, hS.w);
            }
        }

    }


    ////
    //  Functions to build the different help sections
    ////

    private Widget buildSectionSteering() {

        HorizontalPanel hP = new HorizontalPanel();
        hP.setSpacing(8);
        hP.add(new Image("help-steer.png"));

        VerticalPanel vP = new VerticalPanel();
        vP.setWidth("300px");
        vP.setStyleName("helpTxt");
        vP.add(new HTML("<span class=\"tag1\">(1)</span> Search for an artist or tag. Click on its name to add it to your tag cloud."));
        vP.add(new HTML("<span class=\"tag1\">(2)</span> Click and drag a tag to grow it or shrink it. Right-click on a tag to display its context menu, where you can make it sticky, negative or delete it. "));
        vP.add(new HTML("<span class=\"tag1\">(3)</span> Get recommendations that match your tag cloud. You can obtain an explanation of why an artist is recommended by clicking on its \"why?\" link or listen to it by clicking on its Play button."));
        hP.add(vP);


        VerticalPanel mainVp = new VerticalPanel();
        mainVp.setWidth("775px");
        mainVp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        Label title = new Label("Steerable recommendations basics");
        title.addStyleName("tagPop2");
        mainVp.add(title);
        mainVp.add(hP);
        return mainVp;

    }


    private Widget buildSectionTagCloud() {

        String mainHtml = new String();
        mainHtml += "<div style=\"float: left; margin-right:7px\"><img src=\"help-cloud.png\"></div>";
        mainHtml += "<p class=\"helpTxt\" align=\"left\">In the Explaura, each artist is represented by a set of weighted descriptive words - its \"tag cloud\". A typical tag cloud, as displayed on the left, has a series of words of different sizes; the bigger the word, the higher its weight and the more it represents to the artist it is associated to.</p>";
        mainHtml += "<p class=\"helpTxt\" align=\"left\">When the Explaura recommends you artists, it does so by comparing the tag cloud of the artist you're looking at to those of all the other artists it knows about. The more two tags clouds overlap, the more similar the artists are considered. The artists you are being recommended thus have the most similar tag clouds to the seed artist.</p>";
        mainHtml += "<div style=\"float: right;\"><img src=\"help-why.png\"></div>";
        mainHtml += "<p class=\"helpTxt\" align=\"left\">The Explaura offers you transparent recommendations by explaining to you why artists are being recommended by showing you their common tags. Simply click on any recommended artist's \"why?\" button to see the overlap between its tag cloud and the seed artist's tag cloud.</p>";
        mainHtml += "<div style=\"float: left; margin-right:7px\"><img src=\"help-diff.png\"></div>";
        mainHtml += "<p class=\"helpTxt\" align=\"left\">Two artists may be very similar, and thus have a lot of common tags, but might also be very different when you consider other aspects of their music. When comparing two artists, you can use the \"diff\" link, right under the \"why?\" link, to display the difference tag cloud. In this cloud, the more a tag is common to both artists, the smaller it will be. The more unique a tag is to the first artist, the bigger it will be displayed on the top. Distinctive tags for the second artist will also be very big but displayed on the bottom and in different colors than the tags on the top. The smaller the tags get, the more common to both artists they are.</p>";
        mainHtml += "<p class=\"helpTxt\" align=\"left\">In this example, while Muse and Coldplay share the \"uk\" and \"rock\" tags (very small in the middle), Coldplay is much more \"piano rock\" and Muse is much more \"progressive rock\".</p>";
        HTML rH = new HTML(mainHtml);

        VerticalPanel mainVp = new VerticalPanel();
        mainVp.setWidth("775px");
        mainVp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        Label title = new Label("Tag cloud mechanics");
        title.addStyleName("tagPop2");
        mainVp.add(title);
        mainVp.add(rH);
        return mainVp;

    }

    private Widget buildSectionIntro() {
        String mainHtml = "<p class=\"helpTxt\" align=\"left\">Welcome to The Music Explaura! The Explaura is a musical artist recommendation application built using <a href=\"http://www.tastekeeper.com\" target=\"_blank\" style=\"color:#ffffff\">TasteKeeper</a> as its back end. Users can find artists they like, accentuate the aspects of those artists they enjoy the most and get recommendations for other artists. Users can steer the recommendations by adding terms that describe what they want and see explanations of why particular artists were recommended.</p>";
        mainHtml += "<br /><div class=\"tagPop2\" align=\"center\">Screencast of the Music Explaura in action</div>";
        mainHtml += "<center><object width=\"425\" height=\"344\"><param name=\"movie\" value=\"http://www.youtube.com/v/wBgwnKV892I&hl=en&fs=1&color1=0x2b405b&color2=0x6b8ab6\"></param><param name=\"allowFullScreen\" value=\"true\"></param><param name=\"allowscriptaccess\" value=\"always\"></param><embed src=\"http://www.youtube.com/v/wBgwnKV892I&hl=en&fs=1&color1=0x2b405b&color2=0x6b8ab6\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" allowfullscreen=\"true\" width=\"425\" height=\"344\"></embed></object></center>";
        return new HTML(mainHtml);
    }

    private Widget buildSectionNA() {
        Label l = new Label("Sorry, no help is available for this section.");
        l.addStyleName("popupColors");
        return l;
    }



    private class HelpSection {
        public HELP_SECTIONS section;
        public String name;
        /**
         * Content widget associated with this section. Built when required
         */
        public Widget w = null;
        /**
         * Label object in the help menu. Kept to be able to modify its style
         */
        public Label lbl = null;

        public HelpSection(HELP_SECTIONS section, String name) {
            this.section = section;
            this.name = name;
            this.lbl = new Label(this.name);
            this.lbl.addStyleName("pointer");
        }
    }

}
