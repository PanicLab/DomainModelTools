package com.github.paniclab.specifications;


public class SpecificationException extends RuntimeException {
    private Specification<?> specification;
    private Object instance;


    public SpecificationException() {
    }

    public SpecificationException(String message) {
        super(message);
    }

    public SpecificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpecificationException(Throwable cause) {
        super(cause);
    }

    public SpecificationException(Object instance, Specification<?> specification) {
        super("Instance is not satisfy specification. Instance: " + instance + ", specification: " + specification);
        this.instance = instance;
        this.specification = specification;
    }

    public Specification<?> getSpecification() {
        return specification;
    }

    public Object getInstance() {
        return instance;
    }
}
