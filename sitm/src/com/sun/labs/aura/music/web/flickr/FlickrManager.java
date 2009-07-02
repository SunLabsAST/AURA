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

package com.sun.labs.aura.music.web.flickr;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.photos.PhotosInterface;
import com.sun.labs.aura.music.web.WebServiceAccessor;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xml.sax.SAXException;

/**
 *
 * @author plamere
 */
public class FlickrManager extends WebServiceAccessor {

    private String FLICKR_SHARED_SECRET;

    private final static int MAX_PER_PAGE = 500;
    private final static boolean ONLY_ATTRIBUTION_LICENSE = false;
    private final static boolean ONLY_CC_LICENSE = true;
    private final static Image[] EMPTY = new Image[0];
    private Set<String> bannedPhotographers = new HashSet<String>();

    /**
     * Creates a new instance of FlickrManager
     */
    public FlickrManager() throws AuraException {

        super("Flickr", "FLICKR_API_KEY");
        FLICKR_SHARED_SECRET = getProperty("FLICKR_SHARED_SECRET");

        // ban photos by photographers that tag spam
        bannedPhotographers.add("phlezk");
    }

    public Image[] getPhotosForArtist(String name, int maxCount) {
        List<Image> imageList = new ArrayList<Image>();

        imageList.addAll(getImagesByTags(new String[]{normalizeName(name, false), "concert"}, true, maxCount, true));
        //imageList.addAll(getImagesByTags(normalizeName(name, true) + " concert", true, maxCount, true));

        if (imageList.size() < maxCount) {
            imageList.addAll(getImagesByTags(new String[]{normalizeName(name, false), "show"}, true, maxCount - imageList.size(), true));
        }

        if (imageList.size() < maxCount) {
            imageList.addAll(getImagesByText(normalizeName(name, false) + " concert", true, maxCount - imageList.size(), true));
        }

        if (imageList.size() < maxCount) {
            imageList.addAll(getImagesByText(normalizeName(name, false), true, maxCount - imageList.size(), true));
        }

        if (imageList.size() < maxCount) {
            imageList.addAll(getImagesByTags(new String[]{normalizeName(name, false)}, true, maxCount - imageList.size(), true));
        }

        if (imageList.size() < maxCount) {
            imageList.addAll(getImagesByTags(normalizeName(name, false).split("\\s+"), true, maxCount - imageList.size(), true));
        }

        if (imageList.size() < maxCount) {
            imageList.addAll(getImagesByTags(new String[]{normalizeName(name, true)}, true, maxCount - imageList.size(), true));
        }

        return imageList.toArray(EMPTY);
    }

    private String normalizeName(String name, boolean removeSpace) {
        name = name.toLowerCase();
        if (removeSpace) {
            name = name.replaceAll("^the\\s+", "");
            name = name.replaceAll(" ", "");
        }
        name = name.replaceAll("&", " ");
        name = name.replaceAll("\\s+and\\s+", " ");
        name = name.replaceAll(",", " ");
        //name = name.replaceFirst("\\s+the\\s+", "");
        return name;
    }

    private List<Image> getImagesByTags(String[] tags, boolean all, int max, boolean interesting) {
        Flickr flickr = new Flickr(API_KEY);
        flickr.setSharedSecret(FLICKR_SHARED_SECRET);
        PhotosInterface photosInterface = flickr.getPhotosInterface();

        List<Image> list = new ArrayList<Image>(max);

        if (tags.length == 0) {
            return list;
        }

        if (max > 500) {
            max = 500;
        }

        SearchParameters search = new SearchParameters();
        search.setTags(tags);
        if (false) {
            for (String s : tags) {
                System.out.print("'" + s + "' ");
            }
            System.out.println("");
        }
        //search.setText(text);

        if (ONLY_ATTRIBUTION_LICENSE) {
            search.setLicense("4");
        } else if (ONLY_CC_LICENSE) {
            search.setLicense("1,2,3,4,5,6");
        }
        if (all) {
            search.setTagMode("all");
        } else {
            search.setTagMode("any");
        }
        if (interesting) {
            search.setSort(SearchParameters.INTERESTINGNESS_DESC);
        } else {
            search.setSort(SearchParameters.RELEVANCE);
        }


        try {
            PhotoList photos = null;
            search.setMedia("photos");

            photos = photosInterface.search(search, max, 1);
            for (Object oPhoto : photos) {
                Photo photo = (Photo) oPhoto;
                photo = photosInterface.getPhoto(photo.getId());
                Image image = new Image();
                image.setTitle(photo.getTitle());
                image.setImageURL(photo.getMediumUrl());
                image.setId(photo.getId());
                image.setCreatorRealName(photo.getOwner().getRealName());
                image.setCreatorUserName(photo.getOwner().getUsername());
                image.setPhotoPageURL(photo.getUrl());
                image.setSmallImageUrl(photo.getSmallUrl());
                image.setThumbNailImageUrl(photo.getSmallSquareUrl());

                if (!bannedPhotographers.contains(image.getCreatorUserName())) {
                    list.add(image);
                }
            }
        } catch (IOException ex) {
            error("Trouble talking to flickr");
            delay(1);
        } catch (SAXException ex) {
            error("Trouble parsing flickr results");
            delay(1);
        } catch (FlickrException ex) {
            error("Flickr is complaining " + ex);
            ex.printStackTrace();
            delay(1);
        } catch (Exception e) {
            error("Unexpected exception " + e);
            e.printStackTrace();
            delay(1);
        }
        return list;
    }

    private List<Image> getImagesByText(String text, boolean all, int max, boolean interesting) {
        Flickr flickr = new Flickr(API_KEY);
        flickr.setSharedSecret(FLICKR_SHARED_SECRET);
        PhotosInterface photosInterface = flickr.getPhotosInterface();

        List<Image> list = new ArrayList<Image>(max);

        if (text.length() == 0) {
            return list;
        }

        if (max > 500) {
            max = 500;
        }

        SearchParameters search = new SearchParameters();
        search.setText(text);
        if (false) {
            System.out.println("'" + text + "' ");
        }
        //search.setText(text);

        if (ONLY_ATTRIBUTION_LICENSE) {
            search.setLicense("4");
        } else if (ONLY_CC_LICENSE) {
            search.setLicense("1,2,3,4,5,6");
        }
        if (all) {
            search.setTagMode("all");
        } else {
            search.setTagMode("any");
        }
        if (interesting) {
            search.setSort(SearchParameters.INTERESTINGNESS_DESC);
        } else {
            search.setSort(SearchParameters.RELEVANCE);
        }


        try {
            PhotoList photos = null;
            search.setMedia("photos");

            photos = photosInterface.search(search, max, 1);
            for (Object oPhoto : photos) {
                Photo photo = (Photo) oPhoto;
                photo = photosInterface.getPhoto(photo.getId());
                Image image = new Image();
                image.setTitle(photo.getTitle());
                image.setImageURL(photo.getMediumUrl());
                image.setId(photo.getId());
                image.setCreatorRealName(photo.getOwner().getRealName());
                image.setCreatorUserName(photo.getOwner().getUsername());
                image.setPhotoPageURL(photo.getUrl());
                image.setSmallImageUrl(photo.getSmallUrl());
                image.setThumbNailImageUrl(photo.getSmallSquareUrl());

                if (!bannedPhotographers.contains(image.getCreatorUserName())) {
                    list.add(image);
                }
            }
        } catch (IOException ex) {
            error("Trouble talking to flickr");
            delay(1);
        } catch (SAXException ex) {
            error("Trouble parsing flickr results");
            delay(1);
        } catch (FlickrException ex) {
            error("Flickr is complaining " + ex);
            delay(1);
        } catch (Exception e) {
            error("Unexpected exception " + e);
            e.printStackTrace();
            delay(1);
        }
        return list;
    }

    private void error(String msg) {
        System.err.println("Flickr Error: " + msg);
    }

    private void delay(int secs) {
        try {
            Thread.sleep(secs * 1000L);
        } catch (InterruptedException ie) {
        }
    }
    private static int totalImages = 0;
    private static int totalGoodArtists = 0;
    private static int totalArtists = 0;

    private static void dumpPhotosFull(FlickrManager fm, String name) {
        totalArtists++;
        Image[] images = fm.getPhotosForArtist(name, 10);
        System.out.println(name + ":" + images.length);
        totalImages += images.length;
        if (images.length > 0) {
            totalGoodArtists++;
        }
        if (true) {
            for (Image image : images) {
                image.dump();
            }
        }
    }

    private static void dumpPhotos(FlickrManager fm, String name) {

        totalArtists++;
        Image[] images = fm.getPhotosForArtist(name, 10);
        System.out.println("<h2>" + name + "</h2>");
        System.out.println("Total images: " + images.length);
        System.out.println("<p>");
        totalImages += images.length;
        if (images.length > 0) {
            totalGoodArtists++;
        }
        if (true) {
            for (Image image : images) {
                System.out.printf("<img src='%s'/><p>%s<p>\n", image.getImageURL(), image.getTitle());
            }
        }
    }

    private static void troublesomeArtists(FlickrManager fm) {
        dumpPhotos(fm, "cake");
        dumpPhotos(fm, "blur");
        dumpPhotos(fm, "muse");
        dumpPhotos(fm, "the beatles");
        dumpPhotos(fm, "Stevie Ray Vaughan and Double Trouble");
        dumpPhotos(fm, "Thelonious Monk");
        dumpPhotos(fm, "The Byrds");
        dumpPhotos(fm, "Kari Ruesl�tten");
        dumpPhotos(fm, "Boards of Canada");
        dumpPhotos(fm, "Nick Drake");
        dumpPhotos(fm, "Enya");
        dumpPhotos(fm, "T�l�popmusik");
        dumpPhotos(fm, "Cat Stevens");
        dumpPhotos(fm, "Hans Zimmer & Lisa Gerrard");
        dumpPhotos(fm, "Boozoo Bajou");
    }

    private static void nolongerTroublesome(FlickrManager fm) {
        dumpPhotos(fm, "The Dogs D'Amour");
        dumpPhotos(fm, "They Might Be Giants");
    }

    private static void troublesomeInternationalArtists(FlickrManager fm) {
        dumpPhotos(fm, "Mot�rhead");
        dumpPhotos(fm, "J�rgen Drews");
        dumpPhotos(fm, "R�yksopp");
        dumpPhotos(fm, "Mot�rhead");
        dumpPhotos(fm, "Bob Marley & The Wailers");
        dumpPhotos(fm, "Die �rzte");
        dumpPhotos(fm, "Beyonc�");
        dumpPhotos(fm, "The Jimi Hendrix Experience");
        dumpPhotos(fm, "T�l�popmusik");
        dumpPhotos(fm, "Jos� Gonz�lez");
        dumpPhotos(fm, "Blue �yster Cult");
        dumpPhotos(fm, "Cat Stevens");
        dumpPhotos(fm, "C�line Dion");
        dumpPhotos(fm, "Nightmares on Wax");
        dumpPhotos(fm, "Everything but the Girl");
        dumpPhotos(fm, "M�tley Cr�e");
        dumpPhotos(fm, "Michael Bubl�");
        dumpPhotos(fm, "Max�mo Park");
        dumpPhotos(fm, "The Byrds");
        dumpPhotos(fm, "Sin�ad O'Connor");
        dumpPhotos(fm, "Hans Zimmer");
        dumpPhotos(fm, "Hans Zimmer & Lisa Gerrard");
        dumpPhotos(fm, "Fila Brazillia");
        dumpPhotos(fm, "Ill Ni�o");
        dumpPhotos(fm, "They Might Be Giants");
        dumpPhotos(fm, "Kruder & Dorfmeister");
        dumpPhotos(fm, "Dr. Dre & Eminem");
        dumpPhotos(fm, "�milie Simon");
        dumpPhotos(fm, "Missy Elliott");
        dumpPhotos(fm, "Fr�d�ric Chopin");
        dumpPhotos(fm, "Funki Porcini");
        dumpPhotos(fm, "The Notorious B.I.G.");
        dumpPhotos(fm, "Stevie Ray Vaughan and Double Trouble");
        dumpPhotos(fm, "Boozoo Bajou");
        dumpPhotos(fm, "Franz Schubert");
        dumpPhotos(fm, "Gang Starr");
        dumpPhotos(fm, "Sarah Brightman");
        dumpPhotos(fm, "Felix Mendelssohn");
        dumpPhotos(fm, "Emerson, Lake & Palmer");
        dumpPhotos(fm, "Smoke City");
        dumpPhotos(fm, "Deep Forest");
        dumpPhotos(fm, "Georg Friedrich H�ndel");
        dumpPhotos(fm, "Sly & The Family Stone");
        dumpPhotos(fm, "Nitin Sawhney");
        dumpPhotos(fm, "Eva Cassidy");
        dumpPhotos(fm, "Queensr�che");
        dumpPhotos(fm, "Crosby, Stills, Nash & Young");
        dumpPhotos(fm, "Thelonious Monk");
        dumpPhotos(fm, "Peggy Lee");
        dumpPhotos(fm, "?????");
        dumpPhotos(fm, "Claude Debussy");
        dumpPhotos(fm, "Kelly Rowland");
        dumpPhotos(fm, "The Mamas & The Papas");
        dumpPhotos(fm, "Julie London");
        dumpPhotos(fm, "R�is�n Murphy");
        dumpPhotos(fm, "Caf� Tacuba");
        dumpPhotos(fm, "Mo' Horizons");
    }

    public static void main(String[] args) throws AuraException {
        FlickrManager fm = new FlickrManager();
        troublesomeArtists(fm);

        System.out.println("Total artists: " + totalArtists);
        System.out.println("Total good artists: " + totalGoodArtists);
        System.out.println("Total images: " + totalImages);
    }
}
