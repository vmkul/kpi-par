import java.util.concurrent.locks.ReentrantLock;

public class AsynchBankTest {
    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10000;

    public static void main(String[] args) {
	Bank b = new Bank(NACCOUNTS, INITIAL_BALANCE);
	int i;
	for (i = 0; i < NACCOUNTS; i++){
	    TransferThread t = new TransferThread(b, i,
						  INITIAL_BALANCE);
	    t.setPriority(Thread.NORM_PRIORITY + i % 2);
	    t.start () ;
	}
    }
}

class Bank {
    public static final int NTEST = 10000;
    private final int[] accounts;
    private long ntransacts = 0;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean isInTransaction = false;

    public Bank(int n, int initialBalance){
	accounts = new int[n];
	int i;
	for (i = 0; i < accounts.length; i++)
	    accounts[i] = initialBalance;
	ntransacts = 0;
    }

    public /* synchronized */ void transfer(int from, int to, int amount) {
	// lock.lock();
	synchronized (this) {
	    while (isInTransaction) {
		try {
		    wait();	    
		} catch (InterruptedException ex) {}
	    }
	    isInTransaction = true;
	}

	accounts[from] -= amount;
	accounts[to] += amount;
	ntransacts++;
	if (ntransacts % NTEST == 0)
	    test();

	isInTransaction = false;
	synchronized (this) {
	    notifyAll();
	}
	// lock.unlock();
    }
    
    public void test(){
	int sum = 0;
	for (int i = 0; i < accounts.length; i++)
	    sum += accounts[i] ;
	System.out.println("Transactions:" + ntransacts
			   + " Sum: " + sum);
    }
    
    public int size(){
	return accounts.length;
    }
}

class TransferThread extends Thread {
    private Bank bank;
    private int fromAccount;
    private int maxAmount;
    private static final int REPS = 1000;
    
    public TransferThread(Bank b, int from, int max){
	bank = b;
	fromAccount = from;
	maxAmount = max;
    }
    
    @Override
    public void run(){
	while (true) {
	    for (int i = 0; i < REPS; i++) {
		int toAccount = (int) (bank.size() * Math.random());
		int amount = (int) (maxAmount * Math.random()/REPS);
		bank.transfer(fromAccount, toAccount, amount);
	    }
	}
    }
}
