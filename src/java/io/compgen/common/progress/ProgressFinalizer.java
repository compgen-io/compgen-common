package io.compgen.common.progress;

import java.util.Iterator;

public interface ProgressFinalizer<T> {
	public void finalize(Iterator<T> it);
}
