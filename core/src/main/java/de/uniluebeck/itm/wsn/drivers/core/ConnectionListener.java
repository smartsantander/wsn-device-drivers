package de.uniluebeck.itm.wsn.drivers.core;


/**
 * Listener for observating the state of a <code>Connection</code>.
 * 
 * @author Malte Legenhausen
 */
public interface ConnectionListener {
	
	/**
	 * This method is called when a connection state occured.
	 * 
	 * @param event The event object.
	 */
	void onConnectionChange(ConnectionEvent event);
}