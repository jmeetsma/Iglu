package org.ijsberg.iglu.configuration.sample.shop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 */
public class ProductInquiryCounter implements InvocationHandler {
	int countedInquiries;
	public Object invoke(Object proxy, Method method, Object[] parameters)
			throws Throwable {
		if(method.getName().startsWith("findProduct")) {
			countedInquiries++;
		}
		return method.invoke(proxy, parameters);
	}
	public int getNrofInquiries() {
		return countedInquiries;
	}
}
