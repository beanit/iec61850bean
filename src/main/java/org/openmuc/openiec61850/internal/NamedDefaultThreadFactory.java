package org.openmuc.openiec61850.internal;

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
