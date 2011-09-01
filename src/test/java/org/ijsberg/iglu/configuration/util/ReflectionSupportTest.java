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

package org.ijsberg.iglu.configuration.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import org.ijsberg.iglu.configuration.sample.Apple;
import org.ijsberg.iglu.configuration.sample.Peach;
import org.junit.Test;

/**
 */
public class ReflectionSupportTest {

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
		assertEquals(27, ReflectionSupport.invokeMethod(peach, "setTaste", 27));
		assertEquals(27, ReflectionSupport.invokeMethod(peach, "setTaste", new Integer[]{27}));
		assertEquals(27, ReflectionSupport.invokeMethod(peach, "setTaste", new Double[]{27.}));
		assertEquals(27, ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{27.}));
		assertEquals("27", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{"27"}));
		assertEquals("sweet27", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{"sweet", 27.}));
		try {
			assertEquals("sweet27", ReflectionSupport.invokeMethod(peach, "setTaste", new Object[]{"sweet", "twentyseven"}));
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
		}
		;
	}
}
