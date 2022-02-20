import java.util.ArrayList;
import java.time.Instant;
import java.time.Duration;

public class Main {
    private static final int numThreads[] = new int[] {4, 16, 64, 256};
    private static final int matrixSizes[] = new int[] {256, 512, 1024, 1536, 2048, 4864};

    private static void availableSizes(int[] numThreads) {
	for (int size = 1; size < 5000; size++) {
	    boolean good = true;

	    for (int nThreads : numThreads) {
		double submSize = Math.sqrt((size * size) / nThreads);

		if (!((submSize % 1) == 0 && (size % nThreads) == 0)) {
		    good = false;
		    break;
		}
	    }
	    if (good) {
		System.out.print(size + " ");
	    }
	}
    }

    public synchronized static void main(String[] args) {
	for (int size : matrixSizes) {
	    System.out.println("Calculating for matrix size: " + size + "...");

	    Matrix a = new Matrix(size, true);
	    Matrix b = new Matrix(size, true);

	    Instant start = Instant.now();
	    Matrix seqResult = Sequential.matrixMult(a, b);
	    Instant finish = Instant.now();
	    long timeElapsed = Duration.between(start, finish).toMillis();
	    System.out.println("  Duration of sequential: " + timeElapsed);

	    for (int threadCount : numThreads) {
		System.out.println("  Calculating for n threads: " + threadCount + "...");

		start = Instant.now();
		BlockStriped striped = new BlockStriped(threadCount, a, b);
		Matrix stripedResult = striped.matrixMult();
		finish = Instant.now();
		timeElapsed = Duration.between(start, finish).toMillis();
		System.out.println("    Duration of BlockStriped: " + timeElapsed);

		start = Instant.now();
		FoxMethod fox = new FoxMethod(threadCount, a, b);
		Matrix foxResult = fox.matrixMult();
		finish = Instant.now();
		timeElapsed = Duration.between(start, finish).toMillis();
		System.out.println("    Duration of Fox's method: " + timeElapsed);

		if (!seqResult.equalTo(stripedResult)) {
		    System.out.println("Striped algorithm multiplication error");
		    return;
		}
		if (!seqResult.equalTo(foxResult)) {
		    System.out.println("Fox's algorithm multiplication error");
		    return;
		}
	    }
	}
    }
}
