package com.sun.labs.aura.service;

import com.sun.labs.util.props.Component;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface for a statistics collection and distribution service.
 */
public interface StatService extends Serializable, Remote, Component {
    
    /**
     * Sets a double value that can be retrieved later
     */
    public void setDouble(String name, double value) throws RemoteException;

    /**
     * Gets a double value that was previously set.  Will return 0 if
     * invoked for a value that doesn't exist.
     */
    public double getDouble(String name) throws RemoteException;
    
    /**
     * Creates a counter, initializes its value to zero and records the time
     * at which the counter was started.
     * 
     * @param counterName the name of the counter to add.  If the counter already
     * exits, then nothing happens.
     */
    public void create(String counterName) throws RemoteException;
    
    /**
     * Sets the named counter to the provided value.
     * @param counterName the name of the counter whose value we want to set
     * @param val the value to which the counter should be set
     */
    public void set(String counterName, long val) throws RemoteException;
    
    /**
     * Increments the value of the named counter by one.
     * @param counterName the name of the counter to increment
     * @return the value of the counter after the increment.
     */
    public long incr(String counterName) throws RemoteException;
    
    /**
     * Increments the value of the named counter.
     * @param counterName the name of the counter to increment
     * @param val the value to increment by
     * @return the value of the counter after the addition
     */
    public long incr(String counterName, int val) throws RemoteException;
    
    /**
     * Increments the value of the named counter.  This method provides a way
     * to batch increments to the service and still have averages come out
     * correctly.
     * 
     * @param counterName the name of the counter to increment
     * @param val the value to increment by
     * @param n the number of increments on the client side that this increment
     * represents.
     * @return the value of the counter after the addition
     */
    public long incr(String counterName, int val, int n) throws RemoteException;
    
    /**
     * Gets the value of the named counter.
     * @param counterName the name of the counter whose value is required.
     * @return the value of the counter
     */
    public long get(String counterName) throws RemoteException;
    
    /**
     * Gets the average value of the named counter.
     * @param counterName the name of the counter whose average value that 
     * we want.
     * @return the average value for the given counter
     */
    public double getAverage(String counterName) throws RemoteException;

    /**
     * Gets the average value of the named counter over the time period (in seconds) 
     * that data has been collected for the counter.
     * 
     * @param counterName the name of the counter whose average value we should
     * get.
     * @return the average change of the named counter per second over the time period 
     * that data has been collected for the counter.  If the named counter does
     * not exist, then a value less than zero will be returned.
     */
    public double getAveragePerSecond(String counterName) throws RemoteException;
    
    /**
     * Gets the average value of the named counter over the time period (in seconds) 
     * that data has been collected for the counter.
     * 
     * @param counterName the name of the counter whose average value we should
     * get.
     * @return the average change of the named counter per minute over the time period 
     * that data has been collected for the counter.  If the named counter does
     * not exist, then a value less than zero will be returned.
     */
    public double getAveragePerMinute(String counterName) throws RemoteException;

    /**
     * Gets all of the counter names
     * @return an array of the counter names
     * @throws java.rmi.RemoteException
     */
    public String[] getCounterNames() throws RemoteException;

    /**
     * Gets all of the names of the doubles in the stat service
     * @return an array of double names
     * @throws java.rmi.RemoteException
     */
    public String[] getDoubleNames() throws RemoteException;
}
