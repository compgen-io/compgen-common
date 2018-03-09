package io.compgen.common;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * This class is a sort of one-way Radix Tree that stores a counter in the node 
 *  
 * @author mbreese
 *
 */
public class RadixCounter implements Set<String>{
    
    private class Node {
        private char[] value;
        private Node[] children;
        private boolean isKey;

        private Node() {
            this(null, null, false);
        }
        private Node(String value) {
            this(value.toCharArray(), null, true);
        }
        
        private Node(char[] value, Node[] children, boolean isLeaf) {
            this.value = value;
            this.children = children;
            this.isKey = isLeaf;
        }
        
        /**
         * Add a new string to this node
         * @param key
         * @return -1 (not added), 0 (same as this node), 1 (added to this node)  
         */
        private int add(String key) {
            if (key == null) {
                throw new NullPointerException();
            }

            for (int i=0; i<key.length(); i++) {
                if (value == null || value.length <= i) {
                    int added = -1;
                    String suffix = key.substring(i);
                    if (children != null) {
                        for (int j=0; j<children.length && added < 0; j++) {
                            // can we add this suffix to a child?
                            added = children[j].add(suffix);
                        }
                    }

                    if (added < 0) {
                        // prefix not found in a child node...
                        addChildNode(suffix);
                        return 1;
                    }
                    return added;
                }
                
                if (key.charAt(i) != value[i]) {
                    if (i == 0) {
                        return -1;
                    }
                    splitNode(i, key.substring(i));
                    return 1;
                }
            }
            
            // exact match
            if (!isKey) {
                isKey = true;
                return 1;
            }
            return 0;
        }

        private void splitNode(int pos, String subkey) {
            String valueStr = new String(value);
            String myPrefix = valueStr.substring(0, pos);
            String mySuffix = valueStr.substring(pos);
            
            value = myPrefix.toCharArray();
            
            Node suffixNode = new Node(mySuffix.toCharArray(), children, isKey);
            Node newNode = new Node(subkey);
            isKey = false;
            
            if (subkey.compareTo(mySuffix) < 0) {
                children = new Node[] {newNode, suffixNode};
            } else {
                children = new Node[] {suffixNode, newNode};
            }
        }

        public String toString() {
            return "("+((value==null) ? "":new String(value))+")";
        }
        
        private void addChildNode(String key) {
            Node child = new Node(key);
            if (children == null) {
                children = new Node[] { child };
                return;
            }
            Node[] tmp = new Node[children.length+1];
            int pos = 0;
            boolean inserted = false;
            for (int i=0; i<children.length; i++) {
                if (key.compareTo(new String(children[i].value)) > 0 && !inserted) {
                    tmp[pos++] = child;
                    tmp[pos++] = children[i];
                    inserted = true;
                } else {
                    tmp[pos++] = children[i];
                }
            }
            if (pos == children.length) {
                tmp[pos] = child;
            }
            
            children = tmp;
        }
        private boolean contains(String key) {
            if (key == null) {
                return false;
            }

            for (int i=0; i<key.length(); i++) {
                if (value == null || value.length <= i) {
                    String suffix = key.substring(i);
                    if (children != null) {
                        for (int j=0; j<children.length ; j++) {
                            // can we add this suffix to a child?
                            if (children[j].contains(suffix)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                
                if (key.charAt(i) != value[i]) {
                    return false;
                }
            }
            
            return isKey;
        }

        /**
         * Remove a key from the set
         * @param key
         * @return -1 (not found), 0 (found), 1 (already removed)
         */
        private int remove(String key) {
            if (key == null) {
                return -1;
            }

            for (int i=0; i<key.length(); i++) {
                if (value == null || value.length <= i) {
                    String suffix = key.substring(i);
                    int found = -1;
                    if (children != null) {
                        for (int j=0; j<children.length && found == -1; j++) {
                            // can we add this suffix to a child?
                            found = children[j].remove(suffix); 
                            if (found == 0) {
                                if (children[j].children == null && !children[j].isKey) {
                                    // prune away orphan node
                                    removeChild(j);
                                }
                                return 1;
                            }
                        }
                    }
                    return found;
                }
                
                if (key.charAt(i) != value[i]) {
                    return -1;
                }
            }
            
            if (isKey) {
                isKey = false;
                if (children != null && children.length == 1) {
                    // absorb the single child
                    value = (new String(value) + new String(children[0].value)).toCharArray();
                    isKey = children[0].isKey;
                    children = children[0].children;
                }
                return 0;
            }
            return -1;
        }
        
        private void removeChild(int index) {
            if (children.length == 1) {
                // remove only child... nothing to merge
                children = null;
                return;
            }
            
            if (children.length == 2 && !isKey) {
                // Only two children, and we aren't a key, so absorb the remaining child's value
                if (index == 0) {
                	if (value != null) {
                		value = (new String(value) + new String(children[1].value)).toCharArray();
                	} else {
                		value = children[1].value;
                	}
                    isKey = children[1].isKey;
                    children = children[1].children;
                } else {
                	if (value != null) {
                		value = (new String(value) + new String(children[0].value)).toCharArray();
                	} else {
                		value = children[0].value;
                	}
                    isKey = children[0].isKey;
                    children = children[0].children;
                }
                return;
            }

            // Either more than two children or we are a key. Trim the old child.
            
            Node[] tmp = new Node[children.length-1];
            int pos = 0;
            for (int i=0;i<children.length; i++) {
                if (i != index) {
                    tmp[pos++] = children[i];
                }
            }
            children = tmp;
        }
        private void dump(int indent) {
            String spacer = "";
            for (int i=0; i<indent; i++) {
                spacer += "--";
            }

            System.out.println(spacer + toString() + (isKey?"*":""));
            if (children != null) {
                for (Node child: children) {
                    if (child != null) {
                        child.dump(indent+1);
                    }
                }
            }
        }
    }
    
    private Node head = new Node();
    private int size = 0;
    
    public RadixCounter() {
    }

    public int size() {
        return size;
    }
    
    @Override
    public boolean add(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (head.add(key) > 0) {
            size++;
            return true;
        }
        return false;
    }
    
    public boolean contains(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return head.contains(key);
    }

    public boolean remove(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (head.remove(key) > 0) {
            size--;
            return true;
        }
        return false;
    }

    private void dump() {
        head.dump(0);
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return contains(o.toString());
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        T[] working;
        if (a.length >= size) {
            working = a;
        } else {
            working = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        
        int pos = 0;
        for (String s:this) {
            working[pos++] = (T) s;
        }
        
        if (pos < working.length) {
            working[pos] = null;
        }

        return working;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return remove(o.toString());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object val:c) {
            if (!contains(val)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean changed = true;
        for (String v: c) {
            if (add(v)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (String v:this) {
            if (!c.contains(v)) {
                remove(v);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object v: c) {
            if (remove(v)) {
                changed = true;
            }
         }
        return changed;
    }

    @Override
    public void clear() {
        // start over, let the GC figure it out...
        head = new Node();        
    }
    
    @Override
    public Iterator<String> iterator() {
        final Deque<Node> backtrack = new LinkedList<Node>();
        final Deque<Integer> backtrackPos = new LinkedList<Integer>();
        backtrack.push(head);
        backtrackPos.push(-1);
        return new Iterator<String>() {
            String nextval = null;
            boolean first = true;
            @Override
            public boolean hasNext() {
                if (first) {
                    populate();
                    first = false;
                }
                return nextval != null;
            }

            private boolean populate() {
                nextval = null;
                Node current = backtrack.peekLast();
                if (current == null) {
                    return false;
                }
                int curpos = backtrackPos.peekLast();
                
                if (curpos == -1) {
                    backtrackPos.removeLast();
                    backtrackPos.addLast(0);
                    curpos = 0;
                    
                    if (current.isKey) {
                        nextval = "";
                        for (Node n:backtrack) {
                            if (n.value != null) {
                                nextval += new String(n.value);
                            }
                        }
                        return true;
                    }
                }
                
                if (current.children != null && curpos < current.children.length) {
                    backtrack.addLast(current.children[curpos]);
                    backtrackPos.removeLast();
                    backtrackPos.addLast(curpos+1);
                    backtrackPos.addLast(-1);
                    if (populate()) {
                        return true;
                    }
                }
                
                if (backtrack.peek() != null) {
                    backtrack.removeLast();
                    backtrackPos.removeLast();
                    if (populate()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String next() {
                String tmp = nextval;
                populate();
                return tmp;
            }

            @Override
            public void remove() {
                populate();
            }
        };
    }
    
    public static void main(String[] args) {
        RadixCounter set = new RadixCounter();
        set.add("house");
        set.dump();
        set.add("horse");
        set.dump();
        set.add("horsefly");
        set.dump();
        set.add("housefly");
        set.dump();
        set.add("horseshoe");
        set.add("housecoat");
        set.add("cat");
        set.dump();

        set = new RadixCounter();
        set.add("romane");
        set.add("romanus");
        set.add("romulus");
        set.add("rubens");
        set.add("ruber");
        set.add("rubicon");
        set.add("rubicundus");
        set.dump();
        System.err.println("set.size = "+ set.size);
        set.add("rubicon");
        System.err.println("set.size = "+ set.size);
        set.add("roman");
        System.err.println("set.size = "+ set.size);
        set.dump();
        
        set.add("athens");
        set.add("athena");
        set.add("athlete");
        set.dump();

        System.err.println("contains athens? "+ set.contains("athens"));
        System.err.println("contains athena? "+ set.contains("athena"));
        System.err.println("contains athen? "+ set.contains("athen"));
        System.err.println("contains roman? "+ set.contains("roman"));
        System.err.println("contains rub? "+ set.contains("rub"));

        System.err.println("set.size = "+ set.size);
        System.err.println("remove athena");
        set.remove("athena");
        set.dump();
        System.err.println("set.size = "+ set.size);
        System.err.println("remove romanus");
        set.remove("romanus");
        set.dump();
        System.err.println("set.size = "+ set.size);
        System.err.println("remove roman");
        set.remove("roman");
        set.dump();
        System.err.println("set.size = "+ set.size);
        System.err.println("remove romulus");
        set.remove("romulus");
        set.dump();
        System.err.println("set.size = "+ set.size);
        System.err.println("====");
        for (String s: set) {
            System.err.println(s);
        }
        
    }
}
