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

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public abstract class PopularitySelect extends Composite {

    private ListBox l;
    
    public PopularitySelect() {
        init("ALL");
    }
    
    public PopularitySelect(String valueToSelect) {
        init(valueToSelect);
    }
    
    private void init(String itemToSelect) {
        
        l = new ListBox(false);
        l.addItem("All", "ALL");
        l.addItem("Popular", "HEAD");
        l.addItem("Mainstream", "HEAD_MID");
        l.addItem("Hipster", "MID_TAIL");
        l.addItem("Rarities", "TAIL");
        
        for (int i = 0; i < l.getItemCount(); i++) {
            if (l.getValue(i).equals(itemToSelect)) {
                l.setSelectedIndex(i);
            }
        }

        l.addChangeListener(new ChangeListener() {

            public void onChange(Widget sender) {
                onSelectionChange(getSelectedValue());
            }
        });

        initWidget(l);
    }

    public String getSelectedValue() {
        return l.getValue(l.getSelectedIndex());
    }

    /**
     * Called when the selection is changed
     * @param newPopularity new popularity value selected
     */
    public abstract void onSelectionChange(String newPopularity);
    
}
