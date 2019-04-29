// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

import io.github.merlinosayimwen.crtd.set.TwoPhaseSet;
import junit.framework.TestCase;

/**
 * Tests the TwoPhaseSet methods.
 *
 * @see TwoPhaseSet
 */
public final class TwoPhaseSetTest extends TestCase {

  /**
   * Merges a set with a tombstone for element A, with another
   * set that contains element A and ensures that A is not
   * in the merge result and a tombstone for A exists.
   */
  public void testTombstoneWinsMerge() {
    TwoPhaseSet<String> set = TwoPhaseSet.empty();
    set.remove("a");

    TwoPhaseSet<String> merged = set.merge(TwoPhaseSet.of("a"));
    assertFalse(merged.contains("a"));
    assertTombstoneCreated(merged, "a");
  }

  /**
   * Creates a tombstone for element A, then tries to add A
   * to the set and ensures that it fails by checking whether
   * A is within the set after the call to add.
   */
  public void testRemoveWins() {
    TwoPhaseSet<String> set = TwoPhaseSet.empty();
    set.remove("a");
    assertTombstoneCreated(set, "a");

    set.add("a");
    assertFalse(set.contains("a"));
  }

  /**
   * Removes an element from a set that does not contain the element and
   * ensures that a tombstone is also created if the element is not in
   * the set at the time that {@code remove} is called. This is crucial
   * for coordination free use of this type.
   */
  public void testTombstoneCreation_elementAbsent() {
    testTombstoneCreation(TwoPhaseSet.empty(), "a");
  }

  /**
   * Removes an element from a set that contains the element and
   * ensures that a tombstone is created.
   */
  public void testTombstoneCreation_elementPresent() {
    testTombstoneCreation(TwoPhaseSet.of("a"), "a");
  }

  private void testTombstoneCreation(TwoPhaseSet<String> set, String element) {
    set.remove(element);
    assertTombstoneCreated(set, element);
  }

  private void assertTombstoneCreated(TwoPhaseSet<String> set, String element) {
    assertTrue(set.tombstones().contains(element));
  }

  /**
   * Ignores the parameter. Used to prevent complains from a linter.
   *
   * @param any Parameter that is ignored.
   */
  private static void ignore(Object any) {}
}
