package utils;

import models.SolverResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Classe per generare tabelle HTML dai risultati dei solutori iterativi.
 */
public class TableGenerator {

    private final File outputDir;
    private final String timestamp;

    public TableGenerator(File outputDir) {
        this.outputDir = outputDir;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        this.timestamp = sdf.format(new Date());
    }

    /**
     * Genera una tabella HTML con i risultati per una matrice.
     *
     * @param matrixName Nome della matrice
     * @param resultsByTolerance Mappa dei risultati per ogni tolleranza
     * @param tolerances         Tolleranze analizzate
     * @param trueErrorsByTolerance Errori veri (sul vettore x esatto) per tolleranza
     */
    public void generateTableForMatrix(String matrixName,
                                       Map<Double, Map<String, SolverResult>> resultsByTolerance,
                                       double[] tolerances,
                                       Map<Double, Map<String, Double>> trueErrorsByTolerance) {
        StringBuilder html = new StringBuilder();

        // Inizio HTML
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <title>Risultati - ").append(matrixName).append("</title>\n");
        html.append("  <style>\n");
        html.append("    body { font-family: Arial, sans-serif; margin: 20px; background: #fafafa; color: #222; }\n");
        html.append("    h1 { color: #222; }\n");
        html.append("    .info { background-color: #fff; padding: 12px; margin-bottom: 20px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);}\n");
        html.append("    .container { max-width: 1200px; margin: 0 auto; }\n");
        html.append("    table { border-collapse: collapse; width: 100%; margin-top: 20px; font-family: 'Courier New', monospace; font-size: 14px; }\n");
        html.append("    th, td { border: 1px solid #e6e6e6; padding: 10px; text-align: right; }\n");
        html.append("    th { background-color: #2b6cb0; color: white; text-align: center; padding: 12px; }\n");
        html.append("    tr:nth-child(even) { background-color: #ffffff; }\n");
        html.append("    tr:nth-child(odd) { background-color: #f7fbff; }\n");
        html.append("    td:first-child, th:first-child { text-align: left; font-weight: 600; }\n");
        html.append("    .charts img { max-width: 100%; height: auto; border: 1px solid #ddd; margin-bottom: 12px; }\n");
        html.append("    .download { margin-top: 12px; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Intestazione
        html.append("<div class=\"container\">\n");
        html.append("<h1>Risultati della Risoluzione - ").append(matrixName).append("</h1>\n");
        html.append("<div class=\"info\">\n");
        html.append("<p><strong>Tolleranza:</strong> ");
        if (tolerances.length == 1) {
            html.append(formatTolerance(tolerances[0]));
        } else {
            html.append("batch ");
            html.append("[");
            for (int i = 0; i < tolerances.length; i++) {
                if (i > 0) {
                    html.append(", ");
                }
                html.append(formatTolerance(tolerances[i]));
            }
            html.append("]");
        }
        html.append("</p>\n");
        html.append("<p><strong>Data e Ora:</strong> ").append(timestamp).append("</p>\n");
        html.append("</div>\n");

        // Tabella
        html.append("<table>\n");
        html.append("  <thead>\n");
        html.append("    <tr>\n");
        html.append("      <th>Tolleranza</th>\n");
        html.append("      <th>Metodo</th>\n");
        html.append("      <th>Iterazioni</th>\n");
        html.append("      <th>Tempo (s)</th>\n");
        html.append("      <th>Convergente</th>\n");
        html.append("      <th>Errore Relativo</th>\n");
        html.append("      <th>Errore Vero (su x)</th>\n");
        html.append("    </tr>\n");
        html.append("  </thead>\n");
        html.append("  <tbody>\n");

        // Righe della tabella
        for (double tol : tolerances) {
            Map<String, SolverResult> results = resultsByTolerance.get(tol);
            if (results == null) {
                continue;
            }

            Map<String, Double> trueErrors = trueErrorsByTolerance.getOrDefault(tol, new java.util.HashMap<>());

            for (String solverName : results.keySet()) {
                SolverResult result = results.get(solverName);
                Double trueError = trueErrors.getOrDefault(solverName, 0.0);

                html.append("    <tr>\n");
                html.append("      <td>").append(formatTolerance(tol)).append("</td>\n");
                html.append("      <td><strong>").append(solverName).append("</strong></td>\n");
                html.append("      <td>").append(result.iterations).append("</td>\n");
                html.append("      <td>").append(String.format(Locale.US, "%.6f", result.time)).append("</td>\n");
                html.append("      <td>").append(result.converged ? "Sì" : "No").append("</td>\n");
                html.append("      <td>").append(String.format(Locale.US, "%.5e", result.relativeError)).append("</td>\n");
                html.append("      <td>").append(String.format(Locale.US, "%.5e", trueError)).append("</td>\n");
                html.append("    </tr>\n");
            }
        }

        html.append("  </tbody>\n");
        html.append("</table>\n");

        // Includi eventuali grafici generati per questa matrice
        File[] images = outputDir.listFiles((ignoredDir, name) -> name.toLowerCase().endsWith(".png") && name.contains(matrixName));
        if (images != null && images.length > 0) {
            html.append("<h2>Grafici</h2>\n");
            html.append("<div class=\"charts\">\n");
            for (File img : images) {
                html.append("  <div>\n");
                html.append("    <h3>").append(img.getName()).append("</h3>\n");
                html.append("    <img src=\"").append(img.getName()).append("\" alt=\"").append(img.getName()).append("\">\n");
                html.append("  </div>\n");
            }
            html.append("</div>\n");
        }

        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        // Salva il file HTML
        String filename = String.format("Tabella_%s_%s.html", matrixName, timestamp);
        try {
            File file = new File(outputDir, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(html.toString());
            writer.close();
            System.out.println("✓ Tabella HTML salvata: " + filename);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della tabella HTML: " + e.getMessage());
        }

        // Salva anche una versione CSV per questa matrice (utile per importare in Excel)
        String csvName = String.format("Tabella_%s_%s.csv", matrixName, timestamp);
        StringBuilder csv = new StringBuilder();
        csv.append("Tolleranza,Metodo,Iterazioni,Tempo(s),Convergente,Errore Relativo,Errore Vero\n");
        for (double tol : tolerances) {
            Map<String, SolverResult> results = resultsByTolerance.get(tol);
            if (results == null) {
                continue;
            }

            Map<String, Double> trueErrors = trueErrorsByTolerance.getOrDefault(tol, new java.util.HashMap<>());

            for (String solverName : results.keySet()) {
                SolverResult result = results.get(solverName);
                Double trueError = trueErrors.getOrDefault(solverName, 0.0);
                csv.append(formatTolerance(tol)).append(",")
                        .append(solverName).append(",")
                        .append(result.iterations).append(",")
                        .append(String.format(Locale.US, "%.6f", result.time)).append(",")
                        .append(result.converged ? "Sì" : "No").append(",")
                        .append(String.format(Locale.US, "%.5e", result.relativeError)).append(",")
                        .append(String.format(Locale.US, "%.5e", trueError)).append("\n");
            }
        }

        try {
            File csvFile = new File(outputDir, csvName);
            FileWriter csvWriter = new FileWriter(csvFile);
            csvWriter.write(csv.toString());
            csvWriter.close();
            System.out.println("✓ Tabella CSV salvata: " + csvName);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della tabella CSV: " + e.getMessage());
        }
    }

    /**
     * Genera una tabella CSV con i risultati per tutte le matrici.
     *
     * @param allResults Mappa delle matrici con i loro risultati
     * @param allTrueErrors Mappa degli errori veri per tutte le matrici
     */
    @SuppressWarnings("unused")
    public void generateSummaryTable(Map<String, Map<String, SolverResult>> allResults,
                                     Map<String, Map<String, Double>> allTrueErrors) {
        StringBuilder csv = new StringBuilder();

        // Intestazione CSV
        csv.append("Matrice,Metodo,Iterazioni,Tempo(s),Convergente,Errore Relativo,Errore Vero\n");

        // Dati CSV
        for (String matrixName : allResults.keySet()) {
            Map<String, SolverResult> results = allResults.get(matrixName);
            Map<String, Double> trueErrors = allTrueErrors.getOrDefault(matrixName, new java.util.HashMap<>());

            for (String solverName : results.keySet()) {
                SolverResult result = results.get(solverName);
                Double trueError = trueErrors.getOrDefault(solverName, 0.0);

                csv.append(matrixName).append(",")
                        .append(solverName).append(",")
                        .append(result.iterations).append(",")
                        .append(String.format("%.6f", result.time)).append(",")
                        .append(result.converged ? "Yes" : "No").append(",")
                        .append(String.format("%.5e", result.relativeError)).append(",")
                        .append(String.format("%.5e", trueError)).append("\n");
            }
        }

        // Salva il file
        String filename = String.format("Risultati_Completi_%s.csv", timestamp);
        try {
            File file = new File(outputDir, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(csv.toString());
            writer.close();
            System.out.println("✓ Tabella CSV salvata: " + filename);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della tabella CSV: " + e.getMessage());
        }
    }

    private String formatTolerance(double tol) {
        return String.format(Locale.US, "%.0e", tol).replace("E", "e");
    }
}

