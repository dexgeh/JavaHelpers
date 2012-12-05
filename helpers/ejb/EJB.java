package helpers.ejb;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class EJB {
	private static final Object[] EMPTY_OBJECTS = new Object[0];
	private static final Class[] EMPTY_CLASSES = new Class[0];
	private static final String create = "create";
	public static Object getRemote(String jndi, Class homeClass) throws Exception {
		System.out.println(jndi);
		Object home = new InitialContext().lookup(jndi);
		System.out.println(home);
		System.out.println(home.getClass());
		try {
        	home = PortableRemoteObject.narrow(home, homeClass);
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return home
			.getClass()
			.getMethod(create, EMPTY_CLASSES)
			.invoke(home, EMPTY_OBJECTS);
	}
	public static String toCellPersistentJndi(String jndi) {
		return "cell/persistent/" + jndi.replace('/', '.');
	}
	public static String toJndi(Class homeClass) {
		String qualifiedName = homeClass.getName();
		String jndi = "ejb/" + qualifiedName.replace('.', '/');
		return jndi;
	}
	public static Object getRemote(String jndiPrefix, boolean toCellPersistent, Class homeClass) throws Exception {
		String jndi = toJndi(homeClass);
		if (toCellPersistent)
			jndi = toCellPersistentJndi(jndi);
		jndi = jndiPrefix + jndi;
		return getRemote(jndi, homeClass);
	}
}
