// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

/**
 * Immutable type that can be safely replicated without any conflicts, also known as 'Conflict-free
 * replicated data type' (CRDT).
 *
 * @param <M> Type of the implementation.
 */
public interface Replicated<M extends Replicated<M>> {

  /**
   * Merges both values together.
   *
   * @param other Value that is merged with the instance.
   * @return New instance that contains the merge results of both values.
   */
  M merge(M other);

  Object value();
}
