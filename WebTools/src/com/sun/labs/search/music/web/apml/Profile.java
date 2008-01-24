/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
