// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

import java.util.Collection;

/**
 * Immutable set that can be replicated.
 *
 * @see Replicated
 * @see TwoPhaseSet
 * @param <V> Type of the sets elements.
 * @param <T> Type of the implementation.
 */
public interface ReplicatedSet<V, T extends ReplicatedSet<V, T>> extends Replicated<T> {

  T add(V element);

  T remove(V element);

  boolean contains(V value);

  int size();

  Collection<V> value();
}
