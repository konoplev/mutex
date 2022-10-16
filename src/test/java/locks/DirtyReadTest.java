package locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest;
import org.junit.jupiter.api.Test;

@StressCTest
public class DirtyReadTest {

  public static class StringAndNumber {

    private int number = 0;
    private String string = "0";
    private Lock lock = new ReentrantLock();
    public int getNumber() {
      return number;
    }

    public String getString() {
      return string;
    }

    public void increment() {
      lock.lock();
      try {
        number++;
        string = String.valueOf(number);
      } finally {
        lock.unlock();
      }
    }
  }
  StringAndNumber counter = new StringAndNumber();

  @Operation
  public void increment() {
    counter.increment();
  }

  @Operation
  public String getString() {
    return counter.getString();
  }

  @Operation
  public int getNumber() {
    return counter.getNumber();
  }

  @Test
  public void test() {
    LinChecker.check(DirtyReadTest.class);
  }
}
