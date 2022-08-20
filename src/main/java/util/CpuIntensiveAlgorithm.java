package util;

/**
 * To test some concurrent operations sometimes a long-running function that keeps a thread busy for a while is needed.
 * We use some not very efficient algorithm for finding prime numbers to implement it and give a big enough number to occupy
 * the CPU for a desired amount of time.
 */
public class CpuIntensiveAlgorithm {

  public static void run100Ms() {
    // Value from util.CpuIntensiveAlgorithmTest.testThereIsANumberToRunFor100Ms
    findAllPrimesBefore(15121);
  }

  public static void run1s() {
    // Value from util.CpuIntensiveAlgorithmTest.testThereIsANumberToRunFor1s
    findAllPrimesBefore(52021);
  }

  static long getBoundThatIsBigEnoughToKeepCpuBusyFor(long milliseconds) {
    long nextPrime = 1L;
    long start = System.currentTimeMillis();
    while (nextPrime < Long.MAX_VALUE) {
      nextPrime = getNextPrime(nextPrime);
      if (System.currentTimeMillis() - start > milliseconds) {
        return nextPrime;
      }
    }
    throw new RuntimeException(
        "Could not find a prime number that is big enough to keep CPU busy for " + milliseconds + " milliseconds");
  }

  private static void findAllPrimesBefore(long rightBound) {
    long nextPrime = 1L;
    while (nextPrime < rightBound) {
      nextPrime = getNextPrime(nextPrime);
    }
  }

  private static long getNextPrime(long previous) {
    long potentiallyPrime = previous + 1;
    while (true) {
      int i = 2;
      for (; i < potentiallyPrime; i++) {
        if (potentiallyPrime % i == 0) {
          break;
        }
      }
      //we found prime
      if (potentiallyPrime == i) {
        return potentiallyPrime;
      }
      potentiallyPrime++;
    }
  }
}
