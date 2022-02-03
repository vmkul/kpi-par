public class BallThread extends Thread {
    private Ball b;

    public BallThread(Ball ball){
	b = ball;
    }
    
    @Override
    public void run(){
	// try{
	    while (true) {
		boolean hasDropped = b.move();
		if (hasDropped) {
		    System.out.println("Ball dropped thread = " + Thread.currentThread().getName());
		    break;
		}
		
		System.out.println("Thread name = " + Thread.currentThread().getName());
		// Thread.sleep(5);
	    }
	// } catch(InterruptedException ex){

	// }
    }
}
