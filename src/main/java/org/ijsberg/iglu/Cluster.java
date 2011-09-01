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

package org.ijsberg.iglu;

import java.util.Map;
import java.util.Set;

/**
 * A cluster represents a group of interconnected modules.
 * A cluster contains internal modules that are considered to be
 * part of a layer or subsystem. Internal modules may access any interface
 * of other internal modules in the cluster. Proxies for module interfaces
 * are exchanged and injected upon connection.
 * <p/>
 * A cluster may be accessed as layer to which external modules can connect.
 * Internal modules may expose certain interfaces through the layer boundary to external modules.
 * Proxies for these interfaces will be injected upon connection of an external module.
 * An external module is considered as an anonymous, untrusted consumer of the cluster's services.
 * Proxies for external module interfaces are not injected in internal modules.
 *
 * @author jmeetsma
 */
public interface Cluster {

	/**
	 * @return a map of connected internal modules, keyed by modulke ID
	 */
	Map<String, Module> getInternalModules();

	/**
	 * @return a set of connected external modules
	 */
	Set<Module> getExternalModules();

	/**
	 * Connects internal module.
	 *
	 * @param moduleId
	 * @param module
	 */
	void connect(String moduleId, Module module);

	/**
	 * Connects internal module and exposes interfaces.
	 *
	 * @param moduleId
	 * @param module
	 * @param exposedInterfaces
	 */
	void connect(String moduleId, Module module, Class<?>... exposedInterfaces);

	/**
	 * Disconnects internal or external module.
	 *
	 * @param module
	 */
	void disconnect(Module module);

	/**
	 * Updates exposure of interfaces of an internal module.
	 *
	 * @param internalModuleId
	 * @param interfaces
	 */
	void expose(String internalModuleId, Class<?>... interfaces);

	/**
	 * @return a layer that represents the cluster
	 */
	Layer asLayer();
}
