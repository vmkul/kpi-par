public class BallThread extends Thread {
    private Ball b;
    private BallThread blockingThread;

    public BallThread(Ball ball, BallThread blockingThread){
	b = ball;
	this.blockingThread = blockingThread;
    }

    @Override
    public void run(){
	try{
	    if (blockingThread != null) {
		blockingThread.join();
	    }

	    while (true) {
		boolean hasDropped = b.move();
		if (hasDropped) {
		    System.out.println("Ball dropped thread = " + Thread.currentThread().getName());
		    break;
		}

		// System.out.println("Thread name = " + Thread.currentThread().getName());
		Thread.sleep(5);
	    }
	} catch(InterruptedException ex){

	}
    }
}
