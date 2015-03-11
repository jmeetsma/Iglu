package org.ijsberg.iglu.util.reflection;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class IgluInstantiationException extends InstantiationException {

	private Throwable cause;

	public IgluInstantiationException() {
	}

	public IgluInstantiationException(String message) {
		super(message);
	}

	public IgluInstantiationException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	//TODO make static, move to helper
	public StackTraceElement[] getStackTrace() {
		if(cause == null) {
			return super.getStackTrace();
		}
		List<StackTraceElement> stackTraceElements = Arrays.asList(super.getStackTrace());
		stackTraceElements.addAll(Arrays.asList(cause.getStackTrace()));
		return stackTraceElements.toArray(new StackTraceElement[]{});
	}

	public void printStackTrace() {
		printStackTrace(System.out);
	}

	public void printStackTrace(PrintStream out) {
		super.printStackTrace(out);
		if(cause != null) {
			out.print("cause: ");
			cause.printStackTrace(out);
		}
	}
}
