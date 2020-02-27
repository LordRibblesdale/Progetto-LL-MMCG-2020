package renderer;

import primitive.Obj;
import primitive.Point3D;
import primitive.Ray;
import ui.RenderAction;

public class DirectIlluminationClass {
  //Metodo per il calcolo dell'illuminazione diretta, cioe'
  //il contributo che arriva all'oggetto direttamente dalla
  //fonte di luce
  //I parametri in input sono:
  //r: raggio di entrata
  //o: oggetto dal quale partira' il nuovo raggio
  //x e y indici del seme iniziale per la generazione
  //di numeri randomici
  public static Point3D directIllumination(Ray r, Obj o, int x, int y) {
    Utilities utilities = new Utilities();

    //inizializzo le variabili aleatorie comprese tra 0 e
    //1 che utilizzeremo per campionare un punto all'
    //interno dell'oggetto o in maniera equidistribuita
    float rnd1 = 0;
    float rnd2 = 0;
    float rnd3 = 0;

    //si inizializza a 0 il valore in uscita dal processo
    Point3D radianceOutput = new Point3D();

    //si carica la normale all'oggetto nel punto r.o
    Point3D n1 = o.normal(r.o);

    //si carica l'identificativo del materiale
    int mId = o.matId;

    //definizione e inizializzoazione a null dell'oggetto
    //che si andra' ad intersecare
    Obj objX;

    //per ogni luce
    for (int i = 0; i < RenderAction.lights.size(); i++) {
      //carico l'area della luce in esame
      double area = RenderAction.lights.get(i).areaObj;

      //per ogni s campione della luce tra i dirsamps
      //campioni totali per l'illuminazione diretta
      for (int s = 0; s < RenderAction.dirSamps; s++) {
        Point3D B;
        //Caso BSSRDF
        if(RenderAction.material[mId].translucent) {
          /* zv e zr sono le distanze dalla superficie interna ed
           *  esterna per applicare il modello di Jensen
           *  per l'illuminazione empirica di un materiale traslucente
           */
          float zv=0.005f;
          float dv;
          float zr=0.0025f;
          float dr;
          dv=(float) (Math.random()*(60) + 90);
          //lo divido per non avere un numero troppo
          //grande
          dv /= 10000f;
          //rispetto la triangolarita'
          float l2=dv*dv-zv*zv;
          float l=(float) Math.sqrt(l2);
          dr=(float) Math.sqrt(l2+zr*zr);
          //r.o e' il punto in cui arriva il raggio:
          //per rispettare il modello di Jensen e'
          //necessario spostarlo di l per un
          //angolo casuale

          //utilizzo questa variabile tt perche' non
          //posso usare il valore x+y*width nell'array
          //dirSamples1[], altrimenti l'ultimo indice
          //sarebbe fuori dal range (ricordo che la
          //misura e' width*height ma gli indici vanno da 0 a
          //width*height-1)
          int tt = x+y* RenderAction.width;
          //allora faccio l'if per tt<width*height cosi' da
          //accertarmi che non sia considerato l'indice
          //width*height-esimo
          if(tt < RenderAction.width * RenderAction.height) {
            rnd1 = Utilities.generateRandom(RenderAction.dirSamples1[tt]);
            rnd2 = Utilities.generateRandom(RenderAction.dirSamples2[tt]);
            rnd3 = Utilities.generateRandom(RenderAction.dirSamples3[tt]);
          }

          //genero due angoli casuali
          float rndPhi = 2 * Utilities.MATH_PI *(rnd1);
          float rndTeta = (float)Math.acos((float)Math.
              sqrt(rnd2));
          float cosP=(float)Math.cos(rndPhi);
          float cosT=(float)Math.cos(rndTeta);
          float sinT=(float)Math.sin(rndTeta);
          double px=r.o.x+l*cosP*cosT;
          double py=r.o.y+l*cosP*sinT;
          double pz=r.o.z+l*sinT;
          Point3D newPoint=new Point3D(px,py,pz);

          //si carica il punto campionato sulla luce
          Point3D p = RenderAction.lights.get(i).randomPoint(rnd1,rnd2,rnd3);

          //la direzione e' quella che congiunge il punto
          //r.o al punto campionato
          Point3D dir = (p.subtract(newPoint));
          //salviamo la distanza tra i due punti
          double norma = dir.normalize();
          dir = dir.getNormalizedPoint();
          //creazione del raggio d'ombra diretto verso
          //la luce
          double cosTheta=r.d.dotProduct(n1);
          Point3D Ftheta= RenderAction.material[mId].getFresnelCoefficient(cosTheta);

          dir.x *= Ftheta.x;
          dir.y *= Ftheta.y;
          dir.z *= Ftheta.z;

          Ray directRay= new Ray(newPoint, dir);

          //viene inizializzato l'oggetto che il raggio
          //intersechera' con l'oggetto che il raggio
          //punta
          objX = RenderAction.lights.get(i);
          //si inizializza la massima distanza a cui il
          //raggio puo' arrivare

          //verifica del fattore di visibilita'
          if (utilities.intersect(directRay, objX)) {
            utilities.inters = Utilities.inf;
            utilities.intersObj =null;
            //vengono caricati i dati della luce:
            //normale nel punto p
            Point3D n2 = RenderAction.lights.get(i).normal(p);
            //identificativo del materiale della luce
            int lid = RenderAction.lights.get(i).matId;
            //calcoliamo la BSSRDF
            double cosPsi=directRay.d.dotProduct(n1);
            Point3D Fpsi= RenderAction.material[mId].getFresnelCoefficient(
                cosPsi);
            Point3D one=new Point3D(1.0f);


            Point3D pi4=new Point3D(4* Utilities.MATH_PI);
            //i valori si sigmas e sigmaa sono specifici per la giada
            Point3D sigmas=new Point3D(0.657f,0.786f,0.9f);
            Point3D sigmaa=new Point3D(0.2679f,0.3244f,0.1744f);

            Point3D sigmat=sigmaa.add(sigmas);
            Point3D sigmatr=(sigmaa.multiplyComponents(sigmat)).multiplyScalar(3.0f);
            sigmatr= Point3D.getSquareCompPoint(sigmatr);
            Point3D alpha=sigmas.divideComponents(sigmat);

            Point3D expdr=sigmatr.multiplyScalar(dr*-1.0f);
            float dr3=dr*dr*dr;
            Point3D edivdr=(Point3D.exponent(expdr)).
                divideScalar(dr3);

            Point3D expdv=sigmatr.multiplyScalar(dv*-1.0f);
            float dv3=dv*dv*dv;
            Point3D edivdv=(Point3D.exponent(expdv)).divideScalar(dv3);
            Point3D rPart=(((sigmatr.multiplyScalar(dr)).add(one)).multiplyComponents(edivdr))
                .multiplyScalar(zr);
            Point3D vPart=(((sigmatr.multiplyScalar(dv)).add(one)).multiplyComponents(edivdv))
                .multiplyScalar(zv);

            Point3D Rd=(alpha.divideComponents(pi4))
                .multiplyComponents(rPart.add(vPart));

            B=(Rd.multiplyComponents(Fpsi).multiplyComponents(Ftheta)).
                divideScalar(Utilities.MATH_PI);

            //enfatizzo il colore verde
            B.x=B.x*0.31f;
            B.y=B.y*0.65f;
            B.z=B.z*0.246f;

            //calcolo dell'illuminazione diretta
            //vengono definiti i seguenti float3 per
            //leggibilita' del risultato
            double dirN1N2=(-dir.dotProduct(n1))*(dir.dotProduct(n2));
            float norma2=(float) Math.pow(norma, 2);
            radianceOutput = radianceOutput.
                add(RenderAction.material[lid].emittedLight.
                    multiplyComponents(B).multiplyScalar(area).
                    multiplyScalar(dirN1N2).multiplyScalar(5)).
                divideScalar(norma2);
          }
        }

        //Caso BRDF
        int tt =x+y* RenderAction.width;
        if(tt < RenderAction.width * RenderAction.height) {
          rnd1 = Utilities.generateRandom(RenderAction.dirSamples1[tt]);
          rnd2 = Utilities.generateRandom(RenderAction.dirSamples2[tt]);
          rnd3 = Utilities.generateRandom(RenderAction.dirSamples3[tt]);
        }

        //si carica il punto campionato sulla luce
        Point3D p = RenderAction.lights.get(i).randomPoint(rnd1,rnd2,rnd3);

        //la direzione e' quella che congiunge il punto
        //r.o al punto campionato
        Point3D dir = (p.subtract(r.o));
        //salviamo la distanza tra i due punti
        double norma = dir.normalize();
        dir = dir.getNormalizedPoint();

        //creazione del raggio d'ombra diretto verso
        //la luce
        Ray directRay= new Ray(r.o, dir);

        //viene inizializzato l'oggetto che il raggio
        //intersechera' con l'oggetto che il raggio
        //punta
        objX = RenderAction.lights.get(i);
        //si inizializza la massima distanza a cui il
        //raggio puo' arrivare

        //verifica del fattore di visibilita'
        if (utilities.intersect(directRay, objX)) {
          utilities.inters = Utilities.inf;
          utilities.intersObj = null;
          //vengono caricati i dati della luce:
          //normale nel punto p
          Point3D n2 = RenderAction.lights.get(i).normal(p);
          //identificativo del materiale della luce
          int lid = RenderAction.lights.get(i).matId;
          //calcoliamo la BRDF
          B = RenderAction.material[mId].C_T_BRDF(directRay, r, n1);

          //calcolo dell'illuminazione diretta
          //vengono definiti i seguenti float3 per
          //leggibilita' del risultato
          double dirN1N2=(-dir.dotProduct(n1))*(dir.dotProduct(n2));
          float norma2=(float) Math.pow(norma, 2);
          radianceOutput = radianceOutput
              .add(RenderAction.material[lid].emittedLight
                  .multiplyComponents(B)
                  .multiplyScalar(area)
                  .multiplyScalar(dirN1N2).multiplyScalar(RenderAction.material[mId].translucent ? 0.35 : 1))
              .divideScalar(norma2);
        }
      }
    }

    //dividiamo per tutti i sample utilizzati nell'
    //estimatore di Monte Carlo
    radianceOutput = radianceOutput.divideScalar(RenderAction.dirSamps * RenderAction.lights.size());
    return radianceOutput;
  }
}
