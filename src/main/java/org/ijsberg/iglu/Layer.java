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
