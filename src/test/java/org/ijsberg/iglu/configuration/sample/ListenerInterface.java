package org.ijsberg.iglu.configuration.sample;

public interface ListenerInterface {

	public abstract String getLastMessage();

	public abstract void notify(String message);

	public abstract String getId();

}
