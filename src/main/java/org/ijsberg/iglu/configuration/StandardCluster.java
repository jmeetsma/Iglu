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

package org.ijsberg.iglu.configuration;

import org.ijsberg.iglu.Cluster;
import org.ijsberg.iglu.ConfigurationException;
import org.ijsberg.iglu.Layer;
import org.ijsberg.iglu.Module;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class StandardCluster implements Cluster, Layer, InvocationHandler {

	private HashMap<String, Set<Class<?>>> exposedInterfacesByModuleId = new HashMap<String, Set<Class<?>>>();
	private Set<Module> externalModules = new HashSet<Module>();
	private HashMap<String, Module> internalModulesById = new HashMap<String, Module>();


	//TODO constructor that takes name as argument
	//to embed cluster as module within cluster (Layer exposed, Cluster hidden)

	public boolean isConnected(Module module) {
		return isConnectedInternally(module) || isConnectedExternally(module);
	}

	public boolean isConnectedInternally(Module module) {
		return internalModulesById.values().contains(module);
	}

	public boolean isConnectedExternally(Module module) {
		return externalModules.contains(module);
	}

	public boolean isExposed(String moduleId) {
		return exposedInterfacesByModuleId.containsKey(moduleId);
	}

	//TODO connect method that takes Object as argument and returns StandardModule

	/**
	 * Connects a module as internal module
	 *
	 * @param moduleId
	 * @param module
	 * @throws ConfigurationException if the module is already registered
	 */
	public void connect(String moduleId, Module module) throws ConfigurationException {
		if (isConnectedExternally(module)) {
			throw new ConfigurationException("module " + module + " is already connected as external module");
		}
		ensureIdNotRegisteredByOther(moduleId);
		internalModulesById.put(moduleId, module);
		setDependenciesForNewInternalModule(moduleId, module);
		registerExternalModuleAsListener(moduleId, module);
	}

	/**
	 * Connects a module as internal, exposed module.
	 *
	 * @param moduleId
	 * @param module
	 * @param exposedInterfaces interfaces accessible for external modules
	 * @throws ConfigurationException if the module is already registered
	 */
	public void connect(String moduleId, Module module, Class<?>... exposedInterfaces) throws ConfigurationException {

		ensureModuleExposesInterfaces(module, Arrays.<Class<?>>asList(exposedInterfaces));
		connect(moduleId, module);
		setExposedInterfaces(moduleId, module, exposedInterfaces);
		registerExternalModuleAsListener(moduleId, module);
	}

	/**
	 * @param moduleId
	 * @param module
	 * @param exposedInterfaces
	 */
	private void setExposedInterfaces(String moduleId, Module module,
									  Class<?>... exposedInterfaces) {
		exposedInterfacesByModuleId.put(moduleId, new HashSet<Class<?>>(Arrays.asList(exposedInterfaces)));
		setInterfacesInExternalModules(moduleId, module);
	}

	/**
	 * Connects module as external, anonymous module
	 *
	 * @param externalModule
	 * @throws ConfigurationException if the module is already registered
	 */
	public void connect(Module externalModule) throws ConfigurationException {

		if (isConnected(externalModule)) {
			throw new ConfigurationException("module " + externalModule + " is already connected");
		}

		externalModules.add(externalModule);
		setInterfacesForNewExternalModule(externalModule);
		this.registerNewExternalModule(externalModule);
	}

	/**
	 * @param module
	 */
	public void disconnect(Module module) {
		if (isConnectedInternally(module)) {
			Set<String> moduleIds = lookUpModuleIds(module);
			for (String moduleId : moduleIds) {
				if (isExposed(moduleId)) {
					removeInterfacesForExternalModules(moduleId, module);
				}
				this.unregisterExternalListeners(moduleId, module);
				exposedInterfacesByModuleId.remove(moduleId);
				removeDependenciesForInternalModule(moduleId, module);
				internalModulesById.remove(moduleId);
			}
		}
		else if (isConnectedExternally(module)) {
			removeDependenciesForExternalModule(module);
			externalModules.remove(module);
		}
	}

	/**
	 * @param newExposedModuleId
	 * @param newExposedModule
	 */
	private void setInterfacesInExternalModules(String newExposedModuleId, Module newExposedModule) {
		setInterfacesInExternalModules(newExposedModuleId, getExposedInterfaces(newExposedModuleId));
	}

	/**
	 * @param exposedModuleId
	 * @param exposedInterfaces
	 */
	private void setInterfacesInExternalModules(String exposedModuleId, Class<?>[] exposedInterfaces) {
		for (Module externalModule : externalModules) {
			externalModule.setReference(this.asLayer(), exposedModuleId, exposedInterfaces);
		}
	}

	/**
	 * @param newExposedModuleId
	 * @param newExposedModule
	 */
	private void registerExternalModuleAsListener(String newExposedModuleId, Module newExposedModule) {
		for (Module externalModule : externalModules) {
			newExposedModule.register(externalModule);
		}
	}

	/**
	 * @param exposedModuleId
	 * @param exposedModule
	 */
	private void removeInterfacesForExternalModules(String exposedModuleId, Module exposedModule) {
		for (Module externalModule : externalModules) {
			externalModule.removeDependency(exposedModuleId);
		}
	}

	/**
	 * @param exposedModuleId
	 * @param exposedModule
	 */
	private void unregisterExternalListeners(String exposedModuleId, Module exposedModule) {
		for (Module externalModule : externalModules) {
			exposedModule.unregister(externalModule);
		}
	}

	/**
	 * @param moduleId
	 * @param module
	 */
	private void setDependenciesForNewInternalModule(String moduleId, Module module) {
		for (String internalModuleId : internalModulesById.keySet()) {
			if (!internalModuleId.equals(moduleId)) {
				Module internalModule = internalModulesById.get(internalModuleId);
				internalModule.setReference(this, moduleId, module.getInterfaces());
				internalModule.register(module);
				module.setReference(this, internalModuleId, internalModule.getInterfaces());
				module.register(internalModule);
			}
		}
	}

	/**
	 * @param moduleId
	 * @param module
	 */
	private void removeDependenciesForInternalModule(String moduleId, Module module) {
		for (String internalModuleId : internalModulesById.keySet()) {
			if (!internalModuleId.equals(moduleId)) {
				Module internalModule = internalModulesById.get(internalModuleId);
				internalModule.removeDependency(moduleId);
				internalModule.unregister(module);
				module.removeDependency(internalModuleId);
				module.unregister(internalModule);
			}
		}
	}

	/**
	 * @param externalModule
	 */
	private void setInterfacesForNewExternalModule(Module externalModule) {
		for (String internalModuleId : internalModulesById.keySet()) {
			if (isExposed(internalModuleId)) {
				externalModule.setReference(this.asLayer(), internalModuleId, getExposedInterfaces(internalModuleId));
			}
		}
	}

	/**
	 * @param externalModule
	 */
	private void registerNewExternalModule(Module externalModule) {
		for (String internalModuleId : internalModulesById.keySet()) {
			Module internalModule = internalModulesById.get(internalModuleId);
			internalModule.register(externalModule);
		}
	}

	/**
	 * @param externalModule
	 */
	private void removeDependenciesForExternalModule(Module externalModule) {
		for (String internalModuleId : internalModulesById.keySet()) {
			if (isExposed(internalModuleId)) {
				externalModule.removeDependency(internalModuleId);
			}
			Module internalModule = internalModulesById.get(internalModuleId);
			internalModule.unregister(externalModule);
		}
	}

	/**
	 * @param moduleId
	 */
	private void ensureIdNotRegisteredByOther(String moduleId) {
		if (internalModulesById.containsKey(moduleId)) {
			throw new ConfigurationException("module already registered under id '" + moduleId + "'");
		}
	}

	/**
	 * @param module
	 * @param requestedInterfaces
	 */
	private void ensureModuleExposesInterfaces(Module module, List<Class<?>> requestedInterfaces) {
		List<Class<?>> moduleInterfaces = (List) Arrays.asList(module.getInterfaces());
		for (Class<?> exposedInterface : requestedInterfaces) {
			if (!moduleInterfaces.contains(exposedInterface)) {
				throw new IllegalArgumentException("module '" + module + "' does not expose interface " + exposedInterface);
			}
		}
	}

	/**
	 * @return
	 */
	public Set<String> getExposedModuleIds() {
		return exposedInterfacesByModuleId.keySet();
	}

	/**
	 * @param moduleId
	 * @return
	 */
	public Class<?>[] getExposedInterfaces(String moduleId) {
		if (!isExposed(moduleId)) {
			throw new ConfigurationException("module with id '" + moduleId + "' is not exposed");
		}
		return (Class<?>[]) this.exposedInterfacesByModuleId.get(moduleId).toArray(new Class<?>[0]);
	}

	/**
	 * @param moduleId
	 * @param exposedInterface
	 * @return
	 */
	public Object getProxy(String moduleId, Class<?> exposedInterface) {

		Module module = getInternalModule(moduleId);
		return module.getProxy(exposedInterface);
	}

	/**
	 * @param moduleId
	 * @return
	 */
	private Module getInternalModule(String moduleId) {
		return internalModulesById.get(moduleId);
	}

	/**
	 * @param module
	 * @return
	 */
	private Set<String> lookUpModuleIds(Module module) {
		Set<String> retval = new HashSet<String>();
		for (String moduleId : internalModulesById.keySet()) {
			if (internalModulesById.get(moduleId) == module) {
				retval.add(moduleId);
			}
		}
		return retval;
	}

	/**
	 * @param moduleId
	 * @param interfaceClass
	 * @return
	 */
	private boolean isExposed(String moduleId, Class<?> interfaceClass) {
		Set<Class<?>> exposedInterfaces = exposedInterfacesByModuleId.get(moduleId);
		return exposedInterfaces != null && exposedInterfaces.contains(interfaceClass);
	}


	/**
	 * Invoked through proxy instance for layer.
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
		}
		catch (InvocationTargetException ite) {
			throw ite.getCause();
		}
		return retval;
	}

	/**
	 * @return
	 */
	public Layer asLayer() {
		return (Layer) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Layer.class}, this);
	}

	/**
	 * @return
	 */
	public Map<String, Module> getInternalModules() {
		return new HashMap<String, Module>(internalModulesById);
	}

	/**
	 * @return
	 */
	public Set<Module> getExternalModules() {
		return new HashSet<Module>(externalModules);
	}

	/**
	 * @param internalModuleId
	 * @param interfaces
	 */
	public void expose(String internalModuleId, Class<?>... interfaces) {
		if (!internalModulesById.containsKey(internalModuleId)) {
			throw new ConfigurationException("module '" + internalModuleId + "' is not connected");
		}
		if (isExposed(internalModuleId)) {
			if (interfaces == null || interfaces.length == 0) {
				exposedInterfacesByModuleId.remove(internalModuleId);
				this.removeInterfacesForExternalModules(internalModuleId, this.getInternalModule(internalModuleId));
			}
		}
		ensureModuleExposesInterfaces(this.getInternalModule(internalModuleId), (List) Arrays.asList(interfaces));
		exposedInterfacesByModuleId.put(internalModuleId, new HashSet<Class<?>>(Arrays.asList(interfaces)));
		this.setInterfacesInExternalModules(internalModuleId, interfaces);
	}


}
