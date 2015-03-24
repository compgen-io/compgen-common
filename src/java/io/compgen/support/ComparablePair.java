package io.compgen.support;

public class ComparablePair<X extends Comparable<X>, Y extends Comparable<Y>> extends Pair<X, Y> implements Comparable<ComparablePair<X,Y>>{
	public ComparablePair(X one, Y two) {
		super(one, two);
	}

    @Override
    public int compareTo(ComparablePair<X, Y> o) {
        if (this == o) {
            return 0;
        }
        
        int compareOne = one.compareTo(o.one);
        if (compareOne != 0) {
            return compareOne;
        }
        
        return two.compareTo(o.two);
    }

}
