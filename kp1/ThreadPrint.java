class PrintingThread extends Thread {
    private char c;
    private Object lock;
    
    public PrintingThread(char character, Object lock) {
	c = character;
	this.lock = lock;
    }

    @Override
    public void run(){
	for (int i = 0; i < 1000; i++) {
	    synchronized(lock) {
		System.out.print(c);

		try {
		    lock.notify();
		    if (i == 999) return;
		    lock.wait();
		} catch (InterruptedException ex) {

		}
	    }
	}
    }
}

public class ThreadPrint {
    static Object lock = new Object();
    
    public synchronized static void main(String[] args) {
	PrintingThread threadOne = new PrintingThread('-', lock);
	PrintingThread threadTwo = new PrintingThread('|', lock);

	threadOne.start();
	threadTwo.start();
    }
}
