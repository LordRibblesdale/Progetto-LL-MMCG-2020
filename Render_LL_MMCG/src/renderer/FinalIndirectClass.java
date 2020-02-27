package renderer;

import primitive.Obj;
import primitive.Point3D;
import primitive.Ray;
import ui.RenderAction;

public class FinalIndirectClass {
  //metodo per l'illuminazione indiretta
  //I parametri in input sono:
  //r: raggio di entrata
  //o: oggetto dal quale partira' il nuovo raggio
  //x e y indici del seme iniziale per la generazione
  //di numeri randomici
  static Point3D finalIndirect(Ray r, Obj o, int x, int y) {
    Utilities utilities = new Utilities();

    //inizializzo a 0 il valore che resitituiro' alla fine
    //del processo
    Point3D radianceOutput = new Point3D();

    //normale dell'oggetto in esame
    Point3D n1 = o.normal(r.o);

    int mId = o.matId;

    //per ogni s campione della luce tra gli aosamps
    //campioni totali per l'illuminazione indiretta
    for (int s = 0; s < RenderAction.aoSamps; s++) {
      //quindi distribuito uniformemente sull'emisfero
      Point3D dir;
      float rndX = 0.0f;
      float rndY = 0.0f;

      //utilizzo questa variabile tt perche' non
      //posso usare il valore x+y*width nell'array
      //dirSamples1[], altrimenti l'ultimo indice
      //sarebbe fuori dal range (ricordo che la
      //misura e' width*height ma gli indici vanno da 0 a
      //width*height-1)
      int tt =x+y* RenderAction.width;
      //allora faccio l'if per tt<width*height cosi' da
      //accertarmi che non sia considerato l'indice
      //width*height-esimo
      if(tt< RenderAction.width * RenderAction.height) {
        rndX = Utilities.generateRandom(RenderAction.aoSamplesX[tt]);
        rndY = Utilities.generateRandom(RenderAction.aoSamplesY[tt]);
      }

      //distribuisco i numeri random sull'emisfero
      float rndPhi = 2 * Utilities.MATH_PI *(rndX);
      float rndTeta = (float)Math.acos((float)Math.
          sqrt(rndY));

      // Create onb (ortho normal basis) on iP punto di
      //intersezione
      Point3D u, v, w;
      w = n1;
      //vettore up (simile a (0,1,0))
      Point3D up=new Point3D(0.0015f, 1.0f, 0.021f);
      v = w.crossProduct(up);
      v = v.getNormalizedPoint();
      u = v.crossProduct(w);

      float cosPhi=(float) Math.cos(rndPhi);
      float sinTeta=(float) Math.sin(rndTeta);
      float sinPhi=(float) Math.sin(rndPhi);
      float cosTeta=(float) Math.cos(rndTeta);
      dir = (u.multiplyScalar(cosPhi*sinTeta))
          .add(v.multiplyScalar(sinPhi*sinTeta))
          .add(w.multiplyScalar(cosTeta));
      dir = dir.getNormalizedPoint();

      //creo il raggio dal punto di intersezione ad un
      //punto a caso sull'emisfero
      Ray reflRay = new Ray(r.o, dir);
      // il metodo va' avanti solo se interseca un
      //oggetto e se questo oggetto non e' una luce
      //(poiche' la luminosita' diretta l'abbiamo gia'
      //considerata)

      if (utilities.intersect(reflRay, null)) {
        utilities.inters = Utilities.inf;
        Obj objX = utilities.intersObj;
        utilities.intersObj = null;
        if(RenderAction.material[objX.matId].emittedLight.max() == 0) {
          double _area = 1 / (objX).area();
          radianceOutput
              = radianceOutput.add(((objX).P).multiplyComponents(RenderAction.material[mId].diffusionColor).multiplyScalar(_area));
        }
      }
    }

    radianceOutput = radianceOutput.divideScalar(RenderAction.aoSamps);
    return radianceOutput;
  }
}
