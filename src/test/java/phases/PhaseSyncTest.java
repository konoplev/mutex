package phases;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;
import phases.PhaseSync.Phases;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhaseSyncTest {
  PhaseSync phaseSync = new PhaseSync();

  @Test
  void usageExample() {

    final AtomicReference<String> stages = new AtomicReference<>("");
    BinaryOperator<String> append = (a, b) -> a + b;
    CompletableFuture.runAsync(() -> {
      p(Phases.SECOND, () -> stages.getAndAccumulate("2", append));
      p(Phases.FOURTH, () -> stages.getAndAccumulate("4", append));
    });
    CompletableFuture.runAsync(() -> {
      p(Phases.FIRST, () -> stages.getAndAccumulate("1", append));
      p(Phases.THIRD, () -> stages.getAndAccumulate("3", append));
    });
    p(Phases.FIFTH, () -> {});
    assertEquals("1234", stages.get());
  }

  private void p(Phases second, Runnable runnable) {
    phaseSync.phase(second, runnable);
  }

}
