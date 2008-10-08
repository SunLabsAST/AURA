/*
 * ContingencyTable.java
 *
 * Created on October 19, 2006, 8:21 AM
 *
 */

package com.sun.labs.aura.util.classifiers;

/**
 *
 * A class that will hold a contingency table for a class.
 * We're going to build a contingency table per class that we're
 * evaluating.  The contingency table for a single class looks
 * like:
 *
 *  <table>
 *  <tr><td></td>             <td>YES is correct</td><td>NO is correct</td></tr>
 *  <tr><td>Assigned YES </td><td>a</td>             <td>b</td></tr>
 *  <tr><td>Assigned NO  </td><td>c </td>            <td>d</td></tr>
 *  </table>
 *
 *   This is the format given in Yang's IRJ paper from 99.
 *
 * @author Stephen Green <stephen.green@sun.com>
 */
public class ContingencyTable implements Comparable<ContingencyTable> {
    
    public int test;
    
    public int train;
    
    public int a;
    
    public int b;
    
    public int c;
    
    public int d;
    
    public int N;
    
    float r = Float.NaN;
    
    float p = Float.NaN;
    
    float f1 = Float.NaN;
    
    float mi = Float.NaN;
    
    float chisq = Float.NaN;
    
    public String name;
    
    public ContingencyTable() {
    }
    
    public ContingencyTable(String name) {
        this.name = name;
    }
    
    /**
     * Adds another table to this one.
     *
     * @param t the table to add to this one.
     */
    public void add(ContingencyTable t) {
        a += t.a;
        b += t.b;
        c += t.c;
        d += t.d;
    }
    
    /**
     * Computes the sum of the four table entries.
     */
    public int N() {
        if(N == 0) {
            N = a+b+c+d;
        }
        return N;
    }
    
    /**
     * Computes the recall, given the contingency table.  Recall is
     * defined as a / (a+c).
     *
     * @return the recall, or -1 if (a+c) is 0.
     */
    public float recall() {
        if(Float.isNaN(r)) {
            if(a+c > 0) {
                r = a / (float) (a+c);
            } else {
                r = Float.NaN;
            }
        }
        return r;
    }
    
    /**
     * Computes the precision, given the contingency table.  Precision
     * is defined as a/(a+b).
     *
     * @return the recall, or -1 if (a+b) is 0.
     */
    public float precision() {
        if(Float.isNaN(p)) {
            if(a+b > 0) {
                p =  a / (float) (a+b);
            } else {
                p = Float.NaN;
            }
        }
        return p;
    }
    
    /**
     * Computes the F1 score, a combination of precision and recall
     * defined as (2*recall*precision) / (recall+precision)
     *
     * @return the F1 score, or -1 if either recall or precision is
     * undefined.
     */
    public float f1() {
        if(Float.isNaN(f1)) {
            if(Float.isNaN(recall()) || Float.isNaN(precision())) {
                f1 = Float.NaN;
            } else {
                f1 = (2*recall()*precision()) / (recall()+precision());
            }
        }
        return f1;
    }
    
    /**
     * Computes the mutual information embodied in the table.
     *
     */
    public float mi() {
        if(Float.isNaN(mi)) {
            
            //
            // Avoid low frequency terms.
            if(a+b < 5) {
                mi = 0;
            } else {
                mi = (a * N()) / (float) ((a+c) * (a+b));
            }
        }
        return mi;
    }
    
    /**
     * Computes the Chi-squared statistic for the information embodied in the
     * table.
     */
    public float chisq() {
        if(Float.isNaN(chisq)) {
            chisq = ((float)N() * (a*d - c*b) * (a*d - c*b)) /
                    ((float)(a+c) * (b+d) * (a+b) * (c+d));
        }
        return chisq;
    }
    
    public int compareTo(ContingencyTable o) {
        float myf1 = f1();
        float of1 = o.f1();
        
        if(Float.isNaN(myf1) || myf1 < of1) {
            return -1;
        }
        
        if(Float.isNaN(of1) || myf1 > of1) {
            return 1;
        }
        
        return 0;
    }
    
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        } else {
            return (a * b) + (c * d);
        }
    }
    
    public boolean equals(Object o) {
        if (o instanceof ContingencyTable) {
            ContingencyTable oct = (ContingencyTable)o;
            if (a == oct.a &&
                    b == oct.b &&
                    c == oct.c &&
                    d == oct.d &&
                    test == oct.test &&
                    train == oct.train) {
                if ((name != null && oct.name != null && name.equals(oct.name))
                    || (name == null && oct.name == null)) {
                    return true;
                }
            }
            
        }
        return false;
    }
}
