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

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable append only set implementation.
 *
 * <p>Instead of removing elements from a set, this class adds them to a set of removed elements,
 * which is then used to check whether an element is contained in the set. Operations like {@code
 * add} do not modify any of the instances state, but rather create a new instance of the
 * ReplicatedSet.
 *
 * @see ReplicatedSet
 * @param <V> Type of the sets elements.
 */
@Immutable
public final class TwoPhaseSet<V> implements ReplicatedSet<V, TwoPhaseSet<V>> {
  private static final class Lazy {
    // Initialization-on-demand holder idiom
    static final TwoPhaseSet<?> EMPTY =
        new TwoPhaseSet<>(Collections.emptyList(), Collections.emptyList());
  }

  private Collection<V> added;
  private Collection<V> removed;

  private TwoPhaseSet(Collection<V> added, Collection<V> removed) {
    this.added = added;
    this.removed = removed;
  }

  @Override
  public Collection<V> value() {
    return added.stream().filter(this::isNotRemoved).collect(Collectors.toSet());
  }

  @Override
  @CheckReturnValue
  public TwoPhaseSet<V> add(V element) {
    if (added.contains(element)) {
      return this;
    }
    Collection<V> added = addedElements();
    added.add(element);
    return TwoPhaseSet.create(added, removed);
  }

  @Override
  @CheckReturnValue
  public TwoPhaseSet<V> remove(V element) {
    if (removed.contains(element)) {
      return this;
    }
    if (!added.contains(element)) {
      return this;
    }
    Collection<V> removed = removedElements();
    removed.add(element);
    return TwoPhaseSet.create(added, removed);
  }

  private boolean isNotRemoved(V value) {
    return !removed.contains(value);
  }

  @Override
  public boolean contains(V value) {
    return added.contains(value) && isNotRemoved(value);
  }

  @Override
  public TwoPhaseSet<V> merge(TwoPhaseSet<V> other) {
    if (added.isEmpty() && other.added.isEmpty()) {
      return TwoPhaseSet.empty();
    }
    Collection<V> mergedAdds = mergeSets(this.added, other.added);
    Collection<V> mergedRemoves = mergeSets(this.removed, other.removed);
    return TwoPhaseSet.create(mergedAdds, mergedRemoves);
  }

  @Override
  public int size() {
    return added.size() - removed.size();
  }

  public Collection<V> addedElements() {
    return new HashSet<>(added);
  }

  public Collection<V> removedElements() {
    return new HashSet<>(added);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("added", added.size())
        .add("removed", removed.size())
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
    return new TwoPhaseSet<>(added, Collections.emptySet());
  }

  @SafeVarargs
  public static <V> TwoPhaseSet<V> of(V... elements) {
    Preconditions.checkNotNull(elements);
    if (elements.length == 0) {
      return empty();
    }
    Collection<V> added =
        Stream.of(elements).peek(Preconditions::checkNotNull).collect(Collectors.toSet());
    return new TwoPhaseSet<>(added, Collections.emptyList());
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

    for (V element : removed) {
      // The removed-element-set is a subset of the added-elements, thus we
      // are ensuring that it does only contain elements which are already
      // in the set of added elements.
      Preconditions.checkNotNull(element);
      Preconditions.checkArgument(added.contains(element));
    }

    return new TwoPhaseSet<>(added, removed);
  }

  private static <V> Collection<V> mergeSets(Collection<V> left, Collection<V> right) {
    return Stream.concat(left.stream(), right.stream()).collect(Collectors.toSet());
  }
}
