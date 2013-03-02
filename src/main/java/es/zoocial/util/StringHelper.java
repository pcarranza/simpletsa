package es.zoocial.util;

public class StringHelper {

	public static boolean isEmpty(String str) {
		if (str == null)
			return true;
		
		return str.length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static String voidIfNull(String str) {
		if (str == null)
			return "";
		else
			return str;
	}

	public static boolean startsWith(String prefix, String str) {
		if (isEmpty(prefix)) {
			return false;
		}
		if (isEmpty(str)) {
			return false;
		}
		
		return str.startsWith(prefix);
	}

	public static String notEmpty(String str, String replaceFor) {
		if (isEmpty(str)) 
			return replaceFor;
		return str;
	}
}
