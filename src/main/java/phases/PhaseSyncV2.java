package phases;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PhaseSyncV2 {
  private PhaseSyncV1 phaseSync = new PhaseSyncV1();
  private Lock lock = new ReentrantLock();

  public void waitFor(String phaseName) {
    lock.lock();
    try {
      phaseSync.waitFor(phaseName);
    } finally {
      lock.unlock();
    }
  }

  public void doneWith(String phaseName) {
    phaseSync.doneWith(phaseName);
  }

  public void done() {
    phaseSync.done();
  }

  public void waitForAllPhases() {
    phaseSync.waitForAllPhases();
  }
}

