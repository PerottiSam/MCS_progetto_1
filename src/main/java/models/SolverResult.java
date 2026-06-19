package models;

import org.ejml.data.DMatrixRMaj;
import java.util.ArrayList;
import java.util.List;

/**
 * Oggetto contenitore (DTO) che rappresenta il risultato finale e completo
 * dell'esecuzione di un solutore iterativo.
 * <p>
 * A differenza di {@link IterationResult}, questa classe include le metriche
 * di performance (come il tempo di esecuzione).
 */
public class SolverResult {

    /** Il vettore soluzione finale calcolato dal solutore. */
    public DMatrixRMaj x;

    /** Il numero totale di iterazioni effettuate. */
    public int iterations;

    /** Il tempo di esecuzione totale impiegato dall'algoritmo (in secondi). */
    public double time;

    /** Indica se l'algoritmo ha raggiunto la tolleranza richiesta (true) o si è interrotto al limite di iterazioni (false). */
    public boolean converged;

    /** L'errore relativo finale calcolato al termine dell'esecuzione. */
    public double relativeError;

    /** Lista dei valori di errore relativo per ogni iterazione */
    public List<Double> relativeErrorHistory;

    /** Lista dei valori di residuo relativo per ogni iterazione */
    public List<Double> residualHistory;

    /**
     * Crea un nuovo risultato finale contenente le statistiche di risoluzione.
     *
     * @param x             Vettore soluzione finale.
     * @param iterations    Numero totale di iterazioni eseguite.
     * @param time          Tempo di esecuzione in secondi.
     * @param converged     {@code true} se ha raggiunto la convergenza, {@code false} altrimenti.
     * @param relativeError Errore relativo finale rispetto alla tolleranza.
     */
    public SolverResult(DMatrixRMaj x, int iterations, double time,
                        boolean converged, double relativeError) {
        this.x = x;
        this.iterations = iterations;
        this.time = time;
        this.converged = converged;
        this.relativeError = relativeError;
        this.relativeErrorHistory = new ArrayList<>();
        this.residualHistory = new ArrayList<>();
    }

    /**
     * Aggiunge un valore di errore relativo alla storia.
     */
    public void addRelativeError(double error) {
        this.relativeErrorHistory.add(error);
    }

    /**
     * Aggiunge un valore di residuo relativo alla storia.
     */
    public void addResidual(double residual) {
        this.residualHistory.add(residual);
    }
}