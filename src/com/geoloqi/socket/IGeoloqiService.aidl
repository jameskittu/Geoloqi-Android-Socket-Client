package com.geoloqi.socket;

import com.geoloqi.socket.IGeoloqiServiceCallback;

interface IGeoloqiService {
	int getPid();
	int lastLocationDate();
	
    /**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
    void registerCallback(IGeoloqiServiceCallback cb);
    
    /**
     * Remove a previously registered callback interface.
     */
    void unregisterCallback(IGeoloqiServiceCallback cb);
}
