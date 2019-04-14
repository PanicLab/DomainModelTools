package com.github.paniclab.spectator;

import com.github.paniclab.invariants.Invariant;
import com.github.paniclab.specifications.Specification;

import java.util.function.Predicate;


public interface Spectator {
    default boolean satisfy(Invariant<Spectator> predicate) {
        return predicate.check(this);
    }

    default boolean satisfy(Predicate<Spectator> predicate) {
        return predicate.test(this);
    }

    default <U> boolean satisfy(Specification<U> spec) {
        U subject;

        try {
            subject = this.unwrap(spec.subject());
        } catch (ClassCastException | UnsupportedOperationException e) {
            return false;
        }

        return spec.isSatisfiedBy(subject);
    }

    default <U> U unwrap(Class<? extends U> clazz) throws UnsupportedOperationException {
        if(Spectator.class.isAssignableFrom(clazz)) {
            return clazz.cast(this);
        }

        throw new UnsupportedOperationException("Operation is not supported for this type: " + clazz);
    }
}
