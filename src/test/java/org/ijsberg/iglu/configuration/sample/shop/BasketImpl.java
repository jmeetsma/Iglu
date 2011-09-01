package org.ijsberg.iglu.configuration.sample.shop;

/**
 */
public class BasketImpl implements Basket {

	private Shop drugstore;

	public void setDrugstore(Shop shop) {
		this.drugstore = shop;
	}

	public boolean isShoppingInDrugstore() {
		return drugstore != null;
	}

}
