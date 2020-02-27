package primitive;

//classe che costruisce una sfera attraverso i parametri
//posizione (centro) e raggio
//La classe ha la funzione che calcola l'intersezione 
//della sfera con un raggio, e la funzione che restituisce
//la normale alla sfera in un punto dato

/* Il problema principale di questa classe è dato dalla
 *  natura dell'oggetto, basato su formule matematiche invece
 *  che meshes, il che rende difficile applicare mappe
 *  di tessitura (textures) o lo stesso metodo di Jacobi.
 * Sarebbe necessario strutturare l'oggetto in modo da avere
 *  una creazione di meshes dinamico, in base al grado di
 *  approssimazione.
 */

import renderer.Utilities;
import ui.RenderAction;

public class Sphere {
  // raggio
  public float rad;
  // posizione (centro)
  public Point3D p;

  int matId;

  //costruttore
  public Sphere(float nrad, Point3D np, int matId) {
    rad=nrad;
    p=np;
    this.matId = matId;
  }

  //metodo che imposta, a seconda della scelta
  //effettuata dall'utente, la posizione
  //appropriata alle prime tre sfere
  //Prende come parametro l'indice dell'array
  //spheres di cui si deve settare la posizione
  public static Point3D setSpheresPosition(int index) {
    if(RenderAction.aligned) {
      switch(index) {
        case 0:
          return new Point3D(-1.0f,0.0f,0.0f);
        case 1:
          return new Point3D(-5.0f,0.3f,0.8f);
        case 2:
          return new Point3D(3.5f,0.5f,3.3f);
        default:
          return new Point3D(0.0f);
      }
    } else {
      switch(index) {
        case 0:
          return new Point3D(-4.0f,0.0f,0.0f);
        case 1:
          return new Point3D(-7.0f,0.0f,0.0f);
        case 2:
          return new Point3D(-4.0f,0.0f,5.3f);
        default:
          return new Point3D(0.0f);
      }
    }
  }

  //funzione di intersezione con un raggio: Return
  //distanza o -1.0f se non c'e' intersezione
  double intersect(Ray r) {
    //sostituendo il raggio o+td all'equazione della
    //sfera (td+(o-p)).(td+(o-p)) -R^2 =0 si ottiene
    //l'intersezione. Si deve risolvere
    //t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
    // A=d.d=1 (r.d e' normalizzato)
    // B=2*(o-p).d
    // C=(o-p).(o-p)-R^2
    Point3D op = p.subtract(r.o);
    // calcolo della distanza
    double t;
    // 2*t*B -> semplificato usando b/2 invece di B
    double B=op.dotProduct(r.d);
    double C=op.dotProduct(op)-rad*rad;
    //determinante equazione quadratica
    double det=B*B-C;
    // se e' negativo non c'e' intersezione reale,
    //altrimenti assegna a det il risultato
    if (det<0)
      return -1.0f;
    else {
      det = Math.sqrt(det);
      //ritorna la t piu' piccola se questa e' >0
      //altrimenti vedi quella piu' grande se e' >0
      //se sono tutte negative non c'e' intersezione
      if((t=B-det) > Utilities.EPSILON)
        return t;
      else {
        if((t=B+det) > Utilities.EPSILON)
          return t;
        else
          return -1.0f;
      }
    }
  }

  //funzione che calcola la normale in un punto iP della sfera
  Point3D normal(Point3D iP) {
    //vettore dal centro all'intersezione normalizzato
    return (iP.subtract(p)).getNormalizedPoint();
  }

  @Override
  public String toString() {
    return "Sfera, centro: " + p.toString();
  }
}
