/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.StringFieldDef;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class UniqueStore extends SimpleStore {

    private RecordDef r;
    private HashSet<String> names;

    public UniqueStore(String field, String[] data) {
        super(field, data);
        r = new RecordDef(new FieldDef[]{new StringFieldDef("name")});

        names = new HashSet<String>();
        for (String s : data) {
            names.add(s);
        }
    }

    public void add(String name) {
        if (!names.contains(name)) {
            super.add(r.createRecord(new String[]{name}));
            names.add(name);
        }
    }

    @Override
    public void add(Record record) {
        this.add(record.getFields()[0]);
    }

    @Override
    public void add(Record[] records) {
        for (Record rec : records) {
            this.add(rec);
        }
    }
}
