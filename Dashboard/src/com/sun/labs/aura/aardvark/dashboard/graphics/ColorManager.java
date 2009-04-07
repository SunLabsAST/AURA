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
