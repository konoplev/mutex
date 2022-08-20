package phases;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class PhaseSync {

  final Lock lock = new ReentrantLock();
  final Condition phaseIsDone = lock.newCondition();
  Phases currentPhase = Phases.FIRST;

  public void phase(Phases phase, Runnable execution) {
    lock.lock();
    try {
      while (currentPhase != phase) {
        if (!phaseIsDone.await(10, TimeUnit.SECONDS)) {
          throw new RuntimeException("Timeout waiting for " + phase);
        }
        System.out.println(phase + " awakened but current phase is " + currentPhase);
      }
      execution.run();
      if (currentPhase.hasNext()) {
        currentPhase = currentPhase.next();
      }
      phaseIsDone.signalAll();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted");
    } finally {
      lock.unlock();
    }
  }

  public enum Phases {
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    FIFTH,
    SIXTH,
    SEVENTH,
    EIGHTH,
    NINTH,
    TENTH;

    public boolean hasNext() {
      return next() != null;
    }

    public Phases next() {
      return Phases.values()[ordinal() + 1];
    }
  }

}
