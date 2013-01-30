package es.zoocial.util;

public class ArgsHelper {
	
	public static void notEmpty(String name, String argument) {
		if (StringHelper.isEmpty(argument))
			throw new IllegalArgumentException(String.format("%s should not be empty", name));
	}
	
	public static void notNull(String name, Object argument) {
		if (argument == null)
			throw new IllegalArgumentException(String.format("%s should not be null", name));
	}

}
