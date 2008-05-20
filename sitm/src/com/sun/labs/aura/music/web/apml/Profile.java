/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.apml;

/**
 *
 * @author plamere
 */
public class Profile {
    private String name;
    private Concept[] implicitConcepts;
    private Concept[] explicitConcepts;

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

        sb.append("            <Concepts>\n");
        for (Concept concept : getImplicitConcepts()) {
            sb.append("                " + concept + "\n");
        }
        sb.append("            </Concepts>\n");

        sb.append("  </ImplicitData>\n");
        sb.append("  <ExplicitData>\n");
        sb.append("            <Concepts>\n");
        for (Concept concept : getExplicitConcepts()) {
            sb.append("                " + concept + "\n");
        }
        sb.append("            </Concepts>\n");
        sb.append("  </ExplicitData>\n");
        sb.append("</Profile>\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toXML();
    }
    
}
