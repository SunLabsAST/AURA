package com.sun.labs.aura.reuters.reader;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.reuters.Article;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Import implements AuraService, Configurable {

    @ConfigString(defaultValue = "files.lst")
    public static final String PROP_FILE_LIST = "fileList";

    private String fileList;

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore dataStore;

    @ConfigString(defaultValue = "industry_codes.txt")
    public static final String PROP_INDUSTRY_CODES = "industryCodes";

    private Codes industryCodes;

    @ConfigString(defaultValue = "region_codes.txt")
    public static final String PROP_REGION_CODES = "regionCodes";

    private Codes regionCodes;

    @ConfigString(defaultValue = "topic_codes.txt")
    public static final String PROP_TOPIC_CODES = "topicCodes";

    private Codes topicCodes;

    private ArticleFactory af;

    private static Logger logger;

    public void start() {
        try {
            ArticleFactory af = new ArticleFactory(industryCodes, regionCodes,
                                                   topicCodes);
            BufferedReader r = new BufferedReader(new FileReader(fileList));
            String zf;
            while((zf = r.readLine()) != null) {
                logger.info(String.format("Processing %s", zf));
                ZipProcessor zp = new ZipProcessor(dataStore, af, zf);
                zp.process();
            }
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Exception processing files", ex);
            return;
        }

    }

    public void stop() {
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        logger = ps.getLogger();
        fileList = ps.getString(PROP_FILE_LIST);
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        try {
            (new Article()).defineFields(dataStore);
        } catch(AuraException ex) {
            throw new PropertyException(ex, ps.getInstanceName(),
                                        PROP_DATA_STORE, "Error defining fields");
        }
        try {
            industryCodes = new Codes(ps.getString(PROP_INDUSTRY_CODES));
            regionCodes = new Codes(ps.getString(PROP_REGION_CODES));
            topicCodes = new Codes(ps.getString(PROP_TOPIC_CODES));
        } catch(IOException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), "code files",
                                        "Error reading code files");
        }
    }
}
