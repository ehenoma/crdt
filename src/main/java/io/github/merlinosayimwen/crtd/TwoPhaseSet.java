//  Copyright (c) 2019 Merlin Osayimwen
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
//  IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package io.github.merlinosayimwen.crtd;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

/**
 * Immutable append only set implementation that uses tombstones to indicate removed elements.
 *
 * <p>The TwoPhaseSet has two underlying sets where one is used to store all the current and past
 * elements and the other stores tombstones. Tombstones are created to indicate, that a specific
 * value has been removed from the set. To get the set of current entries, one has to take the
 * difference of all-elements and the tombstones. The TwoPhaseSet uses "remove-wins" semantics,
 * therefor a removed entry will have precedence over an added one.
 *
 * <p>This class is not mutable. Every update to its state will first create an identical copy and
 * then apply the update to the copy. This allows us to do optimisations for certain operations.
 * When multiple updates need to be done without the overhead of a shallow-copy, the Builder may be
 * used.
 *
 * @see ReplicatedSet
 * @param <V> Type of the sets elements.
 */
public final class TwoPhaseSet<V> implements ReplicatedSet<V, TwoPhaseSet<V>> {
  // Uses the initialization on demand holder idiom to create a
  // lazy and lock-free way to access common values.
  private static final class Lazy {
    // Used to return empty sets without allocating a new instance.
    // This way we do not have more short-living objects in our heaps nursery.
    static final TwoPhaseSet<?> EMPTY = new TwoPhaseSet<>(emptySet(), emptySet());
  }

  /** The current and past elements of the set. */
  private Collection<V> added;

  /**
   * List of removed elements, called tombstones.
   * <p>
   * Can not be called a subset of the added elements, because the TwoPhaseSet aims to
   * provide consistency without coordination. If tombstones would have to be in the set
   * of added elements and the replicating node sends the sets updates in a wrong order,
   * remove changes could get ignored. Tombstones can exist earlier than an added element
   * and still override it, because of the "remove-wins" semantics. That being said,
   * they are usually contained in the set of added elements.
   */
  private Collection<V> tombstones;

  private TwoPhaseSet(Collection<V> added, Collection<V> tombstones) {
    this.added = added;
    this.tombstones = tombstones;
  }

  /**
   * Elements that are currently contained in the set.
   *
   * @return Set of present values in the TwoPhaseSet.
   */
  @Override
  public Collection<V> value() {
    return added.stream().filter(this::isNotRemoved).collect(Collectors.toSet());
  }
  /**
   * Returns a copy of the instance, that contains the element.
   *
   * <p>This creates a new set from the instances elements and adds the argument to it. If the
   * argument is already contained in the instance, the same instance is returned.
   *
   * @param element The element to add.
   * @return Set that contains the added element.
   */
  @Override
  public TwoPhaseSet<V> add(V element) {
    Preconditions.checkNotNull(element);

    if (added.contains(element)) {
      return this;
    }
    if (hasTombstone(element)) {
      // The element already has a tombstone and will not have an effect
      // onto the set. We can ignore this call and return the same instance.
      return this;
    }
    Collection<V> added = addedElements();
    added.add(element);
    return new TwoPhaseSet<>(added, tombstones);
  }

  /**
   * Returns a copy of the instance, that does not contain the element.
   *
   * <p>This creates a new set from the instances elements and adds the element to its removed
   * elements. It will not remove anything from the set of added elements. In order for the element
   * to be removed, it has to be in the added elements. If the element is already removed, the same
   * instance is returned.
   *
   * @param element The element to remove.
   * @return Set that does not contain the element.
   */
  @Override
  public TwoPhaseSet<V> remove(V element) {
    if (hasTombstone(element)) {
      return this;
    }
    if (!added.contains(element)) {
      return this;
    }
    Collection<V> tombstones = tombstones();
    tombstones.add(element);
    return TwoPhaseSet.create(added, tombstones);
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
    Collection<V> added = mergeSets(this.added, other.added);
    Collection<V> tombstones = mergeSets(this.tombstones, other.tombstones);
    return TwoPhaseSet.create(added, tombstones);
  }

  /**
   * Returns the count of present elements.
   *
   * @return Count of present elements.
   */
  @Override
  public int size() {
    return added.size() - tombstones.size();
  }

  private int totalSize() {
    return added.size() + tombstones.size();
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
   * @return Elements that have been added to the set.
   */
  public Collection<V> addedElements() {
    return new HashSet<>(added);
  }

  /**
   * Returns a defensive copy of the removed elements.
   *
   * <p>This is always a subset of the added-elements.
   *
   * @return Elements that have been removed from the set.
   */
  public Collection<V> tombstones() {
    return new HashSet<>(added);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("added", added.size())
        .add("tombstones", tombstones.size())
        .add("size", size())
        .toString();
  }

  /**
   * Creates a TwoPhaseSet with no removed elements.
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
    Collection<V> added = new HashSet<>();
    for (V element : elements) {
      Preconditions.checkNotNull(element);
      added.add(element);
    }
    return new TwoPhaseSet<>(added, emptySet());
  }

  @SafeVarargs
  public static <V> TwoPhaseSet<V> of(V... elements) {
    Preconditions.checkNotNull(elements);
    if (elements.length == 0) {
      return empty();
    }
    Collection<V> added =
        Stream.of(elements).peek(Preconditions::checkNotNull).collect(Collectors.toSet());
    return new TwoPhaseSet<>(added, emptySet());
  }

  /**
   * Creates an empty TwoPhaseSet.
   *
   * @param <V> Type of the sets elements.
   * @return Empty TwoPhaseSet.
   */
  @SuppressWarnings("unchecked")
  public static <V> TwoPhaseSet<V> empty() {
    return (TwoPhaseSet<V>) Lazy.EMPTY;
  }

  /**
   * @param added Elements that have been added to the set.
   * @param removed Subset of the added elements, contains removed elements;
   * @param <V> Type of the sets elements.
   * @return Instance of a TwoPhaseSet.
   */
  public static <V> TwoPhaseSet<V> create(Collection<V> added, Collection<V> removed) {
    Preconditions.checkNotNull(added);
    Preconditions.checkNotNull(removed);
    if (added.isEmpty()) {
      // The collection of added elements is empty. In this case there
      // may not be any removed elements. Since the TwoPhaseSet is immutable,
      // we can create a lazy instance of an empty set.
      Preconditions.checkArgument(removed.isEmpty());
      return empty();
    }
    added.forEach(Preconditions::checkNotNull);
    removed.forEach(Preconditions::checkNotNull);

    return new TwoPhaseSet<>(added, removed);
  }

  private static <V> Collection<V> mergeSets(Collection<V> left, Collection<V> right) {
    return Stream.concat(left.stream(), right.stream()).collect(Collectors.toSet());
  }

  public static <V> Builder<V> newBuilder() {
    return new Builder<>(new HashSet<>(), new HashSet<>());
  }

  public static <V> Builder<V> newBuilder(TwoPhaseSet<V> prototype) {
    Preconditions.checkNotNull(prototype);
    return new Builder<>(prototype.addedElements(), prototype.tombstones());
  }

  public static final class Builder<V> {
    private Collection<V> added;
    private Collection<V> tombstones;

    private Builder(Collection<V> added, Collection<V> tombstones) {
      this.added = added;
      this.tombstones = tombstones;
    }

    public Builder<V> add(V element) {
      Preconditions.checkNotNull(element);
      added.add(element);
      return this;
    }

    public Builder<V> addTombstone(V element) {
      Preconditions.checkNotNull(element);
      added.add(element);
      tombstones.add(element);
      return this;
    }

    public TwoPhaseSet<V> create() {
      return TwoPhaseSet.create(added, tombstones);
    }
  }
}
