package io.compgen.common.cache;

public class TieredCache<K, V> implements Cache<K,V>{
	final protected Cache<K,V> primary;
	final protected Cache<K,V> secondary;
	
	public TieredCache(Cache<K,V> primary, Cache<K,V> secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}

	@Override
	public V remove(K k) {
		return primary.remove(k);
	}

	@Override
	public V get(K k) {
		if (!primary.containsKey(k)) {
			V val = secondary.get(k);
			if (val != null) {
				primary.put(k, val);
			}
		}
		
		return primary.get(k);
	}

	@Override
	public void put(K k, V v) {
		primary.put(k, v);
		secondary.put(k, v);
	}

	@Override
	public void clear() {
		primary.clear();
	}

	@Override
	public boolean containsKey(K k) {
		if (primary.containsKey(k)) {
			return true;
		}
		
		return secondary.containsKey(k);
	}
}
