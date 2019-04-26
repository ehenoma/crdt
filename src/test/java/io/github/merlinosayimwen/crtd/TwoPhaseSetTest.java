//  Copyright (c) 2019 Merlin Osayimwen
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
//  IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

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
