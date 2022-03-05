import java.util.ArrayList;

public class ProducerConsumerExample {
    public static void main(String[] args) {
        Drop drop = new Drop();
	Producer p = new Producer(drop);
	Consumer c = new Consumer(drop);

        Thread t1 = new Thread(p);
        Thread t2 = new Thread(c);
	t1.start();
	t2.start();
	try {
	    t1.join();
	    t2.join();
	} catch (InterruptedException ex) {}

	int[] producerData = p.getData();
	ArrayList<Integer> consumerData = c.getData();

	for (int i = 0; i < producerData.length; i++) {
	    if (producerData[i] != consumerData.get(i)) {
		System.out.println("Consumer and producer data not equal!");
		return;
	    }
	}
	System.out.println("Consumer data == Producer data");
    }
}
