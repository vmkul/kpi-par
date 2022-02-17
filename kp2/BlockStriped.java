import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

class Subtask extends Thread {
    BlockStriped controller;
    Integer[] row;
    Integer[] col;
    int size;
    boolean isAlive = true;

    public Subtask(int size, BlockStriped controller) {
	this.size = size;
	this.controller = controller;
    }

    public void setRow(Integer[] row) {
	this.row = row;
    }

    public void setCol(Integer[] col) {
	this.col = col;
    }

    public void kill() {
	this.isAlive = false;
    }

    @Override
    public void run(){
	while (isAlive) {
	    int res = 0;
	    for (int i = 0; i < size; i++) {
		res += row[i] * col[i];
	    }

	    controller.reportFinished(this, res);
	}
    }
}

public class BlockStriped {
    final int numThreads;
    final ArrayList<Subtask> subtasks = new ArrayList<Subtask>();
    final ArrayList<Integer> subtaskColIds = new ArrayList<Integer>();
    final ArrayList<Integer[]> rows = new ArrayList<Integer[]>();
    final ArrayList<Integer[]> cols = new ArrayList<Integer[]>();
    final Matrix MatrixC;
    int subtasksFinished = 0;
    int elementsCalculated = 0;

    public BlockStriped(int numThreads, Matrix MatrixA, Matrix MatrixB) {
	int size = MatrixA.getSize();
	this.numThreads = size;
	if (size != MatrixB.getSize()) {
	    throw new ArithmeticException("Got matrices of different size!");
	}
	this.MatrixC = new Matrix(size);

	for (int i = 0; i < size; i++) {
	    rows.add(MatrixA.getRow(i));
	    cols.add(MatrixB.getCol(i));
	}
    }

    public Matrix matrixMult() {
	for (int i = 0; i < cols.size(); i++) {
	    Subtask subtask = new Subtask(cols.size(), this);
	    subtask.setRow(rows.get(i));
	    subtask.setCol(cols.get(i));
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

    public synchronized void reportFinished(Subtask subtask, int result) {
	elementsCalculated++;
	int i = subtasks.indexOf(subtask);
	int j = subtaskColIds.get(i);
	MatrixC.set(i, j, result);
	//System.out.println("Finished: " + i + " " + j);
	
	int prevSubtaskId = getPrevSubtaskId(i);

	int newColId = (j + 1) % numThreads;
	subtaskColIds.set(i, newColId);
	//System.out.println("subtasks finished : " + subtasksFinished);
	subtask.setCol(cols.get(newColId));
	
	if (++subtasksFinished == numThreads) {
	    //System.out.println("Control thread, thread: " + i);
	    subtasksFinished = 0;
	    if (elementsCalculated == rows.size() * cols.size()) {
		for (int k = 0; k < subtasks.size(); k++) {
		    subtasks.get(k).kill();
		}
	    }
	    synchronized(this) {
		notifyAll();
	    }
	} else {
	    synchronized(this) {
		// while (!iterationFinished) {
		    try {
			//System.out.println("Waiting for iter, thread: " + i);
			wait();
			// Try to use reentrant lock here
		    } catch (Exception ex) {}
		// }
	    }
	}
    }

    private int getPrevSubtaskId(int id) {
	if (id == 0) {
	    return numThreads - 1;
	}

	return id - 1;
    }
}
