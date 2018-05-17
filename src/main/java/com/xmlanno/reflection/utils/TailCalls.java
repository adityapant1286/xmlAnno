package com.xmlanno.reflection.utils;

/**
 * <p>
 *     API class for {@link TailCall} interface.<br>
 *     This can be used in replace of traditional recursion.
 * </p>
 * @author Aditya.Pant
 * @since 21-Mar-2018
 */
public class TailCalls {

    /**
     *
     * @param nextCall next {@link TailCall} instance
     * @param <T> next {@link TailCall} instance
     * @return next {@link TailCall} instance
     */
    public static <T> TailCall<T> call(final TailCall<T> nextCall) { return nextCall; }

    /**
     * <p>
     *     This function should invoke when it is time to escape
     *     recursion.
     * </p>
     * @param value user input to be process for this call
     * @param <T> The type on which <i>tail-call</i> should apply
     * @return result of recursive call
     */
    public static <T> TailCall<T> done(final T value) {

        return new TailCall<T>() {
            @Override
            public boolean isComplete() { return true; }

            @Override
            public T result() { return value; }

            @Override
            public TailCall<T> apply() { throw new Error("not implemented"); }
        };
    }
}
