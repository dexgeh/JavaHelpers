package helpers.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class ContextInspector {
	private static String tabs(int n) {
		StringBuffer sb = new StringBuffer(n);
		for (int i=0;i<n;i++) sb.append('\t');
		return sb.toString();
	}
	public static void printList(String jndiBase, int indent, int maxrecursion) throws NamingException {
		if (indent > maxrecursion) return;
		System.err.println("Entering context: "+jndiBase);
		NamingEnumeration en = new InitialContext().list(jndiBase);
		while (en.hasMoreElements()) {
			NameClassPair pair = (NameClassPair) en.nextElement();
			String jndiName = jndiBase + "/" + pair.getName();
			if (pair.getClassName().equals(Context.class.getName())) {
				printList(jndiName, indent + 1, maxrecursion);
				continue;
			}
			if (pair.getClassName().equals("java.lang.String")) {
				System.out.println(tabs(indent)+jndiName+": \"" + new InitialContext().lookup(jndiName) + "\"");
			} else if (pair.getClassName().equals("java.net.URL")) {
				System.out.println(tabs(indent)+jndiName+": \"" + new InitialContext().lookup(jndiName) + "\"");
			} else {
				System.out.println(tabs(indent)+jndiName+": "+pair.getClassName());
			}
		}
	}
}
