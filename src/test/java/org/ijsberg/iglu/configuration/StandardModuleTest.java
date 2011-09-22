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

package org.ijsberg.iglu.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Properties;

import org.ijsberg.iglu.Cluster;
import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.Module;
import org.ijsberg.iglu.sample.configuration.Apple;
import org.ijsberg.iglu.sample.configuration.AppleInterface;
import org.ijsberg.iglu.sample.configuration.Banana;
import org.ijsberg.iglu.sample.configuration.BananaInterface;
import org.ijsberg.iglu.sample.configuration.Elstar;
import org.ijsberg.iglu.sample.configuration.ElstarInterface;
import org.ijsberg.iglu.sample.configuration.GetMessageIntercepter;
import org.ijsberg.iglu.sample.configuration.Listener;
import org.ijsberg.iglu.sample.configuration.Notifier;
import org.ijsberg.iglu.sample.configuration.Peach;
import org.junit.Before;
import org.junit.Test;


public class StandardModuleTest {

	private Apple apple;
	private StandardModule appleModule;

	private Elstar elstar;
	private Module elstarModule;

	private Notifier notifier;
	private Listener listener1;
	private Listener listener2;
	private Module notifierModule;
	private Module listenerModule1;
	private Module listenerModule2;


	@Before
	public void setUp() throws Exception {
		apple = new Apple();
		appleModule = new StandardModule(apple);

		elstar = new Elstar();
		elstarModule = new StandardModule(elstar);

		notifier = new Notifier();
		listener1 = new Listener("listener 1");
		listener2 = new Listener("listener 2");
		notifierModule = new StandardModule(notifier);
		listenerModule1 = new StandardModule(listener1);
		listenerModule2 = new StandardModule(listener2);
	}

	@Test
	public void testInstantiation() throws Exception {
		assertEquals(1, appleModule.getInterfaces().length);
	}

	@Test
	public void testGetInterfaces() throws Exception {
		assertEquals(1, appleModule.getInterfaces().length);
		assertEquals(AppleInterface.class, appleModule.getInterfaces()[0]);

		assertEquals(2, elstarModule.getInterfaces().length);
		assertEquals(ElstarInterface.class, elstarModule.getInterfaces()[1]);
		assertEquals(AppleInterface.class, elstarModule.getInterfaces()[0]);

	}

	@Test
	public void testGetProxy() throws Exception {

		AppleInterface proxy = (AppleInterface) appleModule.getProxy(AppleInterface.class);
		assertTrue(proxy instanceof AppleInterface);
		assertFalse(proxy instanceof BananaInterface);

		assertEquals("hello", proxy.returnInput("hello"));
	}

	@Test
	public void testGetProxy2() throws Exception {

		ElstarInterface proxy = (ElstarInterface) elstarModule.getProxy(ElstarInterface.class);
		//proxy implements AppleInterface through ElstarInterface
		assertTrue(proxy instanceof AppleInterface);
		//proxy implements 1 interface directly
		assertEquals(1, proxy.getClass().getInterfaces().length);

		assertEquals("hello", proxy.returnInput("hello"));
	}


	@Test
	public void testSetProperties() throws Exception {

		assertNull(apple.getMessage());
		assertNull(appleModule.getProperties());

		Properties properties = new Properties();
		properties.setProperty("message", "Hello World!");
		appleModule.setProperties(properties);

		AppleInterface proxy = (AppleInterface) appleModule.getProxy(AppleInterface.class);
		assertEquals("Hello World!", apple.getMessage());
		assertEquals("Hello World!", proxy.getMessage());

		assertNotNull(appleModule.getProperties());
		assertEquals(1, appleModule.getSetterInjectedProperties().size());
	}


	@Test
	public void testSetPropertiesInOneSetter() throws Exception {

		Peach peach = new Peach();
		Module peachModule = new StandardModule(peach);

		assertNull(peach.getColor());

		Properties properties = new Properties();
		peachModule.setProperties(properties);

		assertEquals("green", peach.getColor());

		properties.setProperty("color", "red");
		peachModule.setProperties(properties);

		assertEquals("red", peach.getColor());
	}


	@Test
	public void testSetPropertiesWithDuplicateSetter() throws Exception {

		Peach peach = new Peach();
		Module peachModule = new StandardModule(peach);

		assertNull(peach.getColor());

		Properties properties = new Properties();
		properties.setProperty("taste", "sweet");
		try {
			peachModule.setProperties(properties);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
		;
	}


	@Test
	public void testSetPropertiesAsInteger() throws Exception {

		assertNull(apple.getMessage());

		Properties properties = new Properties();
		properties.setProperty("someInt", "3");
		appleModule.setProperties(properties);

		AppleInterface proxy = (AppleInterface) appleModule.getProxy(AppleInterface.class);
		assertEquals(3, apple.getSomeInt());
		assertEquals(3, proxy.getSomeInt());

		properties.setProperty("someInt", "three");
		try {
			appleModule.setProperties(properties);
			fail("NumberFormatException expected");
		}
		catch (NumberFormatException e) {
			//expected
		}
	}

	@Test
	public void testSetPropertiesAsProperties() throws Exception {

		Peach peach = new Peach();
		Module peachModule = new StandardModule(peach);
		assertNull(peach.getColor());
		Properties properties = new Properties();
		peachModule.setProperties(properties);
		assertEquals("green", peach.getColor());
		properties.setProperty("color", "pinkish");
		peachModule.setProperties(properties);
		assertEquals("pinkish", peach.getColor());

	}

	@Test
	public void testGetInjectedProperties() throws Exception {

		assertEquals(0, appleModule.getSetterInjectedProperties().size());
		Properties properties = new Properties();
		properties.setProperty("message", "Hello World!");
		properties.setProperty("foo", "bar");//no corresponding setter
		appleModule.setProperties(properties);

		assertNotNull(appleModule.getProperties());
		assertEquals(1, appleModule.getSetterInjectedProperties().size());
		assertTrue(appleModule.getSetterInjectedProperties().containsKey("message"));
	}

	@Test
	public void testSetInvocationHandler() throws Exception {

		assertNull(apple.getMessage());

		Properties properties = new Properties();
		properties.setProperty("message", "Hello");
		appleModule.setProperties(properties);

		AppleInterface proxy = (AppleInterface) appleModule.getProxy(AppleInterface.class);
		assertEquals("Hello", apple.getMessage());
		assertEquals("Hello", proxy.getMessage());

		appleModule.setInvocationInterceptor(AppleInterface.class, new GetMessageIntercepter(" world"));
		assertEquals("Hello world", proxy.getMessage());

		assertEquals("not intercepted", proxy.returnInput("not intercepted"));
	}


	@Test
	public void testSetInvocationHandler2() throws Exception {

		Properties properties = new Properties();
		properties.setProperty("message", "Hello");
		elstarModule.setProperties(properties);

		ElstarInterface proxy = (ElstarInterface) elstarModule.getProxy(ElstarInterface.class);
		assertEquals("Hello", elstar.getMessage());
		assertEquals("Hello", proxy.getMessage());

		//proxy for ElstarInterface is affected, since AppleInterface is declaring class
		elstarModule.setInvocationInterceptor(AppleInterface.class, new GetMessageIntercepter(" world"));
		assertEquals("Hello world", proxy.getMessage());

		elstarModule.setInvocationInterceptor(ElstarInterface.class, new GetMessageIntercepter(" baby"));
		assertEquals("Hello baby", proxy.getMessage());

		AppleInterface proxy2 = (AppleInterface) elstarModule.getProxy(AppleInterface.class);
		assertEquals("Hello world", proxy2.getMessage());
	}

	@Test
	public void testSetInvocationHandler3() throws Exception {

		Properties properties = new Properties();
		properties.setProperty("message", "Hello");
		elstarModule.setProperties(properties);

		AppleInterface proxy = (AppleInterface) elstarModule.getProxy(AppleInterface.class);
		assertEquals("Hello", elstar.getMessage());
		assertEquals("Hello", proxy.getMessage());

		//proxy for AppleInterface not affected
		elstarModule.setInvocationInterceptor(ElstarInterface.class, new GetMessageIntercepter(" world"));
		assertEquals("Hello", proxy.getMessage());

//		elstarModule.setInvocationHandler(ElstarInterface.class, new GetMessageIntercepter(" world"));
		assertEquals("Hello world", ((ElstarInterface) elstarModule.getProxy(ElstarInterface.class)).getMessage());

		elstarModule.setInvocationInterceptor(AppleInterface.class, new GetMessageIntercepter(" world"));
		assertEquals("Hello world", proxy.getMessage());
	}

	@Test
	public void testSetInvocationHandler4() throws Exception {
		try {
			appleModule.getProxy(ElstarInterface.class);
			fail("apple does not implement ElstarInterface");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testSetDependency() throws Exception {

		Module bananaModule = new StandardModule(new Banana(27));
		try {
			apple.getIntFromBanana();
			fail("NulPointerException expected");
		}
		catch (NullPointerException expected) {
			//expected;
		}
		Cluster fruit = new StandardCluster();

		fruit.asLayer().connect(bananaModule);
		try {
			appleModule.setReference(fruit.asLayer(), "banana", bananaModule.getInterfaces());
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}


		assertEquals(0, appleModule.getInjectedInterfaces("banana").size());

		fruit.disconnect(bananaModule);
		fruit.connect("banana", bananaModule, bananaModule.getInterfaces());

		appleModule.setReference(fruit.asLayer(), "banana", bananaModule.getInterfaces());

		assertEquals(2, appleModule.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

	}


	@Test
	public void testSetDependency2() throws Exception {

		Module bananaModule = new StandardModule(new Banana(27));
		Cluster fruit = new StandardCluster();
		fruit.connect("banana", bananaModule, bananaModule.getInterfaces());

		appleModule.setReference(fruit.asLayer(), "banana", BananaInterface.class);

		assertEquals(1, appleModule.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

		appleModule.setReference(fruit.asLayer(), "banana", new Class<?>[0]);

		assertEquals(0, appleModule.getInjectedInterfaces("banana").size());


		try {
			apple.getIntFromBanana();
			fail("NulPointerException expected");
		}
		catch (NullPointerException expected) {
			//expected;
		}

		appleModule.setReference(fruit.asLayer(), "banana", BananaInterface.class);
		assertEquals(1, appleModule.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

		appleModule.setReference(fruit.asLayer(), "banana", Serializable.class);
		assertEquals(1, appleModule.getInjectedInterfaces("banana").size());
		try {
			apple.getIntFromBanana();
			fail("NulPointerException expected");
		}
		catch (NullPointerException expected) {
			//expected;
		}
	}


	@Test
	public void testRemoveDependency() throws Exception {

		assertEquals(0, appleModule.getInjectedInterfaces("banana").size());

		Module bananaModule = new StandardModule(new Banana(27));
		Cluster fruit = new StandardCluster();
		fruit.connect("banana", bananaModule, bananaModule.getInterfaces());
		appleModule.setReference(fruit.asLayer(), "banana", bananaModule.getInterfaces());
		assertEquals(2, appleModule.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

		appleModule.removeDependency("banana");
		assertEquals(0, appleModule.getInjectedInterfaces("banana").size());
		try {
			apple.getIntFromBanana();
			fail("NulPointerException expected");
		}
		catch (NullPointerException expected) {
			//expected;
		}
	}


	@Test
	public void testRemoveDependency2() throws Exception {

		Cluster fruit = new StandardCluster();
		fruit.connect("elstar", elstarModule, elstarModule.getInterfaces());


	}


	@Test
	public void testRegisterListener() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		notifierModule.register(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testRegisterListenerMultiple() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		notifierModule.register(listenerModule1);
		notifierModule.register(listenerModule2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
		//duplicate registration
		notifierModule.register(listenerModule2);
		//not a listener
		notifierModule.register(appleModule);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testUnregisterListener() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		notifierModule.register(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		notifierModule.unregister(listenerModule1);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testUnregisterListenerMultiple() throws Exception {

		notifierModule.register(listenerModule1);
		notifierModule.register(listenerModule2);
		assertEquals(2, notifier.getNrofRegisteredListeners());

		notifierModule.unregister(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		notifierModule.unregister(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());

		notifierModule.unregister(listenerModule2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testUnregisterListenerNotRegistered() throws Exception {

		notifierModule.unregister(listenerModule1);
		//no need to throw
	}

}
