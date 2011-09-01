package org.ijsberg.iglu;

/**
 * This exception is thrown if the configuration as envisioned,
 * comprised of module properties and overall assembly, is not feasible. 
 * This may be due to unusable settings, missing references etc.
 */
public class ConfigurationException extends RuntimeException {

	/**
	 *
	 */
	public ConfigurationException() {
	}

	/**
	 *
	 * @param message
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 *
	 * @param cause
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
