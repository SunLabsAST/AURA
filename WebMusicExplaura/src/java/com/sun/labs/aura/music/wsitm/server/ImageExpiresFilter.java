package com.sun.labs.aura.music.wsitm.server;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter that adds an appropriate expires header to all GWT image bundle
 * requests.
 */
public class ImageExpiresFilter implements Filter {

    Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The recommended "forever" cache time, one year.
     */
    private static final long cacheMillis = TimeUnit.DAYS.toMillis(14);

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public ImageExpiresFilter() {
    }

    /**
     * Add an expires header to the response when it's something we should be caching.
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String requestURI = ((HttpServletRequest) request).getRequestURI();
        //
        // Cache things a year in the future.
        if(requestURI.contains(".cache.") ||
                requestURI.endsWith(".css")) {
            ((HttpServletResponse) response).addDateHeader("Expires",
                    System.currentTimeMillis() + cacheMillis);
        }
        chain.doFilter(request, response);
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter 
     */
    public void destroy() {
    }

    /**
     * Init method for this filter 
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if(filterConfig != null) {
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if(filterConfig == null) {
            return ("ImageExpiresFilter()");
        }
        StringBuffer sb = new StringBuffer("ImageExpiresFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }
}
