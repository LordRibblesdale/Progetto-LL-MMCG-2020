package renderer;

import primitive.Camera;
import primitive.Obj;
import primitive.Point3D;
import primitive.Ray;
import renderer.Renderer;
import ui.InterfaceInitialiser;
import ui.RenderAction;

import java.text.DecimalFormat;

import static renderer.RadianceClass.radiance;

/* La classe renderer.ParallelProcessRadiance presenta
 *  il blocco di codice che si occupa di reindirizzare il rendering del
 *  pixel nei metodi interessati (scelti nella classe Render)
 * Tutte queste azioni avvengono in un thread apposito,
 *  creato nella clase renderer.Runner
 */

public class ParallelProcessRadiance implements Runnable {
  private int y;
  private Camera cam;

  ParallelProcessRadiance(int y, Camera cam) {
    this.y = y;
    this.cam = cam;
  }

  @Override
  public void run() {
    Utilities utilities = new Utilities();

    double percentY = (y*100 / (float) RenderAction.height);
    InterfaceInitialiser.label.setText("Percentuale di completamento radianza: "
        + new DecimalFormat("###.##").format(percentY));

    //per tutte le colonne
    for(int x = 0; x <= RenderAction.width; x++) {
      // Ora siamo nel pixel
      // r e' la radianza: in questo caso e' tutto nero
      // Radianza della scena
      Point3D sceneRadiance = new Point3D(0.0f);

      // Loop per ogni campione
      for (int s = 0; s < RenderAction.samps; s++) {
        //inizializiamo un raggio per la camera
        Ray cameraRay;

        //transformazione delle variabili x e y in
        //float corrispondono alla posizione che
        //cameraRay deve raggiungere
        float raster_x = (float)x;
        float raster_y = (float)y;

        //origine del raggio della fotocamera
        Point3D origin = cam.eye;

        //se ho piu' di un campione allora
        //distribuisco gli altri campioni in modo
        //casuale
        if (s > 0) {
          float rndX=0;
          float rndY=0;

          //utilizzo questa variabile tt perche' non
          //posso usare il valore x+y*width nell'array
          //samplesX[], altrimenti l'ultimo indice
          //sarebbe fuori dal range (ricordo che la
          //misura e' width*height ma gli indici vanno da 0 a
          //width*height-1)
          int tt = x+y* RenderAction.width;
          //allora faccio l'if per tt<width*height cosi' da
          //accertarmi che non sia considerato l'indice
          //width*height-esimo
          if(tt < RenderAction.width * RenderAction.height) {
            // gli passo il numero random da cui
            //siamo partiti all'interno del pixel
            rndX = Utilities.generateRandom(RenderAction.samplesX[tt]);
            rndY = Utilities.generateRandom(RenderAction.samplesY[tt]);
          }

          // Gestisco la messa a fuoco dell'immagine finale,
          // valutando il diaframma e il punto di messa a fuoco
          raster_x +=
              Math.cos(2 * Utilities.MATH_PI * rndX)*cam.aperture *rndY;
          raster_y +=
              Math.sin(2 * Utilities.MATH_PI * rndX)*cam.aperture *rndY;
          Point3D camUFuoco =
              cam.U.multiplyScalar(cam.fuoco*(x - raster_x));
          Point3D camVFuoco =
              cam.V.multiplyScalar(cam.fuoco*(y - raster_y));

          origin = origin.add(camUFuoco).add(camVFuoco);
        }

        // prediamo la direzione della fotocamera
        //ray_direction e' calcolato con l'ONB(base
        //ortonormale) della fotocamera
        //il raggio dalla fotocamera al campione sara'
        //data dalla combinazione lineare dell'ONB
        //della fotocamera
        //centro il piano rispetto alla fotocamera
        //sottraendo width/2 alla componente in x e height/2
        //alla componente in y infine la distanza z
        //tra la fotocamera e il piano e' cam.d
        Point3D ray_direction =
            (cam.U.multiplyScalar(raster_x - 0.5f * RenderAction.width))
            .add(cam.V.multiplyScalar(raster_y - 0.5f * RenderAction.height))
            .add(cam.W.multiplyScalar(-cam.d));
        ray_direction=ray_direction.getNormalizedPoint();

        //Ora si crea il raggio della fotocamera
        cameraRay = new Ray(origin, ray_direction);

        //dichiaro e inizializzo la variabile t in cui
        //salveremo il punto di intersezione fra
        //l'oggetto considerato  e cameraRay
        double t;
        //inizializzo a null l'oggetto intersecato
        //dal raggio
        Obj o = null;
        //intersezione del raggio con gli elementi
        //della scena:
        if(utilities.intersect(cameraRay, o)) {
          //pongo t uguale al valore di intersezione
          //memorizzato nella variabile globale inters
          t = utilities.inters;
          //resetto inters uguale a inf in modo da
          //avere il giusto valore di partenza la
          //prossima volta che si utilizzera'
          //il metodo intersect()
          utilities.inters = Utilities.inf;
          //salvo nella variabile o objX l'elemento
          //intersecato dal raggio cameraRay
          o = utilities.intersObj;
          //resetto intersObj=null in modo da avere
          //il giusto valore di partenza la prossima
          //volta che si utilizzera' il metodo
          //intersect()
          utilities.intersObj=null;
          //si calcola il punto di intersezione
          Point3D iP = (cameraRay.o)
              .add(cameraRay.d.multiplyScalar(t));
          //viene creato il primo raggio per il
          //calcolo della radianza
          //questo raggio parte dal punto ed e'
          //diretto verso l'osservatore
          Ray first = new Ray(iP, (cameraRay.d).multiplyScalar(-1));
          //si aggiunge alla variabile r il contributo
          //di radianza del punto considerato
          sceneRadiance =
              sceneRadiance.add(radiance(first, o, x, y));
        } else {
          //se non si interseca nessun oggetto si
          //aggiunge alla variabile r il colore di
          //background (nero)

          sceneRadiance = sceneRadiance.add(RenderAction.background);
        }
      }

      //divido per il numero di campioni del pixel
      sceneRadiance = sceneRadiance.divideScalar(RenderAction.samps);
      sceneRadiance.multiplyScalar(0.3f);
      // A questo punto si crea un'immagine basata sui
      //valori di radianza r

      //le componenti RGB del vettore r vengono tagliate
      //se non comprese in [0,1] dopodiche' vengono
      //caricate nel vettore image
      //nota: per ogni y che aumenta abbiamo gia'
      //caricato width pixel

      //utilizzo questa variabile tt perche' non posso
      //usare il valore x+y*width nell'array image[width*y],
      //altrimenti l'ultimo indice sarebbe fuori dal
      //range (ricordo che la misura e' width*height ma gli
      //indici vanno da 0 a width*height-1)
      int tt =x+y* RenderAction.width;
      //allora faccio l'if per tt<width*height cosi' da
      //accertarmi che non sia considerato l'indice
      //width*height-esimo
      if(tt< RenderAction.width * RenderAction.height) {
        RenderAction.image[x+y* RenderAction.width].x =
            Point3D.clamp(sceneRadiance.x);
        RenderAction.image[x+y* RenderAction.width].y =
            Point3D.clamp(sceneRadiance.y);
        RenderAction.image[x+y* RenderAction.width].z =
            Point3D.clamp(sceneRadiance.z);
      }
    }
  }
}
