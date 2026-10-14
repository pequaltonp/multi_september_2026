import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteBankTransfer {
    private final Map<Integer, Integer> ledger;
    private final ReentrantReadWriteLock LOCK;

    public ReentrantReadWriteBankTransfer(Map<Integer, Integer> ledger) {
        if (ledger == null) {
            throw new IllegalStateException("where is ledger,bro?");
        }

        this.ledger = ledger;
        this.LOCK = new ReentrantReadWriteLock();
    }

    public Integer getBalanceByAccountId(int accountId) {
       LOCK.readLock().lock();
       try {
           return ledger.get(accountId);
       }
       finally {
           LOCK.readLock().unlock();
       }
    }

    public STATUS transferMoney(int fromAccountId, int toAccountId, int amount) {
        if (amount <= 0) {
            return STATUS.ERROR_INSUFFICIENT_FOUNDS;
        }
        else if (fromAccountId == toAccountId) {
            return STATUS.SUCCESS;
        }

        LOCK.writeLock().lock();
        try {
            Integer fromAmount = ledger.get(fromAccountId);
            Integer toAmount = ledger.get(toAccountId);

            if (fromAmount == null || toAmount == null) {
                return STATUS.ERROR_ACCOUNT_NOT_FOUND;
            } else if (amount > fromAmount) {
                return STATUS.ERROR_INSUFFICIENT_FOUNDS;
            }

            ledger.put(fromAccountId, fromAmount - amount);
            ledger.put(toAccountId, toAmount + amount);

            return STATUS.SUCCESS;
        }
        finally {
            LOCK.writeLock().unlock();
        }
    }

}
