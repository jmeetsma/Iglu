package org.ijsberg.iglu.configuration.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 */
public class MethodInvoker {

	private Object impl;
	private String methodName;
	private Object[] initArgs;
	private IllegalArgumentException failedInvocation = null;
	private Object retval = null;
	private boolean invocationSucceeded;


	public MethodInvoker(Object impl, String methodName, Object[] initArgs) {
		this.impl = impl;
		this.methodName = methodName;
		this.initArgs = initArgs;
		if (this.initArgs == null) {
			this.initArgs = new Object[0];
		}
	}

	public Object invoke() throws InvocationTargetException, NoSuchMethodException {
		tryInvokeExactSignature();
		if(!invocationSucceeded) {
			tryInvokeWithConvertedArguments();
		}
		if(invocationSucceeded) {
			return retval;
		}
		if(failedInvocation != null) {
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
				} catch (IllegalArgumentException iae) {
					failedInvocation = iae;
				}
			}
		}
	}

	private void tryInvokeExactSignature() throws InvocationTargetException {
		try {
			Method method = impl.getClass().getMethod(methodName, getInintArgTypes(initArgs));
			retval = invokePublicMethod(impl, initArgs, method);
			invocationSucceeded = true;
		} catch (NoSuchMethodException e) {
			//there are other possibilities to explore
		}
	}

	private static Class[] getInintArgTypes(Object[] initArgs) {
		Class[] types = new Class[initArgs.length];
		for(int i = 0; i < initArgs.length; i++) {
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
		} catch (IllegalAccessException privateOrProtectedInvoked) {
			//should be impossible since Class.getMethods returns only public methods
			throw new RuntimeException("somehow illegal (private or protected) method '" + method.getName() + "' invoked", privateOrProtectedInvoked);
		}
	}

}
