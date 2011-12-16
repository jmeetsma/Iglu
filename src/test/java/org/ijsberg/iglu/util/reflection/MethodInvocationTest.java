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

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.sample.configuration.Apple;
import org.ijsberg.iglu.sample.configuration.AppleInterface;
import org.ijsberg.iglu.sample.configuration.GetMessageInterceptor;
import org.junit.Test;

/**
 */
public class MethodInvocationTest {

	@Test
	public void testInvokePlainObject() throws Exception {
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
		} catch (InvocationTargetException expected) {
			assertEquals(NullPointerException.class, expected.getTargetException().getClass());
		}

		invocation = new MethodInvocation(apple, "setMessage");
		try {
			invocation.invoke();
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {}

		invocation = new MethodInvocation(apple, "absentMethod");
		try {
			invocation.invoke();
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {}
	}

	@Test
	public void testInvokeInvocationHandler() throws Exception {
		Apple apple = new Apple();
		StandardComponent appleComponent = new StandardComponent(apple);
		MethodInvocation invocation = new MethodInvocation(appleComponent, apple, "setMessage",
				ReflectionSupport.getMethodsByName(Apple.class, "setMessage", 1).toArray(new Method[0]), "hello");

		Object result = invocation.invoke();
		assertNull(result);

		assertEquals("hello", apple.getMessage());

		invocation = new MethodInvocation(appleComponent, apple, "getMessage",
				ReflectionSupport.getMethodsByName(AppleInterface.class, "getMessage", 0).toArray(new Method[0]));
		result = invocation.invoke();

		assertEquals("hello", result);

		appleComponent.setInvocationIntercepter(AppleInterface.class, new GetMessageInterceptor("Bingo"));
		result = invocation.invoke();

		assertEquals("helloBingo", result);

		invocation = new MethodInvocation(appleComponent, apple, "getIntFromBanana",
				ReflectionSupport.getMethodsByName(Apple.class, "getIntFromBanana", 0).toArray(new Method[0]));
		try {
			invocation.invoke();
			fail("InvocationTargetException expected");
		} catch (InvocationTargetException expected) {
			assertEquals(NullPointerException.class, expected.getTargetException().getClass());
		}

		invocation = new MethodInvocation(appleComponent, apple, "setMessage",
				ReflectionSupport.getMethodsByName(Apple.class, "setMessage", 1).toArray(new Method[0]));

		try {
			invocation.invoke();
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {}

		invocation = new MethodInvocation(appleComponent, apple, "absentMessage",
				ReflectionSupport.getMethodsByName(Apple.class, "setMessage", 0).toArray(new Method[0]));
		try {
			invocation.invoke();
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {}

	}
}
