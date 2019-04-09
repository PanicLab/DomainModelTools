package com.github.paniclab.invariants;


@FunctionalInterface
public interface Invariant<T> {
    boolean check(T instance) throws InvariantCheckException;
}