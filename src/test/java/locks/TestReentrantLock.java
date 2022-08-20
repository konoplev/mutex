package locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;

import org.junit.jupiter.api.Test;

import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.WAITING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static util.CpuIntensiveAlgorithm.run100Ms;

public class TestReentrantLock {

  @Test
  public void lockIsExclusive() {
    ReentrantLock lock = new ReentrantLock();
    AtomicBoolean protectedSectionReached = new AtomicBoolean(false);

    Thread child = new Thread(() -> {
      lock.lock();
      protectedSectionReached.set(true);
    });

    //lock is acquired by the main thread
    lock.lock();
    child.start();
    run100Ms();

    assertThat(protectedSectionReached.get(), is(false));
    assertThat(child.getState(), is(WAITING));

    lock.unlock();
    run100Ms();
    assertThat(protectedSectionReached.get(), is(true));
    assertThat(child.getState(), is(TERMINATED));
  }

  @Test
  public void deadLockTest() throws InterruptedException {
    Lock lockA = new ReentrantLock();
    Lock lockB = new ReentrantLock();
    AtomicBoolean protectedSectionReachedByThreadA = new AtomicBoolean(false);
    Thread threadA = new Thread(() -> {
      lockA.lock();
      try {
        run100Ms();
        lockB.lock();
        try {
          protectedSectionReachedByThreadA.compareAndSet(false, true);
        } finally {
          lockB.unlock();
        }
      } finally {
        lockA.unlock();
      }
    });

    AtomicBoolean protectedSectionReachedByThreadB = new AtomicBoolean(false);
    Thread threadB = new Thread(() -> {
      lockB.lock();
      try {
        run100Ms();
        lockA.lock();
        try {
          protectedSectionReachedByThreadB.compareAndSet(false, true);
        } finally {
          lockA.unlock();
        }
      } finally {
        lockB.unlock();
      }
    });

    threadA.start();
    threadB.start();
    threadA.join(200);
    threadB.join(200);
    assertThat(protectedSectionReachedByThreadA.get(), is(false));
    assertThat(protectedSectionReachedByThreadB.get(), is(false));
    assertThat(threadA.getState(), is(WAITING));
    assertThat(threadB.getState(), is(WAITING));

  }

  @Test
  public void testConditionAwait() {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    AtomicBoolean awaitReached = new AtomicBoolean(false);
    AtomicBoolean threadIsAwaken = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      lock.lock();
      try {
        awaitReached.compareAndSet(false, true);
        condition.await();
        threadIsAwaken.compareAndSet(false, true);
      } catch (InterruptedException e) {
        //ignore in this test
      } finally {
        lock.unlock();
      }
    });
    thread.start();
    run100Ms();

    assertThat(awaitReached.get(), is(true));
    assertThat(threadIsAwaken.get(), is(false));

    // to use condition we need to acquire the lock
    // otherwise IllegalMonitorStateException is thrown
    assertThrows(IllegalMonitorStateException.class, condition::signalAll);

    //the thread is waiting for the condition
    //and it releases the lock
    assertThat(lock.tryLock(), is(true));

    // now awake the thread
    condition.signalAll();

    // let's delay a bit to let the thread handle the signal if it's received
    run100Ms();

    // no, the thread is still awaiting the condition because it can't acquire the lock
    assertThat(threadIsAwaken.get(), is(false));
    assertThat(thread.getState(), is(WAITING));

    // now we release the lock
    lock.unlock();

    // let's delay a bit to make sure that the thread has enough time to receive the signal
    run100Ms();

    //now the thread is awakened
    assertThat(threadIsAwaken.get(), is(true));
    assertThat(thread.getState(), is(TERMINATED));
  }

  @Test
  public void signalIsNotReceivedIfThreadBlockedOnLockAcquisitionNotOnAwait() {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    AtomicBoolean awaitReached = new AtomicBoolean(false);
    AtomicBoolean threadIsAwaken = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      lock.lock();
      try {
        awaitReached.compareAndSet(false, true);
        condition.await();
        threadIsAwaken.compareAndSet(false, true);
      } catch (InterruptedException e) {
        //ignore in this test
      } finally {
        lock.unlock();
      }
    });

    //we lock before starting the thread
    lock.lock();

    // now we start the thread
    thread.start();
    run100Ms();

    // and it's blocked on the lock acquisition not awaiting the condition
    assertThat(awaitReached.get(), is(false));
    assertThat(threadIsAwaken.get(), is(false));

    // we signal now
    condition.signalAll();
    // and release the lock
    lock.unlock();

    run100Ms();

    // now the thread is able to acquire the lock, start waiting but signal has already been sent and nobody was listening
    assertThat(awaitReached.get(), is(true));

    // so the thread is not awaken
    assertThat(threadIsAwaken.get(), is(false));
    assertThat(thread.getState(), is(WAITING));
  }

}
