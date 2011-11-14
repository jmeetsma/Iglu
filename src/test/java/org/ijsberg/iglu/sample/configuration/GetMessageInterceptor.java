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

package org.ijsberg.iglu.sample.configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class GetMessageInterceptor implements InvocationHandler {

	private String suffix;

	public GetMessageInterceptor(String suffix) {
		this.suffix = suffix;
	}

	public Object invoke(Object proxy, Method method, Object[] parameters)
			throws Throwable {
		if ("getMessage".equals(method.getName())) {
			return method.invoke(proxy, parameters) + suffix;
		}
		return method.invoke(proxy, parameters);
	}

}
