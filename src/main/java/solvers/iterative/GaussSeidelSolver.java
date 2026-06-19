package solvers.iterative;

import exceptions.SolverException;
import models.IterationResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import solvers.direct.TriangularSolver;
import utils.MatrixUtils;
import utils.MatrixValidator;

import static utils.Constants.MAX_ITER;

/**
 * Implementazione del metodo iterativo di Gauss-Seidel.
 * <p>
 * Risolve sistemi lineari Ax = b.
 */
public class GaussSeidelSolver extends AbstractIterativeSolver {

    /**
     * Esegue il metodo iterativo di Gauss-Seidel per risolvere il sistema lineare Ax = b.
     * <p>
     * L'algoritmo si arresta quando l'errore relativo (||Ax - b||_2 / ||b||_2)
     * scende sotto la tolleranza indicata ({@code tol}) o si raggiunge il limite massimo di iterazioni ({@code MAX_ITER}).
     *
     * @param A     Matrice dei coefficienti (formato sparso CSC).
     * @param b     Vettore dei termini noti.
     * @param normB Norma euclidea precalcolata di b (usata per calcolare l'errore).
     * @param tol   Tolleranza per il criterio di convergenza.
     * @return      Oggetto {@link IterationResult} con soluzione, iterazioni, stato di convergenza ed errore finale.
     */

    @Override
    protected IterationResult performIterations(DMatrixSparseCSC A, DMatrixRMaj b, double normB, double tol) {

        //se ci sono zeri sulla diagonale viene lanciata un eccezione
        MatrixValidator.checkNoZeroOnDiagonal(A);

        // Controllo convergenza (Condizioni sufficienti)
        boolean isSPD = false;
        try {
            MatrixValidator.checkSPD(A);
            isSPD = true;
        } catch (SolverException ignored) {
            // Se non è SPD, si procede con la verifica della dominanza diagonale
        }

        if (!isSPD && !MatrixValidator.isDiagonallyDominant(A)) {
            System.err.println("WARNING [GaussSeidel]: La matrice non è né SPD né a dominanza diagonale. " +
                    "La convergenza non è garantita.");
        }

        int n = A.getNumRows();

        // Inizializzazione: x parte dal vettore nullo
        DMatrixRMaj x = new DMatrixRMaj(n, 1);

        // ALLOCAZIONE BUFFER
        //contiene A * x
        DMatrixRMaj bufferAx = new DMatrixRMaj(n, 1);

        //contiene Ax - b
        DMatrixRMaj bufferResidual = new DMatrixRMaj(n, 1);

        // Buffer per la formula y = N*x + b
        DMatrixRMaj y = new DMatrixRMaj(n, 1);

        //contiene la soluzione aggiornata ad ogni iterazione
        DMatrixRMaj xNew = new DMatrixRMaj(n, 1);

        // Estrazione delle componenti
        DMatrixSparseCSC P = MatrixUtils.tril(A);
        DMatrixSparseCSC N = MatrixUtils.triu(A, true);
        MatrixUtils.changeSign(N);


        int iter = 0;

         // Calcolo dell'errore relativo
         double currentRelativeError = calculateRelativeError(A, x, b, normB, bufferAx, bufferResidual);

         IterationResult result = new IterationResult(x, 0, false, currentRelativeError);

         while (iter < MAX_ITER && currentRelativeError >= tol) {

             // y = N * x
             CommonOps_DSCC.mult(N, x, y);

             // y = y + b
             CommonOps_DDRM.addEquals(y, b);

             // risoluzione del sistema triangolare inferiore P * x_new = y
             TriangularSolver.solveLower(P, y, xNew);

             // aggiornamento di x
             x.setTo(xNew);

             // update dell'errore relativo tramite i buffer
             currentRelativeError = calculateRelativeError(A, x, b, normB, bufferAx, bufferResidual);

             result.addResidual(currentRelativeError);
             iter++;
         }

         result.iterations = iter;
         result.converged = currentRelativeError < tol;
         result.relativeError = currentRelativeError;
         return result;
    }

    /**
     * Restituisce una rappresentazione testuale del solver.
     *
     * @return nome del solver
     */
    @Override
    public String toString() {
        return "GaussSeidelSolver";
    }
}
