package io.github.merlinosayimwen.crdt;

import org.junit.Test;

import io.github.merlinosayimwen.crdt.set.GrowOnlySet;

import static org.junit.Assert.assertFalse;
import static io.github.merlinosayimwen.crdt.ReplicatedSetTests.assertContainsAll;

/**
 * Tests for the GrowOnlySet which ensure that the classes contract
 * is followed and all operations function as expected.
 */
public class GrowOnlySetTest {

  /**
   * Creates a set with some elements, asserts that the {@code clear()} method
   * returns false and asserts that all initial elements are still in the set.
   */
  @Test
  public void testClearFails() {
    GrowOnlySet<String> set = GrowOnlySet.of("a", "b", "c");

    assertFalse(set.clear());
    assertContainsAll(set, "a", "b", "c");
  }
}
