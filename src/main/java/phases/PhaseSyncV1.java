package phases;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * This class is used to sync two threads called from the main thread to let the treads
 * execute thing sequentially. It's needed to simulate various race conditions and be able
 * to see them from each thread's perspective.
 */

public class PhaseSyncV1 {
  final Lock lock = new ReentrantLock();
  final Condition phaseIsDone = lock.newCondition();
  private String currentPhase = "";
  private final Condition allDone = lock.newCondition();

  public void waitFor(String phaseName) {
    lock.lock();
    try {
      while (currentPhase.isEmpty()) {
        if (!phaseIsDone.await(1, TimeUnit.SECONDS)) {
          throw new RuntimeException("Timeout waiting for"  + phaseName);
        }
      }
      if (!phaseName.equals(currentPhase)) {
        throw new IllegalStateException("Expected phase " + phaseName + " but got " + currentPhase);
      }
      currentPhase = "";
    } catch (InterruptedException e) {
      throw new RuntimeException("Thread interrupted");
    } finally {
      lock.unlock();
    }
  }

  public void doneWith(String phaseName) {
    lock.lock();
    try {
      if (!currentPhase.isEmpty()) {
        throw new IllegalStateException("Already in phase " + currentPhase);
      }
      currentPhase = phaseName;
      phaseIsDone.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public void done() {
    lock.lock();
    try {
      allDone.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public void waitForAllPhases() {
    lock.lock();
    try {
      boolean await = allDone.await(10, TimeUnit.SECONDS);
      if (!await) {
        throw new IllegalStateException("Timeout waiting for all phases. Current phase is " + currentPhase);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

}
