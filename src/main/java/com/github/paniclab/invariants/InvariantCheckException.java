package com.github.paniclab.invariants;


public class InvariantCheckException extends RuntimeException {
    private Invariant<?> invariant;
    private Object instance;


    public InvariantCheckException() {
    }

    public InvariantCheckException(String message) {
        super(message);
    }

    public InvariantCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvariantCheckException(Throwable cause) {
        super(cause);
    }

    public InvariantCheckException(Object instance, Invariant<?> invariant) {
        super("Invariant checking failed. Instance: " + instance + ", invariant: " + invariant);
        this.invariant = invariant;
        this.instance = instance;
    }

    public Invariant<?> getInvariant() {
        return invariant;
    }

    public Object getInstance() {
        return instance;
    }
}