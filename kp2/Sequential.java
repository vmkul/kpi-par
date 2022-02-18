public class Sequential {
    public static Matrix matrixMult(Matrix MatrixA, Matrix MatrixB) throws ArithmeticException {
	int size = MatrixA.getSize();
	Matrix MatrixC = new Matrix(size, false);
	if (size != MatrixB.getSize()) {
	    throw new ArithmeticException("Got matrices of different size!");
	}

	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		MatrixC.set(i, j, 0);

		for (int k = 0; k < size; k++) {
		    MatrixC.set(i, j, MatrixC.get(i, j) + MatrixA.get(i, k) * MatrixB.get(k, j));
		}
	    }
	}

	return MatrixC;
    }
}
