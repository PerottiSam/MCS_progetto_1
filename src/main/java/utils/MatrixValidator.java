package utils;

import exceptions.InvalidMatrixException;
import exceptions.MatrixConditionException;
import org.ejml.data.DMatrix;

/**
 * La seguente classe valida i dati in ingresso ai solutori iterativi
 */
public class MatrixValidator {

    // Costruttore privato per impedire l'istanziazione
    private MatrixValidator() {
        throw new UnsupportedOperationException("non puo estere instanziata");
    }

    /**
     * Controlla che la matrice sia quadrata (NxN).
     */
    public static void checkSquareMatrix(DMatrix matrix) throws InvalidMatrixException {
        if (matrix.getNumRows() != matrix.getNumCols()) {
            throw new InvalidMatrixException("Errore dimensionale: la matrice deve essere quadrata. Trovata "
                    + matrix.getNumRows() + "x" + matrix.getNumCols() + ".");
        }
    }

    /**
     * Controlla che le dimensioni della matrice A e del vettore b siano compatibili per il sistema Ax=b.
     */
    public static void checkCompatibility(DMatrix A, DMatrix b) throws InvalidMatrixException {
        if (A.getNumRows() != b.getNumRows()) {
            throw new InvalidMatrixException("Incompatibilità: la matrice ha " + A.getNumRows() +
                    " righe, ma il vettore dei termini noti ha dimensione " + b.getNumRows() + ".");
        }
    }

    /**
     * Controlla che non ci siano zeri sulla diagonale principale.
     */
    public static void checkNoZeroOnDiagonal(DMatrix matrix) throws MatrixConditionException {
        int n = Math.min(matrix.getNumRows(), matrix.getNumCols());
        for (int i = 0; i < n; i++) {
            if (matrix.get(i, i) == 0.0) {
                throw new MatrixConditionException("Elemento nullo sulla diagonale principale all'indice ["
                        + i + "][" + i + "]. Impossibile procedere con l'algoritmo.");
            }
        }
    }

    /**
     * Verifica se la matrice è Simmetrica Definita Positiva (SPD).
     */
    public static void checkSPD(DMatrix matrix) throws MatrixConditionException {
        checkSymmetry(matrix);
        checkPositiveDiagonal(matrix);
    }

    /**
     * Verifica la simmetria: A[i][j] deve essere uguale a A[j][i].
     */
    public static void checkSymmetry(DMatrix matrix) throws MatrixConditionException {
        int n = matrix.getNumRows();
        double epsilon = 1e-15; // Tolleranza per errori di floating point

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(matrix.get(i, j) - matrix.get(j, i)) > epsilon) {
                    throw new MatrixConditionException("La matrice non è simmetrica agli indici ["
                            + i + "][" + j + "] e [" + j + "][" + i + "].");
                }
            }
        }
    }

    /**
     * Verifica che tutti gli elementi sulla diagonale principale siano strettamente positivi.
     */
    public static void checkPositiveDiagonal(DMatrix matrix) throws MatrixConditionException {
        int n = Math.min(matrix.getNumRows(), matrix.getNumCols());
        for (int i = 0; i < n; i++) {
            if (matrix.get(i, i) <= 0) {
                throw new MatrixConditionException("La matrice non è definita positiva: " +
                        "trovato elemento non positivo sulla diagonale all'indice [" + i + "].");
            }
        }
    }

    /**
     * Verifica se la matrice è a dominanza diagonale stretta (per righe).
     * Ritorna true se lo è, false altrimenti.
     */
    public static boolean isDiagonallyDominant(DMatrix matrix) {
        int rows = matrix.getNumRows();
        int cols = matrix.getNumCols();

        for (int i = 0; i < rows; i++) {
            double diag = Math.abs(matrix.get(i, i));
            double sum = 0.0;

            for (int j = 0; j < cols; j++) {
                if (i != j) {
                    sum += Math.abs(matrix.get(i, j));
                }
            }

            // Se la somma degli elementi extra-diagonali è maggiore o uguale
            // all'elemento sulla diagonale, non è strettamente dominante.
            if (diag <= sum) {
                return false;
            }
        }
        return true;
    }
}