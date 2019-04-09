package com.github.paniclab.specifications;


public interface SpecId<S> {
    default Enum value() {
        return (Enum) this;
    }
    Class<S> subject();
}
