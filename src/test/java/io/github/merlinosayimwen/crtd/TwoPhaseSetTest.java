// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

import junit.framework.TestCase;

/**
 * Tests the TwoPhaseSet methods.
 *
 * @see TwoPhaseSet
 */
public final class TwoPhaseSetTest extends TestCase {

  /**
   * Calls {@link ReplicatedSet#add(Object)} on a set and asserts that it has not modified the
   * instances state.
   */
  public void testImmutability() {
    TwoPhaseSet<String> set = TwoPhaseSet.empty();
    ignore(set.add("test"));

    assertEquals(set.size(), 0);
    assertEquals(set.value().size(), 0);
  }

  /**
   * Calls {@link ReplicatedSet#remove(Object)} ona set and asserts that the element has not been
   * removed from the instance and is not present in the returned updated set.
   */
  public void testTombstones() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b", "c");
    TwoPhaseSet<String> updated = set.remove("a");

    assertTrue(set.contains("a"));
    assertFalse(updated.contains("a"));
    assertTrue(updated.tombstones().contains("a"));
  }

  /**
   * Ignores the parameter. Used to prevent complains from a linter.
   *
   * @param any Parameter that is ignored.
   */
  private static void ignore(Object any) {}
}
