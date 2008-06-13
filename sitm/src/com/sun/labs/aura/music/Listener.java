/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;

/**
 *
 * @author plamere
 */
public class Listener extends ItemAdapter {
    public final static String FIELD_AGE =             "LISTENER_AGE";
    public final static String FIELD_GENDER =         "LISTENER_GENDER";
    public final static String FIELD_LAST_FM_NAME =   "LISTENER_LAST_FM_NAME";
    public final static String FIELD_PANDORA_NAME =   "LISTENER_PANDORA_NAME";
    public enum Gender { Male, Female, Unknown };

    @Override
    public void defineFields(DataStore ds) throws AuraException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Listener(Item item) {
        super(item, Item.ItemType.USER);
    }

    public int getAge() {
        return getFieldAsInt(FIELD_AGE);
    }

    public Gender getGender() {
        return Gender.valueOf(getFieldAsString(FIELD_GENDER));
    }

    public String getLastFmName() {
        return getFieldAsString(FIELD_LAST_FM_NAME);
    }

    public String getPandoraName() {
        return getFieldAsString(FIELD_LAST_FM_NAME);
    }
}
