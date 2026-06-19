# Risolutore Iterativo di Sistemi Lineari

## 1. Descrizione del Progetto

Applicazione Java eseguibile da riga di comando (CLI) sviluppata per la risoluzione di sistemi lineari sparsi nella forma $Ax = b$ tramite quattro diversi algoritmi iterativi.

Il programma opera secondo il seguente flusso:

- **Input:** Lettura di una matrice sparsa $A$ in formato Matrix Market (`.mtx`).
- **Setup:** Inizializzazione automatica della soluzione esatta $x$ come vettore di soli $1$ e calcolo del vettore dei termini noti $b = Ax$.
- **Risoluzione:** Analisi comparativa tramite i seguenti metodi:
    1. Metodo di Jacobi (`JacobiSolver`)
    2. Metodo di Gauss-Seidel (`GaussSeidelSolver`)
    3. Metodo del Gradiente / Steepest Descent (`GradientSolver`)
    4. Metodo del Gradiente Coniugato (`ConjugateGradientSolver`)
- **Dipendenze principali:** **EJML (v0.44.0)** per l'archiviazione efficiente delle matrici in formato CSC e le operazioni algebriche ad alte prestazioni; **XChart (v3.8.2)** per la renderizzazione dei grafici delle performance.

---

## 2. Struttura del Progetto

Il codice sorgente è organizzato nei seguenti package, ciascuno con responsabilità ben distinte:

- **`app/`** — Logica di avvio (`Main.java`) e classe `Application.java`, responsabile dell'interazione con l'utente e dell'orchestrazione dell'intero flusso di esecuzione.
- **`models/`** — Classi contenitore per i dati finali e intermedi (`SolverResult`, `IterationResult`) e logica di parsing tramite `MatrixMarketReader`.
- **`solvers/`** — Implementazioni degli algoritmi matematici. Suddiviso in `direct/` (risoluzioni triangolari) e `iterative/`, contenente l'interfaccia `IterativeSolver`, la classe base `AbstractIterativeSolver` e le singole implementazioni dei solutori.
- **`utils/`** — Strumenti diagnostici (`MatrixValidator` per il controllo di simmetria e definizione positiva) e generatori dell'output visivo (`ChartGenerator`, `TableGenerator`).
- **`exceptions/`** — Eccezioni custom (`MatrixConditionException`, `InvalidMatrixException`) per la segnalazione di errori e violazioni dei vincoli matematici.

---

## 3. Requisiti di Sistema

Per l'esecuzione del file precompilato (`.jar`) è necessario il seguente prerequisito:

- **Java Runtime Environment (JRE) / Java Development Kit (JDK):** versione **23** o superiore.

Non è richiesta l'installazione di librerie esterne né di Maven: il file JAR è distribuito come *Fat JAR*, con tutte le dipendenze (EJML e XChart) già incluse al suo interno.

---

## 4. Avvio dell'Applicazione

Per avviare l'analisi è necessario eseguire il pacchetto direttamente dal terminale.

### Procedura di Avvio

1. Aprire il **Terminale** (macOS/Linux) o il **Prompt dei comandi / PowerShell** (Windows).
2. Spostarsi nella directory contenente il file `Progetto_1.jar`.
3. Eseguire il seguente comando:

```bash
java -jar Progetto_1.jar
```

---

## 5. Utilizzo dell'Applicazione

All'avvio, il programma guida l'utente attraverso un'interfaccia testuale interattiva.

### 5.1 Inserimento della Matrice

Il programma richiede il percorso del file contenente la matrice dei coefficienti $A$:

```text
Inserire il percorso del file Matrix Market (.mtx) contenente la matrice A:
```

- **Percorso:** È possibile specificare un percorso relativo o assoluto (es. `matrice_test.mtx`).
- **Verifica automatica:** Il sistema controlla che il file esista, che non sia una directory e che presenti la corretta estensione `.mtx`, segnalando eventuali anomalie prima di procedere.

### 5.2 Impostazione della Tolleranza

Il passo successivo richiede la definizione del criterio di arresto (tolleranza sull'errore) per gli algoritmi iterativi:

```text
Inserisci la tolleranza desiderata (es. 1e-10). Premi invio per usare automaticamente [1e-4, 1e-6, 1e-8, 1e-10]:
```

Sono disponibili due modalità di esecuzione:

- **Analisi Batch (consigliata):** Confermando senza inserire alcun valore, il programma esegue automaticamente un'analisi completa su quattro livelli di tolleranza standard ($10^{-4}, 10^{-6}, 10^{-8}, 10^{-10}$), consentendo il confronto diretto dell'efficienza dei solutori.
- **Tolleranza Singola:** È possibile specificare un valore personalizzato utilizzando il punto (`.`) come separatore decimale o la notazione scientifica (es. `0.0001` oppure `1e-6`).

### 5.3 Esecuzione e Risultati

Completata la fase di input, il programma calcola il termine noto $b$ e avvia la risoluzione del sistema con tutti e quattro i metodi iterativi, mostrando l'avanzamento a schermo.

Al termine dell'elaborazione vengono prodotti i seguenti output:

- **Benchmark in console:** Tabella riassuntiva con numero di iterazioni, tempo di esecuzione in secondi, errore relativo raggiunto ed errore vero calcolato sulla soluzione esatta.
- **Report ed esportazioni:** Viene generata automaticamente una cartella `charts/[data-ora-esecuzione]` nella stessa directory dell'eseguibile, contenente:
    - Grafici in formato PNG ad alta risoluzione (andamento del residuo per iterazione, confronto dei tempi di calcolo, confronto degli errori rispetto alla tolleranza).
    - Pagina web HTML (`Tabella_[NomeMatrice]_[Timestamp].html`) con tabella dei risultati e grafici incorporati.
    - File CSV strutturato, importabile in software quali Excel, Python o MATLAB.