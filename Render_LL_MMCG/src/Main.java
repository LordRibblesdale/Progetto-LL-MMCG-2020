import javax.swing.*;

import java.awt.*;

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

public class Main extends JFrame implements StandardMaterial {
	static final JLabel label = new JLabel();
	static EditPanel editPanel;

  //la classe main costruisce una stanza rettangolare con
	//dentro 3 sfere e opportune luci, e un osservatore che
	//guarda in una direzione appropriata: il generico raggio
	//di visuale attraversa un appropriato pixel del
	//viewplane. Su quel pixel vengono calcolati, mediante
	//metodi di radiosita' stocastica, colore e luminosita'
	//di cio' che l'osservatore vede.

  Main() {
    super("Renderer");
    //inizialmente imposto la finestra con le varie
    //scelte per l'utente

    editPanel = new EditPanel();
    add(editPanel);
    add(label, BorderLayout.PAGE_END);

    setMinimumSize(new Dimension(650, 400));
    setResizable(false);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  public static void main(String[] args) {
    new Main();
  }
}
