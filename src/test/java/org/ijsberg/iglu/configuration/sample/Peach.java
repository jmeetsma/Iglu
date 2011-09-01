package org.ijsberg.iglu.configuration.sample;

import java.util.Properties;

public class Peach implements PeachInterface{
	
	String color;
	
	public void setProperties(Properties properties) {
		color = properties.getProperty("color", "green");
	}
	
	public String getColor() {
		return color;
	}
	
	public Integer setTaste(int taste) {
		return taste;
	}

	public String setTaste(String taste) {
		return taste;
	}

	public String setTaste(String taste, int factor) {
		return taste + factor;
	}
}
