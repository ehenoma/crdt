// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

import io.github.merlinosayimwen.crtd.set.ReplicatedSet;
import io.github.merlinosayimwen.crtd.set.TwoPhaseSet;
import junit.framework.TestCase;

/**
 * Tests the TwoPhaseSet methods.
 *
 * @see TwoPhaseSet
 */
public final class TwoPhaseSetTest extends TestCase {

  /**
   * Calls {@link ReplicatedSet#remove(Object)} ona set and asserts that the element has not been
   * removed from the instance and is not present in the returned updated set.
   */
  public void testTombstones() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b", "c");
    assertTrue(set.contains("a"));

    set.remove("a");
    assertFalse(set.contains("a"));
    assertTrue(set.tombstones().contains("a"));
  }

  /**
   * Ignores the parameter. Used to prevent complains from a linter.
   *
   * @param any Parameter that is ignored.
   */
  private static void ignore(Object any) {}
}
