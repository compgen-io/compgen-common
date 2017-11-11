package io.compgen.common.cache;

public interface Cache<K, V> {
	public V remove(K k);
	public 	V get(K k);
	public void put(K k, V v);
	public void clear();
	public boolean containsKey(K k);

}