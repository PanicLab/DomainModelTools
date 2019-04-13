package com.github.paniclab.spectator;

import com.github.paniclab.specifications.Specification;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;


public interface Spectator {
    default <U> boolean is(U something) {
        return something instanceof Spectator;
    }
    default <U> boolean has(U something) {
        return false;
    }
    default boolean can(Predicate<Spectator> predicate) {
        return predicate.test(this);
    }

    default boolean satisfy(Predicate<Spectator> predicate) {
        return predicate.test(this);
    }

    default <U> boolean satisfy(Specification<U> spec) {
        return spec.isSatisfiedBy(this.unwrap(spec.subject()));
    }

    default <U> U unwrap(Class<? extends U> clazz) {
        if(Spectator.class.isAssignableFrom(clazz)) {
            return clazz.cast(this);
        }

        throw new UnsupportedOperationException("Operation is not supported.");
    }
}
