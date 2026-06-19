package solvers.iterative;

import models.IterationResult;
import utils.Constants;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import utils.MatrixValidator;

/**
 * Implementazione del metodo iterativo del Gradiente (Steepest Descent)
 *
 * <p>Ad ogni iterazione aggiorna la soluzione lungo la direzione
 * del gradiente del residuo.</p>
 *
 * <p>La convergenza è controllata tramite l'errore relativo:
 * ||Ax - b|| / ||b||.</p>
 */
public class GradientSolver extends AbstractIterativeSolver {

    /**
     * Esegue le iterazioni del metodo del gradiente fino al raggiungimento
     * della tolleranza desiderata o del numero massimo di iterazioni.
     *
     * @param A matrice sparsa del sistema (n x n)
     * @param b vettore dei termini noti (n x 1)
     * @param normB norma di b (usata per il calcolo dell'errore relativo)
     * @param tol tolleranza per il criterio di arresto
     * @return risultato dell'iterazione contenente:
     *         <ul>
     *             <li>la soluzione approssimata</li>
     *             <li>il numero di iterazioni eseguite</li>
     *             <li>flag di convergenza</li>
     *             <li>errore relativo finale</li>
     *         </ul>
     */
    @Override
    protected IterationResult performIterations
            (DMatrixSparseCSC A,DMatrixRMaj b,double normB, double tol) {

        MatrixValidator.checkSPD(A);

        int n = b.getNumRows();

        // Vettore soluzione inizializzato a zero
        DMatrixRMaj x = new DMatrixRMaj(n, 1);

        // Residuo r = b - Ax
        DMatrixRMaj residual = new DMatrixRMaj(n, 1);

        // Vettore temporaneo per A*r
        DMatrixRMaj Ar = new DMatrixRMaj(n, 1);

        // Alloca i buffer per il calcolo di Ax e del residuo, evitando allocazioni ripetute
        DMatrixRMaj bufferAx = new DMatrixRMaj(n, 1);
        DMatrixRMaj bufferRes = new DMatrixRMaj(n, 1);

        int iter = 0;
         double relError = Double.MAX_VALUE;

         IterationResult result = new IterationResult(x, 0, false, relError);

         while (iter < Constants.MAX_ITER && relError > tol) {

             // residual = b - A*x
             CommonOps_DSCC.mult(A, x, residual);      // residual = A*x
             CommonOps_DDRM.subtract(b, residual, residual); // residual = b - A*x

             // Ar = A * residual
             CommonOps_DSCC.mult(A, residual, Ar);

             // step = (r^T r) / (r^T A r)
             double numerator = CommonOps_DDRM.dot(residual, residual);
             double denominator = CommonOps_DDRM.dot(residual, Ar);

             double step = numerator / denominator;

             // x = x + step * residual
             CommonOps_DDRM.addEquals(x, step, residual);

             // errore relativo ||Ax - b|| / ||b||
             relError = calculateRelativeError(A, x, b, normB, bufferAx, bufferRes);

             result.addResidual(relError);
             iter++;
         }

         boolean converged = relError <= tol;

         result.iterations = iter;
         result.converged = converged;
         result.relativeError = relError;
         return result;
    }

    /**
     * Restituisce una rappresentazione testuale del solver.
     *
     * @return nome del solver
     */
    @Override
    public String toString() {
        return "GradientSolver";
    }
}