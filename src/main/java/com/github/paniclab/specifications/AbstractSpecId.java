package com.github.paniclab.specifications;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AbstractSpecId<S, E extends Enum<E>> implements SpecId<S> {
    private Class<S> subject;
    //private Class<E> enumType;
    private E value;

    protected AbstractSpecId(Class<S> subjectType, E value) {
        this.subject = subjectType;
        this.value = value;
    }

    @Override
    @NotNull
    public E value() {
        return value;
    }

    @Override
    @NotNull
    public Class<S> subject() {
        return subject;
    }


    @Override
    public int hashCode() {
        return Objects.hash(value());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;

        if(!(obj instanceof SpecId)) return false;
        SpecId other = SpecId.class.cast(obj);

        return this.value().equals(other.value());
    }

    @Override
    public String toString() {
        return "AbstractSpecId{" +
                "subject=" + subject +
                ", value=" + value +
                '}';
    }
}
