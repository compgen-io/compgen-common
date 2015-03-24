package io.compgen.support.progress;

import java.util.Iterator;

public interface ProgressFinalizer<T> {
	public void finalize(Iterator<T> it);
}
