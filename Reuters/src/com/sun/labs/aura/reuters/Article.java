package com.sun.labs.aura.reuters;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 */
public class Article extends ItemAdapter implements Serializable {

    public static final String FIELD_ITEM_ID = "itemID";

    public static final String FIELD_TITLE = "title";

    public static final String FIELD_HEADLINE = "headline";

    public static final String FIELD_BYLINE = "byline";

    public static final String FIELD_TEXT = "text";

    public static final String FIELD_COUNTRIES = "countries";

    public static final String FIELD_INDUSTRIES = "industries";

    public static final String FIELD_TOPICS = "topics";

    public static final String FIELD_PUBLISHED = "published";

    public static final String FIELD_COUNTRY = "country";

    public static final String[] INDEXED_FIELDS = {
        FIELD_ITEM_ID, FIELD_TITLE, FIELD_HEADLINE,
        FIELD_BYLINE, FIELD_TEXT, FIELD_COUNTRIES,
        FIELD_INDUSTRIES, FIELD_TOPICS, 
        FIELD_COUNTRY
    };

    public static Logger logger = Logger.getLogger(Article.class.getName());

    public Article() {
        
    }

    public Article(Item item) {
        super(item, Item.ItemType.ARTICLE);
    }
    
    public Article(String key) throws AuraException {
        item = StoreFactory.newItem(Item.ItemType.ARTICLE, key, key);
    }

    public void setTitle(String title) {
        setField(FIELD_TITLE, title);
    }

    public void setHeadline(String headline) {
        setField(FIELD_HEADLINE, headline);
    }
    
    public void setByline(String byline) {
        setField(FIELD_BYLINE, byline);
    }

    public void addCountry(String country) {
        appendToField(FIELD_COUNTRIES, country);
    }

    public void addIndustry(String industry) {
        appendToField(FIELD_INDUSTRIES, industry);
    }

    public void addTopic(String topic) {
        appendToField(FIELD_TOPICS, topic);
    }

    public void setPublished(String published) {
        setFieldAsObject(FIELD_PUBLISHED, published);
    }

    public void setPublished(Date published) {
        setFieldAsObject(FIELD_PUBLISHED, published);
    }

    public void setPublished(long published) {
        setFieldAsObject(FIELD_PUBLISHED, new Date(published));
    }

    @Override
    public void defineFields(DataStore ds) throws AuraException {
        try {
            for(String field : INDEXED_FIELDS) {
                ds.defineField(field, FieldType.STRING, StoreFactory.INDEXED);
            }
            ds.defineField(FIELD_PUBLISHED, FieldType.DATE, null);
        } catch(RemoteException ex) {
            throw new AuraException("Error defining article fields", ex);
        }
    }
}
