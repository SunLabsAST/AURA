/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.Tag;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class ItemFormatter {

    public enum OutputType {

        Tiny, Small, Medium, Large, Full
    };
    private ItemType type;
    private Map<OutputType, Set<String>> dataSetMap = new HashMap<OutputType, Set<String>>();
    private Map<String, String> aliasMap = new HashMap<String, String>();
    private Map<String, ValueGetter> valueGetterMap = new HashMap<String, ValueGetter>();

    public ItemFormatter(ItemType type) {
        this.type = type;
        addValueGetter("dateAdded", new ValueGetter() {

            @Override
            public Object getValue(Item item, String name) {
                return new Date(item.getTimeAdded()).toString();
            }
        });
        for (OutputType outputType : OutputType.values()) {
            dataSetMap.put(outputType, new HashSet<String>());
        }
    }

    public void addToDataSet(OutputType outputType, String fieldName) {
        dataSetMap.get(outputType).add(fieldName);
    }

    public void addAlias(String fieldName, String alias) {
        aliasMap.put(fieldName, alias);
    }

    public void addValueGetter(String fieldName, ValueGetter getter) {
        valueGetterMap.put(fieldName, getter);
    }

    public String toXML(Item item, OutputType outputType) {
        return toXML(item, outputType, null);
    }

    public String toXML(Item item, OutputType outputType, Double score) {
        List<String> fieldNames = getFieldNames(item, outputType);
        if (outputType == OutputType.Tiny || fieldNames.size() == 0) {
            return getItemOpenTag(item, score, true);
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append(getItemOpenTag(item, score, false));

            for (String field : fieldNames) {
                sb.append(formatField(item, outputType, field));
            }

            sb.append(getItemCloseTag(item));
            return sb.toString();
        }
    }

    private List<String> getAllFieldNames(Item item) {
        List<String> fieldNames = new ArrayList<String>();
        for (Map.Entry<String, Serializable> e : item) {
            fieldNames.add(e.getKey());
        }
        fieldNames.addAll(valueGetterMap.keySet());
        Collections.sort(fieldNames);
        return fieldNames;
    }

    private List<String> getFieldNames(Item item, OutputType outputType) {
        if (outputType == OutputType.Full) {
            return getAllFieldNames(item);
        } else {
            return new ArrayList<String>(dataSetMap.get(outputType));
        }
    }

    private String getItemOpenTag(Item item, Double score, boolean close) {
        StringBuilder sb = new StringBuilder();
        String suffix = close ? "/>" : ">";
        String itemTypeName = getAlias(item.getType().name().toLowerCase());
        sb.append("<");
        sb.append(itemTypeName);
        sb.append(" key=");
        sb.append("\"" + Util.filter(item.getKey()) + "\"");
        sb.append(" name=");
        sb.append("\"" + Util.filter(item.getName()) + "\"");
        if (score != null) {
            sb.append(" score=");
            sb.append("\"" + score.toString() + "\"");
        }
        sb.append(suffix);
        return sb.toString();
    }

    private String getItemCloseTag(Item item) {
        String itemTypeName = getAlias(item.getType().name().toLowerCase());
        return "</" + itemTypeName + ">";
    }

    private boolean isWantedField(OutputType outputType, String fieldName) {
        if (outputType == OutputType.Tiny) {
            return false;
        } else if (outputType == OutputType.Full) {
            return true;
        } else {
            Set<String> validSet = dataSetMap.get(outputType);
            if (validSet != null) {
                return validSet.contains(fieldName);
            } else {
                return false;
            }
        }
    }

    private String getAlias(String fieldName) {
        String alias = aliasMap.get(fieldName);
        if (alias == null) {
            alias = fieldName;
        }
        return alias;
    }

    private String formatField(Item item, OutputType outputType, String fieldName) {
        StringBuilder sb = new StringBuilder();

        if (isWantedField(outputType, fieldName)) {
            fieldName = getAlias(fieldName);
            Object value = getValue(item, fieldName);
            if (value != null) {

                if (value instanceof Collection) {
                    sb.append(getOpenTag(fieldName + "-list"));
                    for (Object element : (Collection) value) {
                        sb.append(toXMLString(fieldName, element));
                    }
                    sb.append(getCloseTag(fieldName + "-list"));
                } else if (value instanceof Map) {
                    Map m = (Map) value;
                    sb.append(getOpenTag(fieldName + "-list"));
                    for (Object key : m.keySet()) {
                        sb.append(toXMLString(fieldName, m.get(key)));
                    }
                    sb.append(getCloseTag(fieldName + "-list"));
                } else {
                    sb.append(toXMLString(fieldName, value));
                }
            }
        }
        return sb.toString();
    }

    Object getValue(Item item, String fieldName) {
        Object value = null;
        ValueGetter valueGetter = valueGetterMap.get(fieldName);
        if (valueGetter != null) {
            value = valueGetter.getValue(item, fieldName);
        } else {
            value = item.getField(fieldName);
        }
        return value;
    }

    private String toXMLString(String fieldName, Object o) {
        if (o instanceof Tag) {
            Tag t = (Tag) o;
            return "<" + fieldName + " name=\"" + Util.filter(t.getName()) + "\" freq=\"" + t.getCount() + "\"/>";
        } else if (o == null) {
            return String.format("<%s>null</%s>", fieldName, fieldName);
        } else {
            return "<" + fieldName + ">" + Util.filter(o.toString()) + "</" + fieldName + ">";
        }
    }

    private String getOpenTag(String name) {
        return "<" + name + ">";
    }

    private String getCloseTag(String name) {
        return "</" + name + ">";
    }
}

interface ValueGetter {

    public Object getValue(Item item, String name);
}
