package org.ijsberg.iglu.configuration.sample;

public class Listener implements ListenerInterface {
	
	private String id;
	private String lastMessage;
	
	public Listener(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void notify(String message) {
		lastMessage = message;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	
}
