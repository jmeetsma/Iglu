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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Set;

/**
 * Components are elementary building blocks of an application's structure.
 * An object that represents a structural part (component) of an application may be embedded in a component.
 * A component facilitates setting of properties as well as references to other components.
 */
//TODO preserve type
public interface Component {

	/**
	 * Injects properties into the embedded object.
	 *
	 * @param properties
	 */
	void setProperties(Properties properties);

	/**
	 * @return previously set properties
	 */
	Properties getProperties();

	/**
	 * @return classes of interfaces implemented by the wrapped object
	 */
	Class<?>[] getInterfaces();

	/**
	 * @param interfaceClass
	 * @return a proxy for the wrapped object implementing the given interface
	 */
	<T> T getProxy(Class<T> interfaceClass);
	//Object getProxy(Class<?> interfaceClass);

	/**
	 * Sets a reference to a component if the embedded class contains an appropriate setter.
	 * (Also known as dependency injection.)
	 * The component may actually have references for every exposed interface.
	 * This method must also be used to update references in case the exposure of interfaces changes.
	 *
	 * @param facade	facade that must expose a component with id componentId and given interfaces
	 * @param componentId ID of the component
	 * @param interfaces the interfaces the component exposes
	 */
	void setReference(Facade facade, String componentId, Class<?>... interfaces);

	/**
	 * Removes previously injected proxies for a certain component.
	 *
	 * @param componentId
	 */
	void removeDependency(String componentId);

	/**
	 * Registers a component (as listener) in case the embedded object implements
	 * a method named 'register' with a suiting interface.
	 *
	 * @param component
	 */
	void register(Component component);

	/**
	 * Unregisters a previously registered component in case the embedded object implements
	 * a method named 'unregister' with a suitable interface.
	 *
	 * @param component
	 */
	void unregister(Component component);

	/**
	 * @param componentId
	 * @return a set of classes of interfaces that have been injected by setter
	 */
	Set<Class<?>> getInjectedInterfaces(String componentId);

	/**
	 * Sets intercepter for invocations of a particular interface to deal
	 * with cross-cutting concerns.
	 *
	 * @param interfaceClass interface of which invocations must be intercepted
	 * @param interceptor
	 */
	void setInvocationIntercepter(Class<?> interfaceClass, InvocationHandler interceptor);

	/**
	 *
	 * @param methodName name of a method declared by a component's interface
	 * @param parameters
	 * @return
	 * @throws InvocationTargetException in case the invoked method throws
	 * @throws NoSuchMethodException in case no suitable method is found
	 * @throws IllegalArgumentException in case the arguments can not be converted
	 */
	Object invoke(String methodName, Object... parameters) throws InvocationTargetException, NoSuchMethodException, IllegalArgumentException;
}