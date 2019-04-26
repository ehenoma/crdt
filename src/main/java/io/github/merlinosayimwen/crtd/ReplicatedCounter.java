// Copyright 2019 Merlin Osayimwen. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE file.

package io.github.merlinosayimwen.crtd;

public interface ReplicatedCounter<T extends ReplicatedCounter<T>> extends Replicated<T> {

  Long value(); // Has to be of type object
}