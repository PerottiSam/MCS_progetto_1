package solvers.iterative;

import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;

public interface IterativeSolver {
    SolverResult solve(DMatrixSparseCSC A, DMatrixRMaj b, double tol);
}
