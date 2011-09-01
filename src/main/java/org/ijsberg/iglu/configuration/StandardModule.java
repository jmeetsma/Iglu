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

package org.ijsberg.iglu.configuration;

import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.Layer;
import org.ijsberg.iglu.Module;
import org.ijsberg.iglu.configuration.util.Converter;
import org.ijsberg.iglu.configuration.util.ReflectionSupport;
import org.ijsberg.iglu.configuration.util.StringSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Standard implementation of Module.
 */
public class StandardModule implements Module, InvocationHandler {

	public static final String PROPERTIES_PROPERTY_KEY = "properties";
	public static final String REGISTER_LISTENER_METHOD_NAME = "register";
	public static final String UNREGISTER_LISTENER_METHOD_NAME = "unregister";

	private Object implementation;
	private Properties properties;
	private Properties setterInjectedProperties = new Properties();

	private HashMap<Class<?>, InvocationHandler> invocationHandlers = new HashMap<Class<?>, InvocationHandler>();
	private HashMap<String, Set<Class<?>>> injectedProxyTypesByModuleId = new HashMap<String, Set<Class<?>>>();

	private Map<Module, Map<Class<?>, Object>> registeredListenersByModule = new HashMap<Module, Map<Class<?>, Object>>();

	public StandardModule(Object implementation) {
		if (implementation == null) {
			throw new NullPointerException("implementation can not be null");
		}
		this.implementation = implementation;
	}

	/**
	 * @throws NullPointerException if the cluster does not expose a module with ID moduleId
	 */
	public void setReference(Layer layer, String moduleId, Class<?>... interfaces) {

		if (injectedProxyTypesByModuleId.containsKey(moduleId)) {
			resetReference(layer, moduleId, interfaces);
		}
		else {
			Set<Class<?>> injectedProxyTypes = injectProxies(moduleId, Arrays.asList(interfaces), layer);
			injectedProxyTypesByModuleId.put(moduleId, injectedProxyTypes);
		}
	}

	/**
	 * @param layer
	 * @param moduleId
	 * @param interfaces
	 */
	private void resetReference(Layer layer, String moduleId, Class<?>[] interfaces) {
		Set<Class<?>> currentlyInjectedInterfaces = injectedProxyTypesByModuleId.get(moduleId);

		Set<Class<?>> exposedInterfaces = new HashSet<Class<?>>(Arrays.asList(interfaces));

		Set<Class<?>> interfacesToBeRemoved = new HashSet<Class<?>>(currentlyInjectedInterfaces);
		interfacesToBeRemoved.removeAll(exposedInterfaces);
		currentlyInjectedInterfaces.removeAll(interfacesToBeRemoved);
		injectNulls(moduleId, interfacesToBeRemoved);

		Set<Class<?>> interfacesToAdd = new HashSet<Class<?>>(exposedInterfaces);
		interfacesToAdd.removeAll(currentlyInjectedInterfaces);

		Set<Class<?>> injectedProxyTypes = injectProxies(moduleId, interfacesToAdd, layer);
		currentlyInjectedInterfaces.addAll(injectedProxyTypes);

		if (currentlyInjectedInterfaces.isEmpty()) {
			injectedProxyTypesByModuleId.remove(moduleId);
		}
	}

	/**
	 * @param moduleId
	 */
	public void removeDependency(String moduleId) {
		injectNulls(moduleId, injectedProxyTypesByModuleId.get(moduleId));
		injectedProxyTypesByModuleId.remove(moduleId);
	}

	/**
	 * @param module
	 */
	public void register(Module module) {
		for (Class<?> interfaceClass : module.getInterfaces()) {
			try {
				Method method = implementation.getClass().getMethod(REGISTER_LISTENER_METHOD_NAME, interfaceClass);
				Object listenerProxy = module.getProxy(interfaceClass);
				invokeMethod(method, listenerProxy);
				saveRegisteredListenerProxy(module, interfaceClass, listenerProxy);
			}
			catch (NoSuchMethodException ignore) {
			}
		}
	}


	public void unregister(Module module) {
		Map<Class<?>, Object> registeredListeners = registeredListenersByModule.get(module);
		if (registeredListeners != null) {
			for (Class<?> interfaceClass : module.getInterfaces()) {
				try {
					Method method = implementation.getClass().getMethod(UNREGISTER_LISTENER_METHOD_NAME, interfaceClass);
					Object listenerProxy = registeredListeners.get(interfaceClass);
					if (listenerProxy != null) {
						invokeMethod(method, listenerProxy);
						registeredListeners.remove(interfaceClass);
					}
				}
				catch (NoSuchMethodException ignore) {
				}
			}
		}
	}

	private void saveRegisteredListenerProxy(Module module,
											 Class<?> interfaceClass, Object listenerProxy) {

		Map<Class<?>, Object> registeredListeners = registeredListenersByModule.get(module);
		if (registeredListeners == null) {
			registeredListeners = new HashMap<Class<?>, Object>();
			registeredListenersByModule.put(module, registeredListeners);
		}
		registeredListeners.put(interfaceClass, listenerProxy);
	}


	private HashSet<Class<?>> injectProxies(String otherComponentId, Collection<Class<?>> interfaces, Layer layer) {
		HashSet<Class<?>> injectedProxyTypes = new HashSet<Class<?>>();
		for (Method setter : getModuleSettersByPropertyKey(otherComponentId)) {
			for (Class<?> interfaceClass : interfaces) {
				if (setter.getParameterTypes()[0].isAssignableFrom(interfaceClass)) {
					Object proxy = layer.getProxy(otherComponentId, interfaceClass);
					invokeMethod(setter, proxy);
					injectedProxyTypes.add(interfaceClass);
				}
			}
		}
		return injectedProxyTypes;
	}


	private void injectNulls(String otherComponentId, Set<Class<?>> interfaces) {
		for (Method setter : getModuleSettersByPropertyKey(otherComponentId)) {
			for (Class<?> interfaceClass : interfaces) {
				if (setter.getParameterTypes()[0].isAssignableFrom(interfaceClass)) {
					invokeMethod(setter, null);
				}
			}
		}
	}


	public Object getProxy(Class<?> interfaceClass) {
		this.checkInterfaceValidity(interfaceClass);
		return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, this);
	}


	private void checkInterfaceValidity(Class<?> interfaceClass) {
		if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException("class " + interfaceClass.getName() + " is not an interface");
		}
		if (!interfaceClass.isAssignableFrom(implementation.getClass())) {
			throw new IllegalArgumentException("class " + implementation.getClass().getName() + " does not implement " + interfaceClass.getName());
		}
	}


	public Class<?>[] getInterfaces() {
		return ReflectionSupport.getInterfacesForClass(implementation.getClass()).toArray(new Class<?>[0]);
	}

	public void setProperties(Properties properties) {
		for (Object key : properties.keySet()) {
			//auto_configure_setters
			//setters are not exposed if not part of interface
			String value = properties.getProperty((String) key);
			injectPropertyIfMatchingSetterFound((String) key, value);
		}
		injectPropertyIfMatchingSetterFound(PROPERTIES_PROPERTY_KEY, properties);
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * @return properties that have actually been injected by setter
	 */
	public Properties getSetterInjectedProperties() {
		return setterInjectedProperties;
	}

	private List<Method> getModuleSettersByPropertyKey(String key) {
		String setterName = "set" + StringSupport.makeFirstCharUpperCase(key);
		return getMethodsByName(implementation.getClass(), setterName);
	}

	private static List<Method> getMethodsByName(Class clasz, String methodName) {
		List<Method> retval = new ArrayList<Method>();
		Method[] methods = clasz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (methodName.equals(method.getName()) && method.getParameterTypes().length == 1) {
				retval.add(method);
			}
		}
		return retval;
	}

	private void injectPropertyIfMatchingSetterFound(String key, Object value) {
		List<Method> setters = getModuleSettersByPropertyKey(key);
		if (setters.size() > 1) {
			throw new ConfigurationException("more than 1 (" + setters.size() +
					") setter found for property '" + key + "'");
		}
		if (setters.size() == 1) {
			injectProperty(value, setters.get(0));
			setterInjectedProperties.put(key, value);
		}
	}

	private void injectProperty(Object value, Method method) {
		Object injectingObject = Converter.convertToObject(value, method.getParameterTypes()[0]);

		invokeMethod(method, injectingObject);
	}

	private void invokeMethod(Method method, Object injectingObject) {
		try {
			method.invoke(implementation, injectingObject);
		}
		catch (InvocationTargetException ite) {
			if (ite.getCause() instanceof RuntimeException) {
				throw (RuntimeException) ite.getCause();
			}
			throw new RuntimeException("can't invoke method '" + method.getName() + "'" + " with argument " + injectingObject,
					ite.getCause());
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("can't invoke method '" + method.getName() + "'" + " with argument " + injectingObject, e);
		}
	}


	public void setInvocationInterceptor(Class<?> interfaceClass, InvocationHandler handler) {
		this.checkInterfaceValidity(interfaceClass);
		invocationHandlers.put(interfaceClass, handler);
	}


	public Object invoke(Object proxy, Method method, Object[] parameters)
			throws Throwable {
		//get handler for specific proxy interface
		InvocationHandler handler = invocationHandlers.get(proxy.getClass().getInterfaces()[0]);
		if (handler == null) {
			//get handler for interface that declares invoked method
			handler = invocationHandlers.get(method.getDeclaringClass());
		}
		if (handler != null) {
			return handler.invoke(implementation, method, parameters);
		}
		else return method.invoke(implementation, parameters);
	}


	public Set<Class<?>> getInjectedInterfaces(String moduleId) {
		Set<Class<?>> retval = new HashSet<Class<?>>();
		if (injectedProxyTypesByModuleId.containsKey(moduleId)) {
			retval.addAll(injectedProxyTypesByModuleId.get(moduleId));
		}
		return retval;
	}
}
