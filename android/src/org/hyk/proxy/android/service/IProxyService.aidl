package org.hyk.proxy.android.service;

import org.hyk.proxy.android.service.IProxyServiceCallback;

/**
 * an interface for calling on to a remote service
 */
interface IProxyService {

    /**
     * This allows Tor service to send messages back to the GUI
     */
    void registerCallback(IProxyServiceCallback cb);
    
    /**
     * Remove registered callback interface.
     */
    void unregisterCallback(IProxyServiceCallback cb);
    
    /**
    * Get a simple int status value for the state of Tor
    **/
    int getStatus();
    
    void start();
    void stop();
    
}
