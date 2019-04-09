package com.github.paniclab.specifications;

import com.github.paniclab.invariants.Invariant;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Specification<T> {
    private final Class<T> subject;
    private final Class<? extends Specification<T>> specType;
    private final Function<T, Boolean> predicate;
    private final Invariant<T> invariant;
    private final CompareApproach compareApproach;
    private final SpecId<? extends T> specId;



    protected Specification(Class<T> subject, Function<T, Boolean> predicate) {
        this.subject = subject;
        this.specType = new SpecificationProvider().getSpecType(this.getClass());
        this.predicate = predicate;
        this.invariant = predicate::apply;
        this.compareApproach = CompareApproach.IDENTITY;
        this.specId = null;
    }

    protected Specification(Class<T> clazz, Function<T, Boolean> predicate, SpecId<T> specId) {
        this.subject = clazz;
        this.specType = new SpecificationProvider().getSpecType(this.getClass());
        this.predicate = predicate;
        this.invariant = predicate::apply;
        this.specId = specId;
        if (this.specId != null) {
            this.compareApproach = CompareApproach.EQUALITY;
        } else {
            this.compareApproach = CompareApproach.IDENTITY;
        }
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


    public static <U> Specification<U> as(Specification<U> spec, SpecId<U> specId) {
        Specification<U> newSpec = new Specification<>(spec.subject(), spec.predicate, specId);
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



    protected SpecificationBuilder<T, Specification<T>> builder() {
        return new SpecificationBuilder<>(subject, specType);
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
        SpecificationProvider provider = new SpecificationProvider();

        Invariant<T> invariant = instance -> this.isSatisfiedBy(instance) && other.isSatisfiedBy(instance);
        SpecificationBuilder<T, ?> builder = new SpecificationBuilder<>();
        R resultSpec = (R)provider.getInstance(this.getClass(), builder);

        return resultSpec;
    }

    public Specification<T> or(Specification<T> other) {
        Specification<T> newSpec;
        newSpec = new Specification<>(other.subject(),
                                      instance -> this.isSatisfiedBy(instance) || other.isSatisfiedBy(instance));
        return newSpec;
    }

    public Specification<T> not() {
        Predicate<T> predicate = this::isSatisfiedBy;

        return new Specification<>(this.subject,
                instance -> predicate.negate().test(instance)
        );
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
