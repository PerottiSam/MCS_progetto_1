package models;

import org.ejml.data.DMatrixRMaj;
import java.util.ArrayList;
import java.util.List;

/**
 * Oggetto contenitore (DTO) che incapsula i risultati restituiti da un solutore iterativo.
 * <p>
 * Trasferisce lo stato della computazione dalla logica matematica specifica
 */
public class IterationResult {

    /** Il vettore soluzione finale o parziale calcolato dall'algoritmo. */
    public DMatrixRMaj x;

    /** Il numero di iterazioni effettivamente eseguite. */
    public int iterations;

    /** Indica se l'algoritmo ha rispettato la tolleranza richiesta (true) o ha raggiunto il limite massimo di iterazioni (false). */
    public boolean converged;

    /** L'errore relativo calcolato all'ultimo step dell'algoritmo. */
    public double relativeError;

    /** Lista dei valori di residuo relativo per ogni iterazione */
    public List<Double> residualHistory;

    /**
     * Crea un nuovo risultato contenente i dati dell'esecuzione.
     *
     * @param x             Vettore soluzione calcolato.
     * @param iterations    Numero di iterazioni eseguite.
     * @param converged     {@code true} se ha raggiunto la convergenza, {@code false} altrimenti.
     * @param relativeError Errore relativo finale rispetto alla tolleranza.
     */
    public IterationResult(DMatrixRMaj x, int iterations, boolean converged, double relativeError) {
        this.x = x;
        this.iterations = iterations;
        this.converged = converged;
        this.relativeError = relativeError;
        this.residualHistory = new ArrayList<>();
    }

    /**
     * Aggiunge un valore di residuo alla storia.
     *
     * @param residual valore del residuo relativo da aggiungere
     */
    public void addResidual(double residual) {
        this.residualHistory.add(residual);
    }
}