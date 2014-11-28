/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Handles invocation of a method with a particular set of arguments.
 */
public class MethodInvocation {

	private Object impl;
	private Method[] methods;

	private String methodName;
	private Object[] initArgs;
	private IllegalArgumentException failedInvocation = null;
	private Object retval = null;
	private boolean invocationSucceeded;
	private InvocationHandler invocationHandler;

	public MethodInvocation(InvocationHandler invocationHandler, Object impl, String methodName, Method[] methodSubset, Object... arguments) {
		this.impl = impl;
		this.invocationHandler = invocationHandler;
		this.methods = methodSubset;
		this.methodName = methodName;
		this.initArgs = arguments;
		if (this.initArgs == null) {
			this.initArgs = new Object[0];
		}
	}

	/**
	 * Arguments do not have to match exactly; they will be converted if possible.
	 *
	 * @param impl       the object on which the method is invoked
	 * @param methodName name of method to be invoked
	 * @param arguments  zero or more arguments
	 */
	public MethodInvocation(Object impl, String methodName, Object... arguments) {
		this.impl = impl;
		this.methods = impl.getClass().getDeclaredMethods();
		this.methodName = methodName;
		this.initArgs = arguments;
		if (this.initArgs == null) {
			this.initArgs = new Object[0];
		}
	}

	/**
	 * @return whatever the method returns
	 * @throws InvocationTargetException if the invoked method throws
	 * @throws NoSuchMethodException     if no suitable method is found
	 */
	public Object invoke() throws InvocationTargetException, NoSuchMethodException {
		retval = null;
		invocationSucceeded = false;

		if (invocationHandler == null) {
			tryInvokeExactSignature();
		}
		if (!invocationSucceeded) {
			tryInvokeWithConvertedArguments();
		}
		if (invocationSucceeded) {
			return retval;
		}
		if (failedInvocation != null) {
			throw failedInvocation;
		}
		throw new NoSuchMethodException("method '" + impl.getClass().getSimpleName() + "." + methodName + "' not found or no suitable arguments " + Arrays.asList(initArgs));

	}

	private void tryInvokeWithConvertedArguments() throws InvocationTargetException {
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == initArgs.length) {
				try {
					invokePublicMethod(impl, initArgs, methods[i]);
					invocationSucceeded = true;
					return;
				} catch (IllegalArgumentException iae) {
					failedInvocation = iae;
				}
			}
		}
	}

	private void tryInvokeExactSignature() throws InvocationTargetException {
		try {
			Method method = impl.getClass().getMethod(methodName, getInitArgTypes(initArgs));
			//System.out.println(method);
			invokePublicMethod(impl, initArgs, method);
			invocationSucceeded = true;
		} catch (NoSuchMethodException e) {
			//there are other possibilities to explore
            e.printStackTrace();
		}
	}

	private static Class<?>[] getInitArgTypes(Object[] initArgs) {
		Class<?>[] types = new Class[initArgs.length];
		for (int i = 0; i < initArgs.length; i++) {
			types[i] = initArgs[i] != null ? initArgs[i].getClass() : null;
		}
		return types;
	}

	private void invokePublicMethod(Object impl, Object[] initArgs, Method method)
			throws InvocationTargetException {
		Class<?>[] inputTypes = method.getParameterTypes();
		Object[] alternativeInitArgs = Converter.convertToMatchingTypes(initArgs, inputTypes);
		try {
			if (invocationHandler != null) {
				invokeInvocationHandler(impl, alternativeInitArgs, method);
			} else {
				retval = method.invoke(impl, alternativeInitArgs);
			}
		} catch (IllegalAccessException privateOrProtectedInvoked) {
			//should be impossible since Class.getMethods returns only public methods
			throw new RuntimeException("illegal (private or protected) method '" + method.getName() + "' invoked", privateOrProtectedInvoked);
		}
	}


	private Object invokeInvocationHandler(Object impl, Object[] initArgs, Method method) throws InvocationTargetException {
		try {
			retval = invocationHandler.invoke(impl, method, initArgs);
			return retval;
		} catch (InvocationTargetException t) {
			throw (InvocationTargetException) t;
		} catch (IllegalArgumentException t) {
			throw (IllegalArgumentException) t;
		} catch (Throwable t) {
			throw new InvocationTargetException(t);
		}
	}

}
