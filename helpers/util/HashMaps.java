package helpers.util;

import java.util.HashMap;


public class HashMaps {
	
	public static class KeyRef {
		private final HashMaps parentRef;
		private final Object key;
		public KeyRef(HashMaps parentRef, Object key) {
			this.parentRef = parentRef;
			this.key = key;
		}
		public HashMaps value(Object value) {
			return parentRef.put(key, value);
		}
	}
	
	private HashMaps() {}
	public static HashMaps create() {
		return new HashMaps();
	}
	private HashMap me = new HashMap();
	
	public HashMap get() {
		return me;
	}
	
	public HashMaps put(Object key, Object value) {
		me.put(key, value);
		return this;
	}
	
	public KeyRef key(Object key) {
		return new KeyRef(this, key);
	}
}
