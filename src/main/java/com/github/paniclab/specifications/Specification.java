package com.github.paniclab.specifications;

import com.github.paniclab.invariants.Invariant;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Specification<T> {
    private final SpecificationProvider provider = new SpecificationProvider();

    private final Class<T> subject;
    private final Class<? extends Specification<T>> specType;
    private final Function<T, Boolean> predicate;
    private final Invariant<T> invariant;
    private final Map<SpecId<? extends T>, Invariant<T>> invariantsChain;
    private final CompareApproach compareApproach;
    private final SpecId<? extends T> specId;



    protected Specification(Class<T> subject, Function<T, Boolean> predicate) {
        this.subject = subject;
        this.specType = provider.getSpecType(this.getClass());
        this.predicate = predicate;
        this.invariant = predicate::apply;
        this.invariantsChain = Collections.emptyMap();
        this.compareApproach = CompareApproach.IDENTITY;
        this.specId = null;
    }

    protected Specification(Class<T> clazz, Function<T, Boolean> predicate, SpecId<T> specId) {
        this.subject = clazz;
        this.specType = new SpecificationProvider().getSpecType(this.getClass());
        this.predicate = predicate;
        this.invariant = predicate::apply;
        this.invariantsChain = Collections.emptyMap();
        this.specId = specId;
        if (this.specId != null) {
            this.compareApproach = CompareApproach.EQUALITY;
        } else {
            this.compareApproach = CompareApproach.IDENTITY;
        }

        this.provider.register(this);
    }


/*    protected Specification(SpecificationBuilder<T> builder) {
        this(builder.getSubject(), builder.getPredicate(), builder.getSpecId());
        this.type = builder.getSpecType();
        specIdValidness.check(this);
    }*/


    public static <U> Specification<U> of(Class<U> subject, Function<U, Boolean> invariant) {
        return new Specification<>(subject, invariant);
    }


    protected static <U, R extends Specification<U>, B extends SpecificationBuilder<U, ? extends R>> R from(B builder) {
        SpecificationBuilder.readiness().check(builder);
        return builder.build();
    }


    public static <U, R extends Specification<U>> R as(Specification<U> spec, SpecId<U> specId) {
        SpecificationBuilder<U, R> builder = spec.builder();

        R newSpec = builder.withSubject(spec.subject())
                           .withInvariant(spec.invariant())
                           .withSpecType(spec.specType())
                           .withSpecId(specId)
                           .build();

        return newSpec;
    }

    @SafeVarargs
    public static <U> Specification<U> compose(Specification<U>...specifications) {
        Collection<Specification<U>> composites = Arrays.asList(specifications);
        Class<U> subject = composites.stream()
                                     .map(Specification::subject)
                                     .findAny()
                                     .orElseThrow(() -> new SpecificationException("Unable combine specifications from array: "
                        + Arrays.toString(specifications)));

        Invariant<U> invariant = instance -> Arrays.stream(specifications).allMatch(spec -> spec.isSatisfiedBy(instance));
        return new Specification<>(subject, invariant::check);
    }

/*    public static <U> Specification<U>  not(Specification<U> spec) {
        Predicate<U> predicate = spec::isSatisfiedBy;

        Specification<U> newSpec = new Specification<>(spec.subject(),
                instance -> predicate.negate().test(instance),
                spec.specId
        );

        return newSpec;
    }*/

    public static <U> Specification<U> not(Specification<U> spec) {
        Predicate<U> predicate = spec::isSatisfiedBy;

/*        R newSpec = Specification.builder()
                .withSubject(spec.subject())
                .withPredicate(instance -> predicate.negate().test(instance))
                .withSpecId(spec.id())
                .build();*/

        Specification<U> newSpec = new Specification<>(spec.subject(),
                                                       instance -> predicate.negate().test(instance));

        return newSpec;
    }



/*    protected <U, R extends Specification<U>> SpecificationBuilder<U, R> builder() {
        return new SpecificationBuilder<>((Class<U>)subject, (Class<R>) specType);
    }*/
    protected <R extends Specification<T>> SpecificationBuilder<T, R> builder() {
        return new SpecificationBuilder<>(subject, (Class<R>)specType);
    }

    public <U extends T> boolean isSatisfiedBy(U instance) throws SpecificationException {
        return this.predicate.apply(instance);
    }

    public Set<T> selectSatisfying(Collection<? extends T> collection) {
        return collection.stream()
                         .filter(this::isSatisfiedBy)
                         .collect(Collectors.toSet());
    }

/*    public Specification<T> and(Specification<T> other) {
        Specification<T> newSpec = new Specification<>(subject(),
                instance -> this.isSatisfiedBy(instance) && other.isSatisfiedBy(instance));

        return newSpec;
    }*/

    @SuppressWarnings("unchecked")
    public <U extends Specification<T>, R extends Specification<T>> R and(U other) {
        Invariant<T> invariant = instance -> this.isSatisfiedBy(instance) && other.isSatisfiedBy(instance);
        SpecificationBuilder<T, R> builder = this.builder();
        R resultSpec = builder.withSubject(subject())
                              .withSpecType(specType())
                              .withInvariant(invariant)
                              .build();

        return resultSpec;
    }

    public <U extends Specification<T>, R extends Specification<T>> R or(U other) {
        R newSpec;
        SpecificationBuilder<T, R> builder = this.builder();

        newSpec = builder.withSubject(subject())
                         .withSpecType(specType())
                         .withInvariant(instance -> this.isSatisfiedBy(instance) || other.isSatisfiedBy(instance))
                         .build();

        return newSpec;
    }

    public <R extends Specification<T>> R not() {
        Predicate<T> predicate = this::isSatisfiedBy;

        R newSpec;
        SpecificationBuilder<T, R> builder = this.builder();

        newSpec = builder.withSubject(subject())
                         .withSpecType(specType())
                         .withInvariant(instance -> predicate.negate().test(instance))
                         .build();

        return newSpec;
    }

    public Specification<T> withId(SpecId<T> id) {
        return new Specification<>(this.subject(), this.predicate, id);
    }


    public Class<T> subject() {
        return subject;
    }

    protected SpecId<? extends T> id() {
        return this.specId;
    }

    protected Invariant<T> invariant() {
        return invariant;
    }

    protected SpecificationProvider provider() {
        return this.provider;
    }

    protected <U extends Specification<T>> Class<U> specType() {
        return (Class<U>) this.specType;
    }


    @Override
    public String toString() {
        return "Specification{" +
                "subject=" + subject.getSimpleName() +
                (specId == null ? "" : ", id=" + specId.toString()) +
                '}';
    }


    enum CompareApproach {
        IDENTITY,
        EQUALITY,
        COMPARISON
    }


    private Invariant<Specification<T>> specIdValidness = spec -> {
        if(spec.compareApproach == CompareApproach.IDENTITY) {
            return true;
        }

        if(spec.specId == null) {
            throw new SpecificationException("Missing SpecId, specification: " + this);
        }

        return true;
    };
}
