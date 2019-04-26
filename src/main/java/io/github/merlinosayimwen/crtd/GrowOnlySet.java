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

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrowOnlySet<V> implements ReplicatedSet<V, GrowOnlySet<V>> {
  private static final class Lazy {
    // Initialization-on-demand holder idiom
    static final GrowOnlySet<?> EMPTY = new GrowOnlySet<>(Collections.emptyList());
  }

  private Collection<V> elements;

  private GrowOnlySet(Collection<V> elements) {
    this.elements = elements;
  }

  @Override
  public GrowOnlySet<V> merge(GrowOnlySet<V> other) {
    if (elements.isEmpty() && other.elements.isEmpty()) {
      return GrowOnlySet.empty();
    }
    Collection<V> merged =
        Stream.concat(elements.stream(), other.elements.stream()).collect(Collectors.toSet());
    return GrowOnlySet.of(merged);
  }

  @Override
  public GrowOnlySet<V> add(V element) {
    if (contains(element)) {
      return this;
    }
    Collection<V> copied = Sets.newHashSet(elements);
    copied.add(element);
    return new GrowOnlySet<>(copied);
  }

  @Override
  public GrowOnlySet<V> remove(V element) {
    return this;
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
  public Collection<V> value() {
    return new HashSet<>(elements);
  }

  @SuppressWarnings("unchecked")
  public static <V> GrowOnlySet<V> empty() {
    return (GrowOnlySet<V>) Lazy.EMPTY;
  }

  public static <V> GrowOnlySet<V> of(Collection<V> elements) {
    return empty();
  }

  public static <V> GrowOnlySet<V> of(Iterable<V> elements) {
    return empty();
  }

  public static <V> GrowOnlySet<V> of(V... elements) {
    return empty();
  }
}
