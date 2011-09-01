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

import org.junit.Test;

/**
 */
public class ConverterTest {

	@Test
	public void convertToObject() throws Exception {
		Object object = Converter.convertToObject("10", Integer.class);
		assertEquals(10, object);

		object = Converter.convertToObject("11", int.class);
		assertEquals(11, object);

		object = Converter.convertToObject(new Integer(12), int.class);
		assertEquals(12, object);

		object = Converter.convertToObject("true", boolean.class);
		assertEquals(true, object);

		object = Converter.convertToObject("true", Boolean.class);
		assertEquals(true, object);

		object = Converter.convertToObject("123", Boolean.class);
		assertEquals(false, object);

		try {
			object = Converter.convertToObject("word", Integer.class);
			fail("NumberFormatException expected");
		}
		catch (NumberFormatException expected) {
		}
		;

	}

	@Test
	public void convertToMatchingTypesTest() throws Exception {
		Object[] objects = Converter.convertToMatchingTypes(new Class[]{Integer.class}, new String[]{"10"});
		assertEquals(10, objects[0]);
		try {
			objects = Converter.convertToMatchingTypes(new Class[]{Integer.class}, new String[0]);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
		}
		try {
			objects = Converter.convertToMatchingTypes(new Class[]{Integer.class}, new String[]{"word"});
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
		}

		objects = Converter.convertToMatchingTypes(new Class[0], new String[0]);
		assertEquals(0, objects.length);
	}
}
