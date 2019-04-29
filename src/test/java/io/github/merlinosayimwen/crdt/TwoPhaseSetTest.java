// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crdt;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

import io.github.merlinosayimwen.crdt.set.TwoPhaseSet;

/**
 * Tests the TwoPhaseSet methods.
 *
 * @see TwoPhaseSet
 */
public final class TwoPhaseSetTest extends TestCase {

  /**
   * Creates a TwoPhaseSet from a collection of distinct strings and asserts
   * that the set contains all of the elements.
   */
  public void testCreation() {
    Collection<String> initialElements = Lists.newArrayList("a", "b", "c");
    TwoPhaseSet<String> set = TwoPhaseSet.of(initialElements);
    assertContains(set, initialElements);
  }

  /**
   * Creates a TwoPhaseSet from a collection and asserts that modifications
   * to the collection do not affect the set.
   */
  public void testDefensive() {
    Collection<String> initial = new ArrayList<>();
    TwoPhaseSet<String> set = TwoPhaseSet.of(initial);
    // The TwoPhaseSet has to make a defensive copy of the initial-elements
    // arguments. Therefor adding an element to the initial-elements should
    // not add it to the sets elements.
    initial.add("d");
    assertFalse(set.contains("d"));
  }

  /**
   * Tries to create a TwoPhaseSet from a list with a null element and
   * asserts that a NullPointerException is thrown.
   */
  public void testCreation_nullElement() {
    Collection<String> initialElements = Lists.newArrayList("a", "b", null);
    try {
      TwoPhaseSet.of(initialElements);
      fail("Expected TwoPhaseSet#of to throw NPE on null element in argument list");
    } catch (NullPointerException expected) {
      // This exception is expected
    }
  }

  private void assertContains(TwoPhaseSet<String> set, Collection<String> elements) {
    for (String element : elements) {
      assertTrue(set.contains(element));
    }
  }

  /**
   * Creates a set with N different initial elements and asserts that the
   * number returned by {@code size()} is N.
   */
  public void testSize_differentElements() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b", "c");
    assertEquals(set.size(), 3);
  }

  /**
   * Creates a set with initial elements that contain duplications and
   * asserts that the size equals the amount of distinct elements.
   */
  public void testSize_equalElements() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b", "b");
    assertEquals(set.size(), 2);
  }

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
   * Creates a new empty set and asserts that it has no tombstones.
   */
  public void testTombstoneCreationAtInitialization() {
    TwoPhaseSet<String> set = TwoPhaseSet.empty();
    assertEquals(set.tombstones().size(), 0);
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