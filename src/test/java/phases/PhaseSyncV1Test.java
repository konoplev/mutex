package phases;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhaseSyncV1Test {
  @Test
  void usageExample() {
    PhaseSyncV1 phaseSync = new PhaseSyncV1();
    final AtomicReference<String> stages = new AtomicReference<>("");
    BinaryOperator<String> accumulator = (existing, newValue) -> existing + newValue;
    CompletableFuture.runAsync(() -> {
      phaseSync.waitFor("first");
      stages.getAndAccumulate("2", accumulator);
      phaseSync.doneWith("second");
      phaseSync.waitFor("third");
      stages.getAndAccumulate("4", accumulator);
      phaseSync.done();
    });
    CompletableFuture.runAsync(() -> {
      stages.getAndAccumulate("1", accumulator);
      phaseSync.doneWith("first");
      phaseSync.waitFor("second");
      stages.getAndAccumulate("3", accumulator);
      phaseSync.doneWith("third");
    });
    phaseSync.waitForAllPhases();
    assertEquals("1234", stages.get());
  }

  @Test
  public void noErrorsIfSwitchAndWaitAreInSameThread() {
    assertDoesNotThrow(() -> {
      PhaseSyncV1 phaseSync = new PhaseSyncV1();
      phaseSync.doneWith("first");
      phaseSync.waitFor("first");
      phaseSync.done();
    });
  }

  @Test
  public void errorIfDoneIsNotCalled() {
    assertThrows(IllegalStateException.class, () -> {
      PhaseSyncV1 phaseSyncV1 = new PhaseSyncV1();
      phaseSyncV1.doneWith("first");
      phaseSyncV1.waitFor("first");
      phaseSyncV1.waitForAllPhases();
    });
  }

  @Test
  public void errorIfWaitingForPhaseThatHasNotStarted() {
    assertThrows(RuntimeException.class, () -> {
      PhaseSyncV1 phaseSyncV1 = new PhaseSyncV1();
      phaseSyncV1.waitFor("first");
    });
  }

}
