// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd.set;

import java.util.Set;

/**
 * Set that can be replicated.
 *
 * @param <E> Type of the sets elements.
 */
public interface ReplicatedSet<E> {

  void add(E element);

  boolean remove(E element);

  boolean contains(E value);

  int size();

  Set<E> toSet();
}
