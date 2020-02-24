public interface ModelerProperties {
  /* ModelerProperties gestisce l'inizializzazione del programma e delle istanze.
   * Le istanze variano tra l'avviare una anteprima (per spostare o creare oggetti) e avviare il render finale.
   * ENABLE_MODELER avvia l'interfaccia di anteprima e il modellatore
   * PREVIEW_ONLY aggiorna il modellatore in caso di varie modifiche
   * START_RENDERING deallora il modellatore per avviare il render finale
   */
  int ENABLE_MODELER = 0;
  int START_RENDERING = 1;
  int PREVIEW_ONLY = 2;
}
