package models;

import org.ejml.data.DMatrixRMaj;

public class IterationResult {
    public DMatrixRMaj x;
    public int iterations;
    public boolean converged;
    public double relativeError;

    public IterationResult(DMatrixRMaj x, int iterations, boolean converged, double relativeError) {
        this.x = x;
        this.iterations = iterations;
        this.converged = converged;
        this.relativeError = relativeError;
    }
}