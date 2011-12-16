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
 * along with Iglu. If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.configuration.module;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.Facade;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.sample.configuration.shop.*;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class FrameworkTest {

	@Test
	public void testEmbedding() throws Exception {

		ShopImpl drugstore = new ShopImpl("The Drugstore");
		Component shopComponent = new StandardComponent(drugstore);

		Shop shop = (Shop) shopComponent.getProxy(Shop.class);

		Class[] implementedInterfaces = shopComponent.getInterfaces();

		Properties shopProperties = new Properties();
		shopProperties.setProperty("ShopCode", "ET-27");
		shopProperties.setProperty("MaxOrdersDeliverablePerDay", "2000");
		shopProperties.setProperty("MinOrderSize", "10");

		shopComponent.setProperties(shopProperties);

		ProductInquiryCounter productInquiryCounter = new ProductInquiryCounter();
		shopComponent.setInvocationIntercepter(Shop.class, productInquiryCounter);

		shop.findProductById(1);
		shop.findProductById(2);
		shop.findProductById(3);

		assertEquals(3, productInquiryCounter.getNrofInquiries());


	}

	@Test
	public void testClustering() throws Exception {

		ShopImpl drugstore = new ShopImpl("The Drugstore");
		Component shopComponent = new StandardComponent(drugstore);

		Cluster cluster = new StandardCluster();
		cluster.connect("Drugstore", shopComponent);


		Map<String, Component> components = cluster.getInternalComponents();
		assertEquals(1, components.size());

		Shop shop = (Shop) cluster.getInternalComponents().get("Drugstore").getProxy(Shop.class);
		assertNotNull(shop);

		PhotoPrintService photoPrintService = new PhotoPrintServiceImpl("Photo Print Service");
		Component photoPrintServiceComponent = new StandardComponent(photoPrintService);

		assertFalse(drugstore.hasPhotoPrintService());
		cluster.connect("PhotoPrintService", photoPrintServiceComponent);
		assertTrue(drugstore.hasPhotoPrintService());


		//
		ShoppingCenterImpl shoppingCenter = new ShoppingCenterImpl();
		Component shoppingCenterComponent = new StandardComponent(shoppingCenter);

		cluster.connect("Shopping Center", shoppingCenterComponent);
		assertEquals(2, shoppingCenter.getListedShopNames().size());


	}

	@Test
	public void testFacadeing() throws Exception {
		ShopImpl drugstore = new ShopImpl("The Drugstore");
		Component shopComponent = new StandardComponent(drugstore);

		PhotoPrintService photoPrintService = new PhotoPrintServiceImpl("Photo Print Service");
		Component photoPrintServiceComponent = new StandardComponent(photoPrintService);

		Cluster cluster = new StandardCluster();
		cluster.connect("PhotoPrintService", photoPrintServiceComponent);
		cluster.connect("Drugstore", shopComponent, Shop.class);

		Facade serviceFacade = cluster.getFacade();

		try {
			serviceFacade.getProxy("PhotoPrintService", PhotoPrintService.class);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}

		Shop shop = (Shop) serviceFacade.getProxy("Drugstore", Shop.class);
		assertNotNull(shop);

		BasketImpl basket = new BasketImpl();
		Component basketComponent = new StandardComponent(basket);
		serviceFacade.connect(basketComponent);

		assertTrue(basket.isShoppingInDrugstore());

		CustomerSurvey customerSurvey = new CustomerSurvey();
		cluster.connect("customer survey", new StandardComponent(customerSurvey));

		assertEquals(1, customerSurvey.getNrofRegisteredBaskets());

	}
}
