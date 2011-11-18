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

import java.util.Map;
import java.util.Set;

/**
 * A cluster represents a group of interconnected components.
 * A cluster contains internal components that are considered to be
 * part of a layer or subsystem. Internal components may access any interface
 * of other internal components in the cluster. Proxies for component interfaces
 * are exchanged and injected upon connection.
 * <p/>
 * A cluster may be accessed as facade to which external components can connect.
 * Internal components may expose certain interfaces through the facade boundary to external components.
 * Proxies for these interfaces will be injected upon connection of an external component.
 * An external component is considered as an anonymous, untrusted consumer of the cluster's services.
 * Proxies for external component interfaces are not injected in internal components.
 *
 * @author jmeetsma
 */
public interface Cluster extends Connector {

	/**
	 * @return a map of connected internal components, keyed by component ID
	 */
	Map<String, Component> getInternalComponents();

	/**
	 * @return a set of connected external components
	 */
	Set<Component> getExternalComponents();

	/**
	 * Connects internal component and exposes interfaces.
	 *
	 * @param componentId
	 * @param component
	 * @param exposedInterfaces
	 */
	void connect(String componentId, Component component, Class<?>... exposedInterfaces);

	/**
	 * Updates exposure of interfaces of an internal component.
	 *
	 * @param internalComponentId
	 * @param interfaces
	 */
	void expose(String internalComponentId, Class<?>... interfaces);

	/**
	 * @return a facade that represents the cluster
	 */
	Facade getFacade();
}
