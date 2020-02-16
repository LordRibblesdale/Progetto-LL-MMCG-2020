//import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class Main implements StandardMaterial {
	static final JTextField tf = new JTextField();
	static final JButton ok_button=new JButton("Conferma");
  static JFrame f=new JFrame("Image creator");

  //la classe main costruisce una stanza rettangolare con
	//dentro 3 sfere e opportune luci, e un osservatore che
	//guarda in una direzione appropriata: il generico raggio
	//di visuale attraversa un appropriato pixel del
	//viewplane. Su quel pixel vengono calcolati, mediante
	//metodi di radiosita' stocastica, colore e luminosita'
	//di cio' che l'osservatore vede.

	public static void main(String[] args) {
  	//inizialmente imposto la finestra con le varie
  	//scelte per l'utente

  	int[] bool=new int[5];
  	createMenu(f,tf,ok_button, bool);
  	ok_button.addActionListener(new RenderAction(bool));
  }

  private static void addItemsToMethodMenu(
      JMenu method_menu,int[] bool)
  {
    JRadioButtonMenuItem menuItem;
    ButtonGroup bg = new ButtonGroup();

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Solo Jacobi");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[0]=0;//doFinalGathering
      }
    });

    // Aggiungo il bottone al gruppo
    bg.add(menuItem);
    // Infine lo aggiungo al menu
    method_menu.add(menuItem);

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Final Gathering");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[0]=1;//doFinalGathering
      }
    });

    bg.add(menuItem);
    method_menu.add(menuItem);

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Final Gathering + Photon Mapping");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[0]=2;//doPhotonFinalGathering
      }
    });

    bg.add(menuItem);
    method_menu.add(menuItem);

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("MultiPass Photon Mapping");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[0]=3;//doMultiPassPhotonMapping
      }
    });

    bg.add(menuItem);
    method_menu.add(menuItem);
  }

  private static void addItemsToMaterialMenu(JMenu material_menu,int[] bool) {
    JRadioButtonMenuItem menuItem;
    ButtonGroup bg = new ButtonGroup();

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Giada Realistica (traslucente)");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[1]=1;//translucentJade=true;
        bool[2]=0;//diffusiveJade=false;
        bool[3]=0;//glass=false;
      }
    });

    bg.add(menuItem);
    material_menu.add(menuItem);

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Giada Diffusiva");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[1]=0;//translucentJade=false;
        bool[2]=1;//diffusiveJade=true;
        bool[3]=0;//glass=false;
      }
    });

    bg.add(menuItem);
    material_menu.add(menuItem);

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Cristallo");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[1]=0;//FgtranslucentJade=false;
        bool[2]=0;//FgdiffusiveJade=false;
        bool[3]=1;//glass=true;
      }
    });

    bg.add(menuItem);
    material_menu.add(menuItem);
  }

  private static void addItemsToPositionMenu(JMenu position_menu,int[] bool) {
    JRadioButtonMenuItem menuItem;
    ButtonGroup bg = new ButtonGroup();

    menuItem = new JRadioButtonMenuItem();
    menuItem.setText("Apri modellatore");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        bool[4]=1;//aligned=true;
      }
    });
    bg.add(menuItem);
    position_menu.add(menuItem);
  }

  //metodo per creare il menu box
  private static void createMenu(JFrame f, final JTextField tf, final JButton ok_button, int[] bool) {
    // componenti del jFrame
    JMenuBar menu_bar;
    JMenu method_menu;
    JMenu material_menu;
    JMenu position_menu;

    // creo la menu bar
    menu_bar = new JMenuBar();
    menu_bar.setBounds(15,15,300,40);

    // creo i menu
    method_menu = new JMenu("Metodo");
    material_menu = new JMenu("Materiale");
    position_menu = new JMenu("Posizione");

    ok_button.setBounds(250,15,100,40);
    tf.setText("Scegliere i parametri");
    tf.setBounds(50,15, 200,100);

    addItemsToMethodMenu(method_menu,bool);
    addItemsToMaterialMenu(material_menu,bool);
    addItemsToPositionMenu(position_menu,bool);

    // aggiungo alla menu bar i menu
    menu_bar.add(method_menu);
    menu_bar.add(material_menu);
    menu_bar.add(position_menu);
    f.add(ok_button, -1);
    f.add(tf, -1);
    f.setJMenuBar(menu_bar);

    // imposto le dimensioni della finestra
    f.setSize(400,200);

    f.setLayout(null);

    f.setVisible(true);

    // centro la finestra nello schermo
    f.setLocationRelativeTo(null);

    // scelgo cosa succedera'  quando si chiudera'
    //  la finestra
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
