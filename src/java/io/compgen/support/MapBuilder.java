package io.compgen.support;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<T,U> {
	final private Map<T,U> impl;
	
	public MapBuilder() {
		this(new HashMap<T,U>());
	}
	
	public MapBuilder(Map<T,U> impl) {
		this.impl = impl;
	}
	
	public MapBuilder<T,U> put(T foo, U bar) {
		impl.put(foo,  bar);
		return this;
	}
	
	public Map<T,U> build() {
		return impl;
	}
}
