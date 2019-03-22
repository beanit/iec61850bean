/*
 * Copyright 2018 beanit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.openiec61850.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedDefaultThreadFactory implements ThreadFactory {

  private static final AtomicInteger factoryCounter = new AtomicInteger(1);
  private final AtomicInteger threadCounter = new AtomicInteger(1);
  private final String namePrefix;
  private ThreadFactory backingDefaultThreadFactory = Executors.defaultThreadFactory();

  public NamedDefaultThreadFactory(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = backingDefaultThreadFactory.newThread(r);
    String threadName =
        namePrefix
            + "-"
            + factoryCounter.getAndIncrement()
            + "-thread-"
            + threadCounter.getAndIncrement();
    thread.setName(threadName);
    return thread;
  }
}
