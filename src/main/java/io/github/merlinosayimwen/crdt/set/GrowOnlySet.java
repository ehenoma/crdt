// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crdt.set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.github.merlinosayimwen.crdt.Mergeable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @param <V>
 */
public final class GrowOnlySet<V> implements ReplicatedSet<V>, Mergeable<GrowOnlySet<V>> {
  private Set<V> elements;

  private GrowOnlySet(Set<V> elements) {
    this.elements = elements;
  }

  @Override
  public GrowOnlySet<V> merge(GrowOnlySet<V> other) {
    Preconditions.checkNotNull(other);
    if (elements.isEmpty() && other.elements.isEmpty()) {
      return GrowOnlySet.empty();
    }

    return GrowOnlySet.of(Sets.union(elements, other.elements));
  }

  @Override
  public void add(V element) {
    elements.add(element);
  }

  @Override
  public boolean clear() {
    return false;
  }

  @Override
  public boolean remove(V element) {
    return false;
  }

  @Override
  public boolean contains(V value) {
    return elements.contains(value);
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public Set<V> toSet() {
    return new HashSet<>(elements);
  }

  public static <V> GrowOnlySet<V> empty() {
    return of(Sets.newHashSet());
  }

  public static <V> GrowOnlySet<V> of(Collection<V> elements) {
    return new GrowOnlySet<>(Sets.newHashSet(elements));
  }

  public static <V> GrowOnlySet<V> of(Iterable<V> elements) {
    return new GrowOnlySet<>(Sets.newHashSet(elements));
  }

  public static <V> GrowOnlySet<V> of(V... elements) {
    return new GrowOnlySet<>(Sets.newHashSet(elements));
  }
}
