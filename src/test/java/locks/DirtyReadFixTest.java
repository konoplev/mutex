package locks;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest;
import org.junit.jupiter.api.Test;

@StressCTest
public class DirtyReadFixTest {

  public static class StringAndNumber {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    private int number = 0;
    private String string = "0";
    public int getNumber() {
      lock.readLock().lock();
      try {
        return number;
      } finally {
        lock.readLock().unlock();
      }
    }

    public String getString() {
      lock.readLock().lock();
      try {
        return string;
      } finally {
        lock.readLock().unlock();
      }
    }

    public void increment() {
      lock.writeLock().lock();
      try {
        number++;
        string = String.valueOf(number);
      } finally {
        lock.writeLock().unlock();
      }
    }


  }
  StringAndNumber counter = new StringAndNumber();

  @Operation
  public void increment() {
    counter.increment();
  }

  @Operation
  public String getSting() {
    return counter.getString();
  }

  @Operation
  public int getNumber() {
    return counter.getNumber();
  }

  @Test
  public void test() {
    LinChecker.check(DirtyReadFixTest.class);
  }
}
