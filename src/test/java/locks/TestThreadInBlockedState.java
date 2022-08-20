package locks;

import java.lang.Thread.State;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestThreadInBlockedState {
  @Test
  public void testThreadInBlockingState() throws InterruptedException {
    final Object lock = new Object();
    Thread thread = new Thread(() -> {
      synchronized(lock){
        //do something here
      }
    });
    synchronized (lock){
      thread.start();
      thread.join(100);
      assertThat(thread.getState(), is(State.BLOCKED));
      thread.interrupt();
      thread.join(100);
      assertThat(thread.getState(), is(State.BLOCKED));
    }
  }

  @Test
  public void testInterruptWaitingThread() throws InterruptedException {
    Lock lock = new ReentrantLock();
    AtomicBoolean interrupted = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      try {
        lock.lockInterruptibly();
      } catch (InterruptedException e) {
        interrupted.compareAndSet(false, true);
        return;
      }
      try {
        //do something here
      } finally {
        lock.unlock();
      }
    });
    lock.lock();
    try {
      thread.start();
      thread.join(100);
      assertThat(thread.getState(), is(State.WAITING));
      thread.interrupt();
      thread.join(100);
      assertThat(interrupted.get(), is(true));
      assertThat(thread.getState(), is(State.TERMINATED));
    } finally {
      lock.unlock();
    }
  }
}
