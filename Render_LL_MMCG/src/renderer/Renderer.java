package renderer;

import partition.PhotonBox;
import primitive.*;
import ui.InterfaceInitialiser;
import ui.RenderAction;

import java.util.ArrayList;

/* La classe Render contiene i metodi di rendering adottati nel programma
 *  e tutti quelli alle loro dipendenze.
 * La classe è gestita con variabili non statiche e come oggetti
 *  nel programma per far sì di non far interferire i calcoli per un pixel
 *  con altri nel multithreading
 */

public class Renderer {
  /* Metodo per l'impostazione della multithreader radiance (qualsiasi metodo esso sia poichè il calcolo
   *  è basato sulla selezione dei pixel, quindi parallelizzabile
   */
  public static void calculateThreadedRadiance(Camera cam) {
    new Runner(cam);
  }
}
