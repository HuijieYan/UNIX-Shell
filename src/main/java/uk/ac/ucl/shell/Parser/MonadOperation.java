package uk.ac.ucl.shell.Parser;

import java.util.function.Function;

public interface MonadOperation {
    <T> Monad<T> result(final T value);
    <T,A> Monad<A> bind(final Monad<T> parser, final Function<T, Monad<A>> function);
    <T> Monad<T> zero();
}
