// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crdt.set;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.github.merlinosayimwen.crdt.Mergeable;

/**
 * Append only set implementation that uses tombstones to indicate removed elements.
 *
 * <p>The TwoPhaseSet has two underlying sets where one is used to store all the current and past
 * elements and the other stores tombstones. Tombstones are created to indicate, that a specific
 * value has been removed from the set. To get the set of current elements, one has to take the
 * difference of all-elements and the tombstones. The TwoPhaseSet uses "remove-wins" semantics,
 * therefor a removed entry will have precedence over an added one.
 *
 * @see ReplicatedSet
 * @param <V> Type of the sets elements.
 */
public final class TwoPhaseSet<V> implements ReplicatedSet<V>, Mergeable<TwoPhaseSet<V>> {
  /** The current and past elements of the set. */
  private Set<V> added;

  /** Set of removed elements, called tombstones. */
  private Set<V> tombstones;

  private TwoPhaseSet(Set<V> added, Set<V> tombstones) {
    this.added = added;
    this.tombstones = tombstones;
  }

  /**
   * Elements that are currently contained in the set.
   *
   * @return Set of present values in the TwoPhaseSet.
   */
  @Override
  public Set<V> toSet() {
    return added.stream().filter(this::isNotRemoved).collect(Collectors.toSet());
  }

  /**
   * Adds the element to the set if no tombstone of that element exists.
   *
   * @param element The element to add.
   */
  @Override
  public void add(V element) {
    Preconditions.checkNotNull(element);
    if (hasTombstone(element)) {
      // The element already has a tombstone and adding it would
      // not have an effect onto the set.
      return;
    }
    added.add(element);
  }

  /**
   * Returns a copy of the instance, that does not contain the element.
   *
   * <p>This creates a new set from the instances elements and adds the element to its removed
   * elements. It will not remove anything from the set of added elements. In order for the element
   * to be removed, it has to be in the added elements.
   *
   * @param element The element to remove.
   * @return True if the element has been removed.
   */
  @Override
  public boolean remove(V element) {
    tombstones.add(element);
    return true;
  }

  @Override
  public boolean clear() {
    tombstones.addAll(added);
    return true;
  }

  /**
   * Returns true, if the value is in the added-elements set and not contained in the
   * removed-element-set.
   *
   * @param value Value that is looked up in the phase sets.
   * @return If the element is in the set.
   */
  @Override
  public boolean contains(V value) {
    return added.contains(value) && isNotRemoved(value);
  }

  /**
   * Merges to TwoPhaseSets by merging their added-elements and removed-elements sets and returning
   * an instance with the results.
   *
   * @param other Value that is merged with the instance.
   * @return Set with the merged results from both sets.
   */
  @Override
  public TwoPhaseSet<V> merge(TwoPhaseSet<V> other) {
    if (size() == 0 && other.size() == 0) {
      return TwoPhaseSet.empty();
    }
    Collection<V> mergedAdds = mergeSets(this.added, other.added);
    Collection<V> mergedTombstones = mergeSets(this.tombstones, other.tombstones);
    return TwoPhaseSet.create(mergedAdds, mergedTombstones);
  }

  /**
   * Returns the count of present elements.
   *
   * @return Count of present elements.
   */
  @Override
  public int size() {
    return (int) added.stream().filter(this::isNotRemoved).count();
  }

  private boolean isNotRemoved(V value) {
    return !hasTombstone(value);
  }

  private boolean hasTombstone(V value) {
    return tombstones.contains(value);
  }

  /**
   * Returns a defensive copy of the added elements.
   *
   * <p>All elements that have been added to the set in the past and are currently contained, are
   * inside of this set.
   *
   * @return Elements that have been added to the set.
   */
  public Set<V> addedElements() {
    return new HashSet<>(added);
  }

  /**
   * List of removed elements, called tombstones.
   *
   * <p>Can not be called a subset of the added elements, because the TwoPhaseSet aims to provide
   * consistency without coordination. If tombstones would have to be in the set of added elements
   * and the replicating node sends the sets updates in a wrong order, remove changes could get
   * ignored. Tombstones can exist earlier than an added element and still override it, because of
   * the "remove-wins" semantics. That being said, they are usually contained in the set of added
   * elements.
   *
   * @return Elements that have been removed from the set.
   */
  public Set<V> tombstones() {
    return new HashSet<>(tombstones);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("added", added.size())
        .add("tombstones", tombstones.size())
        .add("size", size())
        .toString();
  }

  private static <V> Collection<V> mergeSets(Collection<V> left, Collection<V> right) {
    return Stream.concat(left.stream(), right.stream()).collect(Collectors.toSet());
  }

  /**
   * Creates a TwoPhaseSet with no tombstones.
   *
   * @param elements Set of elements in the TwoPhaseSet.
   * @param <V> Type of the sets elements.
   * @return New instance of a TwoPhaseSet.
   */
  public static <V> TwoPhaseSet<V> of(Collection<V> elements) {
    Preconditions.checkNotNull(elements);
    if (elements.isEmpty()) {
      return empty();
    }
    return of((Iterable<V>) elements);
  }

  public static <V> TwoPhaseSet<V> of(Iterable<V> elements) {
    Preconditions.checkNotNull(elements);
    Set<V> added = Sets.newHashSet();
    for (V element : elements) {
      Preconditions.checkNotNull(element);
      added.add(element);
    }
    return new TwoPhaseSet<>(added, Sets.newHashSet());
  }

  @SafeVarargs
  public static <V> TwoPhaseSet<V> of(V first, V... others) {
    Preconditions.checkNotNull(first);
    Preconditions.checkNotNull(others);

    Stream.of(others).forEach(Preconditions::checkNotNull);
    Set<V> elements = Sets.newHashSet(others);
    elements.add(first);
    return new TwoPhaseSet<>(elements, new HashSet<>());
  }

  /**
   * Creates an empty TwoPhaseSet.
   *
   * @param <V> Type of the sets elements.
   * @return Empty TwoPhaseSet.
   */
  public static <V> TwoPhaseSet<V> empty() {
    return new TwoPhaseSet<>(Sets.newHashSet(), Sets.newHashSet());
  }

  /**
   * Creates a TwoPhaseSet with an initial set of added and removed elements.
   *
   * @param added Elements that have been added to the set.
   * @param tombstones Set of initial tombstones.
   * @param <V> Type of the sets elements.
   * @return Instance of a TwoPhaseSet.
   */
  public static <V> TwoPhaseSet<V> create(Collection<V> added, Collection<V> tombstones) {
    Preconditions.checkNotNull(added);
    Preconditions.checkNotNull(tombstones);

    added.forEach(Preconditions::checkNotNull);
    tombstones.forEach(Preconditions::checkNotNull);
    return new TwoPhaseSet<>(Sets.newHashSet(added), Sets.newHashSet(tombstones));
  }
}
