package io.compgen.support.progress;

public interface ProgressMessage<T> {
    public String msg(T current);
}
