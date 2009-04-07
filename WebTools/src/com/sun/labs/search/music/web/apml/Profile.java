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

package com.sun.labs.search.music.web.apml;

/**
 *
 * @author plamere
 */
public class Profile {

    private String name;
    private Concept[] implicitConcepts;
    private Concept[] explicitConcepts;
    public final static Concept[] EMPTY_CONCEPTS = new Concept[0];

    public Profile(String name, Concept[] implicitConcepts, Concept[] explicitConcepts) {
        this.name = name;
        this.implicitConcepts = implicitConcepts;
        this.explicitConcepts = explicitConcepts;
    }

    public Concept[] getExplicitConcepts() {
        return explicitConcepts;
    }

    public Concept[] getImplicitConcepts() {
        return implicitConcepts;
    }

    public String getName() {
        return name;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile name=\"" + getName() + "\">\n");

        sb.append("  <ImplicitData>\n");

        if (getImplicitConcepts().length > 0) {
            sb.append("            <Concepts>\n");
            for (Concept concept : getImplicitConcepts()) {
                sb.append("                " + concept.toXML(false) + "\n");
            }
            sb.append("            </Concepts>\n");
        }
        sb.append("  </ImplicitData>\n");

        sb.append("  <ExplicitData>\n");
        if (getExplicitConcepts().length > 0) {
            sb.append("            <Concepts>\n");
            for (Concept concept : getExplicitConcepts()) {
                sb.append("                " + concept.toXML(true) + "\n");
            }
            sb.append("            </Concepts>\n");
        }
        sb.append("  </ExplicitData>\n");

        sb.append("</Profile>\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toXML();
    }
}
