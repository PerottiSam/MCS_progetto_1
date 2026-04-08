
package solvers.iterative;

import helpers.Constants;
import models.IterationResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;

public class JacobiSolver extends AbstractIterativeSolver {

    @Override
    protected IterationResult performIterations(DMatrixSparseCSC A, DMatrixRMaj b, double normB, double tol) {
        final int n = A.getNumRows();

        // Vettori di stato (x e xOld)
        DMatrixRMaj x = new DMatrixRMaj(n, 1);
        double[] xData = x.data;
        double[] xOldData = new double[n]; // Array primitivo per massime performance

        // Pre-calcolo della diagonale inversa
        // Essendo A simmetrica definita positiva, la diagonale non ha zeri.
        double[] invDiag = new double[n];
        extractInverseDiagonal(A, invDiag);

        double[] bData = b.data;
        int iter = 0;
        double relError = Double.MAX_VALUE;

        // Alloca i buffer per il calcolo di Ax e del residuo, evitando allocazioni ripetute
        DMatrixRMaj bufferAx = new DMatrixRMaj(n, 1);
        DMatrixRMaj bufferRes = new DMatrixRMaj(n, 1);

        while (iter < Constants.MAX_ITER) {
            // Copia dell'array
            System.arraycopy(xData, 0, xOldData, 0, n);

            // Ciclo Jacobi
            for (int i = 0; i < n; i++) {
                double sum = 0.0;

                // Accesso ai dati CSC
                // Poiché la matrice è simmetrica, A(i, j) == A(j, i).
                // In CSC le colonne sono contigue, quindi iterare sulla colonna 'i'
                // è come iterare sulla riga 'i' in formato CSR.
                int start = A.col_idx[i];
                int end = A.col_idx[i + 1];

                for (int k = start; k < end; k++) {
                    int j = A.nz_rows[k];
                    if (i != j) {
                        sum += A.nz_values[k] * xOldData[j];
                    }
                }

                // Moltiplicazione per l'inverso della diagonale (pre-calcolato)
                xData[i] = (bData[i] - sum) * invDiag[i];
            }

            // Calcolo dell'errore (||Ax - b|| / ||b||)
            relError = calculateRelativeError(A, x, b, normB, bufferAx, bufferRes);

            if (relError < tol) {
                return new IterationResult(x, iter + 1, true, relError);
            }

            iter++;
        }

        return new IterationResult(x, iter, false, relError);
    }

    /**
     * Estrae gli elementi diagonali e ne calcola l'inverso.
     * Ottimizzato per il formato CSC.
     */
    private void extractInverseDiagonal(DMatrixSparseCSC A, double[] invDiag) {
        int n = A.getNumRows();
        for (int i = 0; i < n; i++) {
            boolean found = false;
            for (int k = A.col_idx[i]; k < A.col_idx[i + 1]; k++) {
                if (A.nz_rows[k] == i) {
                    double val = A.nz_values[k];
                    if (Math.abs(val) < 1e-18) {
                        throw new ArithmeticException("Diagonale nulla o troppo piccola alla riga " + i);
                    }
                    invDiag[i] = 1.0 / val;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Matrice con diagonale mancante alla riga " + i);
            }
        }
    }
}