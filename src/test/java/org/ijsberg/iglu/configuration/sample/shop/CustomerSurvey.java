package org.ijsberg.iglu.configuration.sample.shop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class CustomerSurvey {

	Set registeredBaskets = new HashSet();

	public void register(Basket basket) {
		registeredBaskets.add(basket);
	}

	public int getNrofRegisteredBaskets() {
		return registeredBaskets.size();
	}

}
