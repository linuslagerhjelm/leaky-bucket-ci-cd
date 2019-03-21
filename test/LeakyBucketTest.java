import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeakyBucketTest {

    @Test
    void basicTest() {
        @SuppressWarnings("unchecked")
        LeakyBucket<String> bucket = LeakyBucket.monitor(System.out::println);

        bucket.invoke("Hello world");
    }

    @Test
    void failsAreIncreasedOnError() {
        @SuppressWarnings("unchecked")
        LeakyBucket<String> bucket = LeakyBucket.monitor(__ -> { throw new RuntimeException(""); });

        try {
            bucket.invoke("");
        } catch (OperationFailedException ignore) {}

        assertEquals(1, bucket.getCurrentFailCount());
    }

    @Test
    void failsAreDecreasedPeriodically() throws InterruptedException {
        @SuppressWarnings("unchecked")
        LeakyBucket<String> bucket = LeakyBucket.monitor(__ -> { throw new RuntimeException(""); });

        try {
            bucket.invoke("");
        } catch (OperationFailedException ignore) {}

        Thread.sleep(LeakyBucket.COUNTDOWN_RATE + 1);

        assertEquals(0, bucket.getCurrentFailCount());
    }

}