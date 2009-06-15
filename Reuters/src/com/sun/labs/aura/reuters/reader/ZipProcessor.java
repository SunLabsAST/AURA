package com.sun.labs.aura.reuters.reader;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.reuters.Article;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.minion.util.NanoWatch;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Processes a zip file full of articles.
 */
public class ZipProcessor {

    private DataStore ds;

    private ZipFile zf;

    private ArticleFactory af;

    private Logger logger = Logger.getLogger(ZipProcessor.class.getName());

    private NanoWatch nw = new NanoWatch();

    private NanoWatch pw = new NanoWatch();

    public ZipProcessor(DataStore ds, ArticleFactory af, String zipFile) throws
            IOException {
        this.ds = ds;
        this.af = af;
        try {
            zf = new ZipFile(zipFile);
        } catch(ZipException ex) {
            logger.log(Level.SEVERE, String.format(
                    "Exception opening zip file: %s", zipFile), ex);
            throw (ex);
        }

    }

    private void reportProgress(int np) {
        logger.info(String.format(
                "%s: processed %d entries in %.2fs (%.2fms/entry) (avg put %.2fms/entry)",
                                  zf.getName(), np,
                                  nw.getTimeMillis() / 1000,
                                  nw.getAvgTimeMillis(),
                                  pw.getAvgTimeMillis()));
    }

    public void process() throws IOException {
        Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zf.entries();
        int np = 0;
        while(en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if(ze.isDirectory()) {
                continue;
            }
            nw.start();
            Article art = af.getArticle(zf.getInputStream(ze));
            try {
                pw.start();
                ds.putItem(art.getItem());
                pw.stop();
            } catch(AuraException ex) {
                logger.log(Level.SEVERE,
                           String.format("Exception adding article %s from %s",
                                         ze.getName(), zf.getName()), ex);
            }
            nw.stop();
            np++;
            if(np % 200 == 0) {
                reportProgress(np);
            }
        }
        zf.close();
        if(np % 200 != 0) {
            reportProgress(np);
        }
    }
}
