package utils;

import models.SolverResult;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Classe per generare grafici dai risultati dei solutori iterativi.
 */
public class ChartGenerator {

    private static final int CHART_WIDTH = 1200;
    private static final int CHART_HEIGHT = 600;
    private final File outputDir;
    private final String timestamp;

    public ChartGenerator(File outputDir) {
        this.outputDir = outputDir;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        this.timestamp = sdf.format(new Date());
    }

    /**
     * Genera tutti i grafici richiesti per una matrice.
     *
     * @param matrixName       Nome della matrice
     * @param resultsByTol     Mappa dei risultati per ogni tolleranza
     * @param tolerances       Tolleranze analizzate
     */
    public void generateChartsForMatrix(String matrixName,
                                       Map<Double, Map<String, SolverResult>> resultsByTol,
                                       double[] tolerances) {
        // 1. Iterazioni vs Residuo relativo: un solo grafico con tutte le tolleranze
        generateIterationsResidualChart(matrixName, resultsByTol, tolerances);

        // 2. Tolleranza vs Tempo di calcolo
        generateToleranceTimeChart(matrixName, resultsByTol, tolerances);

        // 3. Tolleranza vs Errore relativo
        generateToleranceErrorChart(matrixName, resultsByTol, tolerances);
    }

    /**
     * Genera il grafico: Numero di iterazioni e residuo relativo
     */
    private void generateIterationsResidualChart(String matrixName,
                                                 Map<Double, Map<String, SolverResult>> resultsByTol,
                                                 double[] tolerances) {
        XYChart chart = new XYChartBuilder()
                .width(CHART_WIDTH)
                .height(CHART_HEIGHT)
                .title("Iterazioni vs Residuo Relativo - " + matrixName)
                .xAxisTitle("Numero di Iterazioni")
                .yAxisTitle("Residuo Relativo (scala log)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setYAxisLogarithmic(true);

        double[] sortedTolerances = tolerances.clone();
        Arrays.sort(sortedTolerances);

        Map<String, SolverResult> referenceResults = firstAvailableResults(resultsByTol, sortedTolerances);
        if (referenceResults == null) {
            return;
        }

        for (double tol : sortedTolerances) {
            Map<String, SolverResult> results = resultsByTol.get(tol);
            if (results == null) {
                continue;
            }

            for (String solverName : referenceResults.keySet()) {
                SolverResult result = results.get(solverName);
                if (result == null) {
                    continue;
                }

                List<Double> x = new ArrayList<>();
                List<Double> y = new ArrayList<>();

                if (result.residualHistory != null && !result.residualHistory.isEmpty()) {
                    for (int i = 0; i < result.residualHistory.size(); i++) {
                        x.add((double) (i + 1));
                        y.add(result.residualHistory.get(i));
                    }
                } else {
                    x.add((double) result.iterations);
                    y.add(result.relativeError);
                }

                chart.addSeries(solverName + " @ " + formatTolerance(tol), x, y);
            }
        }

        String filename = String.format("01_Iterazioni_Residuo_%s_%s.png", matrixName, timestamp);
        saveChart(chart, filename);
    }

    /**
     * Genera il grafico: Tolleranza vs Tempo di calcolo
     */
    private void generateToleranceTimeChart(String matrixName,
                                           Map<Double, Map<String, SolverResult>> resultsByTol,
                                           double[] tolerances) {
        XYChart chart = new XYChartBuilder()
                .width(CHART_WIDTH)
                .height(CHART_HEIGHT)
                .title("Tolleranza vs Tempo di Calcolo - " + matrixName)
                .xAxisTitle("Tolleranza")
                .yAxisTitle("Tempo (secondi)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setXAxisLogarithmic(true);

        double[] sortedTolerances = tolerances.clone();
        Arrays.sort(sortedTolerances);

        Map<String, SolverResult> referenceResults = firstAvailableResults(resultsByTol, sortedTolerances);
        if (referenceResults == null) {
            return;
        }

        for (String solverName : referenceResults.keySet()) {
            List<Double> xs = new ArrayList<>();
            List<Double> ys = new ArrayList<>();

            for (double tol : sortedTolerances) {
                Map<String, SolverResult> results = resultsByTol.get(tol);
                if (results == null || !results.containsKey(solverName)) {
                    continue;
                }
                xs.add(tol);
                ys.add(results.get(solverName).time);
            }

            chart.addSeries(solverName, xs, ys);
        }

        String filename = String.format("02_Tolleranza_Tempo_%s_%s.png", matrixName, timestamp);
        saveChart(chart, filename);
    }

    /**
     * Genera il grafico: Tolleranza vs Errore relativo
     */
    private void generateToleranceErrorChart(String matrixName,
                                            Map<Double, Map<String, SolverResult>> resultsByTol,
                                            double[] tolerances) {
        XYChart chart = new XYChartBuilder()
                .width(CHART_WIDTH)
                .height(CHART_HEIGHT)
                .title("Tolleranza vs Errore Relativo - " + matrixName)
                .xAxisTitle("Tolleranza")
                .yAxisTitle("Errore Relativo (scala log)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setXAxisLogarithmic(true);
        chart.getStyler().setYAxisLogarithmic(true);

        double[] sortedTolerances = tolerances.clone();
        Arrays.sort(sortedTolerances);

        Map<String, SolverResult> referenceResults = firstAvailableResults(resultsByTol, sortedTolerances);
        if (referenceResults == null) {
            return;
        }

        for (String solverName : referenceResults.keySet()) {
            List<Double> xs = new ArrayList<>();
            List<Double> ys = new ArrayList<>();

            for (double tol : sortedTolerances) {
                Map<String, SolverResult> results = resultsByTol.get(tol);
                if (results == null || !results.containsKey(solverName)) {
                    continue;
                }
                xs.add(tol);
                ys.add(results.get(solverName).relativeError);
            }

            chart.addSeries(solverName, xs, ys);
        }

        String filename = String.format("03_Tolleranza_Errore_%s_%s.png", matrixName, timestamp);
        saveChart(chart, filename);
    }

    /**
     * Salva il grafico come file PNG
     */
    private void saveChart(XYChart chart, String filename) {
        try {
            File file = new File(outputDir, filename);
            // Salva PNG ad alta qualità
            BitmapEncoder.saveBitmapWithDPI(chart, file.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG, 300);
            System.out.println("✓ Grafico salvato: " + filename);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio del grafico: " + e.getMessage());
        }
    }

    private Map<String, SolverResult> firstAvailableResults(Map<Double, Map<String, SolverResult>> resultsByTol,
                                                            double[] tolerances) {
        for (double tol : tolerances) {
            Map<String, SolverResult> results = resultsByTol.get(tol);
            if (results != null && !results.isEmpty()) {
                return results;
            }
        }
        return null;
    }

    private String formatTolerance(double tol) {
        return String.format(Locale.US, "%.0e", tol).replace("E", "e");
    }
}

