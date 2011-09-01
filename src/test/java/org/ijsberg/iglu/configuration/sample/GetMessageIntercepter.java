package org.ijsberg.iglu.configuration.sample;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class GetMessageIntercepter implements InvocationHandler {

	private String suffix;
	
	public GetMessageIntercepter (String suffix) {
		this.suffix = suffix;
	}
	
	public Object invoke(Object proxy, Method method, Object[] parameters)
			throws Throwable {
		if("getMessage".equals(method.getName())) {
			return method.invoke(proxy, parameters) + suffix;
		}
		return method.invoke(proxy, parameters);
	}

}
