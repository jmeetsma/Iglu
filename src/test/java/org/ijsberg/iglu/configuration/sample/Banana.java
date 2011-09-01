package org.ijsberg.iglu.configuration.sample;

import java.io.Serializable;

public class Banana implements BananaInterface, Serializable {
	
	private int returnValue;
	private AppleInterface sampleInterface1;
	
	public Banana(int returnValue) {
		this.returnValue = returnValue;
	}

	public int returnAnInt() {
		return returnValue;
	}
	
	public String getMessageFromApple() {
		return sampleInterface1.getMessage();
	}
	
	public void setApple(AppleInterface sampleInterface) {
		this.sampleInterface1 = sampleInterface;
	}

}
