/*
 * Copyright 2011 Jeroen Meetsma
 *
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

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
