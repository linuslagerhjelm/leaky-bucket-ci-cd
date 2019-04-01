import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Implements a leaky bucket data type. Such a data type can be set to monitor an operation and keep track of the
 * rate at which said operation fails. This can be used to detect operations that has too high fail rate during a
 * specific period of time.
 * @param <T>
 */
public final class LeakyBucket<T> {
    static final long COUNTDOWN_RATE = 1000;
    private static Set<LeakyBucket> buckets = null;

    public final long MAX_FAIL;
    private final Consumer<T> operator;
    private AtomicInteger fails = new AtomicInteger(0);


    private LeakyBucket(Consumer<T> operator, long failsPerSecond) {
        this.operator = operator;
        this.MAX_FAIL = failsPerSecond;
    }

    /**
     * Monitors the specified operation and should the method fail rate exceed the provided fail rate,
     * it will throw an exception.
     * @param operation The consumer method to monitor
     * @param failsPerSecond the error rate at, which exceeded, will throw
     * @param <T> the type parameter for the consumer
     * @return a LeakyBucket instance
     */
    public static <T> LeakyBucket<T> monitor(Consumer<T> operation, long failsPerSecond) {
        if (buckets == null) initBucketMonitor();
        LeakyBucket<T> lb =  new LeakyBucket<>(operation, failsPerSecond);
        buckets.add(lb);
        return lb;
    }

    /**
     * Same as {@link LeakyBucket#monitor(Consumer, long)} but defaults to a error rate of 100 failed
     * operations per second.
     * @param operation The consumer method to monitor
     * @param <T> the type parameter for the consumer
     * @return a LeakyBucket instance
     */
    public static <T> LeakyBucket<T> monitor(Consumer<T> operation) {
        return LeakyBucket.monitor(operation, 100);
    }

    /**
     * Makes a monitored call to the stored operation and keeps track of fails. Note that this method may
     * throw two different types of exceptions, for when the monitored method fails and when the allowed fail
     * rate is exceeded.
     * @param arg the argument to pass to the method
     * @throws OperationFailedException thrown when the monitored operation fails, wraps the cause of the error
     * @throws BucketOverflowException thrown when the error rate is exceeded
     */
    public void invoke(T arg) throws OperationFailedException, BucketOverflowException {
        try {
            operator.accept(arg);
        } catch (Throwable t) {
            int count = fails.incrementAndGet();

            if (count < this.MAX_FAIL) throw new OperationFailedException(t);
            else throw new BucketOverflowException();
        }
    }

    /**
     * Get the current fail count. This method should probably not be used except for testing purposes
     * @return current fail count
     */
    int getCurrentFailCount() {
        return fails.get();
    }

    /**
     * De-registers this LeakyBucket instance from the monitor.
     * Could be used to prevent memory leaks.
     */
    public void destroy() {
        buckets.remove(this);
    }

    private static void initBucketMonitor() {
        buckets = new CopyOnWriteArraySet<>();
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(() -> buckets.forEach(LeakyBucket::decreaseFails),
                        COUNTDOWN_RATE, COUNTDOWN_RATE, TimeUnit.MILLISECONDS);
    }

    private void decreaseFails() {
        fails.updateAndGet(i -> i > 0 ? i - 1 : 0);
    }

    /**
     * Thrown when the error rate for the operation monitored by the bucket is exceeded.
     */
    public static class BucketOverflowException extends RuntimeException {
        BucketOverflowException() {}
        BucketOverflowException(String msg) {
            super(msg);
        }
    }

}
