import app.Application;



public class Main {
    public static void main(String[] args) {
        Application application = new Application();
        application.run();
    }
}



/*
public class Main {
    public static void main(String[] args) {
        // Matrici fornite nel progetto [cite: 31]
        String[] matrixFiles = {
                "src/main/resources/spa1.mtx",
        };

        double[] tolerances = {1e-6, 1e-8, 1e-10}; // [cite: 41]

        for (String filePath : matrixFiles) {
            try {
                DMatrixSparseCSC A = MatrixMarketReader.read(filePath);
                int n = A.numRows;

                // STEP 1: Soluzione esatta x = [1, 1, ..., 1] [cite: 33, 34]
                DMatrixRMaj xExact = new DMatrixRMaj(n, 1);
                Arrays.fill(xExact.data, 1.0);

                // STEP 2: Calcolo b = Ax [cite: 35, 36]
                DMatrixRMaj b = new DMatrixRMaj(n, 1);
                CommonOps_DSCC.mult(A, xExact, b);

                System.out.println("\n>>> Analisi Matrice: " + filePath + " (Dim: " + n + ")");

                for (double tol : tolerances) {
                    System.out.println("Tolleranza impostata: " + tol);

                    // STEP 3: Esecuzione del solutore [cite: 37]
                    // (Sostituisci con le istanze di Gauss-Seidel, Gradiente, CG)
                    JacobiSolver solver = new JacobiSolver();
                    SolverResult result = solver.solve(A, b, tol);

                    // STEP 4: Calcolo errore relativo finale rispetto alla x esatta [cite: 38]
                    double finalError = calculateTrueError(xExact, result.x);

                    // Stampa risultati richiesti
                    printRow(result, finalError);
                }
            } catch (IOException e) {
                System.err.println("Impossibile leggere " + filePath + ": " + e.getMessage());
            }
        }
    }

    private static double calculateTrueError(DMatrixRMaj xExact, DMatrixRMaj xComp) {
        double diffNormSq = 0;
        for (int i = 0; i < xExact.numRows; i++) {
            double diff = xExact.data[i] - xComp.data[i];
            diffNormSq += diff * diff;
        }
        // ||x_esatta|| per un vettore di soli 1 è sqrt(n)
        return Math.sqrt(diffNormSq) / Math.sqrt(xExact.numRows);
    }

    private static void printRow(SolverResult res, double trueError) {
        System.out.printf("%-15s | Iter: %-5d | Tempo: %-8.3f ms | Residuo: %.2e | Errore Vero: %.2e | Conv: %b%n",
                "jacobi", res.iterations, res.time, res.relativeError, trueError, res.converged);
    }
}*/

