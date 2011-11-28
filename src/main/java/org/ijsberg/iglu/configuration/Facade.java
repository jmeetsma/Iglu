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

import java.util.Set;

/**
 * A facade exposes certain interfaces of a cluster of components.
 * Proxies for these interfaces can be obtained directly or by
 * reference injection upon connection.
 * It's also possible to browse all exposed components and their exposed
 * interfaces.
 */
public interface Facade {

	/**
	 * Connects anonymous, untrusted, external component.
	 * The facade's exposed interfaces will be registered with the component.
	 *
	 * @param externalComponent
	 */
	void connect(Component externalComponent);

	/**
	 * Disconnects an internal or external component.
	 *
	 * @param component
	 */
	void disconnect(Component component);

	/**
	 * @return IDs of components that expose interfaces
	 */
	public Set<String> getExposedComponentIds();

	/**
	 * @param componentId
	 * @return an array of exposed interfaces of a certain component
	 */
	public Class<?>[] getExposedInterfaces(String componentId);

	/**
	 * @param componentId
	 * @param exposedInterface
	 * @return a proxy for a component that exposes the desired interface
	 */
	Object getProxy(String componentId, Class<?> exposedInterface);
}
