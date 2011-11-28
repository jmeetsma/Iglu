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
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.configuration.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Properties;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.sample.configuration.*;
import org.junit.Before;
import org.junit.Test;


public class StandardComponentTest {

	private Apple apple;
	private StandardComponent appleComponent;

	private Elstar elstar;
	private Component elstarComponent;

	private Notifier notifier;
	private Listener listener1;
	private Listener listener2;
	private Component notifierComponent;
	private Component listenerComponent1;
	private Component listenerComponent2;


	@Before
	public void setUp() throws Exception {
		apple = new Apple();
		appleComponent = new StandardComponent(apple);

		elstar = new Elstar();
		elstarComponent = new StandardComponent(elstar);

		notifier = new Notifier();
		listener1 = new Listener("listener 1");
		listener2 = new Listener("listener 2");
		notifierComponent = new StandardComponent(notifier);
		listenerComponent1 = new StandardComponent(listener1);
		listenerComponent2 = new StandardComponent(listener2);
	}

	@Test
	public void testInstantiation() throws Exception {
		assertEquals(1, appleComponent.getInterfaces().length);
	}

	@Test
	public void testGetInterfaces() throws Exception {
		assertEquals(1, appleComponent.getInterfaces().length);
		assertEquals(AppleInterface.class, appleComponent.getInterfaces()[0]);

		assertEquals(2, elstarComponent.getInterfaces().length);
		assertEquals(ElstarInterface.class, elstarComponent.getInterfaces()[1]);
		assertEquals(AppleInterface.class, elstarComponent.getInterfaces()[0]);

	}

	@Test
	public void testGetProxy() throws Exception {

		AppleInterface proxy = (AppleInterface) appleComponent.getProxy(AppleInterface.class);
		assertTrue(proxy instanceof AppleInterface);
		assertFalse(proxy instanceof BananaInterface);

		assertEquals("hello", proxy.returnInput("hello"));
	}

	@Test
	public void testGetProxy2() throws Exception {

		ElstarInterface proxy = (ElstarInterface) elstarComponent.getProxy(ElstarInterface.class);
		//proxy implements AppleInterface through ElstarInterface
		assertTrue(proxy instanceof AppleInterface);
		//proxy implements 1 interface directly
		assertEquals(1, proxy.getClass().getInterfaces().length);

		assertEquals("hello", proxy.returnInput("hello"));
	}


	@Test
	public void testSetProperties() throws Exception {

		assertNull(apple.getMessage());
		assertNull(appleComponent.getProperties());

		Properties properties = new Properties();
		properties.setProperty("message", "Hello World!");
		appleComponent.setProperties(properties);

		AppleInterface proxy = (AppleInterface) appleComponent.getProxy(AppleInterface.class);
		assertEquals("Hello World!", apple.getMessage());
		assertEquals("Hello World!", proxy.getMessage());

		assertNotNull(appleComponent.getProperties());
		assertEquals(1, appleComponent.getSetterInjectedProperties().size());
	}


	@Test
	public void testSetPropertiesInOneSetter() throws Exception {

		Peach peach = new Peach();
		Component peachComponent = new StandardComponent(peach);

		assertNull(peach.getColor());

		Properties properties = new Properties();
		peachComponent.setProperties(properties);

		assertEquals("green", peach.getColor());

		properties.setProperty("color", "red");
		peachComponent.setProperties(properties);

		assertEquals("red", peach.getColor());
	}


	@Test
	public void testSetPropertiesWithDuplicateSetter() throws Exception {

		Peach peach = new Peach();
		Component peachComponent = new StandardComponent(peach);

		assertNull(peach.getColor());

		Properties properties = new Properties();
		properties.setProperty("taste", "sweet");
		try {
			peachComponent.setProperties(properties);
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
		appleComponent.setProperties(properties);

		AppleInterface proxy = (AppleInterface) appleComponent.getProxy(AppleInterface.class);
		assertEquals(3, apple.getSomeInt());
		assertEquals(3, proxy.getSomeInt());

		properties.setProperty("someInt", "three");
		try {
			appleComponent.setProperties(properties);
			fail("NumberFormatException expected");
		}
		catch (NumberFormatException e) {
			//expected
		}
	}

	@Test
	public void testSetPropertiesAsProperties() throws Exception {

		Peach peach = new Peach();
		Component peachComponent = new StandardComponent(peach);
		assertNull(peach.getColor());
		Properties properties = new Properties();
		peachComponent.setProperties(properties);
		assertEquals("green", peach.getColor());
		properties.setProperty("color", "pinkish");
		peachComponent.setProperties(properties);
		assertEquals("pinkish", peach.getColor());

	}

	@Test
	public void testGetInjectedProperties() throws Exception {

		assertEquals(0, appleComponent.getSetterInjectedProperties().size());
		Properties properties = new Properties();
		properties.setProperty("message", "Hello World!");
		properties.setProperty("foo", "bar");//no corresponding setter
		appleComponent.setProperties(properties);

		assertNotNull(appleComponent.getProperties());
		assertEquals(1, appleComponent.getSetterInjectedProperties().size());
		assertTrue(appleComponent.getSetterInjectedProperties().containsKey("message"));
	}

	@Test
	public void testSetInvocationHandler() throws Exception {

		assertNull(apple.getMessage());

		Properties properties = new Properties();
		properties.setProperty("message", "Hello");
		appleComponent.setProperties(properties);

		AppleInterface proxy = (AppleInterface) appleComponent.getProxy(AppleInterface.class);
		assertEquals("Hello", apple.getMessage());
		assertEquals("Hello", proxy.getMessage());

		appleComponent.setInvocationInterceptor(AppleInterface.class, new GetMessageInterceptor(" world"));
		assertEquals("Hello world", proxy.getMessage());

		assertEquals("not intercepted", proxy.returnInput("not intercepted"));
	}


	@Test
	public void testSetInvocationHandler2() throws Exception {

		Properties properties = new Properties();
		properties.setProperty("message", "Hello");
		elstarComponent.setProperties(properties);

		ElstarInterface proxy = (ElstarInterface) elstarComponent.getProxy(ElstarInterface.class);
		assertEquals("Hello", elstar.getMessage());
		assertEquals("Hello", proxy.getMessage());

		//proxy for ElstarInterface is affected, since AppleInterface is declaring class
		elstarComponent.setInvocationInterceptor(AppleInterface.class, new GetMessageInterceptor(" world"));
		assertEquals("Hello world", proxy.getMessage());

		elstarComponent.setInvocationInterceptor(ElstarInterface.class, new GetMessageInterceptor(" baby"));
		assertEquals("Hello baby", proxy.getMessage());

		AppleInterface proxy2 = (AppleInterface) elstarComponent.getProxy(AppleInterface.class);
		assertEquals("Hello world", proxy2.getMessage());
	}

	@Test
	public void testSetInvocationHandler3() throws Exception {

		Properties properties = new Properties();
		properties.setProperty("message", "Hello");
		elstarComponent.setProperties(properties);

		AppleInterface proxy = (AppleInterface) elstarComponent.getProxy(AppleInterface.class);
		assertEquals("Hello", elstar.getMessage());
		assertEquals("Hello", proxy.getMessage());

		//proxy for AppleInterface not affected
		elstarComponent.setInvocationInterceptor(ElstarInterface.class, new GetMessageInterceptor(" world"));
		assertEquals("Hello", proxy.getMessage());

//		elstarComponent.setInvocationHandler(ElstarInterface.class, new GetMessageIntercepter(" world"));
		assertEquals("Hello world", ((ElstarInterface) elstarComponent.getProxy(ElstarInterface.class)).getMessage());

		elstarComponent.setInvocationInterceptor(AppleInterface.class, new GetMessageInterceptor(" world"));
		assertEquals("Hello world", proxy.getMessage());
	}

	@Test
	public void testSetInvocationHandler4() throws Exception {
		try {
			appleComponent.getProxy(ElstarInterface.class);
			fail("apple does not implement ElstarInterface");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testSetDependency() throws Exception {

		Component bananaComponent = new StandardComponent(new Banana(27));
		try {
			apple.getIntFromBanana();
			fail("NulPointerException expected");
		}
		catch (NullPointerException expected) {
			//expected;
		}
		Cluster fruit = new StandardCluster();

		fruit.getFacade().connect(bananaComponent);
		try {
			appleComponent.setReference(fruit.getFacade(), "banana", bananaComponent.getInterfaces());
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}


		assertEquals(0, appleComponent.getInjectedInterfaces("banana").size());

		fruit.disconnect(bananaComponent);
		fruit.connect("banana", bananaComponent, bananaComponent.getInterfaces());

		appleComponent.setReference(fruit.getFacade(), "banana", bananaComponent.getInterfaces());

		assertEquals(2, appleComponent.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

	}


	@Test
	public void testSetDependency2() throws Exception {

		Component bananaComponent = new StandardComponent(new Banana(27));
		Cluster fruit = new StandardCluster();
		fruit.connect("banana", bananaComponent, bananaComponent.getInterfaces());

		appleComponent.setReference(fruit.getFacade(), "banana", BananaInterface.class);

		assertEquals(1, appleComponent.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

		appleComponent.setReference(fruit.getFacade(), "banana", new Class<?>[0]);

		assertEquals(0, appleComponent.getInjectedInterfaces("banana").size());


		try {
			apple.getIntFromBanana();
			fail("NulPointerException expected");
		}
		catch (NullPointerException expected) {
			//expected;
		}

		appleComponent.setReference(fruit.getFacade(), "banana", BananaInterface.class);
		assertEquals(1, appleComponent.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

		appleComponent.setReference(fruit.getFacade(), "banana", Serializable.class);
		assertEquals(1, appleComponent.getInjectedInterfaces("banana").size());
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

		assertEquals(0, appleComponent.getInjectedInterfaces("banana").size());

		Component bananaComponent = new StandardComponent(new Banana(27));
		Cluster fruit = new StandardCluster();
		fruit.connect("banana", bananaComponent, bananaComponent.getInterfaces());
		appleComponent.setReference(fruit.getFacade(), "banana", bananaComponent.getInterfaces());
		assertEquals(2, appleComponent.getInjectedInterfaces("banana").size());
		assertEquals(27, apple.getIntFromBanana());

		appleComponent.removeDependency("banana");
		assertEquals(0, appleComponent.getInjectedInterfaces("banana").size());
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
		fruit.connect("elstar", elstarComponent, elstarComponent.getInterfaces());


	}


	@Test
	public void testRegisterListener() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		notifierComponent.register(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testRegisterListenerMultiple() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		notifierComponent.register(listenerComponent1);
		notifierComponent.register(listenerComponent2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
		//duplicate registration
		notifierComponent.register(listenerComponent2);
		//not a listener
		notifierComponent.register(appleComponent);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testUnregisterListener() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		notifierComponent.register(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		notifierComponent.unregister(listenerComponent1);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testUnregisterListenerMultiple() throws Exception {

		notifierComponent.register(listenerComponent1);
		notifierComponent.register(listenerComponent2);
		assertEquals(2, notifier.getNrofRegisteredListeners());

		notifierComponent.unregister(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		notifierComponent.unregister(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());

		notifierComponent.unregister(listenerComponent2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testUnregisterListenerNotRegistered() throws Exception {

		notifierComponent.unregister(listenerComponent1);
		//no need to throw
	}

	@Test
	public void testInvoke() throws Exception {
		try {
			appleComponent.invoke("setMessage", "test invoke");
			fail("setMessage not specified in interface");
		} catch (NoSuchMethodException setMessageNotSpecifiedInInterface) {};

		Object result = appleComponent.invoke("returnInput", "test invoke");
		assertEquals("test invoke", result);

		result = appleComponent.invoke("returnInput", true, '-', 23);
		assertEquals("true-23", result);

		result = appleComponent.invoke("returnInput", "false", "+", "12");
		assertEquals("false+12", result);

		result = appleComponent.invoke("returnInput", 100);
		assertEquals(100, result);

		try {
			result = appleComponent.invoke("returnInput", "arg2", "arg2");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {};

		try {
			result = appleComponent.invoke("returnInput", "true", "=", "twelve");
			fail("NumberFormatException expected");
		} catch (NumberFormatException expected) {};

		result = appleComponent.invoke("returnInput", 0, 65, "12");
		assertEquals("falseA12", result);
	}


}
