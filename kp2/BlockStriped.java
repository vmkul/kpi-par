import java.util.ArrayList;

class Subtask extends Thread {
    BlockStriped controller;
    ArrayList<Integer[]> rowBatch;
    ArrayList<Integer[]> colBatch;
    boolean isAlive = true;

    public Subtask(BlockStriped controller) {
	this.controller = controller;
    }

    public void setRowBatch(ArrayList<Integer[]> rowBatch) {
	this.rowBatch = rowBatch;
    }

    public void setColBatch(ArrayList<Integer[]> colBatch) {
	this.colBatch = colBatch;
    }

    public void kill() {
	isAlive = false;
    }

    @Override
    public void run(){
	while (isAlive) {
	    Integer[][] result = new Integer[rowBatch.size()][colBatch.size()];

	    for (int i = 0; i < rowBatch.size(); i++) {
		Integer[] row = rowBatch.get(i);

		for (int j = 0; j < colBatch.size(); j++) {
		    Integer[] col = colBatch.get(j);

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
    final int numThreads;
    final int colsPerBatch;
    final ArrayList<Subtask> subtasks = new ArrayList<Subtask>();
    final ArrayList<Integer> subtaskColIds = new ArrayList<Integer>();
    final ArrayList<ArrayList<Integer[]>> rowBatches = new ArrayList<ArrayList<Integer[]>>();
    final ArrayList<ArrayList<Integer[]>> colBatches = new ArrayList<ArrayList<Integer[]>>();
    final Matrix MatrixC;
    int subtasksFinished = 0;
    int elementsCalculated = 0;

    public BlockStriped(int numThreads, Matrix MatrixA, Matrix MatrixB) throws ArithmeticException {
	int size = MatrixA.getSize();
	this.numThreads = numThreads;
	if (size != MatrixB.getSize()) {
	    throw new ArithmeticException("Got matrices of different size!");
	}
	this.MatrixC = new Matrix(size);

	if (size % numThreads != 0 || numThreads > size || numThreads == 0) {
	    throw new ArithmeticException("Can't divide cols for this number of threads!");
	}

	colsPerBatch = size / numThreads;

	for (int i = 0; i < numThreads; i++) {
	    ArrayList<Integer[]> rowBatch = new ArrayList<Integer[]>();
	    ArrayList<Integer[]> colBatch = new ArrayList<Integer[]>();

	    for (int j = 0; j < colsPerBatch; j++) {
		rowBatch.add(MatrixA.getRow(i * colsPerBatch + j));
		colBatch.add(MatrixB.getCol(i * colsPerBatch + j));
	    }

	    rowBatches.add(rowBatch);
	    colBatches.add(colBatch);
	}
    }

    public Matrix matrixMult() {
	for (int i = 0; i < numThreads; i++) {
	    Subtask subtask = new Subtask(this);
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

    public synchronized void reportFinished(Subtask subtask, Integer[][] result) {
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
		for (int k = 0; k < subtasks.size(); k++) {
		    subtasks.get(k).kill();
		}
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
}
