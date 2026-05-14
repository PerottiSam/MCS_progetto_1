package app;

import models.MatrixMarketReader;
import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import solvers.iterative.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

public class Application {
    public void run(){
        Scanner sc = new Scanner(System.in);
        sc.useLocale(Locale.US);

        System.out.println("Benvenuto nell'applicazione di risoluzione di sistemi lineari iterativi");

        String pathMtxA = getPathFile(sc);

        double tol = getTol(sc);

        DMatrixSparseCSC A = null;
        DMatrixRMaj xExact = null;
        DMatrixRMaj b = null;

        try {
            A = MatrixMarketReader.read(pathMtxA);
            int n = A.getNumRows();

            System.out.println("\nMatrice A caricata con successo.");

            xExact = new DMatrixRMaj(n, 1);
            CommonOps_DDRM.fill(xExact, 1.0);

            b = getVectorB(A, xExact, n);
            System.out.println("Vettore b calcolato come Ax.");


            IterativeSolver[] solvers = {
                    new JacobiSolver(),
                    new GaussSeidelSolver(),
                    new GradientSolver(),
                    new ConjugateGradientSolver()
            };

            System.out.println("\n\n");

            System.out.printf("%-20s | %-12s | %-12s | %-17s | %-18s%n",
                    "Metodo", "Iterazioni", "Tempo (s)", "Errore Relativo", "Errore Vero (su x)");

            System.out.println("-------------------------------------------------------------------------------------------");

            for (IterativeSolver solver : solvers) {

                String solverName = solver.getClass().getSimpleName().replace("Solver", "");

                SolverResult result = solver.solve(A, b, tol);

                double trueError = calculateTrueError(result.x, xExact);

                System.out.printf("%-20s | %-12d | %-12.6f | %-17.5e | %-18.5e%n",
                        solverName,
                        result.iterations,
                        result.time,
                        result.relativeError,
                        trueError);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String getPathFile(Scanner sc){
        Path path = null;
        boolean isFileValid = false;

        do {
            System.out.print("Inserire il percorso del file Matrix Market (.mtx) contenente la matrice A: ");
            String inputPath = sc.nextLine().trim();

            if (inputPath.isEmpty()) {
                System.out.println("Errore: Il percorso non può essere vuoto.\n");
                continue;
            }

            path = Paths.get(inputPath);

            if (!Files.exists(path)) {
                System.out.println("Errore: Il file non esiste nel percorso specificato.\n");
            } else if (!Files.isRegularFile(path)) {
                System.out.println("Errore: Il percorso indica una cartella, devi selezionare un file.\n");
            } else if (!inputPath.toLowerCase().endsWith(".mtx")) {
                System.out.println("Errore: Il file deve avere estensione .mtx.\n");
            } else {
                isFileValid = true;
            }

        } while (!isFileValid);

        System.out.println("File trovato: " + path.toAbsolutePath());
        return path.toString();
    }

    public double getTol(Scanner sc) {
        double tol = 0.0;

        while (true) {
            System.out.print("\nInserisci la tolleranza desiderata (es. 1e-10): ");


            if (sc.hasNextDouble()) {
                tol = sc.nextDouble();
                sc.nextLine();

                if (tol > 0 && tol < 1) {
                    break;
                } else {
                    System.out.println("Errore: La tolleranza deve essere un numero positivo minore di 1.");
                }
            } else {
                System.out.println("Errore: Formato non valido. Usa il punto per i decimali (es. 0.0001).");
                sc.nextLine();
            }

        }
        return tol;
    }

    public DMatrixRMaj getVectorB(DMatrixSparseCSC A, DMatrixRMaj xExact, int n){
        DMatrixRMaj b = new DMatrixRMaj(n, 1);
        CommonOps_DSCC.mult(A, xExact, b);
        return b;
    }

    /**
     * Calcola l'errore relativo tra la soluzione computata e la soluzione esatta.
     * Formula: ||x_computed - x_exact||_2 / ||x_exact||_2
     */
    private static double calculateTrueError(DMatrixRMaj xComputed, DMatrixRMaj xExact) {
        int n = xExact.getNumRows();
        DMatrixRMaj diff = new DMatrixRMaj(n, 1);

        // diff = x_computed - x_exact
        CommonOps_DDRM.subtract(xComputed, xExact, diff);

        double normDiff = NormOps_DDRM.normP2(diff);
        double normExact = NormOps_DDRM.normP2(xExact);

        return normDiff / normExact;
    }

}
