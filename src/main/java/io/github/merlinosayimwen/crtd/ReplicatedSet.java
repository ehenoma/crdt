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

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;

/**
 * Immutable set that can be replicated.
 *
 * @see Replicated
 * @see TwoPhaseSet
 * @param <V> Type of the sets elements.
 * @param <T> Type of the implementation.
 */
@Immutable
public interface ReplicatedSet<V, T extends ReplicatedSet<V, T>> extends Replicated<T> {

  @CheckReturnValue
  T add(V element);

  @CheckReturnValue
  T remove(V element);

  boolean contains(V value);

  int size();

  Collection<V> value();
}
