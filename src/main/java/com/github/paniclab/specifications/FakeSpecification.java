package com.github.paniclab.specifications;

import java.util.function.Function;

import static com.github.paniclab.specifications.Specification.not;

public class FakeSpecification extends Specification<String>{
    private String x;

    public FakeSpecification(Function<String, Boolean> invariant) {
        super(String.class, invariant);
    }


    public static void main(String[] args) {
        Specification<String> toBe = Specification.of(String.class, instance -> true);
        Specification<String> question = toBe.or(not(toBe));

/*        Specification<Employee> another = Specification.builder()
                                                       .withSubject(Employee.class)
                                                       .withPredicate(instance -> true)
                                                       .withSpecId(Brand.ONE)
                                                       .build();*/
        Specification<String> another = question.withId(Brand.ONE);
        Specification<String> onEnd = Specification.as(toBe, Brand.TWO);

        System.out.println(toBe);
        System.out.println(question);
        System.out.println(another);
        System.out.println(onEnd);

        FakeSpecification toBeFake = new FakeSpecification(instance -> true);
        FakeSpecification questionFake = toBeFake.and(another);
        Specification<String> otherFake = new FakeSpecification(string -> true).or(questionFake);
    }


    public enum Brand implements SpecId<String> {
        ONE,
        TWO,
        THREE;


        @Override
        public Class<String> subject() {
            return String.class;
        }
    }


/*    public class Builder extends SpecificationBuilder<Employee> {

        @Override
        public FakeSpecification build() {
            return Specification.from(this);
        }

        @Override
        public <U> FakeSpecification withSubject(Class<U> subject) {

        }
    }*/
}
