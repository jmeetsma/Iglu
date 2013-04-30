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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.Facade;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.sample.configuration.Apple;
import org.ijsberg.iglu.sample.configuration.AppleInterface;
import org.ijsberg.iglu.sample.configuration.Banana;
import org.ijsberg.iglu.sample.configuration.BananaInterface;
import org.ijsberg.iglu.sample.configuration.Elstar;
import org.ijsberg.iglu.sample.configuration.ElstarInterface;
import org.ijsberg.iglu.sample.configuration.Listener;
import org.ijsberg.iglu.sample.configuration.Notifier;
import org.ijsberg.iglu.sample.configuration.NotifierInterface;
import org.junit.Before;
import org.junit.Test;

public class StandardClusterTest {

	private StandardCluster fruit;
	private Apple appleCore;
	private Component appleComponent;
	private Banana bananaCore;
	private Component bananaComponent;
	private Elstar elstar;
	private Component elstarComponent;


	private StandardCluster cluster;
	private Notifier notifier;
	private Listener listener1;
	private Listener listener2;
	private Component notifierComponent;
	private Component listenerComponent1;
	private Component listenerComponent2;

	@Before
	public void setUp() throws Exception {
		fruit = new StandardCluster();
		appleCore = new Apple();
		appleComponent = new StandardComponent(appleCore);
		bananaCore = new Banana(27);
		bananaComponent = new StandardComponent(bananaCore);
		elstar = new Elstar();
		elstarComponent = new StandardComponent(elstar);

		cluster = new StandardCluster();
		notifier = new Notifier();
		listener1 = new Listener("listener 1");
		listener2 = new Listener("listener 2");
		notifierComponent = new StandardComponent(notifier);
		listenerComponent1 = new StandardComponent(listener1);
		listenerComponent2 = new StandardComponent(listener2);
	}

	@Test
	public void testCluster() throws Exception {
		assertFalse(Cluster.class.isAssignableFrom(Facade.class));
		assertFalse(Facade.class.isAssignableFrom(Cluster.class));
	}

	@Test
	public void testConnectInternalComponent() throws Exception {
		assertEquals(0, fruit.getInternalComponents().size());
		fruit.connect("apple", appleComponent);
		assertEquals(1, fruit.getInternalComponents().size());
	}

	@Test
	public void testConnectInternalComponentAndExpose() throws Exception {
		assertEquals(0, fruit.getExposedComponentIds().size());
		fruit.connect("apple", appleComponent, AppleInterface.class);
		assertEquals(1, fruit.getExposedComponentIds().size());
		assertEquals("apple", fruit.getExposedComponentIds().toArray()[0]);
	}

	@Test
	public void testDisconnectInternalComponent() throws Exception {
		fruit.connect("apple", appleComponent);
		assertEquals(1, fruit.getInternalComponents().size());
		fruit.disconnect(appleComponent);
		assertEquals(0, fruit.getInternalComponents().size());
	}

	@Test
	public void testDisonnectInternalComponentAndExpose() throws Exception {
		fruit.connect("apple", appleComponent, AppleInterface.class);
		fruit.disconnect(appleComponent);
		assertEquals(0, fruit.getExposedComponentIds().size());
	}

	@Test
	public void testConnectExternalComponent() throws Exception {
		assertEquals(0, fruit.getExternalComponents().size());
		fruit.getFacade().connect(appleComponent);
		assertEquals(1, fruit.getExternalComponents().size());
	}

	@Test
	public void testDisconnectExternalComponent() throws Exception {
		fruit.getFacade().connect(appleComponent);
		assertEquals(1, fruit.getExternalComponents().size());
		fruit.getFacade().disconnect(appleComponent);
		assertEquals(0, fruit.getExternalComponents().size());
	}

	@Test
	public void testConnectExternalComponentTwice() throws Exception {
		fruit.getFacade().connect(appleComponent);
		try {
			fruit.getFacade().connect(appleComponent);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectExternalComponentAsInternalComponent() throws Exception {
		fruit.getFacade().connect(appleComponent);
		try {
			fruit.connect("apple", appleComponent);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectInternalComponentAsExternalComponent() throws Exception {
		fruit.connect("apple", appleComponent);
		try {
			fruit.getFacade().connect(appleComponent);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectInternalComponentTwice1() throws Exception {
		fruit.connect("apple", appleComponent);
		try {
			fruit.connect("apple", appleComponent);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectInternalComponentTwice2() throws Exception {
		fruit.connect("red apple", appleComponent);
		fruit.connect("sumptious apple", appleComponent);
		assertEquals(2, fruit.getInternalComponents().size());
	}

	@Test
	public void testConnectInternalComponentTwice3() throws Exception {
		fruit.connect("apple", elstarComponent, AppleInterface.class);
		fruit.connect("elstar", elstarComponent, ElstarInterface.class);
		assertEquals(2, fruit.getInternalComponents().size());
		assertEquals(2, fruit.getExposedComponentIds().size());
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		assertEquals(1, fruit.getExposedInterfaces("elstar").length);
	}

	@Test
	public void testDisonnectInternalComponentTwice3() throws Exception {
		fruit.connect("apple", elstarComponent, AppleInterface.class);
		fruit.connect("elstar", elstarComponent, ElstarInterface.class);
		fruit.disconnect(elstarComponent);
		assertEquals(0, fruit.getInternalComponents().size());
		assertEquals(0, fruit.getExposedComponentIds().size());
	}

	@Test
	public void testConnectInjectionInExternalComponent() throws Exception {
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}

		fruit.connect("banana", bananaComponent, bananaComponent.getInterfaces());
		fruit.getFacade().connect(appleComponent);

		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testConnectInjectionInExternalComponent2() throws Exception {

		fruit.getFacade().connect(appleComponent);
		fruit.connect("banana", bananaComponent, bananaComponent.getInterfaces());

		assertEquals(27, appleCore.getIntFromBanana());
	}


	@Test
	public void testConnectInjectionInExternalComponent3() throws Exception {
		fruit.connect("banana", bananaComponent);
		fruit.getFacade().connect(appleComponent);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}
	}

	@Test
	public void testConnectInjectionInExternalComponent4() throws Exception {

		fruit.getFacade().connect(appleComponent);
		fruit.connect("banana", bananaComponent);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException expected) {
		}
	}

	@Test
	public void testDisconnectInternalComponentFromExternal() throws Exception {

		fruit.connect("banana", bananaComponent, BananaInterface.class);
		fruit.getFacade().connect(appleComponent);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.disconnect(bananaComponent);

		assertEquals(0, fruit.getInternalComponents().size());

/*		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}    */

	}

	@Test
	public void testDisconnectExternalComponentFromInternal() throws Exception {

		fruit.connect("banana", bananaComponent, BananaInterface.class);
		fruit.getFacade().connect(appleComponent);


		assertEquals(27, appleCore.getIntFromBanana());

		fruit.getFacade().disconnect(appleComponent);

		assertEquals(0, fruit.getExternalComponents().size());

/*		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}   */

	}

	@Test
	public void testDisconnectDoubleConnectedInternalComponent() throws Exception {
		fruit.connect("red apple", appleComponent);
		fruit.connect("sumptious apple", appleComponent);
		fruit.disconnect(appleComponent);
		assertEquals(0, fruit.getInternalComponents().size());
	}

	@Test
	public void testDisconnectInjectionInInternalComponent() throws Exception {

		fruit.connect("banana", bananaComponent);
		fruit.connect("apple", appleComponent);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.disconnect(bananaComponent);

		assertEquals(1, fruit.getInternalComponents().size());

/*		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}                        */

	}

	@Test
	public void testDisconnectInjectionInInternalComponent2() throws Exception {

		fruit.connect("banana", bananaComponent);
		fruit.connect("apple", appleComponent);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.disconnect(appleComponent);

		assertEquals(1, fruit.getInternalComponents().size());

/*		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}  */

	}

	@Test
	public void testConnectInjectionInInternalComponent() throws Exception {

		assertEquals(0, fruit.getInternalComponents().size());

		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}

		fruit.connect("banana", bananaComponent);
		fruit.connect("apple", appleComponent);

		assertEquals(2, fruit.getInternalComponents().size());

		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testConnectInjectionInInternalComponent2() throws Exception {

		fruit.connect("apple", appleComponent);
		fruit.connect("banana", bananaComponent);

		assertEquals(27, appleCore.getIntFromBanana());
	}


	@Test
	public void testConnectExternalComponentsMustNotRegisterListeners() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(notifierComponent);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(listenerComponent1);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectExternalComponentsMustNotRegisterListeners2() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(listenerComponent1);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(notifierComponent);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToUnexposedComponent() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierComponent);
		assertFalse(cluster.isExposed("notifier"));
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(listenerComponent2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToExposedComponent() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierComponent, NotifierInterface.class);
		assertTrue(cluster.isExposed("notifier"));
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.getFacade().connect(listenerComponent2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToUnexposedComponent2() throws Exception {

		cluster.getFacade().connect(listenerComponent1);
		cluster.getFacade().connect(listenerComponent2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierComponent);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToExposedComponent2() throws Exception {

		cluster.getFacade().connect(listenerComponent1);
		cluster.getFacade().connect(listenerComponent2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierComponent, NotifierInterface.class);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterInternalListeners() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierComponent);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("listener1-a", listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.connect("listener1-b", listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.connect("listener2", listenerComponent2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterInternalListeners2() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("listener1-a", listenerComponent1);
		cluster.connect("listener1-b", listenerComponent1);
		cluster.connect("listener2", listenerComponent2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierComponent);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testDisconnect() throws Exception {
		fruit.connect("apple", appleComponent);
		assertTrue(fruit.isConnected(appleComponent));
		fruit.disconnect(appleComponent);
		assertFalse(fruit.isConnected(appleComponent));
	}

	@Test
	public void testDisconnectInternalComponentThroughFacade() throws Exception {
		fruit.connect("apple", appleComponent);
		assertTrue(fruit.isConnected(appleComponent));
		//It's possible to disconnect internal component from outside, since it's
		//  normally impossible to retrieve a reference to the component anyway
		fruit.getFacade().disconnect(appleComponent);
		assertFalse(fruit.isConnected(appleComponent));
	}

	@Test
	public void testDisconnectUnregisterExternalListeners() throws Exception {

		cluster.connect("notifier", notifierComponent);
		cluster.getFacade().connect(listenerComponent1);
		cluster.getFacade().connect(listenerComponent2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
		cluster.getFacade().disconnect(listenerComponent1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testDisconnectUnregisterInternalListeners() throws Exception {

		cluster.connect("notifier", notifierComponent);
		cluster.connect("listener1-a", listenerComponent1);
		cluster.connect("listener1-b", listenerComponent1);
		cluster.connect("listener2", listenerComponent2);
		assertEquals(4, cluster.getInternalComponents().size());
		assertEquals(2, notifier.getNrofRegisteredListeners());

		cluster.disconnect(listenerComponent1);
		assertEquals(2, cluster.getInternalComponents().size());
		assertEquals(1, notifier.getNrofRegisteredListeners());

	}

	@Test
	public void testDisconnectRegisterExternalListenersFromUnexposedComponent() throws Exception {

		cluster.getFacade().connect(listenerComponent1);
		cluster.getFacade().connect(listenerComponent2);
		cluster.connect("notifier", notifierComponent);
		cluster.getFacade().disconnect(listenerComponent2);
		assertEquals(1, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testDisconnectRegisterExternalListenersFromUnexposedComponent2() throws Exception {

		cluster.getFacade().connect(listenerComponent1);
		cluster.getFacade().connect(listenerComponent2);
		cluster.connect("notifier", notifierComponent);
		cluster.disconnect(notifierComponent);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testExposeFacade() {
		fruit.connect("banana", bananaComponent, bananaComponent.getInterfaces());
		Facade basket = fruit.getFacade();
		basket.connect(appleComponent);
		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testGetExposedInterfaces() throws Exception {

		try {
			fruit.getExposedInterfaces("apple");
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
		fruit.connect("apple", appleComponent);
		try {
			fruit.getExposedInterfaces("apple");
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
		fruit.disconnect(appleComponent);
		fruit.connect("apple", appleComponent, appleComponent.getInterfaces());
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		fruit.disconnect(appleComponent);
		try {
			fruit.getExposedInterfaces("apple");
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}


	@Test
	public void testExpose() throws Exception {

		fruit.connect("apple", appleComponent);
		assertFalse(fruit.isExposed("apple"));
		fruit.expose("apple", AppleInterface.class);
		assertTrue(fruit.isExposed("apple"));
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
	}


	@Test
	public void testExpose2() throws Exception {
		fruit.connect("apple", elstarComponent, AppleInterface.class);
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		fruit.expose("apple", AppleInterface.class);
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		assertEquals(AppleInterface.class, fruit.getExposedInterfaces("apple")[0]);
		fruit.expose("apple", ElstarInterface.class);
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		assertEquals(ElstarInterface.class, fruit.getExposedInterfaces("apple")[0]);
	}

	@Test
	public void testExposeInjectionInExternalComponent1() throws Exception {
		fruit.connect("banana", bananaComponent);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}
		fruit.expose("banana", bananaComponent.getInterfaces());
		fruit.getFacade().connect(appleComponent);

		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testExposeInjectionInExternalComponent2() throws Exception {
		fruit.connect("banana", bananaComponent);
		fruit.getFacade().connect(appleComponent);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}

		fruit.expose("banana", bananaComponent.getInterfaces());

		assertEquals(27, appleCore.getIntFromBanana());
	}


	@Test
	public void testExposeInjectionInExternalComponent3() throws Exception {
		fruit.connect("banana", bananaComponent);
		fruit.expose("banana", bananaComponent.getInterfaces());
		fruit.getFacade().connect(appleComponent);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.expose("banana", new Class<?>[0]);

/*		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}   */
	}


	@Test
	public void testGetProxy() {


		//AppleInterface is exposed
		fruit.connect("elstar", elstarComponent, AppleInterface.class);
		//  so it can be obtained
		AppleInterface appleProxy = (AppleInterface) fruit.getFacade().getProxy("elstar", AppleInterface.class);

		try {
			ElstarInterface elstarProxy = (ElstarInterface) fruit.getFacade().getProxy("elstar", ElstarInterface.class);
			fail("ElstarInterface is not supposed to be exposed");
		}
		catch (ConfigurationException expected) {
		}
	}
}
