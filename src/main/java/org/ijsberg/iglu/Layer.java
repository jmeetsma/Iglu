package org.ijsberg.iglu;

import java.util.Set;

/**
 * A layer exposes certain interfaces of a cluster of modules.
 * Proxies for these interfaces can be obtained directly or by
 * reference injection upon connection.
 * It's also possible to browse all exposed modules and their exposed
 * interfaces.
 */
public interface Layer {

	/**
	 * Connects anonymous, untrusted, external module.
	 * The layer's exposed interfaces will be registered with the module.
	 *
	 * @param externalModule
	 */
	void connect(Module externalModule);

	/**
	 * Disconnects an internal or external module.
	 *
	 * @param module
	 */
	void disconnect(Module module);

	/**
	 * @return IDs of modules that expose interfaces
	 */
	public Set<String> getExposedModuleIds();

	/**
	 * @param moduleId
	 * @return an array of exposed interfaces of a certain module
	 */
	public Class<?>[] getExposedInterfaces(String moduleId);

	/**
	 * @param moduleId
	 * @param exposedInterface
	 * @return a proxy for a module that exposes the desired interface
	 */
	Object getProxy(String moduleId, Class<?> exposedInterface);
}
