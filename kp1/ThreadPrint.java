class Printer {
    private final int ROW_WIDTH = 100;
    private int charCount = 0;

    public void printChar(char c) {
	if (charCount != 0 &&charCount % ROW_WIDTH == 0) {
	    System.out.print('\n');
	}

	System.out.print(c);
	charCount++;
    }
}

class PrintingThread extends Thread {
    private char c;
    private Object lock;
    private Printer printer;
    
    public PrintingThread(char character, Printer printer, Object lock) {
	c = character;
	this.lock = lock;
	this.printer = printer;
    }

    @Override
    public void run(){
	for (int i = 0; i < 1000; i++) {
	    synchronized(lock) {
		printer.printChar(c);

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
	Printer p = new Printer();
	
	PrintingThread threadOne = new PrintingThread('-', p, lock);
	PrintingThread threadTwo = new PrintingThread('|', p, lock);

	threadOne.start();
	threadTwo.start();
    }
}
