package com.linkedin.venice.kafka.consumer;

import com.linkedin.venice.utils.TestUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MemoryBoundBlockingQueueTest {

  private static class MeasurableObject implements Measurable {
    public static final int size = 10;
    @Override
    public int getSize() {
      return this.size;
    }
  }

  @Test
  public void testPut() throws InterruptedException {
    int memoryCap = 5000;
    MemoryBoundBlockingQueue<MeasurableObject> queue = new MemoryBoundBlockingQueue<>(memoryCap, 1000);
    int objectCntAtMost = memoryCap / (MemoryBoundBlockingQueue.LINKED_QUEUE_NODE_OVERHEAD_IN_BYTE + MeasurableObject.size);
    Thread t = new Thread(() -> {
        while (true) {
          try {
            queue.put(new MeasurableObject());
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
    });
    t.start();
    Thread.sleep(50);
    Assert.assertTrue(t.isAlive());
    Assert.assertEquals(queue.size(), objectCntAtMost);
    t.interrupt();
  }

  @Test
  public void testTake() throws InterruptedException {
    int memoryCap = 5000;
    MemoryBoundBlockingQueue<MeasurableObject> queue = new MemoryBoundBlockingQueue<>(memoryCap, 1000);
    int objectCntAtMost = memoryCap / (MemoryBoundBlockingQueue.LINKED_QUEUE_NODE_OVERHEAD_IN_BYTE + MeasurableObject.size);
    for (int i = 0; i < objectCntAtMost; ++i) {
      queue.put(new MeasurableObject());
    }
    AtomicInteger objectTakenNum = new AtomicInteger(0);
    Thread t = new Thread(() -> {
      while (true) {
        try {
          queue.take();
          objectTakenNum.addAndGet(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    t.start();
    Thread.sleep(50);
    Assert.assertTrue(t.isAlive());
    Assert.assertEquals(objectTakenNum.get(), objectCntAtMost);
    Assert.assertEquals(queue.size(), 0);
    t.interrupt();
  }

  @Test
  public void testThrottling() throws InterruptedException {
    int memoryCap = 5000;
    int notifyDelta = 1000;
    MemoryBoundBlockingQueue<MeasurableObject> queue = new MemoryBoundBlockingQueue<>(memoryCap, notifyDelta);
    int objectCntAtMost = memoryCap / (MemoryBoundBlockingQueue.LINKED_QUEUE_NODE_OVERHEAD_IN_BYTE + MeasurableObject.size);
    Thread t = new Thread(() -> {
      while (true) {
        try {
          queue.put(new MeasurableObject());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    t.start();
    TestUtils.waitForNonDeterministicAssertion(5, TimeUnit.SECONDS, () -> {
      Assert.assertTrue(t.isAlive());
      Assert.assertEquals(queue.size(), objectCntAtMost);
    });

    int previousQueueSize = queue.size();
    // Here we need to take out some objects to allow more put
    double objectCntTakenAtLeast = Math.ceil((double)notifyDelta / (MemoryBoundBlockingQueue.LINKED_QUEUE_NODE_OVERHEAD_IN_BYTE + MeasurableObject.size));
    for (int i = 1; i < objectCntTakenAtLeast; ++i) {
      queue.take();
      Assert.assertEquals(queue.size(), previousQueueSize - 1);
      --previousQueueSize;
    }
    // This will trigger a notification, which will allow more puts
    queue.take();
    Thread.sleep(50);
    Assert.assertTrue(t.isAlive());
    Assert.assertEquals(queue.size(), objectCntAtMost);

    t.interrupt();
  }
}
