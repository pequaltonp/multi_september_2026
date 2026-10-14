import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankTransfer {
    private final Map<Integer, Integer> ledger;
    private final Object LOCK = new Object();

    public BankTransfer(Map<Integer, Integer> ledger) {
        if (ledger == null) {
            throw new IllegalStateException("where is ledger,bro?");
        }

        this.ledger = ledger;
    }

    public Integer getBalanceByAccountId(int accountId) {
        synchronized (LOCK) {
            return ledger.get(accountId);
        }
    }

    public STATUS transferMoney(int fromAccountId, int toAccountId, int amount) {
        if (amount <= 0) {
            return STATUS.ERROR_INSUFFICIENT_FOUNDS;
        }
        else if (fromAccountId == toAccountId) {
            return STATUS.SUCCESS;
        }

        synchronized (LOCK) {
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
    }

}
