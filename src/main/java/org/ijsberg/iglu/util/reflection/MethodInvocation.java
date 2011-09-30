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

import org.ijsberg.iglu.util.types.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Handles invocation of a method with a particular set of arguments.
 */
public class MethodInvocation {

	private Object impl;
	private String methodName;
	private Object[] initArgs;
	private IllegalArgumentException failedInvocation = null;
	private Object retval = null;
	private boolean invocationSucceeded;

	/**
	 * Arguments do not have to match exactly; they will be converted if possible.
	 *
	 * @param impl the object on which the method is invoked
	 * @param methodName name of method to be invoked
	 * @param arguments zero or more arguments
	 */
	public MethodInvocation(Object impl, String methodName, Object... arguments) {
		this.impl = impl;
		this.methodName = methodName;
		this.initArgs = arguments;
		if (this.initArgs == null) {
			this.initArgs = new Object[0];
		}
	}

	/**
	 *
	 * @return whatever the method returns
	 * @throws InvocationTargetException if the invoked method throws
	 * @throws NoSuchMethodException if no suitable method is found
	 */
	public Object invoke() throws InvocationTargetException, NoSuchMethodException {
		tryInvokeExactSignature();
		if (!invocationSucceeded) {
			tryInvokeWithConvertedArguments();
		}
		if (invocationSucceeded) {
			return retval;
		}
		if (failedInvocation != null) {
			throw failedInvocation;
		}
		throw new NoSuchMethodException("arguments " + Arrays.asList(initArgs) + " not suitable for method '" + methodName + "'");

	}

	private void tryInvokeWithConvertedArguments() throws InvocationTargetException {
		Method[] methods = impl.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == initArgs.length) {
				try {
					retval = invokePublicMethod(impl, initArgs, methods[i]);
					invocationSucceeded = true;
					return;
				}
				catch (IllegalArgumentException iae) {
					failedInvocation = iae;
				}
			}
		}
	}

	private void tryInvokeExactSignature() throws InvocationTargetException {
		try {
			Method method = impl.getClass().getMethod(methodName, getInitArgTypes(initArgs));
			retval = invokePublicMethod(impl, initArgs, method);
			invocationSucceeded = true;
		}
		catch (NoSuchMethodException e) {
			//there are other possibilities to explore
		}
	}

	private static Class[] getInitArgTypes(Object[] initArgs) {
		Class[] types = new Class[initArgs.length];
		for (int i = 0; i < initArgs.length; i++) {
			types[i] = initArgs[i] != null ? initArgs[i].getClass() : null;
		}
		return types;
	}

	private static Object invokePublicMethod(Object impl, Object[] initArgs, Method method)
			throws InvocationTargetException {
		Class[] inputTypes = method.getParameterTypes();
		Object[] alternativeInitArgs = Converter.convertToMatchingTypes(inputTypes, initArgs);
		try {
			return method.invoke(impl, alternativeInitArgs);
		}
		catch (IllegalAccessException privateOrProtectedInvoked) {
			//should be impossible since Class.getMethods returns only public methods
			throw new RuntimeException("somehow illegal (private or protected) method '" + method.getName() + "' invoked", privateOrProtectedInvoked);
		}
	}

}
