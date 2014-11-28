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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * Helper class containing methods to be used to investigate method declarations,
 * invoke methods, instantiate or clone objects.
 */
public class ReflectionSupport {

	/**
	 * @param clasz
	 * @return a list of all classes the given class extends
	 */
	public static ArrayList<Class<?>> getAllSuperClassesFromClass(Class<?> clasz) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();
		while (clasz.getSuperclass() != null) {
			result.add(clasz.getSuperclass());
			clasz = clasz.getSuperclass();
		}
		return result;
	}

	/**
	 * @param clasz
	 * @return all interfaces the given class implements directly or implicitly
	 */
	//TODO the next method (commented out) actually works -> test & replace
	public static ArrayList<Class<?>> getInterfacesForClass(Class<?> clasz) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();

		ArrayList<Class<?>> superClasses = getAllSuperClassesFromClass(clasz);
		superClasses.add(clasz);

		for (Class<?> superClass : superClasses) {
			Class<?>[] interfaces = superClass.getInterfaces();
			for (int j = 0; j < interfaces.length; j++) {
				if (!result.contains(interfaces[j])) {
					result.add(interfaces[j]);
				}
			}
		}
		return result;
	}

	/**
	 * @param clasz
	 * @return all interfaces the given class implements directly or implicitly
	 */
/*	public static Set<Class<?>> getInterfacesForClass(Class<?> clasz)
	{
		Set<Class<?>> result = new HashSet<Class<?>>();

		for(Class<?> interfaceClass : clasz.getInterfaces())
		{
			result.add(interfaceClass);
			result.addAll(getInterfacesForClass(interfaceClass));
		}

		ArrayList<Class<?>> superClasses = getAllSuperClassesFromClass(clasz);
		//superClasses.add(clasz);

		for (Class<?> superClass : superClasses) {
			if(superClass.isInterface())
			{
				result.add(superClass);
			}
			else
			{
				Class<?>[] interfaces = superClass.getInterfaces();
				for (int j = 0; j < interfaces.length; j++) {
					result.addAll(getInterfacesForClass(interfaces[j]));
				}
			}
		}
		return result;
	}
*/

	/**
	 * Arguments do not have to match exactly; they will be converted if possible.
	 *
	 * @param impl       the object on which the method is invoked
	 * @param methodName name of method to be invoked
	 * @param arguments  zero or more arguments
	 * @return whatever the method returns
	 * @throws InvocationTargetException if the invoked method throws
	 * @throws NoSuchMethodException     if no suitable method is found
	 */
	public static Object invokeMethod(Object impl, String methodName, Object... arguments) throws NoSuchMethodException, InvocationTargetException {
		return new MethodInvocation(impl, methodName, arguments).invoke();
	}


	/**
	 * Instantiates a class by its default constructor.
	 *
	 * @param className
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(String className)
			throws InstantiationException {
		try {
			return Class.forName(className).newInstance();
		} catch (IllegalAccessException iae) {
			throw new InstantiationException("can not instantiate class " + className + " with message: " + iae.getClass().getName() + ": " + iae.getMessage());
		} catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("class " + className + " can not be found with message: " + cnfe.getMessage());
		}
	}


	/**
	 * Instantiates a class by invoking a constructor with the given init parameters.
	 *
	 * @param className
	 * @param initArgs
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(String className, Object... initArgs)
			throws InstantiationException {
		try {
			Class<?> c = Class.forName(className);
			return instantiateClass(c, initArgs);
		} catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("class " + className + " can not be found with message: " + cnfe.getMessage());
		}
	}

	/**
	 * Instantiates a class by invoking a constructor with certain init parameters.
	 * Uses classloader to load class if class has not been loaded previously.
	 *
	 * @param classloader
	 * @param className
	 * @param initArgs
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object instantiateClass(ClassLoader classloader, String className, Object... initArgs)
			throws InstantiationException {
		try {
			Class<?> clasz = classloader.loadClass(className);
			return instantiateClass(clasz, initArgs);
		} catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("class " + className + " can not be found with message: " + cnfe.getMessage());
		}
	}


	private static HashMap<Class<?>, Constructor<?>> lastUsedConstructors = new HashMap<Class<?>, Constructor<?>>();


	/**
	 * Instantiates a class by invoking a constructor with the given init parameters.
	 *
	 * @param clasz
	 * @param initArgs
	 * @return
	 * @throws InstantiationException
	 */
	public static <T> T instantiateClass(Class<T> clasz, Object... initArgs)
			throws InstantiationException {

		Exception lastException = null;

		Constructor<?> constructor = lastUsedConstructors.get(clasz);
		if(constructor != null) {
			try {
				return (T)instantiateClass(clasz, constructor, initArgs);
			} catch (Exception e) {
				//TODO keep statistics
				lastException = e;
			}
		}


		if (initArgs == null) {
			initArgs = new Object[0];
		}
		Class<?>[] initArgTypes = getTypesForArgs(initArgs);


		try {
			//expensive call
			constructor = clasz.getConstructor(initArgTypes);
			lastUsedConstructors.put(clasz, constructor);
			return (T)instantiateClass(clasz, constructor, initArgs);
		} catch (NoSuchMethodException e) {
			lastException = e;
		}
		return getInstanceForTranslatedArgs(clasz, initArgTypes, lastException, initArgs);
	}

	private static <T> T getInstanceForTranslatedArgs(Class<T> clasz, Class<?>[] initArgTypes, Exception lastException, Object[] initArgs) throws InstantiationException {
		Constructor<?>[] constructors = clasz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			if (Modifier.isPublic(constructors[i].getModifiers())) {
				Class<?>[] inputTypes = constructors[i].getParameterTypes();
				if (inputTypes.length == initArgs.length) {
					try {
						Object[] alternativeInitArgs = Converter.convertToMatchingTypes(initArgs, inputTypes);
						if (alternativeInitArgs != null) {
							lastUsedConstructors.put(clasz, constructors[i]);
							return (T)instantiateClass(clasz, constructors[i], alternativeInitArgs);
						}
					} catch (IllegalArgumentException e) {
						//maybe another one fits
						lastException = e;
					}
				}
			}
		}
		throw new InstantiationException("can not instantiate class " + clasz.getName() + ": no matching public constructor for init args " + Arrays.asList(initArgTypes) +
				lastException != null ? "with message " + lastException : "");
	}

	private static Class<? extends Object>[] getTypesForArgs(Object[] initArgs) {
		Class<? extends Object>[] result = new Class[initArgs.length];
		for(int i = 0; i < initArgs.length; i++) {
			result[i] = initArgs[i].getClass();
		}
		return result;
	}


	/**
	 * Instantiates a class by invoking a constructor with certain init parameters.
	 * Uses classloader to load class if class has not been loaded previously.
	 *
	 * @param clasz
	 * @param constructor
	 * @param initArgs
	 * @return
	 * @throws InstantiationException
	 */
	private static Object instantiateClass(Class<?> clasz, Constructor<?> constructor, Object... initArgs) throws InstantiationException {
		try {
			return constructor.newInstance(initArgs);
		} catch (InvocationTargetException ite) {
			if (ite.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) ite.getTargetException();
			}
			if (ite.getTargetException() instanceof Error) {
				throw (Error) ite.getTargetException();
			}
			//checked target exceptions are not explicitly logged
			//occurrence of checked exception can be evaluated by examining the throws clauses
			//  of the constructors in the the javadoc
			throw new InstantiationException("can not instantiate class " +
					clasz.getName() + " due to exception in constructor with message: " +
					ite.getTargetException().getClass().getName() +
					": " + ite.getTargetException().getMessage() +
					", for init args " + initArgs);
		} catch (IllegalAccessException iae) {
			throw new InstantiationException("can not instantiate class " + clasz.getName() + " with message: " + iae.getClass().getName() + ": " + iae.getMessage() + ", for init args " + initArgs);
		}
	}

	/**
	 * @param clasz
	 * @param methodName
	 * @param requiredNrofParameters
	 * @return a set of methods with the given name and number of parameters
	 */
	public static Set<Method> getMethodsByName(Class<?> clasz, String methodName, int requiredNrofParameters) {
		Set<Method> retval = new HashSet<Method>();
		Method[] methods = clasz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (methodName.equals(method.getName()) && method.getParameterTypes().length == requiredNrofParameters) {
				retval.add(method);
			}
		}
		return retval;
	}
}