package ui;

import javax.swing.*;
import java.awt.*;

/* L'accesso a questa classe viene effettuato tramite
 *  chiamate statiche. Per questioni di sicurezza e di accesso
 *  alle variabili, sarebbe ideale creare un contollore
 *  che modifica solo i parametri necessari delle variabili
 *  invece che renderle statiche.
 * (Ad esempio, cambiare testo al label potrebbe essere fatto
 *  con un metodo che chiede come argomento una stringa,
 *  invece di dare l'accesso all'intera variabile)
 */

public class InterfaceInitialiser {
  //la classe main costruisce una stanza rettangolare con
  //dentro 3 sfere e opportune luci, e un osservatore che
  //guarda in una direzione appropriata: il generico raggio
  //di visuale attraversa un appropriato pixel del
  //viewplane. Su quel pixel vengono calcolati, mediante
  //metodi di radiosita' stocastica, colore e luminosita'
  //di cio' che l'osservatore vede.

  static JFrame mainFrame;
  public static final JLabel label = new JLabel();
  static EditPanel editPanel;

  public InterfaceInitialiser() {
    mainFrame = new JFrame("renderer.Renderer");
    //inizialmente imposto la finestra con le varie
    //scelte per l'utente

    try {
      // Set System L&F
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      // handle exception
    }

    editPanel = new EditPanel();
    mainFrame.add(editPanel);
    mainFrame.add(label, BorderLayout.PAGE_END);

    mainFrame.pack();
    mainFrame.setResizable(false);
    mainFrame.setLocationRelativeTo(null);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setVisible(true);
  }
}
