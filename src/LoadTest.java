import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class LoadTest {

    // 50 / 50
    // 0.165 sync
    // 0.159 reentrant
    // 0,254 read write
    // 0.151 stamped
    // 90 / 10
    // 0.108 sync
    // 0.088 reentrant
    // 0.179 read write
    // 0.072 stamped
    // 99 / 1
    // 0.091 sync
    // 0.071 reentrant
    // 0.146 read write
    // 0.049 stamped
    public static void main(String[] args) throws InterruptedException {
        Map<Integer, Integer> ledger = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            ledger.put(i, 1000);
        }

//        BankTransfer bank = new BankTransfer(ledger);
//        ReentrantBankTransfer bank = new ReentrantBankTransfer(ledger);
//        ReentrantReadWriteBankTransfer bank = new ReentrantReadWriteBankTransfer(ledger);
        StampedBankTransfer bank = new StampedBankTransfer(ledger);

        int ops = 100_000;          // на поток
        int threads = Runtime.getRuntime().availableProcessors();
        int readPercent = 99;

        LongAdder ok = new LongAdder(), fail = new LongAdder();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    var rnd = ThreadLocalRandom.current();
                    ready.countDown();
                    go.await();
                    for (int j = 0; j < ops; j++) {
                        int from = rnd.nextInt(1000);
                        if (rnd.nextInt(100) < readPercent) {
                            bank.getBalanceByAccountId(from);
                        } else {
                            int to = rnd.nextInt(1000);
                            if (to == from) continue;
                            (bank.transferMoney(from, to, 10) == STATUS.SUCCESS ? ok : fail).increment();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        long start = System.nanoTime();
        go.countDown();
        done.await();
        long elapsed = System.nanoTime() - start;
        executor.shutdown();

        System.out.printf("time: %.3f s, ok=%d, fail=%d%n", elapsed / 1e9, ok.sum(), fail.sum());
        System.out.println("sum: " + ledger.values().stream().mapToLong(e -> e).sum() + " (expected " + 1000 * 1000 + ")");

    }
}
