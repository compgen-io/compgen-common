package io.compgen.common;

import java.util.HashSet;
import java.util.Set;

public class SetBuilder<T> {
	final private Set<T> impl;
	
	public SetBuilder() {
		this(new HashSet<T>());
	}
	
	public SetBuilder(Set<T> impl) {
		this.impl = impl;
	}
	
	public SetBuilder<T> add(T foo) {
		impl.add(foo);
		return this;
	}
	
	public Set<T> build() {
		return impl;
	}

}
