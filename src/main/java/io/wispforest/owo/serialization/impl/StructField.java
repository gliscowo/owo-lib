package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.StructDeserializer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class StructField<S, F> {

    @Nullable
    public final F defaultValue;

    public final String name;
    public final Codeck<F> codec;
    public final Function<S, F> getter;

    private StructField(String name, Codeck<F> codec, Function<S, F> getter, F defaultValue) {
        this.name = name;
        this.codec = codec;
        this.getter = getter;
        this.defaultValue = defaultValue;
    }

    public static <S, F> StructField<S, F> of(String name, Codeck<F> codec, Function<S, F> getter){
        return new StructField<>(name, codec, getter, null);
    }

    public static <S, F> StructField<S, Optional<F>> optional(String name, Codeck<F> codec, Function<S, Optional<F>> getter){
        return new StructField<>(name, codec.ofOptional(), getter, Optional.empty());
    }
}
