package helpers.exception;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

public class HelperException extends Exception {
	
	private static final long serialVersionUID = -2829382122507388523L;
	private static String BUNDLE_PATHNAME = "/config/messages";
	private static ResourceBundle resourceBundle = null;
	
	static {
		resourceBundle = ResourceBundle.getBundle(BUNDLE_PATHNAME,Locale.getDefault());
	}
	
	private static String fmt(String key, HashMap params) {
		String message = resourceBundle.getString(key);
		for (Iterator it = params.keySet().iterator(); it.hasNext();) {
			Object paramKey = it.next();
			Object paramValue = params.get(paramKey);
			message = message.replaceAll("\\{" + paramKey + "\\}", paramValue.toString());
		}
		return message;
	}
	
	private String key;
	public String getKey() {
		return key;
	}
	
	public HelperException(String key) {
		super(resourceBundle.getString(key));
		this.key = key;
	}
	public HelperException(String key, Throwable cause) {
		super(resourceBundle.getString(key), cause);
		this.key = key;
	}
	public HelperException(String key, HashMap params) {
		super(fmt(key, params));
		this.key = key;
	}
	public HelperException(String key, HashMap params, Throwable cause) {
		super(fmt(key, params), cause);
		this.key = key;
	}
}
