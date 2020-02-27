package renderer;

import primitive.Obj;
import primitive.Photon;
import primitive.Point3D;
import primitive.Ray;
import ui.RenderAction;

public class PhotonScatterClass {
  /* Metodo per il rimbalzo delle caustiche, controllando l'intersezione con la superficie e quindi col suo materiale
   *  per definirne le proprietà della riflessione/rifrazione e con quale potenza
   */
  static void photonScatter(Obj obj, int n_, Photon p) {
    Utilities utilities = new Utilities();

    //carichiamo il numero massimo di rimbalzi previsti per un fotone
    float MAX = Utilities.MAX_DEPTH_PHOTON;
    //ci ricaviamo quindi il peso da utilizzare nella roulette russa
    double peso=(MAX-n_)/MAX;

    //carichiamo l'ID del materiale dell'oggetto colpito
    int mId = obj.matId;

    //dai coefficenti del materiale otteniamo le probabilità di riflessione , diffusione , rifrazione del fotone
    double P_refl = RenderAction.material[mId].reflectionColor.average();
    double P_diff = RenderAction.material[mId].diffusionColor.average();
    double P_glass = RenderAction.material[mId].refractionColor.average();

    //ci assicuriamo che la somma di questi valori corrispondino ad una probabilità
    double Ptot = P_refl + P_diff + P_glass;

    if(Ptot > 1) {
      P_refl /= Ptot;
      P_diff /= Ptot;
      P_glass /= Ptot;
    }

    //aggiungiamo la probabilità di assorbimento del fotone data dal parametro peso
    double[] P = new double[3];
    P[0] = P_refl*peso;
    P[1] = P[0] + P_diff*peso;
    P[2] = P[1] + P_glass*peso;

    //carichiamo la normale dell'oggetto in esame
    Point3D n = obj.normal(p.position);
    Obj objY = null;

    //raggio di entrata del fotone:
    Ray entryRay = new Ray(p.position, p.direction);

    double rnd = Math.random();


    //metodo della Roulette russa
    //probabilità materiali riflettenti: il fotone viene riflesso perfettamente
    if(rnd < P[0]) {
      Point3D refl = utilities.reflect(p.direction, n);
      Ray reflRay = new Ray(p.position, refl);

      double t2 = Utilities.inf;

      if(utilities.intersect(reflRay, objY)){
        n_++;
        t2 = utilities.inters;
        utilities.inters = Utilities.inf;

        Point3D iP = reflRay.o.add(reflRay.d.multiplyScalar(t2));
        Point3D oldP = p.power;
        double cos_i = p.direction.dotProduct(n);
        Point3D Fresn = RenderAction.material[mId].getFresnelCoefficient(cos_i);
        Point3D BRDF = RenderAction.material[mId].S_BRDF(Fresn);
        Point3D newP = oldP.multiplyComponents(BRDF).multiplyScalar(1/(P_refl*peso));
        Photon p2 = new Photon(iP,refl.multiplyScalar(-1), newP);

        objY = utilities.intersObj;
        utilities.intersObj = null;

        photonScatter(objY, n_, p2);
      }
    }

    //probabilità materiali diffusivi: il fotone viene riflesso con probabilità uniforme sull'emisfero.
    if((rnd<P[1])&&(rnd>P[0])) {
      double rndls;
      double rndlt;

      //s parte da 0 quindi per il numero del sample metto s+1 (parto stavolta dal numero random generato per ogni pixel aoSampleX e aoSamplesY)
      rndls = Math.random();
      rndlt = Math.random();

      //distribuiamo i numeri sull'emisfero
      double rndPhi = 2* Utilities.MATH_PI*(rndls);
      double rndTeta = Math.acos(Math.sqrt(rndlt));

      // Create onb (ortho normal basis) on iP punto di intersezione
      Point3D u,v,w;
      w = obj.normal(p.position);
      //vettore up (simile a (0,1,0))
      Point3D up = new Point3D(0.0015f,1.0f,0.021f);
      v = w.crossProduct(up);
      v = v.getNormalizedPoint();
      u = v.crossProduct(w);

      Point3D dir = u.multiplyScalar((Math.cos(rndPhi)*Math.sin(rndTeta))).add(v.multiplyScalar(Math.sin(rndPhi)*Math.sin(rndTeta))).add(w.multiplyScalar(Math.cos(rndTeta)));
      dir = dir.getNormalizedPoint();

      //creazione del raggio
      Ray reflRay = new Ray(p.position,dir);

      double t2 = Utilities.inf;

      if(utilities.intersect(reflRay, objY)){
        n_++;
        t2 = utilities.inters;
        utilities.inters = Utilities.inf;

        Point3D iP = reflRay.o.add(reflRay.d.multiplyScalar(t2));
        Point3D oldP = p.power;
        Point3D BRDF = RenderAction.material[mId].diffusionColor.add(RenderAction.material[mId].reflectionColor);
        Point3D newP= oldP.multiplyComponents((BRDF).multiplyScalar(1/(P_diff*peso)));
        Photon p2 = new Photon(iP,dir.multiplyScalar(-1),newP);

        RenderAction.photons.add(p2);

        objY = utilities.intersObj;
        utilities.intersObj = null;

        photonScatter(objY,n_,p2);
      }
    }


    //probabilità materiali trasparenti: il fotone viene rifratto.
    if((rnd<P[2])&&(rnd>P[1])) {
      // primitive.Ray refraction based on normal
      //carico un array di 3 raggi corrispondenti alle 3 lunghezza d'onda di base RGB
      Ray[] refrRay = new Ray[3];

      for (int i = 0; i < 3; i++) {
        refrRay[i] = new Ray();
      }

      refrRay[0].o = p.position;
      refrRay[1].o = p.position;
      refrRay[2].o = p.position;
      //i raggi vengono rifratti in base all'indice ior ovvero l'indice di rifrazione del materiale esterno diviso l'indice di rifrazione del materiale interno

      refrRay = utilities.refract(refrRay, p.direction, n, RenderAction.material[mId].refractionIndexRGB);

      //velocizzazione del calcolo
      //se IOR è uguale per tutte e tre le componenti uso un solo raggio rifratto
      if((RenderAction.material[mId].refractionIndexRGB.x == RenderAction.material[mId].refractionIndexRGB.y)
          &&(RenderAction.material[mId].refractionIndexRGB.x == RenderAction.material[mId].refractionIndexRGB.z)){

        if(refrRay[0].depth!=0){
          double t2 = Utilities.inf;

          if(utilities.intersect(refrRay[0], objY)){
            n_++;
            t2 = utilities.inters;
            utilities.inters = Utilities.inf;

            Point3D iP = refrRay[0].o.add(refrRay[0].d.multiplyScalar(t2));
            Point3D oldP = p.power;
            double cos_i = p.direction.dotProduct(n);
            Point3D Fresn= RenderAction.material[mId].getFresnelCoefficient(cos_i);
            Point3D BRDF = RenderAction.material[mId].T_BRDF(Fresn);
            Point3D newP= oldP.multiplyComponents(BRDF).multiplyScalar(1/(P_glass*peso));
            Photon p2= new Photon(iP,refrRay[0].d.multiplyScalar(-1),newP);

            objY = utilities.intersObj;
            utilities.intersObj = null;

            photonScatter(objY, n_, p2);
          }
        }
      } else{ //altrimenti si deve utilizzare un raggio per ogni componente
        double t2 = Utilities.inf;
        Point3D oldP = p.power;
        n_++;

        if(refrRay[0].depth!=0){
          objY = null;

          if(utilities.intersect(refrRay[0], objY)){
            t2 = utilities.inters;
            utilities.inters = Utilities.inf;

            Point3D iP=refrRay[0].o.add(refrRay[0].d.multiplyScalar(t2));
            Point3D BRDF = RenderAction.material[mId].C_T_BRDF(entryRay,refrRay[0],n).add(RenderAction.material[mId].refractionColor);
            Point3D newP = new Point3D(oldP.x*BRDF.x*(1/(3*P_glass*peso)),0,0);
            Photon p2 = new Photon(iP,refrRay[0].d.multiplyScalar(-1),newP);

            objY = utilities.intersObj;
            utilities.intersObj = null;

            photonScatter(objY,n_,p2);
          }
        }

        if(refrRay[1].depth!=0){

          t2 = Utilities.inf;
          objY = null;

          if(utilities.intersect(refrRay[1], objY)){
            t2 = utilities.inters;
            utilities.inters = Utilities.inf;

            Point3D iP=refrRay[1].o.add(refrRay[2].d.multiplyScalar(t2));
            Point3D BRDF = RenderAction.material[mId].C_T_BRDF(entryRay,refrRay[1],n).add(RenderAction.material[mId].refractionColor);
            Point3D newP = new Point3D(0,oldP.y*BRDF.y*(1/(3*P_glass*peso)),0);
            Photon p2 = new Photon(iP,refrRay[1].d.multiplyScalar(-1),newP);

            objY = utilities.intersObj;
            utilities.intersObj = null;

            photonScatter(objY, n_, p2);
          }
        }

        if(refrRay[2].depth!=0) {
          t2 = Utilities.inf;
          objY = null;

          if(utilities.intersect(refrRay[2], objY)){
            t2 = utilities.inters;
            utilities.inters = Utilities.inf;

            Point3D iP=refrRay[2].o.add(refrRay[2].d.multiplyScalar(t2));
            Point3D BRDF = RenderAction.material[mId].C_T_BRDF(entryRay,refrRay[2],n).add(RenderAction.material[mId].refractionColor);
            Point3D newP = new Point3D(0,0,oldP.z*BRDF.z*(1/(3*P_glass*peso)));
            Photon p2 = new Photon(iP,refrRay[2].d.multiplyScalar(-1),newP);

            objY = utilities.intersObj;
            utilities.intersObj = null;

            photonScatter(objY, n_, p2);
          }
        }
      }
    }
  }

  /* Metodo per il rimbalzo delle caustiche, controllando l'intersezione con la superficie e quindi col suo materiale
   *  per definirne le proprietà della riflessione/rifrazione e con quale potenza
   */
  static void causticScatter(Obj obj, int n_, Photon p) {
    Utilities utilities = new Utilities();

    int mId = obj.matId;

    if(RenderAction.material[mId].diffusionColor.max()>0){
      RenderAction.caustics.add(p);
    }

    n_++;
    if(n_< Utilities.MAX_DEPTH_CAUSTIC){
      Obj objY = null;

      if((RenderAction.material[mId].refractionColor.max()>0)&&(RenderAction.material[mId].absorptionCoefficient.max()==0)) {
        Point3D n = obj.normal(p.position);

        Ray entryRay = new Ray(p.position, p.direction);
        double cos_i= n.dotProduct(p.direction);
        Point3D Fresn= RenderAction.material[mId].getFresnelCoefficient(cos_i);

        //velocizzazione del calcolo
        //se IOR è uguale per tutte e tre le componenti uso un solo raggio rifratto
        if((RenderAction.material[mId].refractionIndexRGB.x == RenderAction.material[mId].refractionIndexRGB.y)
            &&(RenderAction.material[mId].refractionIndexRGB.x == RenderAction.material[mId].refractionIndexRGB.z)){
          Point3D dir = utilities.refract(entryRay.d,n, RenderAction.material[mId].refractionIndexRGB.x);

          if(dir != null) {
            Ray refrRay = new Ray(entryRay.o, dir);

            double t2 = Utilities.inf;
            if(utilities.intersect(refrRay, objY)){
              objY = utilities.intersObj;
              t2 = utilities.inters;

              utilities.inters = Utilities.inf;
              utilities.intersObj = null;

              n_++;
              Point3D iP=refrRay.o.add(refrRay.d.multiplyScalar(t2));
              Point3D oldP = p.power;
              Point3D BRDF = RenderAction.material[mId].T_BRDF(Fresn);
              Point3D newP = oldP.multiplyComponents(BRDF);
              Photon p2= new Photon(iP,refrRay.d.multiplyScalar(-1),newP);

              causticScatter(objY, n_, p2);
            }
          }
        } else {
          //altrimenti si deve utilizzare un raggio per ogni componente
          // primitive.Ray refraction based on normal
          //carico un array di 3 raggi corrispondenti alle 3 lunghezza d'onda di base RGB

          Ray[] refrRay= new Ray[3];
          refrRay[0].o=p.position;
          refrRay[1].o=p.position;
          refrRay[2].o=p.position;

          //i raggi vengono rifratti in base all'indice ior ovvero l'indice di rifrazione del materiale esterno diviso l'indice di rifrazione del materiale interno
          refrRay = utilities.refract(refrRay, p.direction, n, RenderAction.material[mId].refractionIndexRGB);
          double t2 = Utilities.inf;
          Point3D oldP = p.power;
          n_++;
          Point3D BRDF = RenderAction.material[mId].T_BRDF(Fresn);

          if(refrRay[0].depth!=0){
            if(utilities.intersect(refrRay[0], objY)){
              objY = utilities.intersObj;
              t2 = utilities.inters;

              Point3D iP=refrRay[0].o.add(refrRay[0].d.multiplyScalar(t2));
              Point3D newP= new Point3D(oldP.x*BRDF.x,0,0);
              Photon p2 = new Photon(iP,refrRay[0].d.multiplyScalar(-1),newP);

              utilities.intersObj = null;
              utilities.inters = Utilities.inf;

              causticScatter(objY, n_, p2);
            }
          }

          if(refrRay[1].depth!=0){
            t2= Utilities.inf;
            objY = null;

            if(utilities.intersect(refrRay[1], objY)){
              objY = utilities.intersObj;
              t2 = utilities.inters;

              Point3D iP = refrRay[1].o.add(refrRay[2].d.multiplyScalar(t2));
              Point3D newP = new Point3D(0,oldP.y*BRDF.y,0);
              Photon p2=new Photon(iP,refrRay[1].d.multiplyScalar(-1),newP);

              utilities.intersObj = null;
              utilities.inters = Utilities.inf;

              causticScatter(objY, n_, p2);
            }
          }

          if(refrRay[2].depth!=0) {
            t2= Utilities.inf;
            objY = null;

            if(utilities.intersect(refrRay[2], objY)) {
              objY = utilities.intersObj;
              t2 = utilities.inters;

              Point3D iP = refrRay[2].o.add(refrRay[2].d.multiplyScalar(t2));
              Point3D newP = new Point3D(0,0,oldP.z*BRDF.z);
              Photon p2 = new Photon(iP,refrRay[2].d.multiplyScalar(-1),newP);


              utilities.intersObj = null;
              utilities.inters = Utilities.inf;

              causticScatter(objY, n_, p2);
            }
          }
        }
      }
    }
  }
}
