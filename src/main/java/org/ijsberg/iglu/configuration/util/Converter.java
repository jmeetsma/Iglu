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

package org.ijsberg.iglu.configuration.util;

import java.util.Arrays;


public abstract class Converter {

	/**
	 * @param input
	 * @return
	 */
	public static Integer convertToInteger(Object input) {
		if (input instanceof Integer) {
			return (Integer) input;
		}
		if (input instanceof Number) {
			return new Integer(((Number) input).intValue());
		}
		if (input instanceof String) {
			return new Integer((String) input);
		}
		return new Integer(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Long convertToLong(Object input) {
		if (input instanceof Long) {
			return (Long) input;
		}
		if (input instanceof Number) {
			return new Long(((Number) input).longValue());
		}
		else if (input instanceof String) {
			return new Long((String) input);
		}
		return new Long(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Short convertToShort(Object input) {
		if (input instanceof Short) {
			return (Short) input;
		}
		if (input instanceof Number) {
			return new Short(((Number) input).shortValue());
		}
		else if (input instanceof String) {
			return new Short((String) input);
		}
		return new Short(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Byte convertToByte(Object input) {
		if (input instanceof Byte) {
			return (Byte) input;
		}
		if (input instanceof Number) {
			return new Byte(((Number) input).byteValue());
		}
		else if (input instanceof String) {
			return new Byte((String) input);
		}
		return new Byte(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Float convertToFloat(Object input) {
		if (input instanceof Float) {
			return (Float) input;
		}
		if (input instanceof Number) {
			return new Float(((Number) input).floatValue());
		}
		if (input instanceof String) {
			if ((String.valueOf(Float.NEGATIVE_INFINITY)).equals(input)) {
				return new Float(Float.NEGATIVE_INFINITY);
			}
			if ((String.valueOf(Float.POSITIVE_INFINITY)).equals(input)) {
				return new Float(Float.POSITIVE_INFINITY);
			}
			return new Float((String) input);
		}
		return new Float(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Double convertToDouble(Object input) {
		if (input instanceof Double) {
			return (Double) input;
		}
		if (input instanceof Number) {
			return new Double(((Number) input).doubleValue());
		}
		if (input instanceof String) {
			if ((String.valueOf(Double.NEGATIVE_INFINITY)).equals(input)) {
				return new Double(Double.NEGATIVE_INFINITY);
			}
			if ((String.valueOf(Double.POSITIVE_INFINITY)).equals(input)) {
				return new Double(Double.POSITIVE_INFINITY);
			}
			return new Double((String) input);
		}
		return new Double(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Boolean convertToBoolean(Object input) {
		if (input instanceof Boolean) {
			return (Boolean) input;
		}
		if (input instanceof Number) {
			return Boolean.valueOf((int) ((Number) input).doubleValue() != 0);
		}
		if (input instanceof String) {
			if (((String) input).length() == 1) {
				input = new Character(((String) input).charAt(0));
			}
			else {
				return Boolean.valueOf((String) input);
			}
		}
		if (input instanceof Character) {
			switch (((Character) input).charValue()) {
				case 'N':
					return Boolean.FALSE;
				case 'n':
					return Boolean.FALSE;
				case 'f':
					return Boolean.FALSE;
				case 'F':
					return Boolean.FALSE;
				case '0':
					return Boolean.FALSE;
			}
			return Boolean.TRUE;
		}
		return Boolean.valueOf(input.toString());
	}

	/**
	 * @param input
	 * @return
	 */
	public static Character convertToCharacter(Object input) {
		if (input instanceof Character) {
			return (Character) input;
		}
		if (input instanceof Number) {
			//a bit of a long shot
			return new Character((char) (Integer.parseInt(input.toString())));
		}
		String s = input.toString();
		if (s.length() > 0) {
			return new Character(s.charAt(0));
		}
		return new Character('\0');
	}

	protected static Object convertToPrimitive(Class type, Object source) {
		if ("byte".equals(type.getName())) {
			return convertToByte(source);
		}
		if ("char".equals(type.getName())) {
			return convertToCharacter(source);
		}
		if ("double".equals(type.getName())) {
			return convertToDouble(source);
		}
		if ("float".equals(type.getName())) {
			return convertToFloat(source);
		}
		if ("int".equals(type.getName())) {
			return convertToInteger(source);
		}
		if ("long".equals(type.getName())) {
			return convertToLong(source);
		}
		if ("short".equals(type.getName())) {
			return convertToShort(source);
		}
		if ("boolean".equals(type.getName())) {
			return convertToBoolean(source);
		}
		throw new IllegalArgumentException("can not convert '" + source + "' (" + type.getName() + ") to primitive");
	}


	/**
	 * @param source
	 * @param type   desired type
	 * @return converted source object
	 * @throws IllegalArgumentException in case the object can not be converted to the desired type
	 */
	public static Object convertToObject(Object source, Class type) {
		if (source == null) {
			return null;
		}
		if (type.isAssignableFrom(source.getClass())) {
			return source;
		}
		if (type.isPrimitive()) {
			return convertToPrimitive(type, source);
		}
		if (source instanceof String && (Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type))) {
			try {
				return ReflectionSupport.instantiateClass(type, new Object[]{source});
			}
			catch (InstantiationException e) {
				throw new IllegalArgumentException("can not convert '" + source + "' to type " + type + " with message: " + e.getMessage());
			}
		}
		if (type == String.class) {
			return source.toString();
		}
		throw new IllegalArgumentException("can not convert '" + source + "' to type " + type);
	}

	/**
	 * Tries to convert the objects in the array into the types specified.
	 * This is typically necessary if a method is invoked command-line by reflection.
	 *
	 * @param targetInputTypes needed input types
	 * @param args
	 * @return the converted objects
	 * @throws IllegalArgumentException in case conversion is not possible
	 */
	public static Object[] convertToMatchingTypes(Class[] targetInputTypes, Object[] args) {
		Object[] alternativeArgs = new Object[args.length];
		if (targetInputTypes.length == args.length) {
			for (int j = 0; j < args.length; j++) {
				if (args[j] == null || targetInputTypes[j] == args[j].getClass()) {
					alternativeArgs[j] = args[j];
				}
				else {
					alternativeArgs[j] = convertToObject(args[j], targetInputTypes[j]);
				}
			}
			return alternativeArgs;
		}
		throw new IllegalArgumentException("arguments " + Arrays.asList(args) +
				" can not be converted to types " + Arrays.asList(targetInputTypes));
	}
}
