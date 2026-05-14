
package solvers.iterative;

import utils.Constants;
import models.IterationResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import utils.MatrixValidator;

/**
 * Implementazione del metodo iterativo di Jacobi
 *
 * <p>Questa classe estende {@link AbstractIterativeSolver} e utilizza
 * una rappresentazione sparsa della matrice A in formato CSC
 * ({@link DMatrixSparseCSC}) per migliorare le performance su matrici sparse.</p>
 *
 * <p><b>Assunzioni:</b></p>
 * <ul>
 *   <li>La matrice A è simmetrica definita positiva (SPD).</li>
 *   <li>Gli elementi diagonali sono tutti non nulli.</li>
 * </ul>
 */
public class JacobiSolver extends AbstractIterativeSolver {

    /**
     * Esegue le iterazioni del metodo di Jacobi fino al raggiungimento
     * della tolleranza richiesta o del numero massimo di iterazioni.
     *
     * @param A matrice dei coefficienti (sparsa in formato CSC)
     * @param b vettore dei termini noti
     * @param normB norma del vettore b (usata per errore relativo)
     * @param tol tolleranza per il criterio di arresto
     * @return risultato dell'iterazione contenente:
     *         <ul>
     *             <li>la soluzione approssimata</li>
     *             <li>numero di iterazioni eseguite</li>
     *             <li>flag di convergenza</li>
     *             <li>errore relativo finale</li>
     *         </ul>
     */
    @Override
    protected IterationResult performIterations(DMatrixSparseCSC A, DMatrixRMaj b, double normB, double tol) {

        MatrixValidator.checkSymmetry(A);
        MatrixValidator.checkNoZeroOnDiagonal(A);

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
     * Estrae gli elementi diagonali della matrice A e ne calcola l'inverso.
     *
     * <p>Il metodo è ottimizzato per matrici in formato CSC, iterando
     * direttamente sugli elementi non nulli di ciascuna colonna.</p>
     *
     * @param A matrice sparsa in formato CSC
     * @param invDiag array di output contenente gli inversi della diagonale
     *
     * @throws ArithmeticException se un elemento diagonale è nullo o troppo piccolo
     * @throws IllegalArgumentException se un elemento diagonale non viene trovato
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

    /**
     * Restituisce una rappresentazione testuale del solver.
     *
     * @return nome del solver
     */
    @Override
    public String toString() {
        return "JacobiSolver";
    }
}