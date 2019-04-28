// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd.set;

import com.google.common.base.Preconditions;
import io.github.merlinosayimwen.crtd.Mergeable;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

/**
 * Replicated set that allows elements to be reinserted after removal by making the last update win.
 * <p>
 * Every element is bound to a {@code Mutation}, which stores information of its recent changes.
 * Elements can be added and removed from the set at every time and will update the elements mutation
 * information's last add (or remove) timestamp to the current time. When checking if an element is
 * inside the set, the last add is compared to the last remove and which of them both occurred more
 * recent, does win.
 *
 * @param <E> Type of the sets elements.
 */
public final class LastWriteWinsElementSet<E> implements ReplicatedSet<E>, Mergeable<LastWriteWinsElementSet<E>> {

  /**
   *
   */
  static final class Mutation {
    private static final long INVALID_MILLIS = 0;
    private static final class Lazy {
      static final Instant EMPTY_INSTANT = Instant.ofEpochMilli(INVALID_MILLIS);
      static final Mutation EMPTY = new Mutation(EMPTY_INSTANT, EMPTY_INSTANT);
    }

    private Instant add;
    private Instant remove;

    private Mutation(Instant add, Instant remove) {
      this.add = add;
      this.remove = remove;
    }

    Instant removeTime() {
      return remove;
    }

    Instant addTime() {
      return add;
    }

    boolean isUpdateWrite() {
      return add.isAfter(remove);
    }

    boolean hasBeenRemoved() {
      return remove.toEpochMilli() == INVALID_MILLIS;
    }

    boolean hasBeenAdded() {
      return add.toEpochMilli() == INVALID_MILLIS;
    }

    Mutation merge(Mutation target) {
      Preconditions.checkNotNull(target);
      return Mutation.create(
        mergeInstant(add, target.add),
        mergeInstant(remove, target.remove)
      );
    }

    private Instant mergeInstant(Instant left, Instant right) {
      return left.isBefore(right) ? right : left;
    }

    static Mutation empty() {
      return Lazy.EMPTY;
    }

    static Mutation create(Instant left, Instant right) {
      Preconditions.checkNotNull(left);
      Preconditions.checkNotNull(right);
      return new Mutation(left, right);
    }

    static Mutation merge(Mutation left, Mutation right) {
      return left.merge(right);
    }
  }

  private Clock clock;
  private Map<E, Mutation> elements;

  @Override
  public void add(E element) {
    Mutation current = getCurrentMutation(element);
    Mutation added =  Mutation.create(clock.instant(), current.removeTime());
    elements.put(element, added);
  }

  @Override
  public boolean remove(E element) {
    Mutation current = getCurrentMutation(element);
    Mutation removed = Mutation.create(current.addTime(), clock.instant());
    elements.put(element, removed);
    return true;
  }

  private Mutation getCurrentMutation(E element) {
    return elements.getOrDefault(element, Mutation.empty());
  }

  private boolean hasTombstone(E element) {
    return getCurrentMutation(element).hasBeenRemoved();
  }

  @Override
  public Set<E> toSet() {
    Set<E> present = new HashSet<>();
    for (Map.Entry<E, Mutation> element : elements.entrySet()) {
      if (element.getValue().isUpdateWrite()) {
        continue;
      }
      present.add(element.getKey());
    }
    return present;
  }

  @Override
  public boolean contains(E value) {
    return getCurrentMutation(value).isUpdateWrite();
  }

  @Override
  public int size() {
    return toSet().size();
  }


  @Override
  public LastWriteWinsElementSet<E> merge(LastWriteWinsElementSet other) {
    Preconditions.checkNotNull(other);
    return this;
  }

  private Instant mergeInstant(Instant left, Instant right) {
    return left.isAfter(right) ? left : right;
  }
}
