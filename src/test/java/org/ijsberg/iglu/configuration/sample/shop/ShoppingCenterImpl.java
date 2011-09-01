package org.ijsberg.iglu.configuration.sample.shop;

import java.util.HashMap;
import java.util.Set;

/**
 */
public class ShoppingCenterImpl implements ShoppingCenter {
	private HashMap<String, Shop> listedShops = new HashMap<String, Shop>();

	public void register(Shop shop) {
		listedShops.put(shop.getName(), shop);
	}

	public Set<String> getListedShopNames() {
		return listedShops.keySet();
	}
}
