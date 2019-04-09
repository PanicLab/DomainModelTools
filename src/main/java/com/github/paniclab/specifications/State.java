package com.github.paniclab.specifications;


import com.github.paniclab.invariants.Invariant;


public class State {
    private Class<?> subject;
    private Invariant<?> invariant;
    private SpecId<?> specId;

    public Class<?> getSubject() {
        return subject;
    }

    public void setSubject(Class<?> subject) {
        this.subject = subject;
    }

    public Invariant<?> getInvariant() {
        return invariant;
    }

    public void setInvariant(Invariant<?> invariant) {
        this.invariant = invariant;
    }

    public SpecId<?> getSpecId() {
        return specId;
    }

    public void setSpecId(SpecId<?> specId) {
        this.specId = specId;
    }
}
