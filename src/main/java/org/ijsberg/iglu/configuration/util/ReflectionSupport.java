/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.configuration.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;


/**
 * Helper class containing methods to be used to investigate method declarations,
 * invoke methods, instantiate or clone objects.
 */
public class ReflectionSupport {

	/**
	 * @param clasz
	 * @return a list of all classes the given class extends
	 */
	public static ArrayList<Class<?>> getAllSuperclassesFromClass(Class<?> clasz) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();
		while (clasz.getSuperclass() != null) {
			result.add(clasz.getSuperclass());
			clasz = clasz.getSuperclass();
		}
		return result;
	}

	/**
	 * Note: this method recurses into all superclasses
	 *
	 * @param clasz
	 * @return all interfaces this class implements
	 */
	public static ArrayList<Class<?>> getInterfacesForClass(Class<?> clasz) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();

		ArrayList<Class<?>> superClasses = getAllSuperclassesFromClass(clasz);
		superClasses.add(clasz);

		Iterator<Class<?>> i = superClasses.iterator();
		while (i.hasNext()) {
			Class<?>[] interfaces = ((Class<?>) i.next()).getInterfaces();
			for (int j = 0; j < interfaces.length; j++) {
				if (!result.contains(interfaces[j])) {
					result.add(interfaces[j]);
				}
			}
		}
		return result;
	}


	/**
	 * Instantiates a class by invoking a constructor with certain init parameters.
	 *
	 * @param className
	 * @param initArgs
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(String className, Object[] initArgs)
			throws InstantiationException {
		try {
			Class c = Class.forName(className);
			return instantiateClass(c, initArgs);
		}
		catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("class " + className + " can not be found with message: " + cnfe.getMessage());
		}
	}

	/**
	 * Instantiates a class by invoking a constructor with certain init parameters.
	 * Uses classloader to load class if class was not loaded previously.
	 *
	 * @param classloader
	 * @param className
	 * @param initArgs
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(ClassLoader classloader, String className, Object[] initArgs)
			throws InstantiationException {
		try {
			Class clasz = classloader.loadClass(className);
			return instantiateClass(clasz, initArgs);
		}
		catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("class " + className + " can not be found with message: " + cnfe.getMessage());
		}
	}


	/**
	 * Does utmost to instantiate class by a public constructor
	 * for the given arguments.
	 *
	 * @param clasz
	 * @param initArgs
	 * @return
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(Class clasz, Object[] initArgs)
			throws InstantiationException {
		if (initArgs == null) {
			initArgs = new Object[0];
		}
		Constructor[] constructors = clasz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			if (Modifier.isPublic(constructors[i].getModifiers())) {
				Class[] inputTypes = constructors[i].getParameterTypes();
				Object[] alternativeInitArgs = Converter.convertToMatchingTypes(inputTypes, initArgs);
				if (alternativeInitArgs != null) {
					return invokeConstructor(constructors[i], alternativeInitArgs, clasz);
				}
			}
		}
		throw new InstantiationException("can not instantiate class " + clasz.getName() + ": no matching constructor for init args " + initArgs);
	}


	/**
	 *
	 * @param impl
	 * @param methodName
	 * @param initArgs
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public static Object invokeMethod(Object impl, String methodName, Object ... initArgs) throws NoSuchMethodException, InvocationTargetException {
		return new MethodInvoker(impl, methodName, initArgs).invoke();
	}

/*
	//example of a too long method for BLOG

	public static Object invokeMethod(Object impl, String methodName, Object ... initArgs) throws NoSuchMethodException, InvocationTargetException {
		if (initArgs == null) {
			initArgs = new Object[0];
		}
		boolean methodWithNameAndMatchingNrofParamsPresent = false;
		IllegalArgumentException failedInvocation = null;

		//try exact match first
		try {
			Method method = impl.getClass().getMethod(methodName, getInintArgTypes(initArgs));
			return invokePublicMethod(impl, initArgs, method);
		} catch (NoSuchMethodException e) {
			//this shouldn't be an exception in the first place
		}

		Method[] methods = impl.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == initArgs.length) {
				try {
					methodWithNameAndMatchingNrofParamsPresent = true;
					return invokePublicMethod(impl, initArgs, methods[i]);
				} catch (IllegalArgumentException iae) {
					failedInvocation = iae;
				}
			}
		}
		if(failedInvocation != null) {
			throw failedInvocation;
		}
		if(!methodWithNameAndMatchingNrofParamsPresent) {
			throw new NoSuchMethodException("no method found with name '" + methodName + "' and " + initArgs.length + " parameters");
		}
		throw new NoSuchMethodException("arguments " + Arrays.asList(initArgs) + " not suitable for method '" + methodName + "'");
	}




	 */


	private static Object invokeConstructor(Constructor constructor, Object[] alternativeInitArgs, Class clasz) throws InstantiationException {
		try {
			return constructor.newInstance(alternativeInitArgs);
		}
		catch (InvocationTargetException ite) {
			if (ite.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) ite.getTargetException();
			}
			if (ite.getTargetException() instanceof Error) {
				throw (Error) ite.getTargetException();
			}
			//checked target exceptions are not explicitly logged
			//occurrance of checked exception can be evaluated by examining the throws clauses
			//  of the constructors in the the javadoc
			throw new InstantiationException("can not instantiate class " +
					clasz.getName() + " due to exception in constructor with message: " +
					ite.getTargetException().getClass().getName() +
					": " + ite.getTargetException().getMessage() +
					", for init args " + alternativeInitArgs);
		}
		catch (IllegalAccessException iae) {
			throw new InstantiationException("can not instantiate class " + clasz.getName() + " with message: " + iae.getClass().getName() + ": " + iae.getMessage() + ", for init args " + alternativeInitArgs);
		}
	}

	/**
	 * Instantiates a class by its default constructor.
	 *
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(String className)
			throws InstantiationException {
		try {
			return Class.forName(className).newInstance();
		}
		catch (IllegalAccessException iae) {
			throw new InstantiationException("can not instantiate class " + className + " with message: " + iae.getClass().getName() + ": " + iae.getMessage());
		}
		catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("class " + className + " can not be found with message: " + cnfe.getMessage());
		}
	}


}