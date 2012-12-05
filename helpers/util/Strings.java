package helpers.util;

public class Strings {
	public static boolean eq(String s1, String s2) {
		if (s1 == null && s2 == null) return true;
		if (s1 == null) return false;
		if (s2 == null) return false;
		return s1.equals(s2);
	}
	public static boolean nullOrEmpty(String s) {
		return s == null || s.equals("");
	}
	
	public static boolean nullOrEmptyTrim(String s) {
		return s == null || s.trim().equals("");
	}
	public static String join(String[] s, char sep) {
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<s.length;i++) {
			sb.append(s);
			if (i < s.length - 1) {
				sb.append(sep);
			}
		}
		return sb.toString();
	}
	public static String reverse(String s) {
		char[] C = s.toCharArray();
		for (int i=0;i<C.length/2;i++) {
			char t = C[i];
			C[i] = C[C.length-i-1];
			C[C.length-i-1] = t;
		}
		return new String(C);
	}
	public static String getter(String attrName, boolean isBoolean) {
		if (isBoolean) {
			return "is" +
			Character.toUpperCase(attrName.charAt(0)) +
			attrName.substring(1);
		} else {
			return "get" + 
				Character.toUpperCase(attrName.charAt(0)) +
				attrName.substring(1);	
		}
	}
	public static String setter(String attrName) {
		return "set" +
			Character.toUpperCase(attrName.charAt(0)) +
			attrName.substring(1);
	}
}
