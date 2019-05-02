// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crdt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.jparams.verifier.tostring.ToStringVerifier;
import io.github.merlinosayimwen.crdt.set.TwoPhaseSet;
import org.junit.Test;

import com.google.common.collect.Lists;


import static io.github.merlinosayimwen.crdt.ReplicatedSetTests.assertContains;
import static org.junit.Assert.*;

/**
 * Tests the TwoPhaseSet methods.
 *
 * @see TwoPhaseSet
 */
public final class TwoPhaseSetTest {

  /**
   * Creates a TwoPhaseSet from a collection of distinct strings and asserts
   * that the set contains all of the elements.
   */
  @Test
  public void testCreation() {
    Collection<String> initialElements = Lists.newArrayList("a", "b", "c");
    TwoPhaseSet<String> set = TwoPhaseSet.of(initialElements);
    assertContains(set, initialElements);
  }

  /**
   * Creates a TwoPhaseSet from a collection and asserts that modifications
   * to the collection do not affect the set.
   */
  @Test
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
  @Test
  public void testCreation_nullElement() {
    Collection<String> initialElements = Lists.newArrayList("a", "b", null);
    try {
      TwoPhaseSet.of(initialElements);
      fail("Expected TwoPhaseSet#of to throw NPE on null element in argument list");
    } catch (NullPointerException expected) {
      // This exception is expected
    }
  }

  /**
   * Asserts that the toString() method is returning an accurate representation.
   */
  @Test
  public void testToString() {
    ToStringVerifier
      .forClass(TwoPhaseSet.class)
      .withPrefabValue(TwoPhaseSet.class, TwoPhaseSet.empty())
      .verify();
  }

  @Test
  public void testToSet() {
    TwoPhaseSet<String> twoPhaseSet = TwoPhaseSet.of("a", "b", "c");
    twoPhaseSet.remove("c");

    Set<String> set = twoPhaseSet.toSet();
    for (String element : twoPhaseSet.addedElements()) {
      if (twoPhaseSet.contains(element)) {
        assertTrue(set.contains(element));
      } else {
        assertFalse(set.contains(element));
      }
    }
  }

  /**
   * Creates a set with some elements, clears it and asserts that
   * all a tombstone for all the initial elements has been created
   * and that the sets {@code size()} method returns 0.
   */
  @Test
  public void testClear() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b" ,"c");
    assertEquals(3, set.size());

    assertTrue(set.clear());
    assertEquals(0, set.size());
    assertTombstonesCreated(set, "a", "b", "c");
  }

  /**
   * Creates a set with N different initial elements and asserts that the
   * number returned by {@code size()} is N.
   */
  @Test
  public void testSize_differentElements() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b", "c");
    assertEquals(3, set.size());
  }

  /**
   * Creates a set with initial elements that contain duplications and
   * asserts that the size equals the amount of distinct elements.
   */
  @Test
  public void testSize_equalElements() {
    TwoPhaseSet<String> set = TwoPhaseSet.of("a", "b", "b");
    assertEquals(2, set.size());
  }

  /**
   * Adds an element to a set with no tombstone of that element and
   * asserts that the element has been added successfully.
   */
  @Test
  public void testAdd_noTombstone() {
    TwoPhaseSet<String> set = TwoPhaseSet.empty();
    assertEquals(0, set.size());

    set.add("a");
    assertContains(set, "a");
  }

  /**
   * Merges a set with a tombstone for element A, with another
   * set that contains element A and ensures that A is not
   * in the merge result and a tombstone for A exists.
   */
  @Test
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
  @Test
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
  @Test
  public void testTombstoneCreationAtInitialization() {
    TwoPhaseSet<String> set = TwoPhaseSet.empty();
    assertEquals(0, set.tombstones().size());
  }

  /**
   * Removes an element from a set that does not contain the element and
   * ensures that a tombstone is also created if the element is not in
   * the set at the time that {@code remove} is called. This is crucial
   * for coordination free use of this type.
   */
  @Test
  public void testTombstoneCreation_elementAbsent() {
    testTombstoneCreation(TwoPhaseSet.empty(), "a");
  }

  /**
   * Removes an element from a set that contains the element and
   * ensures that a tombstone is created.
   */
  @Test
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

  private void assertTombstonesCreated(TwoPhaseSet<String> set, String ...elements) {
    for (String element : elements) {
      assertTombstoneCreated(set, element);
    }
  }
}