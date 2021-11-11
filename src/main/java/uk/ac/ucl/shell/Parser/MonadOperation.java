package uk.ac.ucl.shell.Parser;

import java.util.function.Function;

public interface MonadOperation {
    public <T> Monad<T> result(final T value);
    public <T,A> Monad<A> bind(final Monad<T> parser, final Function<T,Monad<A>> function);
    public <T> Monad<T> zero();
}
