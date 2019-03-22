import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    public static final long COUNTDOWN_RATE = 1000;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Set<LeakyBucket> buckets = null;

    public final long MAX_FAIL;
    private final Consumer<T> operator;
    private AtomicInteger fails = new AtomicInteger(0);


    private LeakyBucket(Consumer<T> operator, long failsPerSecond) {
        this.operator = operator;
        this.MAX_FAIL = failsPerSecond;
    }

    public static <T> LeakyBucket<T> monitor(Consumer<T> operation, long failsPerSecond) {
        if (buckets == null) initBucketMonitor();
        LeakyBucket<T> lb =  new LeakyBucket<>(operation, failsPerSecond);
        buckets.add(lb);
        return lb;
    }

    public static <T> LeakyBucket<T> monitor(Consumer<T> operation) {
        return LeakyBucket.monitor(operation, 100);
    }

    public void invoke(T arg) throws OperationFailedException, BucketOverflowException {
        try {
            operator.accept(arg);
        } catch (Throwable t) {
            int count = fails.incrementAndGet();

            if (count < this.MAX_FAIL) throw new OperationFailedException(t);
            else throw new BucketOverflowException();
        }
    }

    int getCurrentFailCount() {
        return fails.get();
    }

    public void destroy() {
        buckets.remove(this);
    }

    private void decreaseFails() {
        fails.updateAndGet(i -> i > 0 ? i - 1 : 0);
    }

    private static void initBucketMonitor() {
        buckets = new CopyOnWriteArraySet<>();
        scheduler.scheduleAtFixedRate(() -> buckets.forEach(LeakyBucket::decreaseFails),
                COUNTDOWN_RATE, COUNTDOWN_RATE, TimeUnit.MILLISECONDS);
    }

}
