// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

import java.util.Map;

/**
 * @param <K> Type of the id that identifies a node.
 */
public class GrowOnlyCounter<K> implements ReplicatedCounter<GrowOnlyCounter<K>> {
  private Map<K, Long> summands;

  @Override
  public Long value() {
    return null;
  }

  @Override
  public GrowOnlyCounter<K> merge(GrowOnlyCounter<K> other) {
    return null;
  }
}
