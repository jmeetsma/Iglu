package org.ijsberg.iglu.configuration.sample;

import java.io.Serializable;

public class Apple implements AppleInterface {
	
	private String message;
	private BananaInterface banana;
	private int someInt;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		//invoked by setProperties
		this.message = message;
	}

	public Object returnInput(Object input) {
		return input;
	}
	
	
	public void setBanana(BananaInterface banana) {
		this.banana = banana;
	}
	
	public void setBanana(Serializable banana) {
	}

	public int getIntFromBanana() {
		return banana.returnAnInt();
	}
	
	public void setSomeInt(int someInt) {
		this.someInt = someInt;
	}
	
	public int getSomeInt() {
		return someInt;
	}

	protected void touchCore() {
	}
	
}
