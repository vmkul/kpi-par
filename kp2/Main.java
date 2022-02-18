import java.time.Instant;
import java.time.Duration;

public class Main {
    final int MATRIX_SIZE = 1000;
    
    public synchronized static void main(String[] args) {
	Matrix a = new Matrix(1000, true);
	Matrix b = new Matrix(1000, true);
	BlockStriped striped = new BlockStriped(100, a, b);
	FoxMethod fox = new FoxMethod(100, a, b);

	System.out.println("Matrix A: ");

	System.out.println("Matrix B: ");

	System.out.println("Seq A * B: ");
	Instant start = Instant.now();
	Matrix seqResult = Sequential.matrixMult(a, b);
	//seqResult.print();
	Instant finish = Instant.now();
	long timeElapsedSeq = Duration.between(start, finish).toMillis();
	//seqResult.print();

	System.out.println("BlockStriped A * B: ");
	start = Instant.now();
	Matrix stripedResult = striped.matrixMult();
	finish = Instant.now();
	long timeElapsedStriped = Duration.between(start, finish).toMillis();


	System.out.println("Fox's method A * B: ");
	start = Instant.now();
	Matrix foxResult = fox.matrixMult();
	//foxResult.print();
	finish = Instant.now();
	long timeElapsedFox = Duration.between(start, finish).toMillis();
	//stripedResult.print();

	System.out.println("Duration of seq: " + timeElapsedSeq);
	System.out.println("Duration of striped: " + timeElapsedStriped);
	System.out.println("Duration of fox: " + timeElapsedFox);


	if (!foxResult.equalTo(seqResult)) {
	    System.out.println("Stripped mult res != seq");
	}
    }
}
