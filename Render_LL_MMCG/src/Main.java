//questo codice calcola la radiosita' di una scena in
//base al metodo iterativo di Jacobi stocastico.
//Indicazioni per l'utente:
//Ci sono due scelte per la visualizzare dell'immagine 
//finale: una di radiosita' pura ed una di radiosita' 
//seguita da illuminazione riflessa indiretta 
//(impostando true il parametro doFinalGathering) che permette di
//ottenere un'immagine fotorealistica
//Si puo' scegliere se visualizzare sfere
//di vetro o di giada impostando true o false 
//(solo uno puo' essere true) i parametri 
//jade e glass.
//Si puo' inoltre scegliere se visualizzare allineate
//o sovrapposte impostando true o false il parametro
//aligned (la scelta true e' consigliata per 
//le sfere in vetro, cosi' da poter apprezzare le 
//multiriflessioni).
//Tutti i parametri tra cui scegliere si possono 
//trovare nelle prime righe di codice
//Si specifica che agendo sul codice e' possibile
//comunque apportare le volute modifiche sui 
//materiali, sulla posizione delle sfere, etc.

import ui.InterfaceInitialiser;

public class Main {
  public static void main(String[] args) {
    new InterfaceInitialiser();
  }
}
