
package helpers.logging;

public class PrettyPrinter {
    public static class Conf {
        public boolean showNull = false, showErrors = false, indent = true;
        public static Conf getDefaults() {
            return new Conf();
        }
        //field for internal use
        private int currentIndent = 0;
    }
    
    private static boolean in(Object o, Object[] O) {
        for (int i=0;i<O.length;i++)
            if (o.equals(O[i]))
                return true;
        return false;
    }
    
    private static boolean isSimpleType(Object o) {
        return in(o.getClass(), new Object[] {
                String.class,
                Byte.class,
                Byte.TYPE,
                Short.class,
                Short.TYPE,
                Integer.class,
                Integer.TYPE,
                Long.class,
                Long.TYPE,
                Float.class,
                Float.TYPE,
                Double.class,
                Double.TYPE,
                Boolean.class,
                Boolean.TYPE,
                Character.class,
                Character.TYPE,
                java.util.Date.class,
                java.sql.Date.class,
                char[].class,
                byte[].class
        }) || o instanceof java.util.Calendar;
    }
    
    private static int STRING_MAX_LEN = 128;
    private static StringBuffer printString(StringBuffer out, String s) {
        if (s.length() > STRING_MAX_LEN) {
            if (s.startsWith("<?xml"))
                return out.append("<xml-string length="+s.length()+">");
            else
                return out.append("<string length="+s.length()+">");
        }
        out.append('\"');
        for (int i = 0, len = s.length(); i < len; ++i) {
            char c = s.charAt(i);
            if (c == '\"' || c == '\\') {
                out.append('\\');
            }
            out.append(c);
        }
        return out.append('\"');
    }
    
    private static StringBuffer printSimpleType(StringBuffer out, Object o) {
        if (o.getClass().equals(String.class)) {
            return printString(out, (String)o);
        } else if (o.getClass().equals(char[].class)) {
            return out.append("<char[] length="+((char[])o).length+">");
        }else if (o.getClass().equals(byte[].class)) {
            return out.append("<byte[] length="+((byte[])o).length+">");
        } else if (o instanceof java.util.Calendar){
            return out.append("<Calendar "+((java.util.Calendar)o).getTime()+">");
        } else {
            return out.append(o);
        }
    }
    
    private static StringBuffer indent(StringBuffer out, Conf conf) {
        if (out.length() > 0 && out.charAt(out.length()-1) != '\n')
            return out;
        if (conf.indent)
	        for (int i=0;i<conf.currentIndent;i++) {
	            out.append('\t');
	        }
        return out;
    }
    private static StringBuffer newline(StringBuffer out, Conf conf) {
        if (conf.indent) return out.append('\n');
        else return out;
    }
    
    private static StringBuffer printKeyValue(StringBuffer out, Object key, Object value, Conf conf) {
        if (!conf.showNull && value == null) return out;
        indent(out, conf);
        print(print(out, key, conf).append(':'), value, conf);
        if (out.charAt(out.length()-1) == '\n')
            out.delete(out.length()-1, out.length());
        out.append(',');
        return newline(out, conf);
    }
    
    private static StringBuffer printMapOrDict(StringBuffer out, java.util.Map map, java.util.Dictionary dict, Conf conf) {
        if (map!=null && map.size() == 0) return out.append("{}");
        if (dict!=null && dict.size() == 0) return out.append("{}");
        indent(out, conf);
        out.append('{');
        newline(out, conf);
        conf.currentIndent++;
        if (map != null)
	        for (java.util.Iterator it = map.keySet().iterator(); it.hasNext();) {
	            Object key = it.next();
	            Object value = map.get(key);
	            printKeyValue(out, key, value, conf);
	        }
        else
            for (java.util.Enumeration en = dict.keys(); en.hasMoreElements();) {
                Object key = en.nextElement();
                Object value = dict.get(key);
                printKeyValue(out, key, value, conf);
            }
        conf.currentIndent--;
        indent(out, conf);
        out.append('}');
        return newline(out, conf);
    }
    
    private static java.util.List printFields(StringBuffer out, Object o, java.lang.reflect.Field[] fields, java.util.List fieldNames, Conf conf) {
        for (int i=0;i<fields.length;i++) {
            if (java.lang.reflect.Modifier.isPublic(fields[i].getModifiers()) && !java.lang.reflect.Modifier.isStatic(fields[i].getModifiers())) {
                String key = fields[i].getName();
                if (fieldNames.contains(key)) continue;
                Object value;
                try {
                    value = fields[i].get(o);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException();
                }
                fieldNames.add(key);
                printKeyValue(out, key, value, conf);
            }
        }
        return fieldNames;
    }
    private static java.util.List printGetters(StringBuffer out, Object o, java.lang.reflect.Method[] methods, java.util.List methodNames, Conf conf) {
        for (int i=0;i<methods.length;i++) {
            if (java.lang.reflect.Modifier.isPublic(methods[i].getModifiers())&& !java.lang.reflect.Modifier.isStatic(methods[i].getModifiers())) {
                String name = methods[i].getName();
                if (methodNames.contains(name)) continue;
                if (methods[i].getParameterTypes().length > 0) continue;
                String key = null;
                if (name.startsWith("get") && !name.equals("getClass") && !name.equals("getDeclaredClasses")) {
                    if (name.length() > 4)
                        key = name.substring(3,4).toLowerCase() + name.substring(4);
                    else
                        key = name.substring(3,4).toLowerCase();
                } else if (name.startsWith("is")) {
                    if (name.length() > 3)
                        key = name.substring(2,3).toLowerCase() + name.substring(3);
                    else
                        key = name.substring(2,3).toLowerCase();
                }
                //key -> null if getClass
                if (key != null) {
                    methodNames.add(name);
                    Object value;
                    try {
                        value = methods[i].invoke(o, null);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException();
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        if (conf.showErrors) {
                            value = "#InvocationTargetException#";
                        } else value = null;
                    }
                    printKeyValue(out, key, value, conf);
                }
            }
        }
        return methodNames;
    }

    private static StringBuffer printPojo(StringBuffer out, Object o, Conf conf) {
        //try public non-static fields then public non-static getters
        Class c = o.getClass();
        indent(out, conf);
        out.append('#');
        out.append(c.getName());
        out.append('{');
        newline(out, conf);
        conf.currentIndent++;
        java.util.List fieldNames = printFields(out, o, c.getFields(), new java.util.LinkedList(), conf);
        printFields(out, o, c.getDeclaredFields(), fieldNames, conf);
        java.util.List methodNames = printGetters(out, o, c.getMethods(), new java.util.LinkedList(), conf);
        printGetters(out, o, c.getDeclaredMethods(), methodNames, conf);
        conf.currentIndent--;
        indent(out, conf);
        out.append('}');
        return newline(out, conf);
    }
    
    private static StringBuffer printCollectionOrArray(StringBuffer out, Object o, Conf conf) {
        if (o.getClass().isArray()) {
            if (java.lang.reflect.Array.getLength(o) == 0) return out.append("[]");
        } else {
            if (((java.util.Collection)o).size() == 0) return out.append("[]");
        }
        indent(out, conf);
        out.append('[');
        newline(out, conf);
        conf.currentIndent++;
        if (o.getClass().isArray())
            for (int i=0,len=java.lang.reflect.Array.getLength(o); i < len; i++) {
                Object value = java.lang.reflect.Array.get(o, i);
                if (!conf.showNull && value == null) continue;
                indent(out, conf);
	            print(out, value, conf);
	            if (out.charAt(out.length()-1) == '\n')
	                out.delete(out.length()-1, out.length());
	            out.append(',');
	            newline(out, conf);
            }
        else
	        for (java.util.Iterator it = ((java.util.Collection)o).iterator(); it.hasNext();) {
	            Object value = it.next();
	            if (!conf.showNull && value == null) continue;
	            indent(out, conf);
	            print(out, value, conf);
	            if (out.charAt(out.length()-1) == '\n')
	                out.delete(out.length()-1, out.length());
	            out.append(',');
	            newline(out, conf);
	        }
        conf.currentIndent--;
        indent(out, conf);
        out.append(']');
        return newline(out, conf);
    }
    
    private static StringBuffer print(StringBuffer out, Object o, Conf conf) {
        if (o == null)
            return out.append("null");
        if (isSimpleType(o))
            return printSimpleType(out, o);
        else
            try {
                java.util.Map map = (java.util.Map)o;
                return printMapOrDict(out, map, null, conf);
            } catch (ClassCastException e) {
                try {
	                java.util.Dictionary dict = (java.util.Dictionary)o;
	                return printMapOrDict(out, null, dict, conf);
                } catch (ClassCastException e2) {
                    try {
                        java.util.Collection coll = (java.util.Collection)o;
                        return printCollectionOrArray(out, coll, conf);
                    } catch (ClassCastException e3) {
                        if (o.getClass().isArray())
                            return printCollectionOrArray(out, o, conf);
                        else
                            return printPojo(out, o, conf);
                    }
                }
            }
    }
    
    public static String print(Object o) {
        try {
            return print(new StringBuffer(), o, Conf.getDefaults()).toString();
        } catch (Throwable t) {
            return PrettyPrinter.class+" "+t.getClass().getName()+":"+t.getMessage()+"\n"+o;
        }
    }
    
    public static String print(Object o, Conf conf) {
        try {
            return print(new StringBuffer(), o, conf).toString();    
        } catch (Throwable t) {
            return PrettyPrinter.class+" "+t.getClass().getName()+":"+t.getMessage()+"\n"+o;
        }
    }
}
