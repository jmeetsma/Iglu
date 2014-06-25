/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
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

package org.ijsberg.iglu.configuration.module;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.Facade;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class StandardCluster implements Cluster, Facade, InvocationHandler {

	private HashMap<String, Set<Class<?>>> exposedInterfacesByComponentId = new HashMap<String, Set<Class<?>>>();
	private Set<Component> externalComponents = new HashSet<Component>();
	private HashMap<String, Component> internalComponentsById = new HashMap<String, Component>();

	public boolean isConnected(Component component) {
		return isConnectedInternally(component) || isConnectedExternally(component);
	}

	public boolean isConnectedInternally(Component component) {
		return internalComponentsById.values().contains(component);
	}

	public boolean isConnectedExternally(Component component) {
		return externalComponents.contains(component);
	}

	public boolean isExposed(String componentId) {
		return exposedInterfacesByComponentId.containsKey(componentId);
	}

	/**
	 * Connects a component as internal component
	 *
	 * @param componentId
	 * @param component
	 * @throws ConfigurationException if the component is already registered
	 */
	public void connect(String componentId, Component component) throws ConfigurationException {
		if (isConnectedExternally(component)) {
			throw new ConfigurationException("component " + component + " is already connected as external component");
		}
		ensureIdNotRegisteredByOther(componentId);
		internalComponentsById.put(componentId, component);
		setDependenciesForNewInternalComponent(componentId, component);
		registerExternalComponentAsListener(componentId, component);
	}

	/**
	 * Connects a component as internal, exposed component.
	 *
	 * @param componentId
	 * @param component
	 * @param exposedInterfaces interfaces accessible for external components
	 * @throws ConfigurationException if the component is already registered
	 */
	public void connect(String componentId, Component component, Class<?>... exposedInterfaces) throws ConfigurationException {

		ensureComponentExposesInterfaces(component, Arrays.<Class<?>>asList(exposedInterfaces));
		connect(componentId, component);
		setExposedInterfaces(componentId, component, exposedInterfaces);

		registerExternalComponentAsListener(componentId, component);
	}

	/**
	 * @param componentId
	 * @param component
	 * @param exposedInterfaces
	 */
	private void setExposedInterfaces(String componentId, Component component,
									  Class<?>... exposedInterfaces) {
		exposedInterfacesByComponentId.put(componentId, new HashSet<Class<?>>(Arrays.asList(exposedInterfaces)));
		setInterfacesInExternalComponents(componentId, component);
	}

	/**
	 * Connects component as external, anonymous component
	 *
	 * @param externalComponent
	 * @throws ConfigurationException if the component is already registered
	 */
	public void connect(Component externalComponent) throws ConfigurationException {

		if (isConnected(externalComponent)) {
			throw new ConfigurationException("component " + externalComponent + " is already connected");
		}

		externalComponents.add(externalComponent);
		setInterfacesForNewExternalComponent(externalComponent);
		this.registerNewExternalComponent(externalComponent);
	}

	/**
	 * @param component
	 */
	public void disconnect(Component component) {
		if (isConnectedInternally(component)) {
			Set<String> componentIds = lookUpComponentIds(component);
			for (String componentId : componentIds) {
				if (isExposed(componentId)) {
					removeInterfacesForExternalComponents(componentId, component);
				}
				this.unregisterExternalListeners(componentId, component);
				exposedInterfacesByComponentId.remove(componentId);
				removeDependenciesForInternalComponent(componentId, component);
				internalComponentsById.remove(componentId);
			}
		} else if (isConnectedExternally(component)) {
			removeDependenciesForExternalComponent(component);
			externalComponents.remove(component);
		}
	}

	/**
	 * @param newExposedComponentId
	 * @param newExposedComponent
	 */
	private void setInterfacesInExternalComponents(String newExposedComponentId, Component newExposedComponent) {
		setInterfacesInExternalComponents(newExposedComponentId, getExposedInterfaces(newExposedComponentId));
	}

	/**
	 * @param exposedComponentId
	 * @param exposedInterfaces
	 */
	private void setInterfacesInExternalComponents(String exposedComponentId, Class<?>[] exposedInterfaces) {
		for (Component externalComponent : externalComponents) {
			externalComponent.setReference(this.getFacade(), exposedComponentId, exposedInterfaces);
		}
	}

	/**
	 * @param newExposedComponentId
	 * @param newExposedComponent
	 */
	private void registerExternalComponentAsListener(String newExposedComponentId, Component newExposedComponent) {
		for (Component externalComponent : externalComponents) {
			newExposedComponent.register(externalComponent);
		}
	}

	/**
	 * @param exposedComponentId
	 * @param exposedComponent
	 */
	private void removeInterfacesForExternalComponents(String exposedComponentId, Component exposedComponent) {
		for (Component externalComponent : externalComponents) {
			externalComponent.removeDependency(exposedComponentId);
		}
	}

	/**
	 * @param exposedComponentId
	 * @param exposedComponent
	 */
	private void unregisterExternalListeners(String exposedComponentId, Component exposedComponent) {
		for (Component externalComponent : externalComponents) {
			exposedComponent.unregister(externalComponent);
		}
	}

	/**
	 * @param componentId
	 * @param component
	 */
	private void setDependenciesForNewInternalComponent(String componentId, Component component) {
		for (String internalComponentId : new HashSet<String>(internalComponentsById.keySet())) {
			if (!internalComponentId.equals(componentId)) {
				Component internalComponent = internalComponentsById.get(internalComponentId);
				internalComponent.setReference(this, componentId, component.getInterfaces());
				internalComponent.register(component);
				component.setReference(this, internalComponentId, internalComponent.getInterfaces());
				component.register(internalComponent);
			}
		}
	}

	/**
	 * @param componentId
	 * @param component
	 */
	private void removeDependenciesForInternalComponent(String componentId, Component component) {
		for (String internalComponentId : internalComponentsById.keySet()) {
			if (!internalComponentId.equals(componentId)) {
				Component internalComponent = internalComponentsById.get(internalComponentId);
				internalComponent.removeDependency(componentId);
				internalComponent.unregister(component);
				component.removeDependency(internalComponentId);
				component.unregister(internalComponent);
			}
		}
	}

	/**
	 * @param externalComponent
	 */
	private void setInterfacesForNewExternalComponent(Component externalComponent) {
		for (String internalComponentId : internalComponentsById.keySet()) {
			if (isExposed(internalComponentId)) {
				externalComponent.setReference(this.getFacade(), internalComponentId, getExposedInterfaces(internalComponentId));
			}
		}
	}

	/**
	 * @param externalComponent
	 */
	private void registerNewExternalComponent(Component externalComponent) {
		for (String internalComponentId : internalComponentsById.keySet()) {
			Component internalComponent = internalComponentsById.get(internalComponentId);
			internalComponent.register(externalComponent);
		}
	}

	/**
	 * @param externalComponent
	 */
	private void removeDependenciesForExternalComponent(Component externalComponent) {
		for (String internalComponentId : internalComponentsById.keySet()) {
			if (isExposed(internalComponentId)) {
				externalComponent.removeDependency(internalComponentId);
			}
			Component internalComponent = internalComponentsById.get(internalComponentId);
			internalComponent.unregister(externalComponent);
		}
	}

	/**
	 * @param componentId
	 */
	private void ensureIdNotRegisteredByOther(String componentId) {
		if (internalComponentsById.containsKey(componentId)) {
			throw new ConfigurationException("component already registered under id '" + componentId + "'");
		}
	}

	/**
	 * @param component
	 * @param requestedInterfaces
	 */
	private void ensureComponentExposesInterfaces(Component component, List<Class<?>> requestedInterfaces) {
		List<Class<?>> componentInterfaces = Arrays.asList(component.getInterfaces());
		for (Class<?> exposedInterface : requestedInterfaces) {
			if (!componentInterfaces.contains(exposedInterface)) {
				throw new IllegalArgumentException("component '" + component + "' does not expose interface " + exposedInterface);
			}
		}
	}

	/**
	 * @return
	 */
	public Set<String> getExposedComponentIds() {
		return exposedInterfacesByComponentId.keySet();
	}

	/**
	 * @param componentId
	 * @return
	 */
	public Class<?>[] getExposedInterfaces(String componentId) {
		if (!isExposed(componentId)) {
			throw new ConfigurationException("component with id '" + componentId + "' is not exposed");
		}
		return (Class<?>[]) this.exposedInterfacesByComponentId.get(componentId).toArray(new Class<?>[0]);
	}

	/**
	 * @param componentId
	 * @param exposedInterface
	 * @return
	 */
	public Object getProxy(String componentId, Class<?> exposedInterface) {

		Component component = getInternalComponent(componentId);
		return component.createProxy(exposedInterface);
	}

	/**
	 * @param componentId
	 * @return
	 */
	private Component getInternalComponent(String componentId) {
		return internalComponentsById.get(componentId);
	}

	/**
	 * @param component
	 * @return
	 */
	private Set<String> lookUpComponentIds(Component component) {
		Set<String> retval = new HashSet<String>();
		for (String componentId : internalComponentsById.keySet()) {
			if (internalComponentsById.get(componentId) == component) {
				retval.add(componentId);
			}
		}
		return retval;
	}

	/**
	 * @param componentId
	 * @param interfaceClass
	 * @return
	 */
	private boolean isExposed(String componentId, Class<?> interfaceClass) {
		Set<Class<?>> exposedInterfaces = exposedInterfacesByComponentId.get(componentId);
		return exposedInterfaces != null && exposedInterfaces.contains(interfaceClass);
	}


	/**
	 * Invoked through proxy instance for facade.
	 *
	 * @param proxy
	 * @param method
	 * @param arguments
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Object proxy, Method method, Object[] arguments)
			throws Throwable {

		Object retval;
		if (method.getName().equals("getProxy")) {
			if (!isExposed((String) arguments[0], (Class<?>) arguments[1])) {
				throw new ConfigurationException((String) arguments[0] + " does not expose " + arguments[1]);
			}
		}
		try {
			retval = method.invoke(this, arguments);
		} catch (InvocationTargetException ite) {
			throw ite.getCause();
		}
		return retval;
	}

	/**
	 * @return
	 */
	public Facade getFacade() {
		return (Facade) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Facade.class}, this);
	}

	/**
	 * @return
	 */
	public Map<String, Component> getInternalComponents() {
		return new HashMap<String, Component>(internalComponentsById);
	}

	/**
	 * @return
	 */
	public Set<Component> getExternalComponents() {
		return new HashSet<Component>(externalComponents);
	}

	/**
	 * @param internalComponentId
	 * @param interfaces
	 */
	public void expose(String internalComponentId, Class<?>... interfaces) {
		if (!internalComponentsById.containsKey(internalComponentId)) {
			throw new ConfigurationException("component '" + internalComponentId + "' is not connected");
		}
		if (isExposed(internalComponentId)) {
			if (interfaces == null || interfaces.length == 0) {
				exposedInterfacesByComponentId.remove(internalComponentId);
				this.removeInterfacesForExternalComponents(internalComponentId, this.getInternalComponent(internalComponentId));
			}
		}
		ensureComponentExposesInterfaces(this.getInternalComponent(internalComponentId), Arrays.asList(interfaces));
		exposedInterfacesByComponentId.put(internalComponentId, new HashSet<Class<?>>(Arrays.asList(interfaces)));
		this.setInterfacesInExternalComponents(internalComponentId, interfaces);
	}


}
