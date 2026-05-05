package app;

import models.MatrixMarketReader;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
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
            System.out.println("Vettore b calcolato come Ax, pronto per essere usato nei solutori iterativi.");


            IterativeSolver[] solvers = {
                    new JacobiSolver(),
                    new GaussSeidelSolver(),
                    //new GradientSolver(),
                    new ConjugateGradientSolver()
            };

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String getPathFile(Scanner sc){
        sc.nextLine();

        Path path = null;
        boolean isFileValid = false;

        do {
            System.out.print("Per favore, inserisci il percorso del file Matrix Market (.mtx) contenente la matrice A: ");
            String inputPath = sc.nextLine().trim();
            sc.nextLine();

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
        sc.nextLine();
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

}
