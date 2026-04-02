package models;


import org.ejml.data.DMatrixRMaj;

public class SolverResult {
    public DMatrixRMaj x;
    public int iterations;
    public double time;
    public boolean converged;
    public double relativeError;

    public SolverResult(DMatrixRMaj x, int iterations, double time,
                        boolean converged, double relativeError) {
        this.x = x;
        this.iterations = iterations;
        this.time = time;
        this.converged = converged;
        this.relativeError = relativeError;
    }
}