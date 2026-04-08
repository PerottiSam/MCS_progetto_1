package solvers.iterative;



import models.IterationResult;
import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;

public abstract class AbstractIterativeSolver implements IterativeSolver {
    @Override
    public SolverResult solve(DMatrixSparseCSC A, DMatrixRMaj b, double tol) {
        long startTime = System.nanoTime();

        int n = A.getNumRows();

        double normB = NormOps_DDRM.normP2(b);
        if (normB == 0.0) {
            return new SolverResult(new DMatrixRMaj(n, 1), 0, 0.0, true, 0.0);
        }

        // Esegue le iterazioni specifiche del solutore concreto e ottiene il risultato
        IterationResult result = performIterations(A, b, normB, tol);

        long endTime = System.nanoTime();
        double timeInSeconds = (endTime - startTime) / 1_000_000_000.0;

        // Non è convergente se abbiamo raggiunto il numero massimo di iterazioni senza soddisfare la tolleranza
        if (!result.converged) {
            System.out.println("WARNING: " + this.getClass().getSimpleName() +
                    " non è convergente (maxIter raggiunto).");
        }

        // Ritorna il risultato finale con la soluzione, numero di iterazioni, tempo, stato di convergenza e errore relativo
        return new SolverResult(
                result.x,
                result.iterations,
                timeInSeconds,
                result.converged,
                result.relativeError
        );
    }

    /**
     * Metodo che ogni solutore specifico dovrà implementare.
     * NOTA: Ho rimosso il parametro x come input per decouplare la logica di inizializzazione
     *       Meglio inizializzarlo dentro e resituirlo
     */
    protected abstract IterationResult performIterations(
            DMatrixSparseCSC A,
            DMatrixRMaj b,
            double normB,
            double tol
    );

    /**
     * Calcola l'errore relativo riutilizzando i buffer per evitare allocazioni GC.
     */
    protected double calculateRelativeError(
            DMatrixSparseCSC A,
            DMatrixRMaj x,
            DMatrixRMaj b,
            double normB,
            DMatrixRMaj bufferAx,      // Buffer pre-allocato
            DMatrixRMaj bufferResidual // Buffer pre-allocato
    ) {
        // Ax = A * x (Sfrutta l'operazione specializzata per matrici sparse)
        CommonOps_DSCC.mult(A, x, bufferAx);

        // residual = Ax - b
        CommonOps_DDRM.subtract(bufferAx, b, bufferResidual);

        // Ritorna ||residual||_2 / ||b||_2
        return NormOps_DDRM.normP2(bufferResidual) / normB;
    }
}