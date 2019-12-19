import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

//Questo codice calcola la radiosita’ di una scena in
// base al metodo iterativo di Jacobi stocastico, ma
// aggiunge alla fine (opzionalmente) un secondo
// passaggio (Final Gathering), nel corso del quale
// traccia anche un raggio rifratto, per simulare eventuali
// materiali semitrasparenti invece che puramente diffusivi.

// Indicazioni per l’utente:

//Ci sono due scelte per la visualizzare dell’immagine
// finale: una di radiosita’ pura ed una di radiosita’
// seguita da illuminazione riflessa indiretta
// (impostando true il parametro Fg) che permette di
// ottenere un’immagine fotorealistica).
// Si puo’ scegliere se visualizzare sfere
// di vetro o di giada impostando true o false
// (solo uno puo’ essere true) i parametri
// jade e glass.
// Si puo’ inoltre scegliere se visualizzare le sfere allineate
// o sovrapposte, impostando true o false il parametro
// aligned (la scelta true e’ consigliata per
// le sfere in vetro, cosi’ da poter vedere le
// multiriflessioni).
// Tutti i parametri tra cui scegliere si possono
// trovare nelle prime righe del codice.
// Modificando i parametri nel codice e’ possibile
// comunque apportare le volute modifiche sui
// materiali, sulla posizione delle sfere, etc

public class Main {
  public Main() {}

  //metodo: scegliere una delle due flag per
  // visualizzare il rendering con il metodo di Jacobi
  //stocastico (jacob) o con il final gathering(Fg)static boolean jacob=true;static boolean Fg=false;//translucentJade=true se si vuole una//visualizzazione con BSSRDFstatic boolean translucentJade=false;//diffusiveJade=true se vogliamo una giada//"diffusiva"static boolean diffusiveJade=false;static boolean glass=false;static boolean aligned=false;//nome del file in cui si salvera’ l’immagine//di outputprivate final static String filename = "image.ppm";//stringa contenente le informazioni da scrivere nel//file immagine image.ppmprivate static String matrix="";//commentare una delle due seguenti gran
}