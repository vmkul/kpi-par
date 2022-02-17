import java.time.Instant;
import java.time.Duration;

public class Main {
    final int MATRIX_SIZE = 1000;
    
    public synchronized static void main(String[] args) {
	Matrix a = new Matrix(1000);
	Matrix b = new Matrix(1000);
	BlockStriped striped = new BlockStriped(0, a, b);

	System.out.println("Matrix A: ");

	System.out.println("Matrix B: ");

	System.out.println("Seq A * B: ");
	Instant start = Instant.now();
	Matrix seqResult = Sequential.matrixMult(a, b);
	Instant finish = Instant.now();
	long timeElapsedSeq = Duration.between(start, finish).toMillis();

	System.out.println("BlockStriped A * B: ");
	start = Instant.now();
	Matrix stripedResult = striped.matrixMult();
	finish = Instant.now();
	long timeElapsedStriped = Duration.between(start, finish).toMillis();
	System.out.println("Duration of seq: " + timeElapsedSeq);
	System.out.println("Duration of striped: " + timeElapsedStriped);

	if (!seqResult.equalTo(stripedResult)) {
	    System.out.println("Stripped mult res != seq");
	}
    }
}
