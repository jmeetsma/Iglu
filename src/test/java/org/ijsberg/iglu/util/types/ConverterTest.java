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

package org.ijsberg.iglu.util.types;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 */
public class ConverterTest {

	@Test
	public void testConvertToInteger() {

		Object result = Converter.convertToInteger("27");
		assertTrue(result instanceof Integer);
		assertEquals(27, result);

		try {
			Converter.convertToInteger("word");
			fail("NumberFormatException expected");
		} catch(NumberFormatException expected) {}

		try {
			Converter.convertToInteger(null);
			fail("NullPointerException expected");
		} catch(NullPointerException expected) {}

		result = Converter.convertToInteger(3d);
		assertTrue(result instanceof Integer);
		assertEquals(3, result);

		result = Converter.convertToInteger('A');
		assertEquals(65, result);
	}

	@Test
	public void testConvertToLong() {
		Object result = Converter.convertToLong("27");
		assertTrue(result instanceof Long);
		assertEquals(27L, result);

		try {
			Converter.convertToLong("word");
			fail("NumberFormatException expected");
		} catch(NumberFormatException expected) {}

		result = Converter.convertToLong(3d);
		assertTrue(result instanceof Long);
		assertEquals(3L, result);

		result = Converter.convertToLong('A');
		assertEquals(65L, result);
	}

	@Test
	public void testConvertToShort() {
		Object result = Converter.convertToShort("27");
		assertTrue(result instanceof Short);
		assertEquals((short)27, result);

		try {
			Converter.convertToShort("word");
			fail("NumberFormatException expected");
		} catch(NumberFormatException expected) {}

		result = Converter.convertToShort(3d);
		assertTrue(result instanceof Short);
		assertEquals((short)3, result);

		result = Converter.convertToShort('A');
		assertEquals((short)65, result);
	}

	@Test
	public void testConvertToByte() {
		Object result = Converter.convertToByte("27");
		assertTrue(result instanceof Byte);
		assertEquals((byte)27, result);

		try {
			Converter.convertToByte("word");
			fail("NumberFormatException expected");
		} catch(NumberFormatException expected) {}

		result = Converter.convertToByte(3d);
		assertTrue(result instanceof Byte);
		assertEquals((byte)3, result);

		result = Converter.convertToByte('A');
		assertEquals((byte)65, result);
	}

	@Test
	public void testConvertToFloat() {
		Object result = Converter.convertToFloat("27.01");
		assertTrue(result instanceof Float);
		assertEquals(27.01f, result);

		try {
			Converter.convertToFloat("word");
			fail("NumberFormatException expected");
		} catch(NumberFormatException expected) {}

		result = Converter.convertToFloat(3);
		assertTrue(result instanceof Float);
		assertEquals(3.0f, result);
	}

	@Test
	public void testConvertToDouble() {
		Object result = Converter.convertToDouble("27.01");
		assertTrue(result instanceof Double);
		assertEquals(27.01d, result);

		try {
			Converter.convertToDouble("word");
			fail("NumberFormatException expected");
		} catch(NumberFormatException expected) {}

		result = Converter.convertToDouble(3);
		assertTrue(result instanceof Double);
		assertEquals(3.0d, result);
	}

	@Test
	public void testConvertToBoolean() {
		Object result = Converter.convertToBoolean("true");
		assertTrue(result instanceof Boolean);
		assertEquals(true, result);

		result = Converter.convertToBoolean("false");
		assertEquals(false, result);

		try {
			Converter.convertToBoolean(null);
			fail("NullPointerException expected");
		} catch(NullPointerException expected) {}

		Converter.convertToBoolean("word");
		assertEquals(false, result);

		result = Converter.convertToBoolean(1);
		assertTrue(result instanceof Boolean);
		assertEquals(true, result);

		result = Converter.convertToBoolean(0.1);
		assertEquals(true, result);

		result = Converter.convertToBoolean(-0.1);
		assertEquals(true, result);

		result = Converter.convertToBoolean(0);
		assertEquals(false, result);

		result = Converter.convertToBoolean(0.0);
		assertEquals(false, result);
	}

	@Test
	public void testConvertToCharacter() {
		Object result = Converter.convertToCharacter("A");
		assertTrue(result instanceof Character);
		assertEquals('A', result);

		try {
			Converter.convertToCharacter(null);
			fail("NullPointerException expected");
		} catch(NullPointerException expected) {}

		result = Converter.convertToCharacter("Aap");
		assertEquals('A', result);

		result = Converter.convertToCharacter(65);
		assertEquals('A', result);
	}

	@Test
	public void convertToObject() throws Exception {
		Object object = Converter.convertToObject("10", Integer.class);
		assertEquals(10, object);

		object = Converter.convertToObject("11", int.class);
		assertEquals(11, object);

		object = Converter.convertToObject(12, int.class);
		assertEquals(12, object);

		object = Converter.convertToObject("true", boolean.class);
		assertEquals(true, object);

		object = Converter.convertToObject("true", Boolean.class);
		assertEquals(true, object);

		object = Converter.convertToObject("123", Boolean.class);
		assertEquals(false, object);

		try {
			Converter.convertToObject("word", Integer.class);
			fail("NumberFormatException expected");
		}
		catch (NumberFormatException expected) {
		}
	}

	@Test
	public void convertToMatchingTypesTest() throws Exception {
		Object[] objects = Converter.convertToMatchingTypes(new Class[]{Integer.class}, new String[]{"10"});
		assertEquals(10, objects[0]);
		try {
			Converter.convertToMatchingTypes(new Class[]{Integer.class}, new String[0]);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
		}
		try {
			Converter.convertToMatchingTypes(new Class[]{Integer.class}, new String[]{"word"});
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
		}

		objects = Converter.convertToMatchingTypes(new Class[0], new String[0]);
		assertEquals(0, objects.length);
	}
}
