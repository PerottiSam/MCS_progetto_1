package solvers.direct;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;

/**
 * Classe di utilità per la risoluzione diretta di sistemi lineari triangolari.
 * <p>
 * Implementa gli algoritmi di sostituzione in avanti (forward) e all'indietro (backward).
 */
public class TriangularSolver {

    /**
     * Risolve il sistema lineare L * x = f tramite sostituzione in avanti (Forward Substitution).
     *
     * @param L Matrice dei coefficienti triangolare inferiore (formato sparso CSC).
     * @param f Vettore dei termini noti.
     * @param x Buffer pre-allocato in cui verrà salvato il vettore soluzione.
     */
    public static void solveLower(DMatrixSparseCSC L, DMatrixRMaj f, DMatrixRMaj x) {
        int n = L.getNumRows();

        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < i; j++) {
                sum += L.get(i, j) * x.get(j, 0);
            }
            double xi = (f.get(i, 0) - sum) / L.get(i, i);
            x.set(i, 0, xi);
        }
    }

    /**
     * Risolve il sistema lineare U * x = f tramite sostituzione all'indietro (Backward Substitution).
     *
     * @param U Matrice dei coefficienti triangolare superiore (formato sparso CSC).
     * @param f Vettore dei termini noti.
     * @param x Buffer pre-allocato in cui verrà salvato il vettore soluzione.
     */
    public static void solveUpper(DMatrixSparseCSC U, DMatrixRMaj f, DMatrixRMaj x) {
        int n = U.getNumRows();

        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            // Calcola il prodotto scalare usando le x già trovate
            for (int j = i + 1; j < n; j++) {
                sum += U.get(i, j) * x.get(j, 0);
            }
            // Isola l'incognita sulla diagonale
            double xi = (f.get(i, 0) - sum) / U.get(i, i);
            x.set(i, 0, xi);
        }
    }
}