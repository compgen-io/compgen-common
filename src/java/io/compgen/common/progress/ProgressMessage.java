package io.compgen.common.progress;

public interface ProgressMessage<T> {
    public String msg(T current);
}
