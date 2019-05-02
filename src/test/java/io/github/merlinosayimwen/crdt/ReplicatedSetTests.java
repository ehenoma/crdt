package io.github.merlinosayimwen.crdt;

import io.github.merlinosayimwen.crdt.set.ReplicatedSet;
import io.github.merlinosayimwen.crdt.set.TwoPhaseSet;
import junit.framework.AssertionFailedError;

public final class ReplicatedSetTests {
  private ReplicatedSetTests() {}

  public static <E> void assertContains(ReplicatedSet<E> set, E element) {
    if (!set.contains(element)) {
      throw new AssertionFailedError("set does not contain element " + element.toString());
    }
  }

  public static <E> void assertContainsAll(ReplicatedSet<E> set, E... elements) {
    for (E element : elements) {
      assertContains(set, element);
    }
  }

  public static <E> void assertContains(ReplicatedSet<E> set, Iterable<E> elements) {
    for (E element : elements) {
      assertContains(set, element);
    }
  }
}
