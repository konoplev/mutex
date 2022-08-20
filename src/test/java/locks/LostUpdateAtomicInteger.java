package locks;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest;
import org.junit.jupiter.api.Test;

@StressCTest
public class LostUpdateAtomicInteger {
  private static class Counter {

    private AtomicInteger count = new AtomicInteger(0);

    public int getCount() {
      return count.get();
    }

    public void increment() {
      // wrong implementation
      count.set(count.get() + 1);
      count.incrementAndGet();
    }
  }

  private Counter c = new Counter();

  @Operation
  public void inc() {
    c.increment();
  }

  @Operation
  public int get() {
    return c.getCount();
  }

  @Test
  public void test() {
    LinChecker.check(LostUpdateAtomicInteger.class);
  }

}
