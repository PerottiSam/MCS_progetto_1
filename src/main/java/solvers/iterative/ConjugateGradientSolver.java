package solvers.iterative;

import models.IterationResult;
import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;

import static Helpers.Costants.MAX_ITER;


/**
 * Implementazione del metodo iterativo del Gradiente Coniugato (CG).
 * <p>
 * Questo solutore è impiegato per risolvere sistemi lineari Ax = b in cui la
 * matrice dei coefficienti A è simmetrica e definita positiva.
 */

public class ConjugateGradientSolver extends AbstractIterativeSolver {

    /**
     * Esegue il metodo iterativo del Gradiente Coniugato per risolvere il sistema lineare Ax = b.
     * <p>
     * L'algoritmo calcola il residuo internamente senza allocazioni extra e si arresta quando
     * l'errore relativo scende sotto la tolleranza ({@code tol}) o si raggiunge il limite di iterazioni ({@code MAX_ITER}).
     *
     * @param A     Matrice dei coefficienti (formato sparso CSC).
     * @param b     Vettore dei termini noti.
     * @param normB Norma euclidea precalcolata di b (usata per calcolare l'errore).
     * @param tol   Tolleranza per il criterio di convergenza.
     * @return      Oggetto {@link IterationResult} con soluzione, iterazioni, stato di convergenza ed errore finale.
     */
    @Override
    protected IterationResult performIterations(DMatrixSparseCSC A, DMatrixRMaj b, double normB, double tol) {

        int n = A.getNumRows();

        DMatrixRMaj x = new DMatrixRMaj(n, 1);

        DMatrixRMaj r = b.copy();

        DMatrixRMaj p = b.copy();

        //per memorizzare A*p
        DMatrixRMaj Ap = new DMatrixRMaj(n, 1);

        //calcola il prodotto scalare r^T * r
        double rsold = VectorVectorMult_DDRM.innerProd(r, r);

        double currentRelativeError = 0.0;

        int iter = 0;

        while(iter < MAX_ITER ){
            currentRelativeError = Math.sqrt(rsold) / normB;
            if (currentRelativeError < tol) {
                break; // convergenza raggiunta, esci dal ciclo
            }

            // Ap = A * p
            CommonOps_DSCC.mult(A, p, Ap);

            //alpha = (r^T * r) / (p^T * A * p)
            double pAp = VectorVectorMult_DDRM.innerProd(p, Ap);
            double alpha = rsold / pAp;

            //x = x + alpha * p
            CommonOps_DDRM.addEquals(x, alpha, p);

            //r = r - alpha * Ap
            CommonOps_DDRM.addEquals(r, -alpha, Ap);

            //calcola il nuovo prodotto scalare r^T * r
            double rsnew = VectorVectorMult_DDRM.innerProd(r, r);

            //beta = (r_{k+1}^T * r_{k+1}) / (r_k^T * r_k)
            double beta = rsnew / rsold;

            //p = r + beta * p
            CommonOps_DDRM.add(r, beta, p, p);

            rsold = rsnew;

            iter++;

        }

        return new IterationResult(x, iter, iter < MAX_ITER, currentRelativeError);
    }

}
