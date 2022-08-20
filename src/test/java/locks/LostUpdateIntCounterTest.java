package locks;

import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest;
import org.junit.jupiter.api.Test;

@StressCTest
public class LostUpdateIntCounterTest {

  private static class Counter {

    private int count = 0;

    public int getCount() {
      return count;
    }

    public void increment() {
      count++;
      //long version:
      // int localCount = count;
      // localCount = localCount + 1;
      // count = localCount;

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
    LinChecker.check(LostUpdateIntCounterTest.class);
  }

}
