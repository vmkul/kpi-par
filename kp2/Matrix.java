import java.util.Random;

public class Matrix {
    final Integer[][] matrix;
    final Random random = new Random();
    final int size;

    public Matrix(int size) {
	this.size = size;
	matrix = new Integer[size][size];

	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		matrix[i][j] = random.nextInt(1000);
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
}
