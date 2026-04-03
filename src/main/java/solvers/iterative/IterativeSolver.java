package solvers.iterative;

import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;

/**
 * Interfaccia comune per tutti i solutori iterativi di sistemi lineari.
 * <p>
 * Definisce il contratto base per la risoluzione di sistemi Ax = b, permettendo
 * di utilizzare il polimorfismo per eseguire e confrontare algoritmi diversi
 * in modo standardizzato.
 */
public interface IterativeSolver {

    /**
     * Risolve il sistema lineare Ax = b applicando il metodo iterativo specifico.
     *
     * @param A   La matrice dei coefficienti del sistema (formato sparso CSC).
     * @param b   Il vettore colonna dei termini noti.
     * @param tol La tolleranza massima accettata per l'errore relativo.
     * @return    Un oggetto {@link SolverResult} contenente la soluzione finale, i tempi
     * di esecuzione, il numero di iterazioni e le statistiche di convergenza.
     */
    SolverResult solve(DMatrixSparseCSC A, DMatrixRMaj b, double tol);
}