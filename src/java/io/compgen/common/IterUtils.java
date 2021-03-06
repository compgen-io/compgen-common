package io.compgen.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IterUtils {
	public interface EachPair<T, U> {
		public void each(T foo, U bar);
	}

	public interface EachList<T> {
	    public void each(List<T> foo);
	}

	public interface FilterFunc<T> {
		public boolean filter(T val);
	}

	public interface MapFunc<T, V> {
		public V map(T obj);
	}

	public static <T> Iterable<T> filter(Iterable<T> l, FilterFunc<T> filter) {
		List<T> ret = new ArrayList<T>();
		for (T val: l) {
			if (filter.filter(val)) {
				ret.add(val);
			}
		}
		return ret;
	}

	public static <T, V> Iterable<V> map(Iterable<T> l, MapFunc<T,V> func) {
		List<V> ret = new ArrayList<V>();
		for (T val: l) {
			V retval = func.map(val);
			if (retval != null) {
				ret.add(retval);
			}
		}
		return ret;
	}

    public static <T> Iterable<T> wrap(final Iterator<T> it) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return it;
            }};
        
    }
    
    public static <T,U> void zip(Iterable<T> foo, Iterable<U> bar, EachPair<T,U> handler) {
        zip(foo, bar, handler, false);
    }

    public static <T,U> void zip(Iterable<T> foo, Iterable<U> bar, EachPair<T,U> handler, boolean flush) {
		Iterator<T> it1 = foo.iterator();
		Iterator<U> it2 = bar.iterator();
		
		while (it1.hasNext() && it2.hasNext()) {
            T one = it1.next();
            U two = it2.next();
            handler.each(one,  two);
		}
		
		if (flush) {
            while (it1.hasNext()) {
                T one = it1.next();
                U two = null;
                handler.each(one,  two);
            }
            while (it2.hasNext()) {
                T one = null;
                U two = it2.next();
                handler.each(one,  two);
            }
		} else {
		    // clear out the longer of the iterators...
            while (it1.hasNext()) {
                it1.next();
            }
            while (it2.hasNext()) {
                it2.next();
            }
		}
	}

    public static <T> void zipArray(Iterable<T>[] foo, EachList<T> handler) {
        @SuppressWarnings("unchecked")
        Iterator<T>[] its = new Iterator[foo.length];
        
        for (int i=0; i<foo.length; i++) {
            its[i] = foo[i].iterator();
        }

        boolean hasNext = true;
        for (int i=0; i<foo.length; i++) {
            if (!its[i].hasNext()) {
                hasNext = false;
            }
        }
        
        while (hasNext) {
            List<T> out = new ArrayList<T>();
            
            for (int i=0; i<foo.length; i++) {
                out.add(its[i].next());
            }

            handler.each(out);
            
            hasNext = true;
            for (int i=0; i<foo.length; i++) {
                if (!its[i].hasNext()) {
                    hasNext = false;
                }
            }
        }

        // clear out iterators
        hasNext = true;
        while (hasNext) {
            hasNext = false;
            for (int i=0; i<foo.length; i++) {
                if (its[i].hasNext()) {
                    its[i].next();
                    hasNext = true;
                }
            }
        }
    }
    
    public static Iterator<Integer> range(final int start, final int end) {
		return new Iterator<Integer>() {
			int pos = start;
			@Override
			public boolean hasNext() {
				return pos < end;
			}

			@Override
			public Integer next() {
				return pos++;
			}};
    	
    }
    
    public static int[] intListToArray(List<Integer> l) {
    	int[] out = new int[l.size()];
    	for (int i=0; i<l.size(); i++) {
    		Integer val = l.get(i); 
    		if (val == null) {
    			throw new NullPointerException();
    		}
    		out[i] = val;
    	}
    	return out;
    }
    
    public static long[] longListToArray(List<Long> l) {
    	long[] out = new long[l.size()];
    	for (int i=0; i<l.size(); i++) {
    		Long val = l.get(i); 
    		if (val == null) {
    			throw new NullPointerException();
    		}
    		out[i] = val;
    	}
    	return out;
    }
}
