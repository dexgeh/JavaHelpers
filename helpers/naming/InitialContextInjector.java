package helpers.naming;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

public class InitialContextInjector {
	public static class ICFactoryBuilder implements InitialContextFactoryBuilder {
		private final ICFactory factory;
		public ICFactoryBuilder(Hashtable env) throws NamingException {
			this.factory = new ICFactory(env);
		}
		public InitialContextFactory createInitialContextFactory(Hashtable env)
				throws NamingException {
			return this.factory;
		}
	}
	public static class ICFactory implements InitialContextFactory {
		private final InitialContext ic;
		private final Hashtable env;
		public ICFactory(Hashtable env) throws NamingException {
			this.ic = new InitialContext();
			this.env = env;
			Enumeration en = this.env.keys();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				this.ic.addToEnvironment(key, this.env.get(key));
			}
		}
		public Context getInitialContext(Hashtable env) throws NamingException {
			Enumeration en = env.keys();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				this.ic.addToEnvironment(key, env.get(key));
			}
			return ic;
		}
	}
	public static void injectProvider(String provider) throws NamingException {
		Hashtable environment = new Hashtable();
		environment.put(Context.PROVIDER_URL, provider);
		ICFactoryBuilder builder = new ICFactoryBuilder(environment);
		DirectoryManager.setInitialContextFactoryBuilder(builder);
	}
	public static void main(String[] args) throws Exception {
		// preparo le variabili da iniettare
		Hashtable environment = new Hashtable();
		environment.put(Context.PROVIDER_URL, "iiop://10.192.191.1:9809");
		ICFactoryBuilder builder = new ICFactoryBuilder(environment);
		// inserisco il mio builder da chiamare
		DirectoryManager.setInitialContextFactoryBuilder(builder);
		// uso: stampo il valore di url/PROPERTIES_UTENTI preso dal was di integrazione
		System.out.println(new InitialContext().lookup("url/PROPERTIES_UTENTI"));
	}
}
