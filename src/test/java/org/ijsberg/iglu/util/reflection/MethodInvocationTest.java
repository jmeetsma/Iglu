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

package org.ijsberg.iglu.util.reflection;

import org.ijsberg.iglu.sample.configuration.Apple;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 */
public class MethodInvocationTest {

	@Test
	public void execute() throws Exception {
		Apple apple = new Apple();
		MethodInvocation invocation = new MethodInvocation(apple, "setMessage", "hello");

		Object result = invocation.invoke();
		assertNull(result);

		assertEquals("hello", apple.getMessage());

		invocation = new MethodInvocation(apple, "getMessage");
		result = invocation.invoke();

		assertEquals("hello", result);

		invocation = new MethodInvocation(apple, "getIntFromBanana");
		try {
			invocation.invoke();
			fail("InvocationTargetException expected");
		} catch (InvocationTargetException expected) {}

		invocation = new MethodInvocation(apple, "setMessage()");
		try {
			invocation.invoke();
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {}

		invocation = new MethodInvocation(apple, "absentMethod()");
		try {
			invocation.invoke();
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {}

	}
}
