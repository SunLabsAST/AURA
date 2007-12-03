/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.search.music.web.apml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author plamere
 */
public class APML {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private String title;
    private String profile;
    private Concept[] implicit;
    private Concept[] explicit;

    public APML(String title, String profile, Concept[] implicit, Concept[] explicit) {
        this.title = title;
        this.profile = profile;
        this.implicit = implicit;
        this.explicit = explicit;
    }

    public String getTitle() {
        return title;
    }

    public String getProfile() {
        return profile;
    }

    public Concept[] getExplicit() {
        return explicit;
    }

    public Concept[] getImplicit() {
        return implicit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<APML xmlns=\"http://www.apml.org/apml-0.6\" version=\"0.6\" >\n");
        sb.append("<Head>\n");
        sb.append("   <Title>" + title + "</Title>\n");
        sb.append("   <Generator>Created by TasteBroker.org </Generator>\n");
        sb.append("   <DateCreated>" + sdf.format(new Date()) + "</DateCreated>\n");
        sb.append("</Head>\n");
        sb.append("<Body defaultprofile=\"" + profile + "\">\n");
        sb.append("    <Profile name=\"" + profile +"\">\n");


        if (implicit.length > 0) {
            sb.append("        <ImplicitData>\n");
            sb.append("            <Concepts>\n");

            for (Concept concept : implicit) {
                sb.append("                " + concept + "\n");
            }
            sb.append("            </Concepts>\n");
            sb.append("        </ImplicitData>\n");
        }


        if (explicit.length > 0) {
            sb.append("        <ExplicitData>\n");
            sb.append("            <Concepts>\n");

            for (Concept concept : explicit) {
                sb.append("                " + concept + "\n");
            }
            sb.append("            </Concepts>\n");
            sb.append("        </ExplicitData>\n");
        }
        sb.append("    </Profile>\n");
        sb.append("</Body>\n");
        sb.append("</APML>\n");
        return sb.toString();
    }
}
