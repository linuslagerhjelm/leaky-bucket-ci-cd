package LeakyBucket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeakyBucketTest {

    @Test
    void basicTest() {
        var bucket = LeakyBucket.monitor(System.out::println);

        bucket.invoke("Hello world");
    }

    @Test
    void failsAreIncreasedOnError() {
        var bucket = LeakyBucket.monitor(__ -> { throw new RuntimeException(""); });

        try {
            bucket.invoke("");
        } catch (OperationFailedException ignore) {}

        assertEquals(1, bucket.getCurrentFailCount());
    }

    @Test
    void failsAreDecreasedPeriodically() throws InterruptedException {
        var bucket = LeakyBucket.monitor(__ -> { throw new RuntimeException(""); });

        try {
            bucket.invoke("");
        } catch (OperationFailedException ignore) {}

        Thread.sleep(LeakyBucket.COUNTDOWN_RATE + 1);

        assertEquals(0, bucket.getCurrentFailCount());
    }

    @Test
    void tooHighErrorRateCausesBucketToOverflow() {
        var bucket = LeakyBucket.monitor(__ -> { throw new RuntimeException(""); }, 10);
        boolean thrown = false;

        for (int i = 0; i <= 10; ++i) {
            try {
                    bucket.invoke("");
            }
            catch (OperationFailedException ignore) {}
            catch (LeakyBucket.BucketOverflowException e) {
                thrown = true;
            }
        }

        assertTrue(thrown);
    }

}