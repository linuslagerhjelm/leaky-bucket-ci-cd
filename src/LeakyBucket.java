import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class LeakyBucket<T> {
    public static final long COUNTDOWN_RATE = 1000;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Set<LeakyBucket> buckets = null;

    private final Consumer<T> operator;
    private AtomicInteger fails = new AtomicInteger(0);


    private LeakyBucket(Consumer<T> operator) {
        this.operator = operator;
    }

    public static <T> LeakyBucket<T> monitor(Consumer<T> operation) {
        if (buckets == null) initBucketMonitor();
        LeakyBucket<T> lb =  new LeakyBucket<>(operation);
        buckets.add(lb);
        return lb;
    }

    public void invoke(T arg) throws OperationFailedException {
        try {
            operator.accept(arg);
        } catch (Throwable t) {
            fails.incrementAndGet();
            throw new OperationFailedException(t);
        }
    }

    int getCurrentFailCount() {
        return fails.get();
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
