package helpers.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public class Introspection {
	public static boolean isGetter(Method method) {
		if (Modifier.isStatic(method.getModifiers())
				|| !Modifier.isPublic(method.getModifiers()))
			return false;
		String methodName = method.getName();
		if (methodName.length() > 3) {
			return
				methodName.startsWith("get") &&
				Character.isUpperCase(methodName.charAt(3)) &&
				method.getParameterTypes().length == 0;
		} else if (methodName.length() > 2) {
			return
				methodName.startsWith("is") &&
				Character.isUpperCase(methodName.charAt(2)) &&
				method.getParameterTypes().length == 0;
		}
		return false;
	}
	
	public static List getters(Class clazz) {
		List list = new ArrayList();
		for (int i=0;i<clazz.getMethods().length;i++) {
			Method method = clazz.getMethods()[i];
			if (isGetter(method) && !"getClass".equals(method.getName())) {
				if (!list.contains(method)) list.add(method);
			}
		}
		for (int i=0;i<clazz.getDeclaredMethods().length;i++) {
			Method method = clazz.getDeclaredMethods()[i];
			if (isGetter(method) && !"getDeclaringClass".equals(method.getName())) {
				if (!list.contains(method)) list.add(method);
			}
		}
		return list;
	}
	private static String lowerFirst(String word) {
		if (word.length() == 0) return word;
		if (word.length() == 1) return word.toLowerCase();
		return word.substring(0,1).toLowerCase() + word.substring(1, word.length());
	}
	public static String toFieldName(Method getter) {
		String getterName = getter.getName();
		if (getterName.startsWith("get"))
			return lowerFirst(getterName.substring(3));
		if (getterName.startsWith("is"))
			return lowerFirst(getterName.substring(2));
		throw new RuntimeException("Not a getter! "+getterName);
	}
	
	public static List publicFields(Class clazz) {
		List list = new ArrayList();
		for (int i=0;i<clazz.getFields().length;i++) {
			Field field = clazz.getFields()[i];
			if (!Modifier.isStatic(field.getModifiers())
					&& Modifier.isPublic(field.getModifiers())) {
				if (!list.contains(field)) list.add(field);
			}
		}
		for (int i=0;i<clazz.getDeclaredFields().length;i++) {
			Field field = clazz.getDeclaredFields()[i];
			if (!Modifier.isStatic(field.getModifiers())
					&& Modifier.isPublic(field.getModifiers())) {
				if (!list.contains(field)) list.add(field);
			}
		}
		return list;
	}
	
	public static boolean isPrimitive(Class clazz) {
		return clazz.equals(Boolean.TYPE)
		|| clazz.equals(Byte.TYPE)
		|| clazz.equals(Short.TYPE)
		|| clazz.equals(Integer.TYPE)
		|| clazz.equals(Long.TYPE)
		|| clazz.equals(Double.TYPE)
		|| clazz.equals(Float.TYPE)
		|| clazz.equals(Character.TYPE);
	}
	
	public static boolean isSimpleType(Class clazz) {
		return clazz.equals(Boolean.class)
			|| clazz.equals(Boolean.TYPE)
			|| clazz.equals(Byte.class)
			|| clazz.equals(Byte.TYPE)
			|| clazz.equals(Short.class)
			|| clazz.equals(Short.TYPE)
			|| clazz.equals(Integer.class)
			|| clazz.equals(Integer.TYPE)
			|| clazz.equals(Long.class)
			|| clazz.equals(Long.TYPE)
			|| clazz.equals(Double.class)
			|| clazz.equals(Double.TYPE)
			|| clazz.equals(Float.class)
			|| clazz.equals(Float.TYPE)
			|| clazz.equals(Character.class)
			|| clazz.equals(Character.TYPE);
	}
	
	public static boolean isCollection(Class clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}
	public static boolean isDictionary(Class clazz) {
		return Dictionary.class.isAssignableFrom(clazz);
	}
	public static boolean isMap(Class clazz) {
		return Map.class.isAssignableFrom(clazz);
	}
}
