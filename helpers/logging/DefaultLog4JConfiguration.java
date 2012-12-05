package helpers.logging;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import helpers.util.PropertiesLoader;


public class DefaultLog4JConfiguration {
	
	public static void configureDefaultConsole() {
		try {
			PropertyConfigurator.configure(
					PropertiesLoader.fromResource(
							"/helpers/logging/console_log4j.properties",
							DefaultLog4JConfiguration.class.getClassLoader()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
