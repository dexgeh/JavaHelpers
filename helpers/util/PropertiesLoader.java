package helpers.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
	public static Properties fromFile(String fileName) throws IOException {
		InputStream is = new FileInputStream(fileName);
		Properties properties = new Properties();
		properties.load(is);
		is.close();
		return properties;
	}
	public static Properties fromResource(String resourceName, ClassLoader cl) throws IOException {
		InputStream is = cl.getResourceAsStream(resourceName);
		Properties properties = new Properties();
		properties.load(is);
		is.close();
		return properties;
	}
}
