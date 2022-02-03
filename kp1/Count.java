import java.util.concurrent.locks.ReentrantLock;

class Counter {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public void increment() {
	count++;
    }

    public void decrement() {
	count--;
    }

    public synchronized void syncInc() {
	count++;
    }

    public synchronized void syncDec() {
	count--;
    }

    public void syncBlockInc() {
	synchronized(this) {
	    count++;
	}
    }

    public void syncBlockDec() {
	synchronized(this) {
	    count--;
	}
    }

    public void lockInc() {
	lock.lock();
	
	try {
	    count++;
	} finally {
	    lock.unlock();
	}	
    }

    public void lockDec() {
	lock.lock();
	
	try {
	    count--;
	} finally {
	    lock.unlock();
	}	
    }
    
    public int getCount() {
	return count;
    }
}

class IncThread extends Thread {
    private Counter counter;
    
    public IncThread(Counter c) {
	this.counter = c;
    }

    @Override
    public void run(){
	for (int i = 0; i < 100000; i++) {
	    counter.lockInc();
	}
    }
}

class DecThread extends Thread {
    private Counter counter;
    
    public DecThread(Counter c) {
	this.counter = c;
    }

    @Override
    public void run(){
	for (int i = 0; i < 100000; i++) {
	    counter.lockDec();
	}
    }
}


public class Count {
    static Object lock = new Object();
    
    public synchronized static void main(String[] args) {
	Counter counter = new Counter();
	IncThread incThread = new IncThread(counter);
	DecThread decThread = new DecThread(counter);

	incThread.start();
	decThread.start();

	try {
	    incThread.join();
	    decThread.join();
	} catch (InterruptedException ex) {

	}

	System.out.println("Count = " + counter.getCount());
    }
}
