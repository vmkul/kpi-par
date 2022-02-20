import java.util.ArrayList;

class FoxSubtask extends Thread {
    private final FoxMethod controller;
    private Matrix matrixA;
    private Matrix matrixB;
    private final Matrix matrixC;
    private boolean isAlive = true;

    public FoxSubtask(FoxMethod controller, int size) {
	this.controller = controller;
	matrixC = new Matrix(size, false);
    }

    public void setMatrixA(Matrix matrixA) {
	this.matrixA = matrixA;
    }

    public void setMatrixB(Matrix matrixB) {
	this.matrixB = matrixB;
    }

    public Matrix getMatrixC() {
	return matrixC;
    }

    public void kill() {
	isAlive = false;
    }

    @Override
    public void run(){
	while (isAlive) {
	    matrixC.addM(Sequential.matrixMult(matrixA, matrixB));
	    controller.reportFinished(this);
	}
    }
}

public class FoxMethod {
    private final int numThreads;
    private final int submatrixSize;
    private final int subsPerRow;
    private final ArrayList<ArrayList<FoxSubtask>> subtasks = new ArrayList<ArrayList<FoxSubtask>>();
    private final int[] subACols;
    private final int[] subBRows;
    private final Matrix[][] ASubs;
    private final Matrix[][] BSubs;
    private final Matrix MatrixC;
    private int subtasksFinished = 0;
    private int stagesFinished = 0;

    public FoxMethod(int numThreads, Matrix MatrixA, Matrix MatrixB) throws ArithmeticException {
	int size = MatrixA.getSize();
	if (size != MatrixB.getSize()) {
	    throw new ArithmeticException("Got matrices of different size!");
	}

	this.numThreads = numThreads;
	this.MatrixC = new Matrix(size, false);
	double submSize = Math.sqrt((size * size) / numThreads);

	if ((submSize % 1) != 0) {
	    throw new ArithmeticException("Can't divide matrix into submatrices for this number of threads!");
	}
	submatrixSize = (int) submSize;
	subsPerRow = size / submatrixSize;

	ASubs = new Matrix[subsPerRow][subsPerRow];
	BSubs = new Matrix[subsPerRow][subsPerRow];
	subACols = new int[subsPerRow];
	subBRows = new int[subsPerRow];

	for (int i = 0; i < subsPerRow; i++) {
	    for (int j = 0; j < subsPerRow; j++) {
		ASubs[i][j] = MatrixA.getSubmatrix(i * submatrixSize, j * submatrixSize, submatrixSize);
		BSubs[i][j] = MatrixB.getSubmatrix(i * submatrixSize, j * submatrixSize, submatrixSize);
	    }
	}
    }

    public Matrix matrixMult() {
	for (int i = 0; i < subsPerRow; i++) {
	    subtasks.add(new ArrayList<FoxSubtask>());

	    for (int j = 0; j < subsPerRow; j++) {
		FoxSubtask subtask = new FoxSubtask(this, submatrixSize);
		subtask.setMatrixA(ASubs[i][i]);
		subtask.setMatrixB(BSubs[i][j]);
		subtasks.get(i).add(subtask);
		subACols[i] = i;
		subBRows[i] = i;
	    }
	}

	for (int i = 0; i < subsPerRow; i++) {
	    for (int j = 0; j < subsPerRow; j++) {
		subtasks.get(i).get(j).start();
	    }
	}

	for (int i = 0; i < subsPerRow; i++) {
	    for (int j = 0; j < subsPerRow; j++) {
		try {
		    subtasks.get(i).get(j).join();
		} catch (InterruptedException ex) {}
	    }
	}

	for (int i = 0; i < subsPerRow; i++) {
	    for (int j = 0; j < subsPerRow; j++) {
		MatrixC.setSubmatrix(i * submatrixSize, j * submatrixSize, subtasks.get(i).get(j).getMatrixC());
	    }
	}

	return MatrixC;
    }

    public synchronized void reportFinished(FoxSubtask subtask) {
	int rowIndex = 0;
	int colIndex = 0;

	for (int i = 0; i < subtasks.size(); i++) {
	    colIndex = subtasks.get(i).indexOf(subtask);

	    if (colIndex != -1) {
		rowIndex = i;
		break;
	    }
	}

	int newAIndex = (subACols[rowIndex] + 1) % subsPerRow;
	int newBIndex = (subBRows[rowIndex] + 1) % subsPerRow;

	subtask.setMatrixA(ASubs[rowIndex][newAIndex]);
	subtask.setMatrixB(BSubs[newBIndex][colIndex]);

	if (++subtasksFinished == numThreads) {
	    subtasksFinished = 0;

	    if (++stagesFinished == subsPerRow) {
		killSubtasks();
	    }

	    synchronized(this) {
		notifyAll();
	    }
	} else {
	    synchronized(this) {
		try {
		    wait();
		} catch (Exception ex) {}
	    }
	}

	subACols[rowIndex] = newAIndex;
	subBRows[rowIndex] = newBIndex;
    }

    private void killSubtasks() {
	for (int i = 0; i < subsPerRow; i++) {
	    for (int j = 0; j < subsPerRow; j++) {
		subtasks.get(i).get(j).kill();
	    }
	}
    }
}
