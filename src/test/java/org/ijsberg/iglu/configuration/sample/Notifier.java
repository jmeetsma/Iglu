package org.ijsberg.iglu.configuration.sample;

import java.util.HashMap;

public class Notifier implements NotifierInterface {
	
	private HashMap<String, ListenerInterface> registeredListeners = new HashMap<String, ListenerInterface>();
	
	public void register(ListenerInterface listener) {
		registeredListeners.put(listener.getId(), listener);
	}
	
	public void unregister(ListenerInterface listener) {
		registeredListeners.remove(listener.getId());
	}

	public int getNrofRegisteredListeners() {
		return registeredListeners.size();
	}

}
