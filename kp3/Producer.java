import java.util.Random;

public class Producer implements Runnable {
    private Drop drop;
    private final int ARRAY_SIZE = 5000;
    private int[] dataArray = new int[ARRAY_SIZE];

    public Producer(Drop drop) {
        this.drop = drop;
        Random random = new Random();

	for (int i = 0; i < ARRAY_SIZE; i++) {
	    dataArray[i] = random.nextInt(1000);
	}
    }

    public int[] getData() {
	return dataArray;
    }

    public void run() {
        Random random = new Random();

        for (int i = 0;
             i < ARRAY_SIZE;
             i++) {
            drop.put(dataArray[i]);
            try {
                Thread.sleep(random.nextInt(10));
            } catch (InterruptedException e) {}
        }
        drop.put(-1);
    }
}
