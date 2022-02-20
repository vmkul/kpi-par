import java.util.ArrayList;

class Subtask extends Thread {
    private final BlockStriped controller;
    private int[][] rowBatch;
    private int[][] colBatch;
    private final int[][] result;
    private boolean isAlive = true;

    public Subtask(BlockStriped controller, int size) {
	this.controller = controller;
	result = new int[size][size];
    }

    public void setRowBatch(int[][] rowBatch) {
	this.rowBatch = rowBatch;
    }

    public void setColBatch(int[][] colBatch) {
	this.colBatch = colBatch;
    }

    public void kill() {
	isAlive = false;
    }

    @Override
    public void run(){
	while (isAlive) {
	    for (int i = 0; i < rowBatch.length; i++) {
		int[] row = rowBatch[i];

		for (int j = 0; j < colBatch.length; j++) {
		    int[] col = colBatch[j];

		    int res = 0;
		    for (int k = 0; k < row.length; k++) {
			res += row[k] * col[k];
		    }

		    result[i][j] = res;
		}
	    }

	    controller.reportFinished(this, result);
	}
    }
}

public class BlockStriped {
    private final int numThreads;
    private final int colsPerBatch;
    private final ArrayList<Subtask> subtasks = new ArrayList<Subtask>();
    private final ArrayList<Integer> subtaskColIds = new ArrayList<Integer>();
    private final ArrayList<int[][]> rowBatches = new ArrayList<int[][]>();
    private final ArrayList<int[][]> colBatches = new ArrayList<int[][]>();
    private final Matrix MatrixC;
    private int subtasksFinished = 0;
    private int elementsCalculated = 0;

    public BlockStriped(int numThreads, Matrix MatrixA, Matrix MatrixB) throws ArithmeticException {
	int size = MatrixA.getSize();
	this.numThreads = numThreads;
	if (size != MatrixB.getSize()) {
	    throw new ArithmeticException("Got matrices of different size!");
	}
	this.MatrixC = new Matrix(size, false);

	if (size % numThreads != 0 || numThreads > size || numThreads == 0) {
	    throw new ArithmeticException("Can't divide cols for this number of threads!");
	}

	colsPerBatch = size / numThreads;

	for (int i = 0; i < numThreads; i++) {
	    int[][] rowBatch = new int[colsPerBatch][size];
	    int[][] colBatch = new int[colsPerBatch][size];

	    for (int j = 0; j < colsPerBatch; j++) {
		rowBatch[j] = MatrixA.getRow(i * colsPerBatch + j);
		colBatch[j] = MatrixB.getCol(i * colsPerBatch + j);
	    }

	    rowBatches.add(rowBatch);
	    colBatches.add(colBatch);
	}
    }

    public Matrix matrixMult() {
	for (int i = 0; i < numThreads; i++) {
	    Subtask subtask = new Subtask(this, colsPerBatch);
	    subtask.setRowBatch(rowBatches.get(i));
	    subtask.setColBatch(colBatches.get(i));
	    subtaskColIds.add(i);
	    subtasks.add(subtask);
	}

	for (int i = 0; i < numThreads; i++) {
	    subtasks.get(i).start();
	}

	for (int i = 0; i < numThreads; i++) {
	    try {
		subtasks.get(i).join();
	    } catch (InterruptedException ex) {}
	}

	return MatrixC;
    }

    public synchronized void reportFinished(Subtask subtask, int[][] result) {
	int subtaskId = subtasks.indexOf(subtask);
	int colBatchId = subtaskColIds.get(subtaskId);
	int newColId = (colBatchId + 1) % numThreads;

	for (int i = 0; i < result.length; i++) {
	    for (int j = 0; j < result[0].length; j++) {
		MatrixC.set(i + subtaskId * colsPerBatch, j + colBatchId * colsPerBatch, result[i][j]);
	    }
	}

	elementsCalculated += result.length;
	subtaskColIds.set(subtaskId, newColId);
	subtask.setColBatch(colBatches.get(newColId));

	if (++subtasksFinished == numThreads) {
	    subtasksFinished = 0;
	    if (elementsCalculated == MatrixC.getSize() * MatrixC.getSize()) {
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
    }

    private void killSubtasks() {
	for (int k = 0; k < subtasks.size(); k++) {
	    subtasks.get(k).kill();
	}
    }
}
