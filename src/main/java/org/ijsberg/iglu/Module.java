package org.ijsberg.iglu;

import java.lang.reflect.InvocationHandler;
import java.util.Properties;
import java.util.Set;

/**
 * Modules are elementary building blocks of an application's structure.
 * An object that represents a structural part (component) of an application may be embedded in a module.
 * A module facilitates setting of properties as well as references to other components.
 *
 */
public interface Module {

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
	Object getProxy(Class<?> interfaceClass);

	/**
	 * Sets a reference to a module if the embedded class contains an appropriate setter.
	 * (Also known as dependency injection.)
	 * The module may actually have references for every exposed interface.
	 * This method must also be used to update references in case the exposure of interfaces changes.
	 *
	 * @param layer layer that must expose a module with id moduleId and given interfaces
	 * @param moduleId ID of module
	 * @param interfaces the interfaces the module exposes
	 */
	void setReference(Layer layer, String moduleId, Class<?>... interfaces);

	/**
	 * Removes previously injected proxies for a certain module.
	 *
	 * @param moduleId
	 */
	void removeDependency(String moduleId);

	/**
	 * Registers a module (as listener) in case the embedded object implements
	 * a method named 'register' with a suiting interface.
	 *
	 * @param module
	 */
	void register(Module module);

	/**
	 * Unegisters a previously registered module in case the embedded object implements
	 * a method named 'unregister' with a suitable interface.
	 *
	 * @param module
	 */
	void unregister(Module module);

	/**
	 *
	 * @param moduleId
	 * @return a set of classes of interfaces that have been injected by setter
	 */
	Set<Class<?>> getInjectedInterfaces(String moduleId);

	/**
	 * Sets interceptor for invocations of a particular interface to deal
	 * with crosscutting concerns.
	 *
	 * @param interfaceClass interface of which invocations must be intercepted
	 * @param interceptor
	 */
	void setInvocationInterceptor(Class<?> interfaceClass, InvocationHandler interceptor);
}