package utils;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.DConvertMatrixStruct;

public class MatrixUtils {

    /**
     * Estrae la parte triangolare inferiore di una matrice sparsa, includendo
     * la diagonale principale.
     * * @param A La matrice sparsa di origine (formato CSC).
     * @return Una nuova matrice sparsa CSC contenente esclusivamente gli elementi
     * del triangolo inferiore di A.
     */
    public static DMatrixSparseCSC tril(DMatrixSparseCSC A) {

        DMatrixSparseTriplet triplet = new DMatrixSparseTriplet(A.getNumRows(), A.getNumCols(), A.getNonZeroLength());


        for (int col = 0; col < A.getNumCols(); col++) {
            int idxStart = A.col_idx[col];
            int idxEnd = A.col_idx[col + 1];

            for (int i = idxStart; i < idxEnd; i++) {
                int row = A.nz_rows[i];
                double value = A.nz_values[i];

                if (row >= col) {
                    triplet.addItem(row, col, value);
                }
            }
        }

        DMatrixSparseCSC result = new DMatrixSparseCSC(A.getNumRows(), A.getNumCols(), triplet.getNonZeroLength());
        DConvertMatrixStruct.convert(triplet, result);
        return result;
    }

    /**
     * Estrae la parte triangolare superiore di una matrice sparsa.
     * Permette di escludere la diagonale principale tramite il parametro {@code strict}.
     * * @param A La matrice sparsa di origine (formato CSC).
     * @param strict Se {@code true}, ignora la diagonale principale estraendo solo la parte
     * strettamente superiore.
     * Se {@code false}, include la diagonale.
     * @return Una nuova matrice sparsa CSC contenente gli elementi del triangolo superiore di A.
     */
    public static DMatrixSparseCSC triu(DMatrixSparseCSC A, boolean strict) {
        DMatrixSparseTriplet triplet = new DMatrixSparseTriplet(A.getNumRows(), A.getNumCols(), A.getNonZeroLength());

        for (int col = 0; col < A.getNumCols(); col++) {
            int idxStart = A.col_idx[col];
            int idxEnd = A.col_idx[col + 1];

            for (int i = idxStart; i < idxEnd; i++) {
                int row = A.nz_rows[i];
                double value = A.nz_values[i];


                if (strict ? row < col : row <= col) {
                    triplet.addItem(row, col, value);
                }
            }
        }

        DMatrixSparseCSC result = new DMatrixSparseCSC(A.getNumRows(), A.getNumCols(), triplet.getNonZeroLength());
        DConvertMatrixStruct.convert(triplet, result);
        return result;
    }

    /**
     * Inverte il segno di tutti gli elementi non nulli di una matrice sparsa (A = -A).
     * <p> Nota: Questo metodo modifica direttamente la matrice passata come argomento, sovrascrivendo i suoi valori con i nuovi valori invertiti.</p>
     * * @param A La matrice sparsa in cui invertire i segni. La matrice passata
     * verrà sovrascritta con i nuovi valori.
     */
    public static void changeSign(DMatrixSparseCSC A) {
        for (int i = 0; i < A.getNonZeroLength(); i++) {
            A.nz_values[i] = -A.nz_values[i];
        }
    }
}
