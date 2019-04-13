package com.github.paniclab.specifications;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class SpecificationProvider {
    private static final ConcurrentMap<SpecId<?>, Specification<?>> SPEC_REGISTRY = new ConcurrentHashMap<>();

    <U extends Specification<?>> void register(U spec) {
        ConcurrentMap<SpecId<?>, Specification<?>> registry = SPEC_REGISTRY;
        if(registry.containsKey(spec.id())) {
            throw new SecurityException("Specification with id=" + spec.id() + " is already exists");
        }

        registry.put(spec.id(), spec);
    }

    protected <U, R extends Specification<U>, B extends SpecificationBuilder<U, ? extends Specification<U>>> R getInstance(Class<R> specClass, B builder) {
        R spec = getBrandNewInstance(specClass);

        return injectFromBuilder(spec, builder);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected <U> U getBrandNewInstance(@NotNull Class<U> clazz) {
        Constructor<U> constructor;

        constructor = (Constructor<U>) Arrays.stream(clazz.getDeclaredConstructors())
                                             .filter(c -> c.getGenericParameterTypes().length == 0)
                                             .findFirst()
                                             .orElseThrow(() -> new SpecificationException("Unable to create instance of" +
                                                     " class " + clazz.getCanonicalName() + ". This class has no " +
                                                     "appropriate constructor with no args."));

        U instance;
        try {
            constructor.setAccessible(true);
            instance = constructor.newInstance();

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new SpecificationException("Unable to create instance of class " + clazz.getCanonicalName(), e);
        }

        return instance;
    }

    private <U, R extends Specification<U>, B extends SpecificationBuilder<U, ? extends Specification<U>>> R injectFromBuilder(R spec, B builder) {
        Arrays.stream(spec.getClass().getDeclaredFields()).forEach(field -> {
            Optional<Field> builderField = Optional.ofNullable(getField(builder.getClass(), field.getName()));
            Object valueToInject;
            if(builderField.isPresent()) {
                valueToInject = getValue(builderField.get(), builder);
                populate(field, spec, valueToInject);
            }
        });

        return spec;
    }

    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new SpecificationException("Cannot create specification instance, specification class: " + clazz);
        }
    }

    private Object getValue(Field field, Object holder) {
        field.setAccessible(true);
        try {
            return field.get(holder);
        } catch (IllegalAccessException e) {
            throw new SpecificationException("Cannot create specification instance.");
        }
    }

    private <U, R extends Specification<U>> void populate(Field field, R spec, Object value) {
        field.setAccessible(true);
        try {
            field.set(spec, value);
        } catch (IllegalAccessException e) {
            throw new SpecificationException("Cannot create specification instance, specification class: " + spec.getClass());
        }
    }

    //@SuppressWarnings("unchecked")
    <R extends Specification<?>> Class<R> getSpecType(Class<?> clazz) {
        return (Class<R>) clazz;
    }
}
