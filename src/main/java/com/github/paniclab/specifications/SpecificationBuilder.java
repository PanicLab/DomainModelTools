package com.github.paniclab.specifications;


import com.github.paniclab.invariants.Invariant;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;


public class SpecificationBuilder<T, R extends Specification<T>> {
    private Class<T> subject;
    private Class<? extends R> specType;
    private Function<T, Boolean> predicate;
    private Invariant<T> invariant;
    private Specification.CompareApproach compareApproach = Specification.CompareApproach.IDENTITY;
    private SpecId<T> specId;

    SpecificationBuilder() {}

    public SpecificationBuilder(Class<T> subjectClass, Class<? extends R> specClass) {
        subject = subjectClass;
        specType = specClass;
    }

    public SpecificationBuilder<T, R> withSpecType(Class<R> specType) {
        this.specType = specType;
        return this;
    }

    public SpecificationBuilder<T, R> withSubject(Class<T> subject) {
        this.subject = subject;
        return this;
    }


    public SpecificationBuilder<T, R> withPredicate(Function<T, Boolean> predicate) {
        this.predicate = predicate;
        return this;
    }

    public SpecificationBuilder<T, R> withInvariant(Invariant<T> invariant) {
        this.invariant = invariant;
        return this;
    }

    public SpecificationBuilder<T, R> withSpecId(SpecId<T> specId) {
        this.specId = specId;
        if(specId != null) {
            this.compareApproach = Specification.CompareApproach.EQUALITY;
        }
        return this;
    }



    protected Class<T> getSubject() {
        return subject;
    }

    protected Function<T, Boolean> getPredicate() {
        return predicate;
    }


    protected SpecId<T> getSpecId() {
        return specId;
    }

    protected Specification.CompareApproach getCompareApproach() {
        return compareApproach;
    }

    protected Class<? extends Specification<T>> getSpecType() {
        return specType;
    }

    protected Invariant<T> getInvariant() {
        return invariant;
    }

    public static Invariant<SpecificationBuilder<?, ?>> readiness() {
        return readiness;
    }

    public R build() {
        //return Specification.from(this);
        return new SpecificationProvider().getInstance(specType, this);
    }

    @Override
    public String toString() {
        return "SpecificationBuilder{" +
                "subject=" + subject +
                ", predicate=" + predicate +
                ", compareApproach=" + compareApproach +
                ", specId=" + specId +
                '}';
    }

    protected String getSpecClassFieldName(String builderFieldName) {
        return Arrays.stream(getClass().getDeclaredFields())
                                       .map(Field::getName)
                                       .filter(builderFieldName::equals)
                                       .findFirst()
                                       .orElse(null);
    }


    private static Invariant<SpecificationBuilder<?, ?>> readiness = builder -> {
        boolean isCheckSuccessful;

        switch (builder.compareApproach) {
            case COMPARISON:
            case EQUALITY:
                isCheckSuccessful =
                        builder.subject != null &&
                                builder.predicate != null &&
                                builder.specId != null;
                break;
            case IDENTITY:
                isCheckSuccessful =
                        builder.subject != null &&
                                builder.predicate != null;
                break;
            default:
                throw new SpecificationException("Unknown spec compare approach.");
        }

        if(!isCheckSuccessful) {
            throw new SpecificationException("Invalid builder's state: " + builder);
        }

        return true;
    };
}
