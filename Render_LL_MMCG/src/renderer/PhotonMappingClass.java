package renderer;

import partition.PhotonBox;
import primitive.Obj;
import primitive.Photon;
import primitive.Point3D;
import primitive.Ray;
import ui.InterfaceInitialiser;
import ui.RenderAction;

import java.util.ArrayList;

import static renderer.PhotonScatterClass.causticScatter;
import static renderer.PhotonScatterClass.photonScatter;

public class PhotonMappingClass {
  /* Metodo con lo scopo di inizializzare le variabili di interesse, ovvero i partition.PhotonBox per creare la mappa
   *  e chiamare i metodi per sparare i fotoni
   */
  public static void calculatePhotonMapping() {
    int liv = 0;

    // Calcolo del numero di partition.PhotonBox da creare
    for(int i = 1; i < RenderAction.kDepth; i++){
      RenderAction.power += Math.pow(2, i);
    }

    RenderAction.kdTree = new PhotonBox[RenderAction.power +1];
    RenderAction.causticTree = new PhotonBox[RenderAction.power +1];

    //vengono emessi i fotoni dalle luci e fatti rimbalzare all'interno della scena
    emitPhotons();

    if(RenderAction.causticPhoton > 0 && Utilities.checkRefractionObjects()) {
      caustic();
    }

    RenderAction.kdTree[0] = new PhotonBox(RenderAction.min, RenderAction.max, RenderAction.photons);
    RenderAction.causticTree[0] = new PhotonBox(RenderAction.min, RenderAction.max, RenderAction.caustics);

    balance(RenderAction.kdTree,1,liv);
    balance(RenderAction.causticTree,1,liv);
  }

  // Metodo che effette fotoni in direzioni casuali, campionando uniformemente un emisfero
  static void emitPhotons() {
    Utilities utilities = new Utilities();

    float random1;
    float random2;
    float random3;
    Obj objX = null;

    for (int i = 0; i < RenderAction.lights.size(); i++) {
      //carichiamo l'area della luce
      double area = RenderAction.lights.get(i).areaObj;
      //carichiamo i dati relativi alla luce
      int lid = RenderAction.lights.get(i).matId;
      //calcoliamo la potenza trasportata dal singolo fotone (condivisa con gli altri nPhoton)
      Point3D P = RenderAction.material[lid].emittedLight.multiplyScalar(Utilities.MATH_PI*area/(RenderAction.nPhoton));

      //per ogni fotone
      for(int s = 0; s < RenderAction.nPhoton; s++) {
        //campionamo la luce uniformemente
        random1 = Utilities.generateRandom(s);
        random2 = Utilities.generateRandom(s);
        random3 = Utilities.generateRandom(s);

        // punto scelto uniformemente nella patch i;
        Point3D p = RenderAction.lights.get(i).randomPoint(random1,random2,random3);

        //dichiariamo i parametri per distribuire uniformemente i campioni sull'emisfero:
        float rndPhi = 2* Utilities.MATH_PI*(random1);
        double rndTeta = Math.acos(Math.sqrt(random2));

        //creazione della base ortonormale
        // Create onb (ortho normal basis) on iP punto di intersezione
        Point3D u,v,w;
        w = RenderAction.lights.get(i).normal(p);
        //vettore up (simile a (0,1,0))
        Point3D up = new Point3D(0.0015f,1.0f,0.021f);
        v = w.crossProduct(up);
        v = v.getNormalizedPoint();
        u = v.crossProduct(w);

        Point3D dir;
        float cosPhi=(float) Math.cos(rndPhi);
        float sinTeta=(float) Math.sin(rndTeta);
        float sinPhi=(float) Math.sin(rndPhi);
        float cosTeta=(float) Math.cos(rndTeta);
        dir = (u.multiplyScalar(cosPhi*sinTeta))
            .add(v.multiplyScalar(sinPhi*sinTeta))
            .add(w.multiplyScalar(cosTeta));
        dir = dir.getNormalizedPoint();

        //creiamo il raggio dal punto di intersezione r.o al punto aleatorio scelto a caso sull'emisfero:
        Ray photonRay = new Ray(p,dir);
        double t= Utilities.inf;

        if(utilities.intersect(photonRay, objX)) {
          t = utilities.inters;
          utilities.inters = Utilities.inf;

          //punto di intersezione:
          Point3D iP = photonRay.o.add(photonRay.d.multiplyScalar(t));

          //creiamo il nuovo fotone e inserito nella photonMap
          Photon p2 = new Photon(iP, dir.multiplyScalar(-1), P);

          RenderAction.photons.add(p2);

          objX = utilities.intersObj;
          utilities.intersObj = null;

          photonScatter(objX, 0, p2);
        }
      }
    }
  }


  // Metodo per creare i fotoni indirizzati agli oggetti traslucenti/trasparenti chiamando il metodo causticScatter()
  static void caustic() {
    Utilities utilities = new Utilities();
    float rnd1;
    float rnd2;
    float rnd3;
    Obj objX;

    int nP=0;
    while(nP < RenderAction.causticPhoton) {    //per ogni campione
      InterfaceInitialiser.label.setText("Calcolo campioni caustiche: " + nP);

      rnd1= Utilities.generateRandom(RenderAction.loadedBoxes)*(RenderAction.lights.size());

      int l = (int) Math.floor(rnd1);
      l = l >= RenderAction.lights.size() ? 1 : l;

      double area= RenderAction.lights.get(l).areaObj;
      int lid= RenderAction.lights.get(l).matId;

      //mappa di proiezione per la luce
      ArrayList<Point3D> ProjectionMap;

      //carichiamo la potenza di ciascun fotone
      Point3D P = RenderAction.material[lid].emittedLight.multiplyScalar(Utilities.MATH_PI* RenderAction.scaleCausticPower*area/(RenderAction.causticPhoton* RenderAction.aoCausticPhoton));

      //se la luce è un triangolo
      if(RenderAction.lights.get(l).t != null) {
        //campiona uniformemente
        rnd1= Utilities.generateRandom(RenderAction.loadedBoxes);
        rnd2= Utilities.generateRandom(RenderAction.loadedBoxes);
        rnd3= Utilities.generateRandom(RenderAction.loadedBoxes);

        //un punto del triangolo in coordinate convesse
        Point3D point = RenderAction.lights.get(l).randomPoint(rnd1,rnd2,rnd3);
        Point3D normal = RenderAction.lights.get(l).normal(new Point3D());

        //calcoliamo la mappa di proiezione e teniamo conto delle ricorrenze di oggetti trasparenti
        ProjectionMap = utilities.Projection(point,normal);

        //incremento
        float dTheta= Utilities.MATH_PI/(2* RenderAction.projectionResolution);
        float dPhi=(2* Utilities.MATH_PI)/ RenderAction.projectionResolution;

        //angolo solido di ciascuna patch dell'emisfero
        float SolidAngle=dTheta*dPhi;
        //numero di patch in cui è presente un oggetto trasparente
        int nAngle = ProjectionMap.size();

        if(nAngle > 0) {
          //se è stato trovato almeno un oggetto trasparente

          //creo la base ortonormale
          Point3D u,v,w;
          w = RenderAction.lights.get(l).normal(new Point3D());
          Point3D up = new Point3D(0.0015f,1.0f,0.021f);
          v = w.crossProduct(up);
          v = v.getNormalizedPoint();
          u = v.crossProduct(w);

          //scalo la potenza del fotone in base all'angolo solido
          float totSolidAngle=nAngle*SolidAngle;
          float scale= totSolidAngle/2* Utilities.MATH_PI;

          //potenza scalata
          Point3D P2 = P.multiplyScalar(scale);

          nP++;

          int nS=0;

          while(nS < RenderAction.aoCausticPhoton) {
            //campiono uniformemente la mappa di proiezione
            rnd1 = Utilities.generateRandom(RenderAction.loadedBoxes)*(nAngle);

            int floor = (int) Math.floor(rnd1);

            //prendo un angolo a caso dalla mappa di proiezione
            Point3D angle = ProjectionMap.get(floor >= 1 ? 0 : floor);

            //creo un raggio distribuito uniformemente all'interno della patch
            double rndPhi;
            double rndTheta;

            //campiona uniformemente
            rndPhi = Utilities.generateRandom(RenderAction.projectionResolution * RenderAction.projectionResolution)*dPhi;
            rndTheta = Utilities.generateRandom(RenderAction.projectionResolution * RenderAction.projectionResolution)*((angle.z+dTheta)*Math.sin(angle.z+dTheta)-angle.y);

            //angoli finali per la creazione del raggio
            double Phi= angle.x+rndPhi;
            double Theta=angle.y+rndTheta;
            //raggio
            Point3D dir = u.multiplyScalar(Math.cos(Phi)*Math.sin(Theta)).add(v.multiplyScalar(Math.sin(Phi)*Math.sin(Theta))).add(w.multiplyScalar(Math.cos(Theta)));
            Ray pRay= new Ray(point,dir);

            double t;
            objX = null;

            if(utilities.intersect(pRay, objX)) {
              objX = utilities.intersObj;
              t = utilities.inters;
              //si controlla se l'oggetto colpito è trasparente
              if(RenderAction.material[objX.matId].refractionColor.max() > 0) {
                //creiamo il nuovo fotone e ne calcoliamo i rimbalzi nella scena
                Point3D iP=pRay.o.add(pRay.d.multiplyScalar(utilities.inters));
                Photon p2 = new Photon(iP,dir.multiplyScalar(-1),P2);

                utilities.intersObj = null;
                utilities.inters = Utilities.inf;

                causticScatter(objX, 0, p2);
              }
            }

            //aumento il numero di fotoni sparati per l'emisfero
            nS++;
          }
        }
      }
    }
  }


  // Metodo per raffinare la suddivisione dei partition.PhotonBox e ridividerli nell'albero tree
  static void balance(PhotonBox[] tree, int index, int liv) {
    liv++;

    if(liv < RenderAction.kDepth) {
      int dim = tree[index-1].dim;
      double median= tree[index-1].planePos;
      double n = tree[index-1].ph.size();
      ArrayList<Photon> ph= tree[index-1].ph;

      Point3D min= tree[index-1].V[0];
      Point3D max= tree[index-1].V[1];

      ArrayList<Photon> ph1 = new ArrayList<>();
      ArrayList<Photon> ph2 = new ArrayList<>();

      switch (dim) {
        case 0:
          //taglio con il piano x=median a metà del Bound

          for(int i=0; i<n; i++){
            if(ph.get(i).position.x < median) {
              ph1.add(ph.get(i));
            } else {
              ph2.add(ph.get(i));
            }
          }

          tree[(2*index)-1] = new PhotonBox(min, new Point3D(median,max.y,max.z),ph1);
          tree[(2*index)] = new PhotonBox(new Point3D(median,min.y,min.z), max, ph2);

          break;
        case 1:
          //taglio con il piano y=median a metà del Bound

          for(int i=0; i<n; i++){
            if(ph.get(i).position.y < median){
              ph1.add(ph.get(i));
            } else {
              ph2.add(ph.get(i));
            }
          }

          tree[(2*index)-1] = new PhotonBox(min, new Point3D(max.x,median,max.z), ph1);
          tree[2*index] = new PhotonBox(new Point3D(min.x,median,min.z), max, ph2);

          break;
        case 2:
          //taglio con il piano z=median a metà del bound

          for(int i=0; i<n; i++){
            if(ph.get(i).position.z < median){
              ph1.add(ph.get(i));
            } else {
              ph2.add(ph.get(i));
            }
          }

          tree[(2*index)-1] = new PhotonBox(min, new Point3D(max.x,max.y,median), ph1);
          tree[2*index] = new PhotonBox(new Point3D(min.x,min.y,median), max, ph2);
      }

      balance(tree,2*index,liv);
      balance(tree,(2*index)+1,liv);
    }
  }
}
