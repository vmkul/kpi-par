import java.util.Random;

public class Matrix {
    private final Integer[][] matrix;
    private final Random random = new Random();
    private final int size;

    public Matrix(int size, boolean randomize) {
	this.size = size;
	matrix = new Integer[size][size];

	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		if (randomize) {
		    matrix[i][j] = random.nextInt(1000);
		} else {
		    matrix[i][j] = 0;
		}
	    }
	}
    }

    public int getSize() {
	return size;
    }

    public void print() {
	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		System.out.print(matrix[i][j] + " ");
	    }
	    System.out.print("\n");    
	}
    }

    public void set(int i, int j, int val) {
	matrix[i][j] = val;
    }

    public int get(int i, int j) {
	return matrix[i][j];
    }

    public boolean equalTo(Matrix other) {
	if (size != other.getSize()) return false;
	
	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		if (get(i, j) != other.get(i, j)) {
		    return false;
		}
	    }
	}

	return true;
    }

    public Integer[] getCol(int j) {
	Integer[] res = new Integer[size];

	for (int i = 0; i < size; i++) {
	    res[i] = get(i, j);
	}
	
	return res;
    }

    public Integer[] getRow(int i) {
	Integer[] res = new Integer[size];

	for (int j = 0; j < size; j++) {
	    res[j] = get(i, j);
	}
	
	return res;
    }

    public void addM(Matrix other) {
	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		set(i, j, get(i, j) + other.get(i, j));
	    }
	}
    }

    public Matrix getSubmatrix(int rowOffset, int colOffset, int size) {
	Matrix res = new Matrix(size, false);

	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		res.set(i, j, get(i + rowOffset, j + colOffset));
	    }
	}

	return res;
    }

    public void setSubmatrix(int rowOffset, int colOffset, Matrix m) {
	int size = m.getSize();

	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		set(i + rowOffset, j + colOffset, m.get(i, j));
	    }
	}
    }
}
