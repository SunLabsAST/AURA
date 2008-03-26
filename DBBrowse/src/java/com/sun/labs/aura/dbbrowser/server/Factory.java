/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.dbbrowser.client.AttnDesc;
import com.sun.labs.aura.dbbrowser.client.ItemDesc;
import java.util.Date;

/**
 *
 * @author ja151348
 */
public class Factory {
    public static ItemDesc itemDesc(Item i) {
        return new ItemDesc(i.getName(), i.getKey(), i.getType().toString());
    }

    public static AttnDesc attnDesc(Attention a) {
        return new AttnDesc(a.getSourceKey(),
                            a.getTargetKey(),
                            a.getType().toString(),
                            new Date(a.getTimeStamp()).toString());
    
    }
}
