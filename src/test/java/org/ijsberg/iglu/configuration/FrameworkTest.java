package org.ijsberg.iglu.configuration;

import org.ijsberg.iglu.Cluster;
import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.Layer;
import org.ijsberg.iglu.Module;
import org.ijsberg.iglu.configuration.sample.shop.*;
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
public class FrameworkTest{

	@Test
	public void testEmbedding() throws Exception {

		ShopImpl drugstore = new ShopImpl("The Drugstore");
		Module shopModule = new StandardModule(drugstore);

		Shop shop = (Shop)shopModule.getProxy(Shop.class);

		Class[] implementedInterfaces = shopModule.getInterfaces();

		Properties shopProperties = new Properties();
		shopProperties.setProperty("ShopCode", "ET-27");
		shopProperties.setProperty("MaxOrdersDeliverablePerDay", "2000");
		shopProperties.setProperty("MinOrderSize", "10");

		shopModule.setProperties(shopProperties);

		ProductInquiryCounter productInquiryCounter = new ProductInquiryCounter();
		shopModule.setInvocationInterceptor(Shop.class, productInquiryCounter);

		shop.findProductById(1);
		shop.findProductById(2);
		shop.findProductById(3);

		assertEquals(3, productInquiryCounter.getNrofInquiries());



	}

	@Test
	public void testClustering() throws Exception {

		ShopImpl drugstore = new ShopImpl("The Drugstore");
		Module shopModule = new StandardModule(drugstore);

		Cluster cluster = new StandardCluster();
		cluster.connect("Drugstore", shopModule);


		Map<String, Module> modules = cluster.getInternalModules();
		assertEquals(1, modules.size());

		Shop shop = (Shop)cluster.getInternalModules().get("Drugstore").getProxy(Shop.class);
		assertNotNull(shop);

		PhotoPrintService photoPrintService = new PhotoPrintServiceImpl("Photo Print Service");
		Module photoPrintServiceModule = new StandardModule(photoPrintService);

		assertFalse(drugstore.hasPhotoPrintService());
		cluster.connect("PhotoPrintService", photoPrintServiceModule);
		assertTrue(drugstore.hasPhotoPrintService());




		//
		ShoppingCenterImpl shoppingCenter = new ShoppingCenterImpl();
		Module shoppingCenterModule = new StandardModule(shoppingCenter);

		cluster.connect("Shopping Center", shoppingCenterModule);
		assertEquals(2, shoppingCenter.getListedShopNames().size());


	}

	@Test
	public void testLayering() throws Exception {
		ShopImpl drugstore = new ShopImpl("The Drugstore");
		Module shopModule = new StandardModule(drugstore);

		PhotoPrintService photoPrintService = new PhotoPrintServiceImpl("Photo Print Service");
		Module photoPrintServiceModule = new StandardModule(photoPrintService);

		Cluster cluster = new StandardCluster();
		cluster.connect("PhotoPrintService", photoPrintServiceModule);
		cluster.connect("Drugstore", shopModule, Shop.class);

		Layer serviceLayer = cluster.asLayer();

		try {
			serviceLayer.getProxy("PhotoPrintService", PhotoPrintService.class);
			fail("ConfigurationException expected");
		} catch(ConfigurationException expected){}

		Shop shop = (Shop)serviceLayer.getProxy("Drugstore", Shop.class);
		assertNotNull(shop);

		BasketImpl basket = new BasketImpl();
		Module basketModule = new StandardModule(basket);
		serviceLayer.connect(basketModule);

		assertTrue(basket.isShoppingInDrugstore());

		CustomerSurvey customerSurvey = new CustomerSurvey();
		cluster.connect("customer survey", new StandardModule(customerSurvey));

		assertEquals(1, customerSurvey.getNrofRegisteredBaskets());

	}
}
