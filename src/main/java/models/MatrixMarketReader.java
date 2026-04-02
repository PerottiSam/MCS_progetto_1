package models;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.DConvertMatrixStruct;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MatrixMarketReader {

    /**
     * Legge un file .mtx e lo converte in una matrice sparsa CSC di EJML.
     * * @param filePath Il percorso del file .mtx
     * @return La matrice DMatrixSparseCSC
     * @throws IOException Se ci sono problemi di lettura del file
     */
    public static DMatrixSparseCSC read(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isSymmetric = false;

            // 1. Lettura dell'Header
            line = br.readLine();
            if (line == null || !line.startsWith("%%MatrixMarket")) {
                throw new IOException("Il file non sembra essere in formato Matrix Market valido.");
            }
            if (line.toLowerCase().contains("symmetric")) {
                isSymmetric = true;
            }

            // 2. Salta i commenti
            while ((line = br.readLine()) != null && line.startsWith("%")) {
                // Ignora i commenti
            }

            // 3. Lettura delle dimensioni (righe, colonne, numero di valori non nulli)
            String[] dims = line.trim().split("\\s+");
            int rows = Integer.parseInt(dims[0]);
            int cols = Integer.parseInt(dims[1]);
            int nnzFile = Integer.parseInt(dims[2]); // Non-zeros scritti nel file

            // Se è simmetrica, devo allocare spazio fino al doppio degli elementi
            int estimatedNnz = isSymmetric ? nnzFile * 2 : nnzFile;

            // Struttura intermedia per l'inserimento "riga, colonna, valore"
            DMatrixSparseTriplet triplet = new DMatrixSparseTriplet(rows, cols, estimatedNnz);

            // 4. Lettura dei dati riga per riga
            for (int i = 0; i < nnzFile; i++) {
                line = br.readLine();
                if (line == null) break;

                String[] parts = line.trim().split("\\s+");

                // Sottrago 1 per passare da 1-based (MTX) a 0-based
                int r = Integer.parseInt(parts[0]) - 1;
                int c = Integer.parseInt(parts[1]) - 1;
                double val = Double.parseDouble(parts[2]);

                // Aggiungo l'elemento
                triplet.addItem(r, c, val);

                // Se la matrice è simmetrica e non siamo sulla diagonale, aggiungiamo lo speculare
                if (isSymmetric && r != c) {
                    triplet.addItem(c, r, val);
                }
            }

            // 5. Conversione finale in CSC (Compressed Sparse Column)
            DMatrixSparseCSC csc = new DMatrixSparseCSC(rows, cols, triplet.getNonZeroLength());
            DConvertMatrixStruct.convert(triplet, csc);

            return csc;
        }
    }
}
