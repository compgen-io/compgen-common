package io.compgen.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TallyValues<T> {
    private Map<T, Long> map = new TreeMap<T, Long>();
    private long missing = 0;
    private long total = 0;
    
    public TallyValues() {}
    public void incr(T k) {
        if (!map.containsKey(k)) {
            map.put(k,  1l);
        } else {
            map.put(k, map.get(k)+1);
        }
        total++;
    }
	
    public long getTotal() {
    	return total;
    }
    
    public long getCount(T k) {
        if (map.containsKey(k)) {
            return map.get(k);
        }
        return 0;
    }
    
    public Set<T> keySet() {
        return map.keySet();
    }
    
    public void write(OutputStream out) throws IOException {
        if (missing > 0) {
            out.write(("missing\t"+missing+"\n").getBytes());
        }
        for (T k: map.keySet()) {
            out.write((k+"\t"+getCount(k)+"\n").getBytes());
        }        
    }
    public void incrMissing() {
        missing++;
    }
}
