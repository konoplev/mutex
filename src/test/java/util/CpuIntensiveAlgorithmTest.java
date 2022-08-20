package util;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CpuIntensiveAlgorithmTest {

  @Test
  public void testThereIsANumberToRunFor100Ms() {
    assertDoesNotThrow(() -> {
      long boundThatIsBigEnoughToKeepCpuBusyFor = CpuIntensiveAlgorithm.getBoundThatIsBigEnoughToKeepCpuBusyFor(
          TimeUnit.MILLISECONDS.toMillis(100));
      System.out.println(boundThatIsBigEnoughToKeepCpuBusyFor);
    });

  }

  @Test
  public void testThereIsANumberToRunFor1s() {
    assertDoesNotThrow(() -> {
      long boundThatIsBigEnoughToKeepCpuBusyFor = CpuIntensiveAlgorithm.getBoundThatIsBigEnoughToKeepCpuBusyFor(
          TimeUnit.SECONDS.toMillis(1));
      System.out.println(boundThatIsBigEnoughToKeepCpuBusyFor);
    });
  }

  @Test
  public void testRun100Ms() {
    long begin = System.currentTimeMillis();
    CpuIntensiveAlgorithm.run100Ms();
    long end = System.currentTimeMillis();
    assertThat(end - begin,
        is(
            both(
                greaterThan(TimeUnit.MILLISECONDS.toMillis(95)))
                .and(lessThanOrEqualTo(TimeUnit.MILLISECONDS.toMillis(130)))
          )
              );
  }
}
