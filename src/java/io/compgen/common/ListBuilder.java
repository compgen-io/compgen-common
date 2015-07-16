package io.compgen.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListBuilder<T> {

	final private List<T> l;
	
	public ListBuilder() {
		 l = new ArrayList<T>();
	}
	
	public ListBuilder(List<T> l) {
		 this.l = l;
	}
	
	@SafeVarargs
	public ListBuilder(T... items) {
		 l = new ArrayList<T>();
		 for (T e: items) {
			 this.l.add(e);
		 }
	}
	
	public ListBuilder<T> add(T item) {
		l.add(item);
		return this;
	}

	@SuppressWarnings("unchecked")
	public ListBuilder<T> add(T... items) {
		for (T e: items) {
			l.add(e);
		}
		return this;
	}

	public ListBuilder<T> addAll(Collection<T> items) {
		l.addAll(items);
		return this;
	}
	public List<T> list() {
		return l;
	}
	
	public static <T> List<T> build(T[] elements) {
		List<T> l = new ArrayList<T>();
		for (T el: elements) {
			l.add(el);
		}
		return l;
	}
}
