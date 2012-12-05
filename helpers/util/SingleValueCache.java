package helpers.util;

public abstract class SingleValueCache {
	protected volatile Object value = null;
	protected final long interval;
	protected volatile long timestamp = -1;
	
	public SingleValueCache(final long interval) {
		this.interval = interval;
	}
	
	protected abstract Object update() throws Exception;
	
	protected boolean expired() {
		return (value == null) || ((System.currentTimeMillis() - timestamp) > interval);
	}
	
	public Object get() throws Exception {
		if (expired()) {
			synchronized (this) {
				if (expired()) {
					timestamp = System.currentTimeMillis();
					value = update();
				}
			}
		}
		return value;
	}
}
