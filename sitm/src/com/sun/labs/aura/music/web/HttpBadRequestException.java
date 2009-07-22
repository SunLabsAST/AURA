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

package com.sun.labs.aura.music.web;

import com.sun.labs.aura.music.web.lastfm.LastFM2Impl;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author mailletf
 */
public class HttpBadRequestException extends IOException {

    private int responseCode;
    private Document doc;

    public HttpBadRequestException(String message, int responseCode, Document doc) {
        super(message);
        this.responseCode = responseCode;
        this.doc = doc;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Document getDocument() {
        return doc;
    }

    
    public boolean isLastFmTrackNotFound() throws IOException {
        return (isLastFm() &&
                LastFM2Impl.MSG_TRACK_NOT_FOUND.contains(getLastFmErrorMessage()));
    }

    public boolean isLastFm() throws IOException {
        Element docElement = doc.getDocumentElement();
        return docElement.getTagName().equals("lfm");
    }

    public int getLastFmErrorCode() throws IOException {
        Element docElement = doc.getDocumentElement();
        return XmlUtil.getElementAttributeAsInteger(docElement, "error", "code");
    }

    public String getLastFmErrorMessage() throws IOException {
        Element docElement = doc.getDocumentElement();
        return XmlUtil.getElementContents(docElement, "error");
    }

}
