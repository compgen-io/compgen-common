package io.compgen.common.cache;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LRUCache<K,V> implements Cache<K, V> {
	protected Map<K,V> map = new HashMap<K, V>();
	protected Deque<K> list = new LinkedList<K>();
	
	protected int maxSize=1000;
	protected double factor = 0.8; // when pruning, leave 80% of the cache in-tact
	
	public LRUCache(int maxSize, double factor) {
		this.maxSize = maxSize;
		this.factor = factor;
	}
	
	public LRUCache(int maxSize) {
		this(maxSize, 0.8);
	}

	public LRUCache() {
		this(1000, 0.8);
	}

	/* (non-Javadoc)
	 * @see io.compgen.common.Cache#remove(K)
	 */
	@Override
	public V remove(K k) {
		if (map.containsKey(k)) {
			list.remove(k);
			return map.remove(k);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see io.compgen.common.Cache#get(K)
	 */
	@Override
	public V get(K k) {
		if (map.containsKey(k)) {
			list.remove(k);
			list.push(k);
			return map.get(k);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see io.compgen.common.Cache#put(K, V)
	 */
	@Override
	public void put(K k, V v) {
		if (map.containsKey(k)) {
			list.remove(k);
		}
		
		map.put(k, v);
		list.push(k);
		
		prune();
	}
	
	public void prune() {
		while (list.size() > (maxSize * factor)) {
			map.remove(list.removeLast());
		}		
	}

	@Override
	public void clear() {
		map.clear();
		list.clear();		
	}

	@Override
	public boolean containsKey(K k) {
		return map.containsKey(k);
	}
}
