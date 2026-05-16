package app;

import models.MatrixMarketReader;
import models.SolverResult;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import solvers.iterative.*;
import utils.ChartGenerator;
import utils.TableGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Application {

    private static final double[] DEFAULT_TOLERANCES = {1e-4, 1e-6, 1e-8, 1e-10};

    // Cartella di output per grafici e tabelle
    private File chartsOutputDir;

    public Application() {
        // Inizializza la cartella di output nella stessa posizione dell'eseguibile
        initializeChartsDirectory();
    }

    /**
     * Inizializza la cartella di output per grafici e tabelle.
     * La cartella si trova nella stessa posizione dell'eseguibile.
     */
    private void initializeChartsDirectory() {
        try {
            // Ottiene il percorso dell'eseguibile (working directory)
            String workingDir = System.getProperty("user.dir");

            // Creiamo una sottocartella per ogni esecuzione con timestamp, dentro la cartella 'charts'
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new Date());

            File chartsRoot = new File(workingDir, "charts");
            chartsOutputDir = new File(chartsRoot, timestamp);

            // Crea la cartella di esecuzione se non esiste
            if (!chartsOutputDir.exists() && !chartsOutputDir.mkdirs()) {
                throw new IllegalStateException("Impossibile creare la cartella charts: " + chartsOutputDir.getAbsolutePath());
            }

            if (chartsOutputDir.exists()) {
                System.out.println("✓ Cartella di output creata in: " + chartsOutputDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione della cartella charts: " + e.getMessage());
            chartsOutputDir = new File("charts");
        }
    }

    public void run(){
        Scanner sc = new Scanner(System.in);
        sc.useLocale(Locale.US);

        System.out.println("Benvenuto nell'applicazione di risoluzione di sistemi lineari iterativi");

        String pathMtxA = getPathFile(sc);

        double[] tols = getTol(sc);

        try {
            DMatrixSparseCSC A = MatrixMarketReader.read(pathMtxA);
            int n = A.getNumRows();

            System.out.println("\nMatrice A caricata con successo.");

            DMatrixRMaj xExact = new DMatrixRMaj(n, 1);
            CommonOps_DDRM.fill(xExact, 1.0);

            DMatrixRMaj b = getVectorB(A, xExact, n);
            System.out.println("Vettore b calcolato come Ax.");


            Map<Double, Map<String, SolverResult>> resultsByTolerance = new LinkedHashMap<>();
            Map<Double, Map<String, Double>> trueErrorsByTolerance = new LinkedHashMap<>();

            System.out.println("\nAvvio analisi matematica e risoluzione, attendere...");
            System.out.println("-------------------------------------------------------------------------------------------");

            for (double currentTol : tols) {
                IterativeSolver[] currentSolvers = createSolvers();
                SolverResult[] results = new SolverResult[currentSolvers.length];
                double[] trueErrors = new double[currentSolvers.length];

                System.out.printf(Locale.US, "\n>>> Tolleranza: %.2e%n", currentTol);

                for (int i = 0; i < currentSolvers.length; i++) {
                    String name = currentSolvers[i].getClass().getSimpleName().replace("Solver", "");
                    System.out.print("Esecuzione di " + name + "... ");
                    results[i] = currentSolvers[i].solve(A, b, currentTol);
                    trueErrors[i] = calculateTrueError(results[i].x, xExact);
                    System.out.println("[COMPLETATO]");
                }

                System.out.println();
                System.out.printf("%-20s | %-12s | %-12s | %-17s | %-18s%n",
                        "Metodo", "Iterazioni", "Tempo (s)", "Errore Relativo", "Errore Vero (su x)");
                System.out.println("-------------------------------------------------------------------------------------------");

                Map<String, SolverResult> resultsMap = new LinkedHashMap<>();
                Map<String, Double> trueErrorsMap = new LinkedHashMap<>();

                for (int i = 0; i < currentSolvers.length; i++) {
                    String solverName = currentSolvers[i].getClass().getSimpleName().replace("Solver", "");

                    System.out.printf(Locale.US, "%-20s | %-12d | %-12.6f | %-17.5e | %-18.5e%n",
                            solverName,
                            results[i].iterations,
                            results[i].time,
                            results[i].relativeError,
                            trueErrors[i]);

                    resultsMap.put(solverName, results[i]);
                    trueErrorsMap.put(solverName, trueErrors[i]);
                }

                resultsByTolerance.put(currentTol, resultsMap);
                trueErrorsByTolerance.put(currentTol, trueErrorsMap);
            }

            // Genera i grafici e le tabelle
            System.out.println("\n-------------------------------------------------------------------------------------------");
            System.out.println("Generazione di grafici e tabelle...");
            System.out.println("-------------------------------------------------------------------------------------------\n");

            generateChartsAndTables(pathMtxA, resultsByTolerance, trueErrorsByTolerance, tols);

        }catch (Exception e){
            System.err.println("Errore durante l'esecuzione: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    }

    /**
     * Genera i grafici e le tabelle per i risultati.
     */
    private void generateChartsAndTables(String pathMtxA,
                                        Map<Double, Map<String, SolverResult>> resultsByTolerance,
                                        Map<Double, Map<String, Double>> trueErrorsByTolerance,
                                        double[] tolerances) {
        try {
            // Ottiene il nome della matrice dal percorso del file
            String matrixName = new File(pathMtxA).getName().replace(".mtx", "");

            // Genera i grafici
            ChartGenerator chartGen = new ChartGenerator(chartsOutputDir);
            chartGen.generateChartsForMatrix(matrixName, resultsByTolerance, tolerances);

            // Genera le tabelle
            TableGenerator tableGen = new TableGenerator(chartsOutputDir);
            tableGen.generateTableForMatrix(matrixName, resultsByTolerance, tolerances, trueErrorsByTolerance);

            System.out.println("\n✓ Tutti i grafici e le tabelle sono stati salvati in:");
            System.out.println("   " + chartsOutputDir.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Errore durante la generazione dei grafici e delle tabelle: " + e.getMessage());
            e.printStackTrace(System.err);
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

    public double[] getTol(Scanner sc) {
        while (true) {
            System.out.print("\nInserisci la tolleranza desiderata (es. 1e-10). Premi invio per usare automaticamente [1e-4, 1e-6, 1e-8, 1e-10]: ");

            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                return DEFAULT_TOLERANCES.clone();
            }

            try {
                double tol = Double.parseDouble(input);
                if (tol > 0 && tol < 1) {
                    return new double[]{tol};
                }
                System.out.println("Errore: La tolleranza deve essere un numero positivo minore di 1.");
            } catch (NumberFormatException e) {
                System.out.println("Errore: Formato non valido. Usa il punto per i decimali (es. 0.0001).");
            }
        }
    }

    private IterativeSolver[] createSolvers() {
        return new IterativeSolver[]{
                new JacobiSolver(),
                new GaussSeidelSolver(),
                new GradientSolver(),
                new ConjugateGradientSolver()
        };
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
