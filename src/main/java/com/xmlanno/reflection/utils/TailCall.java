package com.xmlanno.reflection.utils;

import java.util.stream.Stream;

@FunctionalInterface
public interface TailCall<T> {

    /**
     * <p>
     *     Part of {@link FunctionalInterface} annotation
     * </p>
     * @return Instance of this
     */
    TailCall<T> apply();

    /**
     * <p>
     *     The implementation class should return {@code true} until
     *     finds the expected results
     * </p>
     * @return {@code false} - because not found yet
     */
    default boolean isComplete() { return false; }

    /**
     * <p>
     *     Implementation specific results
     * </p>
     * @return A type containing results
     */
    default T result() { throw new Error("To be overridden in a derived class"); }

    /**
     * <p>
     *     Iterate over all {@link TailCall} objects and invoke
     *     {@code apply()} method on each, until expected results are found.
     *     i.e. when {@code isComplete()} return {@code true}
     * </p>
     * @return A type containing results
     */
    default T get() {
        return Stream.iterate(this, TailCall::apply)
                .filter(TailCall::isComplete)
                .findFirst()
                .get()
                .result();
    }
}
