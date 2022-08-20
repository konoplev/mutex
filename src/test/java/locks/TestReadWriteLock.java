package locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static util.CpuIntensiveAlgorithm.run1s;

public class TestReadWriteLock {

  @Test
  public void readIsNotExclusive() throws InterruptedException {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    lock.readLock().lock();
    AtomicBoolean dontNeedToWaitForUnlock = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      lock.readLock().lock();
      dontNeedToWaitForUnlock.compareAndSet(false, true);

    });
    thread.start();
    thread.join(100);
    assertThat(dontNeedToWaitForUnlock.get(), is(true));
  }

  @Test
  public void writeIsExclusive() throws InterruptedException {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    lock.writeLock().lock();
    AtomicBoolean reachedByConcurrentWriter = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      lock.writeLock().lock();
      reachedByConcurrentWriter.compareAndSet(false, true);

    });
    thread.start();
    thread.join(100);
    assertThat(reachedByConcurrentWriter.get(), is(false));
    lock.writeLock().unlock();
    thread.join(100);
    assertThat(reachedByConcurrentWriter.get(), is(true));
  }

  @Test
  public void readWaitsForWrite() throws InterruptedException {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    lock.writeLock().lock();
    AtomicBoolean reachedByConcurrentReader = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      lock.readLock().lock();
      reachedByConcurrentReader.compareAndSet(false, true);

    });
    thread.start();
    thread.join(100);
    assertThat(reachedByConcurrentReader.get(), is(false));
    lock.writeLock().unlock();
    thread.join(100);
    assertThat(reachedByConcurrentReader.get(), is(true));
  }

  @Test
  public void writeWaitsForRead() throws InterruptedException {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    lock.readLock().lock();
    AtomicBoolean reachedByConcurrentWriter = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      lock.writeLock().lock();
      reachedByConcurrentWriter.compareAndSet(false, true);

    });
    thread.start();
    thread.join(100);
    assertThat(reachedByConcurrentWriter.get(), is(false));
    lock.readLock().unlock();
    thread.join(100);
    assertThat(reachedByConcurrentWriter.get(), is(true));
  }

  @Test
  public void writeWaitsOnlyForReadersStartedBeforeTheWriteAttempt() throws InterruptedException {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    lock.readLock().lock();
    AtomicBoolean reachedByConcurrentWriter = new AtomicBoolean(false);
    Thread writerThread = new Thread(() -> {
      lock.writeLock().lock();
      reachedByConcurrentWriter.compareAndSet(false, true);

    });
    writerThread.start();
    writerThread.join(100);
    assertThat(reachedByConcurrentWriter.get(), is(false));

    //now start a new reader thread
    AtomicBoolean readerThreadAllowedToAcquireReadLockEvenIfThereIsWriterWaiting = new AtomicBoolean(false);
    Thread readerThread = new Thread(() -> {
      lock.readLock().lock();
      try {
        readerThreadAllowedToAcquireReadLockEvenIfThereIsWriterWaiting.compareAndSet(false, true);
        // if the lock acquisition is successful, the writer thread is blocked by the reading running for long
        run1s();
      } finally {
        lock.readLock().unlock();
      }
    });
    readerThread.start();
    lock.readLock().unlock();
    writerThread.join(100);
    assertThat(reachedByConcurrentWriter.get(), is(true));
    assertThat(readerThreadAllowedToAcquireReadLockEvenIfThereIsWriterWaiting.get(), is(false));
  }

}
