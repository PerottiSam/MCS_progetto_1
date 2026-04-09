package solvers.iterative;

import models.IterationResult;
import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * Classe astratta base per l'implementazione dei solutori iterativi.
 * <p>
 * Utilizza il pattern <i>Template Method</i>: orchestra il flusso generale
 * della risoluzione (c alcolo preventivo della norma, misurazione dei tempi,
 * controllo convergenza globale e formattazione del risultato), delegando
 * la logica matematica pura al metodo astratto implementato dalle sottoclassi.
 */
public abstract class AbstractIterativeSolver implements IterativeSolver {

    /**
     * Avvia il processo di risoluzione del sistema lineare Ax = b.
     * <p>
     * Calcola la norma di partenza e cronometra l'esecuzione del solutore specifico.
     *
     * @param A   Matrice dei coefficienti (formato sparso CSC).
     * @param b   Vettore dei termini noti.
     * @param tol Tolleranza massima per l'errore relativo.
     * @return    Oggetto {@link SolverResult} con i dati finali di esecuzione.
     */
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

        // Ritorna il risultato finale con la soluzione, numero di iterazioni, tempo, stato di convergenza ed errore relativo
        return new SolverResult(
                result.x,
                result.iterations,
                timeInSeconds,
                result.converged,
                result.relativeError
        );
    }

    /**
     * Metodo astratto che contiene la logica algoritmica specifica (es. Jacobi, Gauss-Seidel, etc).
     *
     * @param A     Matrice dei coefficienti (formato sparso CSC).
     * @param b     Vettore dei termini noti.
     * @param normB Norma euclidea precalcolata di b.
     * @param tol   Tolleranza per il criterio di convergenza.
     * @return      Oggetto {@link IterationResult} contenente lo stato finale del ciclo iterativo.
     */
    protected abstract IterationResult performIterations(
            DMatrixSparseCSC A,
            DMatrixRMaj b,
            double normB,
            double tol
    );

    /**
     * Calcola l'errore relativo (||Ax - b||_2 / ||b||_2).
     * <p>
     *
     * @param A              Matrice dei coefficienti.
     * @param x              Vettore soluzione all'iterazione corrente.
     * @param b              Vettore dei termini noti.
     * @param normB          Norma euclidea precalcolata di b.
     * @param bufferAx       Buffer pre-allocato di supporto (conterrà A * x).
     * @param bufferResidual Buffer pre-allocato di supporto (conterrà Ax - b).
     * @return               L'errore relativo corrente.
     */
    protected double calculateRelativeError(
            DMatrixSparseCSC A,
            DMatrixRMaj x,
            DMatrixRMaj b,
            double normB,
            DMatrixRMaj bufferAx,
            DMatrixRMaj bufferResidual
    ) {
        // Ax = A * x (Sfrutta l'operazione specializzata per matrici sparse)
        CommonOps_DSCC.mult(A, x, bufferAx);

        // residual = Ax - b
        CommonOps_DDRM.subtract(bufferAx, b, bufferResidual);

        // Ritorna ||residual||_2 / ||b||_2
        return NormOps_DDRM.normP2(bufferResidual) / normB;
    }
}