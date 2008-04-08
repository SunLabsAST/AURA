/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.renderer.ColorRGBA;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ColorManager {

    private Map<String, ColorRGBA> colormap = new HashMap();

    public ColorRGBA getColorRGBA(String tag) {

        ColorRGBA color;
        if (tag == null) {
            color = ColorRGBA.gray;
        } else {
            color = colormap.get(tag);
            if (color == null) {
                color = ColorRGBA.randomColor();
                colormap.put(tag, color);
            }
        }
        return color;
    }

    public Color getColor(String tag) {
        ColorRGBA color = getColorRGBA(tag);
        return convert(color);
    }

    public static Color convert(ColorRGBA color) {
        return new Color(color.asIntRGBA(), false);
    }
}
