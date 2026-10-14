import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class StampedBankTransfer {
    private final Map<Integer, Integer> ledger;
    private final StampedLock LOCK;

    public StampedBankTransfer(Map<Integer, Integer> ledger) {
        if (ledger == null) {
            throw new IllegalStateException("where is ledger,bro?");
        }

        this.ledger = ledger;
        this.LOCK = new StampedLock();
    }

    public Integer getBalanceByAccountId(int accountId) {
       long stamp = LOCK.tryOptimisticRead();
       Integer balance = ledger.get(accountId);
       if (!LOCK.validate(stamp)) {
           stamp = LOCK.readLock();
           try {
               balance = ledger.get(accountId);
           } finally {
               LOCK.unlockRead(stamp);
           }
       }

       return balance;
    }

    public STATUS transferMoney(int fromAccountId, int toAccountId, int amount) {
        if (amount <= 0) {
            return STATUS.ERROR_INSUFFICIENT_FOUNDS;
        }
        else if (fromAccountId == toAccountId) {
            return STATUS.SUCCESS;
        }

        long stamp = LOCK.writeLock();
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
            LOCK.unlockWrite(stamp);
        }
    }

}
