package io.github.merlinosayimwen.crtd.set;

import io.github.merlinosayimwen.crtd.Mergeable;

/**
 * {@code
 *   Mergeable<TwoPhaseSet<String>> merge(
 *        Mergeable<TwoPhaseSet<String>> left,
 *        Mergeable<TwoPhaseSet<String>> right) {
 *
 *     return left.merge(right);
 *   }
 * }
 *
 * {@code
 *   MergeableReplicatedSet<String, TwoPhaseSet<String>> merge(
 *       MergeableReplicatedSet<String, TwoPhaseSet<String>> left,
 *       MergeableReplicatedSet<String, TwoPhaseSet<String>> right) {
 *
 *     return left.merge(right);
 *   }
 * }
 *
 * @param <E> Type of the elements values.
 * @param <V> The implementation type of mergeable types.
 */
public interface MergeableReplicatedSet<E, V> extends ReplicatedSet<E>, Mergeable<V> { }