import java.util.Random;
import java.util.ArrayList;

public class Consumer implements Runnable {
    private Drop drop;
    private ArrayList<Integer> data = new ArrayList<Integer>();

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    public ArrayList<Integer> getData() {
	return data;
    }

    public void run() {
        Random random = new Random();
        for (int message = drop.take(); message != -1; message = drop.take()) {
            System.out.format("MESSAGE RECEIVED: %d%n", message);
	    data.add(message);
            try {
                Thread.sleep(random.nextInt(10));
            } catch (InterruptedException e) {}
        }
    }
}
