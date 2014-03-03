/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
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

package org.ijsberg.iglu.util.reflection;


import org.ijsberg.iglu.sample.configuration.*;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 */
public class ReflectionSupportTest {

	@Test
	public void testGetAllSuperClassesFromClass() throws Exception {
		ArrayList<Class<?>> classes = ReflectionSupport.getAllSuperClassesFromClass(Elstar.class);
		assertEquals(2, classes.size());
		assertEquals(Apple.class, classes.get(0));
		assertEquals(Object.class, classes.get(1));
	}

	@Test
	public void testGetInterfacesForClass() throws Exception {
		ArrayList<Class<?>> interfaces = ReflectionSupport.getInterfacesForClass(Apple.class);
		assertEquals(1, interfaces.size());
		assertEquals(AppleInterface.class, interfaces.get(0));

		interfaces = ReflectionSupport.getInterfacesForClass(Elstar.class);
		assertEquals(2, interfaces.size());
		assertEquals(AppleInterface.class, interfaces.get(0));
		assertEquals(ElstarInterface.class, interfaces.get(1));
	}

	@Test
	public void testInstantiate() throws Exception {
		Object instance = ReflectionSupport.instantiateClass("java.lang.String");
		assertNotNull(instance);

		try {
			instance = ReflectionSupport.instantiateClass("java.util.String");
			fail("InstantiationException expected");
		} catch (InstantiationException expected) {
		}
	}

	@Test
	public void testInstantiateWithParameters() throws Exception {
		Object instance = ReflectionSupport.instantiateClass("java.lang.String", "hoppa");
		assertEquals("hoppa", instance);

		try {
			instance = ReflectionSupport.instantiateClass("java.util.String", "hoppa");
			fail("InstantiationException expected");
		} catch (InstantiationException expected) {
		}

		instance = ReflectionSupport.instantiateClass("java.lang.String", 8);
		assertEquals("8", instance);
	}

	@Test
	public void invokeMethodTest() throws Exception {

		Apple apple = new Apple();
		assertNull(ReflectionSupport.invokeMethod(apple, "getMessage", new String[]{}));
		apple.setMessage("27");
		assertEquals("27", ReflectionSupport.invokeMethod(apple, "getMessage", new String[]{}));
		ReflectionSupport.invokeMethod(apple, "setMessage", new String[]{"456"});
		assertEquals("456", apple.getMessage());

		ReflectionSupport.invokeMethod(apple, "setSomeInt", new String[]{"789"});
		assertEquals(789, apple.getSomeInt());
	}

	@Test
	public void invokeMethodNoArgumentsTest() throws Exception {
		Apple apple = new Apple();
		apple.setMessage("test");
		assertEquals("test", ReflectionSupport.invokeMethod(apple, "getMessage", new String[]{}));

	}

	@Test
	public void invokeMethodMultipleTest() throws Exception {

		Peach peach = new Peach();

		assertNull(ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{null}));
		assertEquals("sweet", ReflectionSupport.invokeMethod(peach, "setTaste", new String[]{"sweet"}));
		//TODO
        assertEquals(27, ReflectionSupport.invokeMethod(peach, "setTaste", 27));
		assertEquals(27, ReflectionSupport.invokeMethod(peach, "setTaste", new Integer[]{27}));
		assertEquals("27.0", ReflectionSupport.invokeMethod(peach, "setTaste", new Double[]{27.}));
		assertEquals("27.0", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{27.}));
		assertEquals("27", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{"27"}));
		assertEquals("sweet27", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{"sweet", 27.}));
		try {
			assertEquals("sweet27", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{"sweet", "twentyseven"}));
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
		}
	}
}
