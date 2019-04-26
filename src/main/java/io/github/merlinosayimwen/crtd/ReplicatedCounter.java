package io.github.merlinosayimwen.crtd;

public interface ReplicatedCounter<V extends Number, T extends ReplicatedCounter<V, T>> extends Replicated<T> {

  V value();
}