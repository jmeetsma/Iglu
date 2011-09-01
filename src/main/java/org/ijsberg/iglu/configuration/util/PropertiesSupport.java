package org.ijsberg.iglu.configuration.util;

import java.util.*;


public class PropertiesSupport {

	public static char KEY_SEPARATOR = '.';


	/**
	 * @param properties
	 * @param sectionKey
	 * @return
	 */
	public static Properties getSubsection(Properties properties, String sectionKey) {
		Properties retval = new Properties();

		for (Object keyObj : properties.keySet()) {

			String key = (String) keyObj;
			if (key.startsWith(sectionKey + KEY_SEPARATOR)) {
				String subkey = key.substring(sectionKey.length() + 1);
				retval.setProperty(subkey, properties.getProperty(key));
			}
		}
		return retval;
	}

	/**
	 * Property trees consists of properties at different levels, names and subnames are separated by dots (.).
	 * If property keys contain dots they are assumed to be composed keys, consisting of subsection names and
	 * a property name.
	 * <p/>
	 * If a property key is composed, such as "settings.username", this method assumes there's a subsection
	 * "settings" containing a property "user name"
	 *
	 * @return a list of keys of subsections (of type String) defined by the first part of a composed property key
	 */
	public static Set<String> getSubsectionKeys(Properties properties) {
		Set<String> retval = new HashSet<String>();
		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;
			if (key.indexOf(KEY_SEPARATOR) != -1) {
				retval.add(key.substring(0, key.indexOf(KEY_SEPARATOR)));
			}
		}
		return retval;
	}

	/**
	 * @param properties
	 * @return
	 */
	public static Map<String, Properties> getSubsections(Properties properties) {
		Map<String, Properties> retval = new HashMap<String, Properties>();
		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;
			if (key.indexOf(KEY_SEPARATOR) != -1) {
				String subsectionkey = key.substring(0, key.indexOf(KEY_SEPARATOR));
				String subkey = key.substring(subsectionkey.length() + 1);
				Properties props = retval.get(subsectionkey);
				if (props == null) {
					props = new Properties();
					retval.put(subsectionkey, props);
				}
				props.setProperty(subkey, properties.getProperty(key));
			}
		}
		return retval;
	}

	/**
	 * @param properties
	 * @param sectionkey
	 * @return
	 */
	public static Map<String, Properties> getSubsections(Properties properties, String sectionkey) {
		return getSubsections(getSubsection(properties, sectionkey));
	}
}
