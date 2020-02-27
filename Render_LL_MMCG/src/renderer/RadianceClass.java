package renderer;

import primitive.Material;
import primitive.Obj;
import primitive.Point3D;
import primitive.Ray;
import ui.RenderAction;

import static renderer.EmittedObjRadianceClass.emittedObjRadiance;
import static renderer.FinalGatheringClass.finalGathering;
import static renderer.PhotonRadianceClass.photonRadiance;

public class RadianceClass {
  //Metodo che serve a calcolare la radianza nel punto
  //dell'oggetto che stiamo considerando
  //r e' il raggio che parte dal punto dell'oggetto
  //intersecato (dal raggio della fotocamera considerato)
  //e arriva all'osservatore
  //o e' l'oggetto in cui ci troviamo (quello intersecato
  //dal raggio della fotocamera che stavamo considerando)
  //x indica la colonna in cui ci troviamo, y indica la
  //riga in cui ci troviamo
  // Questo metodo tiene conto del supersampling che applichiamo (ovvero un numero di sample per pixel > 1)
  static Point3D radiance(Ray r, Obj o, int x, int y) {
    Utilities utilities = new Utilities();
    //definisco e inizializzo a (0,0,0) il float3 per la
    //radianza riflessa
    Point3D radianceRefl=new Point3D();
    //definisco e inizializzo a (0,0,0) il float3 per la
    //radianza rifratta
    Point3D radianceRefr=new Point3D();

    //salvo in una variabile l'indice del materiale dell'
    //oggetto considerato
    int mId=o.matId;
    //normale dell'oggetto nel punto osservato
    Point3D n1;
    n1= o.normal(r.o);

    //coseno tra il raggio entrante e la normale
    double cos_i= r.d.dotProduct(n1);

    //fattore di Fresnel
    Point3D Fresn = RenderAction.material[mId].getFresnelCoefficient(cos_i);

    //si verifica se il materiale ha una componente speculare
    //e che non sia stato superato il numero massimo di
    //riflessioni
    if((RenderAction.material[mId].reflectionColor.max() > 0) &&
        (RenderAction.nRay < RenderAction.maxDepth +1)) {
      //con questo controllo si evitano le riflessioni interne
      //al materiale
      if (cos_i>0) {
        // riflessione del raggio in entrata rispetto alla
        //normale n1
        Point3D refl= Point3D.reflect(r.d,n1);
        //si verifica che il materiale sia perfettamente
        //speculare o che non sia la prima riflessione (si evita
        //in questo modo l'aumento esponenziale dei raggi nel
        //caso in cui ci siano riflessioni multiple tra specchi
        //imperfetti)
        if((RenderAction.material[mId].refImperfection == 0) || (RenderAction.nRay > 0)) {
          //definisco e inizializzo a (0,0,0) il raggio riflesso
          Ray reflRay=new Ray();
          //l'origine e' la stessa del raggio passato come
          //parametro
          reflRay.o=r.o;
          //la direzione e' la riflessa di quella del raggio
          //passato come parametro
          reflRay.d=refl;
          //dichiaro e inizializzo la variabile t in cui
          //salveremo il punto di intersezione fra l'oggetto
          //considerato e reflRay
          double t= Utilities.inf;
          //definizione e inizializzoazione a null dell'oggetto
          //che si andra' ad intersecare
          Obj objX;
          objX=null;
          //intersezione del raggio con gli elementi della scena
          if(utilities.intersect(reflRay,objX)){
            //pongo t uguale al valore di intersezione
            //memorizzato nella variabile globale inters
            t= utilities.inters;
            //resetto inters uguale a inf in modo da avere
            //il giusto valore di partenza la prossima volta
            //che si utilizzera' il metodo intersect()
            utilities.inters = Utilities.inf;
            //salvo nell'elemento i-esimo dell'array objX
            //l'elemento intersecato dal raggio ffRay
            objX= utilities.intersObj;
            //resetto intersObj=null in modo da avere il
            //giusto valore di partenza la prossima volta che
            //si utilizzera' il metodo intersect()
            utilities.intersObj =null;
            //si calcola il punto di intersezione
            Point3D iP=(reflRay.o).add((reflRay.d).
                multiplyScalar(t));
            //si crea il nuovo raggio
            Ray r2=new Ray(iP,refl.multiplyScalar(-1));
            //si aumentano il numero di riflessioni per il
            //raytracing
            RenderAction.nRay++;
            //si calcola la radianza riflessa utilizzando
            //ricorsivamente la funzione radiance
            radianceRefl
                = radiance(r2, objX, x, y).multiplyComponents(RenderAction.material[mId].S_BRDF(Fresn));
            //si diminuiscono il numero di riflessioni
            RenderAction.nRay--;
          }
        }//fine if per specchi imperfetti
        else {
          //si aumenta il numero di riflessioni una volta per
          //tutti i campioni
          RenderAction.nRay++;
          //per ogni campione
          for(int s = 0; s < RenderAction.refSamps; s++)
          {
            //dichiaro due variabili aleatorie
            float random1;
            float random2;

            //flag per la verifica dell'orientazione del
            //raggio
            boolean okdir=true;
            //direzione del nuovo raggio
            Point3D dir=new Point3D();
            //finche' non abbiamo un raggio con giusta
            //orientazione
            while(okdir){
              //creazione delle variabili aleatorie
              //uniformi usando come semi gli elementi dell'
              //array refSamples1
              random1= Utilities.generateRandom(RenderAction.refSamples1[x+y* RenderAction.width]);
              random2= Utilities.generateRandom(RenderAction.refSamples2[x+y* RenderAction.width]);

              //Inverse Cumulative Distribution Function
              //del coseno modificata
              //creiamo la base ortonormale per generare
              //la direzione del raggio riflesso reflRay
              float rndPhi=2* Utilities.MATH_PI *(random1);
              float rndTeta=(float) Math.acos((float) Math.pow(random2, RenderAction.material[mId].refImperfection));
              // creazione della base ortonormale rispetto
              //alla direzione di riflessione
              Point3D u,v,w;
              //inizializzo width uguale alla riflessione del
              //raggio in entrata
              w=refl;
              //vettore up (simile a (0,1,0))
              Point3D up=new Point3D(0.0015f,1.0f,0.021f);
              //il prodotto vettoriale tra width e up mi
              //genera il vettore v normale a entambi, che
              //normalizzo
              v=w.crossProduct(up);
              v=v.getNormalizedPoint();
              //il prodotto vettoriale tra v e width mi genera
              //il vettore u normale a entambi dal momento
              //che i vettori v e width sono gia' normali, non
              //c'e' bisogno di noemalizzare il vettoe u)
              u=v.crossProduct(w);
              //ora che abbiamo la base ortonormale,
              //possiamo calcolare la direzione dir
              //salvo in delle cariabili i calori di seno
              //e coseno necessari per il calcolo di dir
              float cosPhi=(float) Math.cos(rndPhi);
              float sinTeta=(float) Math.sin(rndTeta);
              float sinPhi=(float) Math.sin(rndPhi);
              float cosTeta=(float) Math.cos(rndTeta);
              //dir=(u*(cosPhi*sinTeta))+(v*(sinPhi*sinTeta)
              //)+(width*(cosTeta)) poi normalizzato
              dir=(u.multiplyScalar(cosPhi*sinTeta)).add(v.multiplyScalar(sinPhi*sinTeta)).add(w.multiplyScalar(cosTeta));

              //si verifica che la direzione formi un angolo
              //di massimo 90 gradi con la normale
              if(dir.dotProduct(n1)>0)
                okdir=false;
              //altrimenti si cerca una nuova direzione
            }//fine while(okdir)

            //creazione del raggio riflesso
            Ray reflRay=new Ray();
            reflRay.o=r.o;
            reflRay.d=dir;
            //massima distanza del raggio
            double t;
            Obj objX = null;
            //intersezione con gli oggetti della scena
            if(utilities.intersect(reflRay, objX)){
              t= utilities.inters;
              utilities.inters = Utilities.inf;
              objX= utilities.intersObj;
              utilities.intersObj =null;
              //punto di intersezione del raggio con
              //l'oggetto objX:
              Point3D iP=(reflRay.o).add((reflRay.d).multiplyScalar(t));
              //nuovo raggio:
              Ray r2=new Ray(iP,refl.multiplyScalar(-1));

              //calcolo della radianza riflessa
              radianceRefl
                  =radianceRefl.add(radiance(r2, objX, x, y).multiplyComponents(RenderAction.material[mId].S_BRDF(Fresn)));
            }
          }
          RenderAction.nRay--;
          //divido per il numero di raggi che sono stati creati
          radianceRefl=radianceRefl.divideScalar((float) RenderAction.refSamps);
        }
      }
    }

    //Rifrazione
    //Si verifica che l'oggetto abbia un coefficiente di rifrazione >0, non sia un metallo
    //  e non si siano superato il numero massimo di riflessioni del ray tracer
    if((RenderAction.material[mId].refractionColor.max()>0)
        &&(RenderAction.nRay < Utilities.MAX_DEPTH +1)
        &&(RenderAction.material[mId].absorptionCoefficient.max()==0)) {
      //si verifica che l'indice di rifrazione sia uguale per
      //tutte le componenti RGB
      //in questo caso il calcolo per la rifrazione sara'
      //semplificato
      if((RenderAction.material[mId].refractionIndexRGB.x == RenderAction.material[mId].refractionIndexRGB.y)
          && (RenderAction.material[mId].refractionIndexRGB.x == RenderAction.material[mId].refractionIndexRGB.z))
      {
        //direzione del raggio rifratto:
        //calcolo della direzione per il raggio rifratto:
        Point3D dir = Point3D.getRefraction(r.d, n1, RenderAction.material[mId].refractionIndexRGB.x);
        //se c'e' effettivamente una rifrazione non interna
        if(!dir.equals(new Point3D(-1.0f))) {

          //viene creato il raggio rigratto
          Ray refrRay=new Ray(r.o,dir);

          //si verifica il parametro di imperfezione del
          //materiale
          if((RenderAction.material[mId].refImperfection==0)||(RenderAction.nRay>0))
          {
            double t= Utilities.inf;
            Obj objX;
            objX=null;

            if(utilities.intersect(refrRay, objX))
            {
              t= utilities.inters;
              utilities.inters = Utilities.inf;
              objX= utilities.intersObj;
              utilities.intersObj =null;
              Point3D iP=(refrRay.o).add(
                  refrRay.d.multiplyScalar(t));
              Ray r2=new Ray(iP,dir.multiplyScalar(-1));

              RenderAction.nRay++;
              //calcolo della radianza rifratta
              radianceRefr = radiance(r2,objX,x,y).multiplyComponents(RenderAction.material[mId].T_BRDF(Fresn));
              RenderAction.nRay--;
            }
          } else {
            RenderAction.nRay++;
            //per ogni sample
            for(int s = 0; s < RenderAction.refSamps; s++){
              float random1;
              float random2;
              //flag di controllo sulla direzione creata
              boolean okdir=true;
              //float3 dir;

              while(okdir){
                //variabili aleatorie uniformi in
                //[0,1]
                random1= Utilities.generateRandom(RenderAction.refSamples1[x+y* RenderAction.width]);
                random2= Utilities.generateRandom(RenderAction.refSamples1[x+y* RenderAction.width]);

                //distribuisco i numeri random sull'
                //emisfero
                float rndPhi=2* Utilities.MATH_PI *(random1);
                float rndTeta=(float) Math.acos((float) Math.pow(random2, RenderAction.material[mId].refImperfection));

                // creazione della base ortonormale
                //creata a partire dal raggio rifratto
                Point3D u,v,w;
                w=refrRay.d;
                //vettore up (simile a (0,1,0))
                Point3D up=new Point3D(0.0015f,1.0f,0.021f);
                v=w.crossProduct(up);
                v=v.getNormalizedPoint();
                u=v.crossProduct(w);
                //si calcola la direzione del raggio

                float cosPhi=(float) Math.cos(rndPhi);
                float sinTeta=(float) Math.sin(rndTeta);
                float sinPhi=(float) Math.sin(rndPhi);
                float cosTeta=(float) Math.cos(rndTeta);
                dir=(u.multiplyScalar(cosPhi*sinTeta))
                    .add(v.multiplyScalar(sinPhi*sinTeta))
                    .add(w.multiplyScalar(cosTeta));
                //si verifica che l'angolo tra la
                //normale e la nuova direzione sia
                //maggiore di 90 gradi
                if(dir.dotProduct(n1.multiplyScalar(-1))>0)
                  okdir=false;
              }//fine while(okdir)

              //nuovo raggio:
              Ray rRay=new Ray();
              rRay.o=r.o;
              rRay.d=dir;

              double t;
              Obj objX = null;

              if(utilities.intersect(refrRay, objX)){
                t= utilities.inters;
                utilities.inters = Utilities.inf;
                objX= utilities.intersObj;
                utilities.intersObj =null;
                //punto di intersezione
                Point3D iP=(refrRay.o).add((refrRay.d).multiplyScalar(t));
                Ray r2=new Ray(iP,dir.multiplyScalar(-1));

                //calcolo della radianca rifratta
                radianceRefr=radianceRefr.add(radiance(r2,objX,x,y).multiplyComponents(RenderAction.material[mId].T_BRDF(Fresn)));
              }
            }
            RenderAction.nRay--;
            //si mediano i contributi di tutti i raggi
            //usati
            radianceRefr=radianceRefr.divideScalar(RenderAction.refSamps);
          }
        }
      } else {
        //se l'indice di rifrazione e' diverso nelle 3
        //componeneti RGB allora si devono calcolare 3 raggi
        //uno per ogni componente. In questo caso pero' per
        //facilitare il calcolo non viene considerato l'indice
        //di imperfezione del materiale

        // rifrazone del raggio basata sulla normale
        // Raggio utilizzato per la rifrazione
        Ray[] refrRay= new Ray[3];
        refrRay[0]=new Ray();
        refrRay[1]=new Ray();
        refrRay[2]=new Ray();
        refrRay[0].o=r.o;
        refrRay[1].o=r.o;
        refrRay[2].o=r.o;
        float[] K = {0, 0, 0};

        //calcolo dei 3 raggi rifratti
        refrRay = Point3D.getRefraction(refrRay,r.d,n1, RenderAction.material[mId].
            refractionIndexRGB);

        //carichiamo la brdf sul vettore g[] di 3 elementi
        //cosi da accedervi piu' facilmente
        Point3D brdf= RenderAction.material[mId].T_BRDF(Fresn);
        double[] g = {brdf.x, brdf.y, brdf.z};
        RenderAction.nRay++;
        //per ogni componente RGB
        for(int i=0;i<3;i++) {
          //si verifica che non ci sia stata riflessione
          //totale
          if(refrRay[i].depth!=0){
            double t= Utilities.inf;
            Obj objX;
            objX=null;

            if(utilities.intersect(refrRay[i], objX)){
              t= utilities.inters;
              utilities.inters = Utilities.inf;
              objX= utilities.intersObj;
              utilities.intersObj =null;
              RenderAction.nRay++;
              Point3D iP=(r.o).add((refrRay[i].d).multiplyScalar(t));
              Ray r2=new Ray(iP,refrRay[i].d.multiplyScalar(-1));

              //viene calcolato il valore di radianza
              //del raggio
              Point3D pr= radiance(r2,objX,x,y);
              //si deve ora estrapolare la componente
              //i di tale radianza
              double[] rg=new double[3];
              rg[0]=pr.x;
              rg[1]=pr.y;
              rg[2]=pr.z;
              //si moltiplica infine per la componente
              //della brdf corrispondente
              K[i]+=rg[i]*g[i];
            }
          }
        }

        radianceRefr=new Point3D(K[0],K[1],K[2]);
        RenderAction.nRay--;
      }
    }

    //algoritmi per il calcolo dell'illuminazione globale:
    //si controlla che il materiale abbia componente Kd>0 in
    //almeno una delle 3 componenti o che il coefficiente
    //slope per la rugosita' della superficie sia >0 o che
    //il materiale emetta luce
    if((RenderAction.material[mId].diffusionColor.max()>0)||(Material.slope >0)||
        (RenderAction.material[mId].emittedLight.max()>0)){
      //photon mapping:
      //if we render with photon mapping
      if(RenderAction.doPhotonMapping) {
        return photonRadiance(r, o, RenderAction.kdTree, RenderAction.photonSearchDisc, RenderAction.nPhotonSearch)
            .add(radianceRefr).add(radianceRefl)
            .add(photonRadiance(r, o, RenderAction.causticTree, RenderAction.causticSearchDisc, RenderAction.nCausticSearch))
            .add(emittedObjRadiance(o));
      }

      //metodo di Final gathering:
      if (RenderAction.doFinalGathering) {
        Point3D f= finalGathering(r, x, y, o);
        return f.add(radianceRefr).add(radianceRefl).add(emittedObjRadiance(o));
      }

      //metodo di Jacobi stocastico:
      if(RenderAction.doJacobi){
        double areaInverse= (1.0f)/((o).areaObj);
        Point3D L=((o).P).multiplyScalar(areaInverse);

        //si somma alla radianza emessa dalla patch
        //(pesata per l'area), la radianza data dalla
        //riflessione e dalla rifrazione, quindi si
        //restituisce il valore di radianza calcolato in
        //questo modo

        return L.add(radianceRefr).add(radianceRefl);
      }

      //se non e' stato impostato nessuno di tali metodi
      //allora viene restituito il colore nero
      return new Point3D();

    } else {
      //se il materiale e' riflettente o trasparente
      return radianceRefl.add(radianceRefr);
    }
  }
}
