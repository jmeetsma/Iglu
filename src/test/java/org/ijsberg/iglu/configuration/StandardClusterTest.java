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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.ijsberg.iglu.Cluster;
import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.Layer;
import org.ijsberg.iglu.Module;
import org.ijsberg.iglu.configuration.sample.Apple;
import org.ijsberg.iglu.configuration.sample.AppleInterface;
import org.ijsberg.iglu.configuration.sample.Banana;
import org.ijsberg.iglu.configuration.sample.BananaInterface;
import org.ijsberg.iglu.configuration.sample.Elstar;
import org.ijsberg.iglu.configuration.sample.ElstarInterface;
import org.ijsberg.iglu.configuration.sample.Listener;
import org.ijsberg.iglu.configuration.sample.Notifier;
import org.ijsberg.iglu.configuration.sample.NotifierInterface;
import org.junit.Before;
import org.junit.Test;

public class StandardClusterTest {

	private StandardCluster fruit;
	private Apple appleCore;
	private Module appleModule;
	private Banana bananaCore;
	private Module bananaModule;
	private Elstar elstar;
	private Module elstarModule;


	private StandardCluster cluster;
	private Notifier notifier;
	private Listener listener1;
	private Listener listener2;
	private Module notifierModule;
	private Module listenerModule1;
	private Module listenerModule2;

	@Before
	public void setUp() throws Exception {
		fruit = new StandardCluster();
		appleCore = new Apple();
		appleModule = new StandardModule(appleCore);
		bananaCore = new Banana(27);
		bananaModule = new StandardModule(bananaCore);
		elstar = new Elstar();
		elstarModule = new StandardModule(elstar);

		cluster = new StandardCluster();
		notifier = new Notifier();
		listener1 = new Listener("listener 1");
		listener2 = new Listener("listener 2");
		notifierModule = new StandardModule(notifier);
		listenerModule1 = new StandardModule(listener1);
		listenerModule2 = new StandardModule(listener2);
	}

	@Test
	public void testCluster() throws Exception {
		assertFalse(Cluster.class.isAssignableFrom(Layer.class));
		assertFalse(Layer.class.isAssignableFrom(Cluster.class));
	}

	@Test
	public void testConnectInternalModule() throws Exception {
		assertEquals(0, fruit.getInternalModules().size());
		fruit.connect("apple", appleModule);
		assertEquals(1, fruit.getInternalModules().size());
	}

	@Test
	public void testConnectInternalModuleAndExpose() throws Exception {
		assertEquals(0, fruit.getExposedModuleIds().size());
		fruit.connect("apple", appleModule, AppleInterface.class);
		assertEquals(1, fruit.getExposedModuleIds().size());
		assertEquals("apple", fruit.getExposedModuleIds().toArray()[0]);
	}

	@Test
	public void testDisconnectInternalModule() throws Exception {
		fruit.connect("apple", appleModule);
		assertEquals(1, fruit.getInternalModules().size());
		fruit.disconnect(appleModule);
		assertEquals(0, fruit.getInternalModules().size());
	}

	@Test
	public void testDisonnectInternalModuleAndExpose() throws Exception {
		fruit.connect("apple", appleModule, AppleInterface.class);
		fruit.disconnect(appleModule);
		assertEquals(0, fruit.getExposedModuleIds().size());
	}

	@Test
	public void testConnectExternalModule() throws Exception {
		assertEquals(0, fruit.getExternalModules().size());
		fruit.asLayer().connect(appleModule);
		assertEquals(1, fruit.getExternalModules().size());
	}

	@Test
	public void testDisconnectExternalModule() throws Exception {
		fruit.asLayer().connect(appleModule);
		assertEquals(1, fruit.getExternalModules().size());
		fruit.asLayer().disconnect(appleModule);
		assertEquals(0, fruit.getExternalModules().size());
	}

	@Test
	public void testConnectExternalModuleTwice() throws Exception {
		fruit.asLayer().connect(appleModule);
		try {
			fruit.asLayer().connect(appleModule);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectExternalModuleAsInternalModule() throws Exception {
		fruit.asLayer().connect(appleModule);
		try {
			fruit.connect("apple", appleModule);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectInternalModuleAsExternalModule() throws Exception {
		fruit.connect("apple", appleModule);
		try {
			fruit.asLayer().connect(appleModule);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectInternalModuleTwice1() throws Exception {
		fruit.connect("apple", appleModule);
		try {
			fruit.connect("apple", appleModule);
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}

	@Test
	public void testConnectInternalModuleTwice2() throws Exception {
		fruit.connect("red apple", appleModule);
		fruit.connect("sumptious apple", appleModule);
		assertEquals(2, fruit.getInternalModules().size());
	}

	@Test
	public void testConnectInternalModuleTwice3() throws Exception {
		fruit.connect("apple", elstarModule, AppleInterface.class);
		fruit.connect("elstar", elstarModule, ElstarInterface.class);
		assertEquals(2, fruit.getInternalModules().size());
		assertEquals(2, fruit.getExposedModuleIds().size());
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		assertEquals(1, fruit.getExposedInterfaces("elstar").length);
	}

	@Test
	public void testDisonnectInternalModuleTwice3() throws Exception {
		fruit.connect("apple", elstarModule, AppleInterface.class);
		fruit.connect("elstar", elstarModule, ElstarInterface.class);
		fruit.disconnect(elstarModule);
		assertEquals(0, fruit.getInternalModules().size());
		assertEquals(0, fruit.getExposedModuleIds().size());
	}

	@Test
	public void testConnectInjectionInExternalModule() throws Exception {
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}

		fruit.connect("banana", bananaModule, bananaModule.getInterfaces());
		fruit.asLayer().connect(appleModule);

		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testConnectInjectionInExternalModule2() throws Exception {

		fruit.asLayer().connect(appleModule);
		fruit.connect("banana", bananaModule, bananaModule.getInterfaces());

		assertEquals(27, appleCore.getIntFromBanana());
	}


	@Test
	public void testConnectInjectionInExternalModule3() throws Exception {
		fruit.connect("banana", bananaModule);
		fruit.asLayer().connect(appleModule);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}
	}

	@Test
	public void testConnectInjectionInExternalModule4() throws Exception {

		fruit.asLayer().connect(appleModule);
		fruit.connect("banana", bananaModule);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException expected) {
		}
	}

	@Test
	public void testDisconnectInternalModuleFromExternal() throws Exception {

		fruit.connect("banana", bananaModule, BananaInterface.class);
		fruit.asLayer().connect(appleModule);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.disconnect(bananaModule);

		assertEquals(0, fruit.getInternalModules().size());

		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}

	}

	@Test
	public void testDisconnectExternalModuleFromInternal() throws Exception {

		fruit.connect("banana", bananaModule, BananaInterface.class);
		fruit.asLayer().connect(appleModule);


		assertEquals(27, appleCore.getIntFromBanana());

		fruit.asLayer().disconnect(appleModule);

		assertEquals(0, fruit.getExternalModules().size());

		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}

	}

	@Test
	public void testDisconnectDoubleConnectedInternalModule() throws Exception {
		fruit.connect("red apple", appleModule);
		fruit.connect("sumptious apple", appleModule);
		fruit.disconnect(appleModule);
		assertEquals(0, fruit.getInternalModules().size());
	}

	@Test
	public void testDisconnectInjectionInInternalModule() throws Exception {

		fruit.connect("banana", bananaModule);
		fruit.connect("apple", appleModule);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.disconnect(bananaModule);

		assertEquals(1, fruit.getInternalModules().size());

		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}

	}

	@Test
	public void testDisconnectInjectionInInternalModule2() throws Exception {

		fruit.connect("banana", bananaModule);
		fruit.connect("apple", appleModule);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.disconnect(appleModule);

		assertEquals(1, fruit.getInternalModules().size());

		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}

	}

	@Test
	public void testConnectInjectionInInternalModule() throws Exception {

		assertEquals(0, fruit.getInternalModules().size());

		try {
			appleCore.getIntFromBanana();
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
		}

		fruit.connect("banana", bananaModule);
		fruit.connect("apple", appleModule);

		assertEquals(2, fruit.getInternalModules().size());

		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testConnectInjectionInInternalModule2() throws Exception {

		fruit.connect("apple", appleModule);
		fruit.connect("banana", bananaModule);

		assertEquals(27, appleCore.getIntFromBanana());
	}


	@Test
	public void testConnectExternalModulesMustNotRegisterListeners() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(notifierModule);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(listenerModule1);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectExternalModulesMustNotRegisterListeners2() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(listenerModule1);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(notifierModule);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToUnexposedModule() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierModule);
		assertFalse(cluster.isExposed("notifier"));
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(listenerModule2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToExposedModule() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierModule, NotifierInterface.class);
		assertTrue(cluster.isExposed("notifier"));
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.asLayer().connect(listenerModule2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToUnexposedModule2() throws Exception {

		cluster.asLayer().connect(listenerModule1);
		cluster.asLayer().connect(listenerModule2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierModule);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterExternalListenersToExposedModule2() throws Exception {

		cluster.asLayer().connect(listenerModule1);
		cluster.asLayer().connect(listenerModule2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierModule, NotifierInterface.class);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterInternalListeners() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierModule);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("listener1-a", listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.connect("listener1-b", listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
		cluster.connect("listener2", listenerModule2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testConnectRegisterInternalListeners2() throws Exception {

		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("listener1-a", listenerModule1);
		cluster.connect("listener1-b", listenerModule1);
		cluster.connect("listener2", listenerModule2);
		assertEquals(0, notifier.getNrofRegisteredListeners());
		cluster.connect("notifier", notifierModule);
		assertEquals(2, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testDisconnect() throws Exception {
		fruit.connect("apple", appleModule);
		assertTrue(fruit.isConnected(appleModule));
		fruit.disconnect(appleModule);
		assertFalse(fruit.isConnected(appleModule));
	}

	@Test
	public void testDisconnectInternalModuleThroughLayer() throws Exception {
		fruit.connect("apple", appleModule);
		assertTrue(fruit.isConnected(appleModule));
		//It's possible to disconnect internal module from outside, since it's
		//  normally impossible to retrieve a reference to the module anyway
		fruit.asLayer().disconnect(appleModule);
		assertFalse(fruit.isConnected(appleModule));
	}

	@Test
	public void testDisconnectUnregisterExternalListeners() throws Exception {

		cluster.connect("notifier", notifierModule);
		cluster.asLayer().connect(listenerModule1);
		cluster.asLayer().connect(listenerModule2);
		assertEquals(2, notifier.getNrofRegisteredListeners());
		cluster.asLayer().disconnect(listenerModule1);
		assertEquals(1, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testDisconnectUnregisterInternalListeners() throws Exception {

		cluster.connect("notifier", notifierModule);
		cluster.connect("listener1-a", listenerModule1);
		cluster.connect("listener1-b", listenerModule1);
		cluster.connect("listener2", listenerModule2);
		assertEquals(4, cluster.getInternalModules().size());
		assertEquals(2, notifier.getNrofRegisteredListeners());

		cluster.disconnect(listenerModule1);
		assertEquals(2, cluster.getInternalModules().size());
		assertEquals(1, notifier.getNrofRegisteredListeners());

	}

	@Test
	public void testDisconnectRegisterExternalListenersFromUnexposedModule() throws Exception {

		cluster.asLayer().connect(listenerModule1);
		cluster.asLayer().connect(listenerModule2);
		cluster.connect("notifier", notifierModule);
		cluster.asLayer().disconnect(listenerModule2);
		assertEquals(1, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testDisconnectRegisterExternalListenersFromUnexposedModule2() throws Exception {

		cluster.asLayer().connect(listenerModule1);
		cluster.asLayer().connect(listenerModule2);
		cluster.connect("notifier", notifierModule);
		cluster.disconnect(notifierModule);
		assertEquals(0, notifier.getNrofRegisteredListeners());
	}

	@Test
	public void testExposeLayer() {
		fruit.connect("banana", bananaModule, bananaModule.getInterfaces());
		Layer basket = fruit.asLayer();
		basket.connect(appleModule);
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
		fruit.connect("apple", appleModule);
		try {
			fruit.getExposedInterfaces("apple");
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
		fruit.disconnect(appleModule);
		fruit.connect("apple", appleModule, appleModule.getInterfaces());
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		fruit.disconnect(appleModule);
		try {
			fruit.getExposedInterfaces("apple");
			fail("ConfigurationException expected");
		}
		catch (ConfigurationException expected) {
		}
	}


	@Test
	public void testExpose() throws Exception {

		fruit.connect("apple", appleModule);
		assertFalse(fruit.isExposed("apple"));
		fruit.expose("apple", AppleInterface.class);
		assertTrue(fruit.isExposed("apple"));
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
	}


	@Test
	public void testExpose2() throws Exception {
		fruit.connect("apple", elstarModule, AppleInterface.class);
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		fruit.expose("apple", AppleInterface.class);
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		assertEquals(AppleInterface.class, fruit.getExposedInterfaces("apple")[0]);
		fruit.expose("apple", ElstarInterface.class);
		assertEquals(1, fruit.getExposedInterfaces("apple").length);
		assertEquals(ElstarInterface.class, fruit.getExposedInterfaces("apple")[0]);
	}

	@Test
	public void testExposeInjectionInExternalModule1() throws Exception {
		fruit.connect("banana", bananaModule);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}
		fruit.expose("banana", bananaModule.getInterfaces());
		fruit.asLayer().connect(appleModule);

		assertEquals(27, appleCore.getIntFromBanana());
	}

	@Test
	public void testExposeInjectionInExternalModule2() throws Exception {
		fruit.connect("banana", bananaModule);
		fruit.asLayer().connect(appleModule);
		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}

		fruit.expose("banana", bananaModule.getInterfaces());

		assertEquals(27, appleCore.getIntFromBanana());
	}


	@Test
	public void testExposeInjectionInExternalModule3() throws Exception {
		fruit.connect("banana", bananaModule);
		fruit.expose("banana", bananaModule.getInterfaces());
		fruit.asLayer().connect(appleModule);

		assertEquals(27, appleCore.getIntFromBanana());

		fruit.expose("banana", new Class<?>[0]);

		try {
			appleCore.getIntFromBanana();
			fail();
		}
		catch (NullPointerException eexpected) {
		}
	}


	@Test
	public void testGetProxy() {


		//AppleInterface is exposed
		fruit.connect("elstar", elstarModule, AppleInterface.class);
		//  so it can be obtained
		AppleInterface appleProxy = (AppleInterface) fruit.asLayer().getProxy("elstar", AppleInterface.class);

		try {
			ElstarInterface elstarProxy = (ElstarInterface) fruit.asLayer().getProxy("elstar", ElstarInterface.class);
			fail("ElstarInterface is not supposed to be exposed");
		}
		catch (ConfigurationException expected) {
		}
	}
}
