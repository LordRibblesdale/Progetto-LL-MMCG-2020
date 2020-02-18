import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class Renderer {
  Utilities utilities;

  Renderer(Utilities utilities) {
    this.utilities = utilities;
  }

  //Metodo per il calcolo dell'illuminazione diretta, cioe'
  //il contributo che arriva all'oggetto direttamente dalla
  //fonte di luce
  //I parametri in input sono:
  //r: raggio di entrata
  //o: oggetto dal quale partira' il nuovo raggio
  //x e y indici del seme iniziale per la generazione
  //di numeri randomici
  Point3D directIllumination(Ray r, Obj o, int x, int y) {
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
      float area = RenderAction.lights.get(i).areaObj;

      //per ogni s campione della luce tra i dirSamps
      //campioni totali per l'illuminazione diretta
      for (int s = 0; s < RenderAction.dirSamps; s++) {

        //controllo per decidere se calcolare la
        //BRDF o la BSSRDF, quindi la calcoliamo
          //nel punto osservato
        //inizializziamo B che e' la variabile in
        //cui salviamo il valore di BRDF o la
        //BSSRDF
        Point3D B;
        //Caso BSSRDF
        if(RenderAction.material[mId].translucent) {
          //zv e zr sono scelti arbitrariamente in
          //modo tale da essere sufficientemente
          //vicini alla superficie
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
          //posso usare il valore x+y*w nell'array
          //dirSamples1[], altrimenti l'ultimo indice
          //sarebbe fuori dal range (ricordo che la
          //misura e' w*h ma gli indici vanno da 0 a
          //w*h-1)
          int tt = x+y* RenderAction.w;
          //allora faccio l'if per tt<w*h cosi' da
          //accertarmi che non sia considerato l'indice
          //w*h-esimo
          if(tt < RenderAction.w * RenderAction.h) {
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
          float norma = dir.normalize();
          dir = dir.getNormalizedPoint();
          //creazione del raggio d'ombra diretto verso
          //la luce
          double cosTheta=r.d.dotProduct(n1);
          Point3D Ftheta= RenderAction.material[mId].getFresnelCoefficient(cosTheta);

          //peso la direzione in base al fattore di Fresnel
          //ma verifico che la direzione formi un angolo
          //di massimo 90 gradi con la normale
          boolean okdir = false;

          //TODO fix here
          //while(!okdir) {
            dir.x *= Ftheta.x;
            dir.y *= Ftheta.y;
            dir.z *= Ftheta.z;

            if(dir.dotProduct(n1)>0)
              okdir=true;
            //altrimenti si cerca una nuova direzione
          //}//fine while(okdir)

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
            //i valori si sigmas e sigmaa sono specifici per
            //la giada
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
            radianceOutput = radianceOutput
                .add(RenderAction.material[lid].emittedLight
                        .multiplyComponents(B)
                        .multiplyScalar(area)
                        .multiplyScalar(dirN1N2))
                .divideScalar(norma2);
          }
        } else {
          //Caso BRDF
          int tt =x+y* RenderAction.w;
          if(tt < RenderAction.w * RenderAction.h) {
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
          float norma = dir.normalize();
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
                    .multiplyScalar(dirN1N2))
                .divideScalar(norma2);
          }
        }
      }
    }

    //dividiamo per tutti i sample utilizzati nell'
    //estimatore di Monte Carlo
    radianceOutput = radianceOutput.divideScalar(RenderAction.dirSamps * RenderAction.lights.size());
    return radianceOutput;
  }

  //metodo per l'illuminazione indiretta
  //I parametri in input sono:
  //r: raggio di entrata
  //o: oggetto dal quale partira' il nuovo raggio
  //x e y indici del seme iniziale per la generazione
  //di numeri randomici
  Point3D finalIndirect(Ray r, Obj o, int x, int y) {
    //inizializzo a 0 il valore che resitituiro' alla fine
    //del processo
    Point3D radianceOutput = new Point3D();

    //normale dell'oggetto in esame
    Point3D n1 = o.normal(r.o);

    int mId = o.matId;

    //TODO inserire Russian Roulette

    //per ogni s campione della luce tra gli aoSamps
    //campioni totali per l'illuminazione indiretta
    for (int s = 0; s < RenderAction.aoSamps; s++) {
      //quindi distribuito uniformemente sull'emisfero
      Point3D dir;
      float rndX = 0.0f;
      float rndY = 0.0f;

      //utilizzo questa variabile tt perche' non
      //posso usare il valore x+y*w nell'array
      //dirSamples1[], altrimenti l'ultimo indice
      //sarebbe fuori dal range (ricordo che la
      //misura e' w*h ma gli indici vanno da 0 a
      //w*h-1)
      int tt =x+y* RenderAction.w;
      //allora faccio l'if per tt<w*h cosi' da
      //accertarmi che non sia considerato l'indice
      //w*h-esimo
      if(tt< RenderAction.w * RenderAction.h) {
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
      v= v.getNormalizedPoint();
      u = v.crossProduct(w);

      float cosPhi=(float) Math.cos(rndPhi);
      float sinTeta=(float) Math.sin(rndTeta);
      float sinPhi=(float) Math.sin(rndPhi);
      float cosTeta=(float) Math.cos(rndTeta);
      dir = (u.multiplyScalar(cosPhi*sinTeta))
          .add(v.multiplyScalar(sinPhi*sinTeta))
          .add(w.multiplyScalar(cosTeta));
      dir=dir.getNormalizedPoint();

      //creo il raggio dal punto di intersezione ad un
      //punto a caso sull'emisfero
      Ray reflRay=new Ray(r.o, dir);
      // il metodo va' avanti solo se interseca un
      //oggetto e se questo oggetto non e' una luce
      //(poiche' la luminosita' diretta l'abbiamo gia'
      //considerata)

      if (utilities.intersect(reflRay, null)) {
        utilities.inters = Utilities.inf;
        Obj objX = utilities.intersObj;
        utilities.intersObj = null;
        if(RenderAction.material[objX.matId].emittedLight.max() == 0) {
          float _area = 1 / (objX).area();
          radianceOutput
              = radianceOutput.add(((objX).P).multiplyComponents(RenderAction.material[mId].diffusionColor).multiplyScalar(_area));
        }
      }
    }

    radianceOutput = radianceOutput.divideScalar(RenderAction.aoSamps);
    return radianceOutput;
  }

  //metodo per il Final Gathering, che si serve di una sola
  //iterazione del raytracing stocastico, nella quale si
  //raccolgono (gathering) le informazioni ottenute dalla
  //soluzione precalcolata di radiosita', attraverso il
  //metodo jacobi stocastico)
  //il valore restituito tiene conto dell'illuminazione
  //generata dall'oggetto, del contributo dell'illuminazione
  //diretta e di quello dell'illuminazione indiretta
  Point3D finalGathering(Ray viewRay, int x, int y, Obj o) {
    //inizializzo a 0 il valore che resitituiro' alla fine
    //del processo
    Point3D radianceOutput = new Point3D();

    //illuminazione generata
    Point3D le= emittedObjRadiance(o);
    radianceOutput = radianceOutput.add(le);

    //illuminazione diretta
    Point3D di= directIllumination(viewRay, o, x, y);
    radianceOutput = radianceOutput.add(di);

    //illuminazione indiretta
    Point3D fi= finalIndirect(viewRay, o, x, y);
    radianceOutput = radianceOutput.add(fi);


    return radianceOutput;
  }

  //metodo che serve a calcolare la radianza nel punto
  //dell'oggetto che stiamo considerando
  //r e' il raggio che parte dal punto dell'oggetto
  //intersecato (dal raggio della fotocamera considerato)
  //e arriva all'osservatore
  //o e' l'oggetto in cui ci troviamo (quello intersecato
  //dal raggio della fotocamera che stavamo considerando)
  //x indica la colonna in cui ci troviamo, y indica la
  //riga in cui ci troviamo
  Point3D radiance(Ray r, Obj o, int x, int y) {
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
    Point3D n1 = o.normal(r.o);

    //coseno tra il raggio entrante e la normale
    double cos_i = r.d.dotProduct(n1);

    //fattore di Fresnel
    Point3D Fresn = RenderAction.material[mId].getFresnelCoefficient(cos_i);

    //si verifica se il materiale ha una componente speculare
    //e che non sia stato superato il numero massimo di
    //riflessioni
    if((RenderAction.material[mId].reflectionColor.max() > 0) &&
        (RenderAction.nRay < Utilities.MAX_DEPTH +1)) {

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
              = radiance(r2,objX,x,y).multiplyComponents(RenderAction.material[mId].S_BRDF(Fresn));
          //si diminuiscono il numero di riflessioni
          RenderAction.nRay--;
        }
     }//fine if per specchi imperfetti
     else {
       //si aumenta il numero di riflessioni una volta per
       //tutti i campioni
       RenderAction.nRay++;
         //per ogni campione
         for(int s = 0; s< RenderAction.refSamps; s++)
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
             random1= Utilities.generateRandom(RenderAction.refSamples1[x+y* RenderAction.w]);
             random2= Utilities.generateRandom(RenderAction.refSamples2[x+y* RenderAction.w]);

             //Inverse Cumulative Distribution Function
             //del coseno modificata
             //creiamo la base ortonormale per generare
             //la direzione del raggio riflesso reflRay
             float rndPhi=2* Utilities.MATH_PI *(random1);
             float rndTeta=(float) Math.acos((float) Math.pow(random2, RenderAction.material[mId].refImperfection));
             // creazione della base ortonormale rispetto
             //alla direzione di riflessione
             Point3D u,v,w;
             //inizializzo w uguale alla riflessione del
             //raggio in entrata
             w=refl;
             //vettore up (simile a (0,1,0))
             Point3D up=new Point3D(0.0015f,1.0f,0.021f);
             //il prodotto vettoriale tra w e up mi
             //genera il vettore v normale a entambi, che
             //normalizzo
             v=w.crossProduct(up);
             v=v.getNormalizedPoint();
             //il prodotto vettoriale tra v e w mi genera
             //il vettore u normale a entambi dal momento
             //che i vettori v e w sono gia' normali, non
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
             //)+(w*(cosTeta)) poi normalizzato
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
                 =radianceRefl.add(radiance(r2,objX,x,y).multiplyComponents(RenderAction.material[mId].S_BRDF(Fresn)));
             }
         }
         RenderAction.nRay--;
         //divido per il numero di raggi che sono stati creati
         radianceRefl=radianceRefl.divideScalar((float) RenderAction.refSamps);
      }
     }
    }

    //rifrazione
    //si verifica che l'oggetto abbia Kg>0, non sia un
    //metallo e non si siano superato il numero massimo di
    //riflessioni del ray tracer
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
              double t= utilities.inf;
              Obj objX;
              objX=null;

              if(utilities.intersect(refrRay, objX))
              {
                t= utilities.inters;
                utilities.inters = utilities.inf;
                objX= utilities.intersObj;
                utilities.intersObj =null;
                Point3D iP=(refrRay.o).add(
                     refrRay.d.multiplyScalar(t));
                Ray r2=new Ray(iP,dir.multiplyScalar(-1));

                RenderAction.nRay++;
                //calcolo della radianza rifratta
                radianceRefr=radiance(r2,objX,x,y).multiplyComponents(RenderAction.material[mId].T_BRDF(Fresn));
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
                  random1= Utilities.generateRandom(RenderAction.refSamples1[x+y* RenderAction.w]);
                  random2= Utilities.generateRandom(RenderAction.refSamples1[x+y* RenderAction.w]);

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
              radianceRefr = radianceRefr.divideScalar(RenderAction.refSamps);
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
            double t;
            Obj objX = null;

            if(utilities.intersect(refrRay[i], objX)){
              t= utilities.inters;
              utilities.inters = Utilities.inf;
              objX= utilities.intersObj;
              utilities.intersObj = null;
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
      if(RenderAction.doPhotonFinalGathering) {
        return photonRadiance(r, o, RenderAction.KdTree, RenderAction.photonSearchDisc, RenderAction.nPhotonSearch)
            .add(radianceRefr).add(radianceRefl)
            .add(photonRadiance(r, o, RenderAction.causticTree, RenderAction.causticSearchDisc, RenderAction.nCausticSearch))
            .add(emittedObjRadiance(o));
      }

      //photon mapping con radianza stocastica:
      if(RenderAction.doMultiPassPhotonMapping) {
        return multiPassPhotonRadiance(r, x, y, o)
            .add(radianceRefr)
            .add(radianceRefl);
      }

      //metodo di Jacobi stocastico e final gathering:
      if (RenderAction.doJacobi && RenderAction.doFinalGathering) {
        Point3D f= finalGathering(r, x, y, o);
        return f.add(radianceRefr).add(radianceRefl);
      }

      //metodo di Jacobi stocastico:
      if(RenderAction.doJacobi){
        float areaInverse= (1.0f)/((o).areaObj);
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

  //metodo che restituisce la radianza emessa dall'oggetto
  //o in direzione r:
  Point3D emittedObjRadiance(Obj o) {
    //carico l'indice del material
    int mId = o.matId;

    //dichiato e inizializzo a 0 il valore in uscita
    Point3D radianceOutput = new Point3D();

    //con il seguente if si controlla se il materiale
    //emette effettivamente luce
    if (RenderAction.material[mId].emittedLight.max() > 0) {
      double Ler = RenderAction.material[mId].emittedLight.max();
      Point3D Ler3=new Point3D(Ler);
      //con il metodo clamp3 si evita che la radianza in
      //uscita superi il valore massimo di radianza: 1
      Point3D.clamp3(Ler3);
      radianceOutput = Ler3;
    }
    return radianceOutput;
  }

  void calculateThreadedRadiance(Camera cam) {
    new Runner(cam);


  }

  //funzione per il calcolo della radiosita' della scena:
  //in questa funzione si utilizza il metodo di Jacobi
  //stocastico per calcolare il valore di potenza di ogni
  //patch della scena
  //si usa la variabile globale GlobalObject in modo da
  //conservare i valori aggiornati delle potenze dei vari
  //oggetti
  void jacobiStoc(int nObj) {
    //definisco la potenza residua totale delle patch:
    Point3D Prtot=new Point3D();
    //definisco la potenza di ogni patch della scena:
    Point3D[] P=new Point3D[nObj];

    //definisco la potenza residua di ogni patch della
    //scena:
    Point3D[] Pr=new Point3D[nObj];

    //inizializzo a (0,0,0) power e Pr
    for(int i=0; i<nObj;i++) {
          P[i]=new Point3D();
          Pr[i]=new Point3D();
    }

    //inizializzo la stima dell'errore raggiunto dal
    //processo
    //inizializzo l'array objX in cui inizialmente gli
    //oggetti sono nulli, ma poi vengono settati con
    //l'oggetto intersecato dal raggio che parte da una
    //patch i-esima presa in consideraazione:l'oggetto
    //intersecato e' proprio la patch j su cui sara'
    //rilasciata la potenza totale delle 3 componenti rgb
    Obj[] objX=new Obj[nObj];

    //Vengono caricati i valori iniziali di Luminosita'
    //Emessa per ogni patch della scena (Pe)
    for(int i=0;i<nObj;i++) {
      //viene caricata l'area dell'oggetto i-esimo
      float area= RenderAction.globalObjects.get(i).areaObj;
      //se l'area e' piu' piccola della precisione di
      //calcolo allora impostiamo l'area a 0
      if(area < Utilities.EPSILON) {
        area=0;
      }

      //potenza della luce
      //Potenza emessa dalla patch i:
      //(potenza emessa)*(pi greco)*(area)
      Point3D LP= RenderAction.material[RenderAction.globalObjects.get(i).matId].emittedLight.
                      multiplyScalar(Utilities.MATH_PI).
                      multiplyScalar(area);

      //viene calcolata la potenza totale iniziale:
      //(potenza residua della patch)+
      //+(potenza emessa dalla patch)
      Prtot=Prtot.add(LP);

      //viene salvata nell'array power la potenza dell'
      //elemento i
      P[i].copy(LP);

      //viene salvata nell'array Pr la potenza residua
      //dell'elemento i
      Pr[i].copy(LP);

      //viene calcolato l'errore di approssimazione
      //(con cui e' possibile fermare il metodo)
      RenderAction.err += Math.pow(LP.average(),2);
    }//fine for

    //dopo aver aggiunto dei quadrati dobbiamo fare la
    //radice del risultato finale

    //iterazioni del metodo di Jacobi:
    //Si continuano le iterazioni finche' l'energia
    //rilasciata non diventa minore di un certo valore
    ///(verificato dalla variabile err)
    //o gli steps superano gli steps massimi (verificato
    //dalla variabile maxSteps).
    //Devono essere vere entrambe

    RenderAction.steps = 0;

    while ((RenderAction.err > RenderAction.maxErr) && (RenderAction.steps < RenderAction.maxSteps)) {
      //percentuale di completamento
      System.out.println("completamento jacobi "+(RenderAction.steps/(float) RenderAction.maxSteps)*100);
      //viene inizializzato un seme iniziale casuale
      //da 0 a 30000
      int s =(int) Math.floor(Math.random()*(30001));
      //inizializzo per le tre componenti una variabile
      //per il numero di sample utilizzati per la patch
      //(N*) e una per il numero di sample utilizzati
      //finora (Nprev*)
      int NprevX=0;
      int NX=0;
      int NprevY=0;
      int NY=0;
      int NprevZ=0;
      int NZ=0;

      //potenza residua totale nelle tre componenti
      double Prt= Prtot.x+Prtot.y+Prtot.z;
      //campioni distribuiti in base alla potenza
      //totale di ciascuna componente RGB che chiamiamo
      //qui (x,y,z)

      Point3D samps=new Point3D(RenderAction.jacobiSamps*Prtot.x,
              RenderAction.jacobiSamps*Prtot.y, RenderAction.jacobiSamps*Prtot.z);
      samps=samps.divideScalar(Prt);
      //parametro che ci permette di contare, quindi
      //utilizzare tutti i campioni che erano stati
      //previsti

      Point3D q=new Point3D();
      //per ogni patch della scena

      for(int i=0;i<nObj;i++) {
        //salviamo nella variabile locale o l'oggetto
        //i-esimo dell'array globalObjects
        Obj o= RenderAction.globalObjects.get(i);

        //probabilita' con cui viene scelta la patch i
        Point3D pi=(Pr[i].divideComponents(Prtot));

        q=q.add(pi);

        //componente rossa:

        //calcoliamo il numero di campioni utilizzati
        //per la patch i in base alla sua potenza
        //e anche in base a quanti campioni sono gia'
        //stati usati
        NX= (int) Math.round((q.x*samps.x)+NprevX*(-1));

        //per ogni campione dell'elemento i
        for (int j=0; j<NX; j++) {
          //creo una direzione randomica uniformemente
          //distribuita sull'emisfero frontale alla
          //patch
          //definisco la variabile che rappresentera'
          //la direzione
          Point3D dir;
          //creo 3 variabili randomiche che si basano
          //sul seme iniziale casuale s definito
          //randomicamente
          float rndX = Utilities.generateRandom(s);
          float rndY = Utilities.generateRandom(s);
          float rndZ = Utilities.generateRandom(s);

          //Inverse Cumulative Distribuction Function
          //creiamo la base ortonormale per generare
          //la direzione del raggio ffRay
          float rndPhi=2* Utilities.MATH_PI *(rndX);
          float rndTeta=(float) Math.acos(Math.sqrt(
                  rndY));

          //dichiaro e inizializzo un punto scelto
          //uniformemente sulla patch i
          Point3D rndPoint=null;
          rndPoint= o.randomPoint(rndX, rndY,rndZ);

          //Si crea ora la base ortonormale
          Point3D u;
          Point3D v;
          Point3D w=null;
          //settiamo w come la normale all'oggetto nel
          //punto rndPoint
          w=o.normal(rndPoint);

          //vettore up (simile a (0,1,0))
          Point3D up=new Point3D(0.0015f,1.0f,0.021f);
          //il prodotto vettoriale tra w e up mi genera
          //il vettore v normale a entambi, che
          //normalizzo
          v=w.crossProduct(up);
          v=v.getNormalizedPoint();
          //il prodotto vettoriale tra v e w mi genera
          //il vettore u normale a entambi
          //dal momento che i vettori v e w sono gia'
          //normali, non c'e' bisogno di noemalizzare
          //il vettore u)
          u=v.crossProduct(w);

          //ora che abbiamo la base ortonormale,
          //possiamo calcolare la direzione dir
          //salvo in delle cariabili i calori di seno
          //e coseno necessari per il calcolo di dir
          float cosRndPhi=(float) Math.cos(rndPhi);
          float sinRndTeta=(float) Math.sin(rndTeta);
          float sinRndPhi=(float) Math.sin(rndPhi);
          float cosRndTeta=(float) Math.cos(rndTeta);
          //dir=(u*(cosRndPhi*sinRndTeta))+(v*
          //*(sinRndPhi*sinRndTeta))+(w*(cosRndTeta))
          //poi normalizzato
          dir=u.multiplyScalar(cosRndPhi*sinRndTeta).
                  add(v.multiplyScalar(
                          sinRndPhi*sinRndTeta)).add(
                  w.multiplyScalar(cosRndTeta));
          dir=dir.getNormalizedPoint();

          //creo il raggio per scegliere la patch j
          //con probabilita' uguale al fattore di forma
          //tra la patch i e quella j
          Ray ffRay=new Ray(rndPoint,dir);

          //inizializzo a null l'oggetto intersecato
          objX[i]=null;

          //l'oggetto intersecato e' proprio la patch j
          //su cui sara' rilasciata la potenza totale
          //della componente rossa

          if(utilities.intersect(ffRay, objX[i])){
            //resetto inters uguale a inf in modo da
            //avere il giusto valore di partenza la
            //prossima volta che si utilizzera' il
            //metodo intersect()
            utilities.inters = Utilities.inf;
            //salvo nell'elemento i-esimo dell'array
            //objX l'elemento intersecato dal raggio
            //ffRay
            objX[i]= utilities.intersObj;
            //resetto intersObj=null in modo da avere
            //il giusto valore di partenza la prossima
            //volta che si utilizzera' il
            //metodo intersect()
            utilities.intersObj =null;
            //salviamo la potenza rilasciata all'interno
            //della struttura dell'oggetto: essa si
            //sommera' con la potenza residua parziale
            //che l'oggetto ha raggiunto finora; solo
            //alla fine del processo infatti avremo
            //la potenza residua totale della patch
            objX[i].P.x = objX[i].P.x
                    + RenderAction.material[objX[i].matId].diffusionColor.x*(Prtot.x)/(samps.x);
          }
        }//fine for per la compoente rossa
        //aggiorniamo il numero di campioni usati per
        //questa componente
        NprevX=NprevX+NX;

        //per le componenti verde e blu si utilizzeranno
        //gli stessi procedimenti della componente rossa,
        //quindi fare riferimento ai commenti sovrastanti
        //per maggiori dettagli

        //componente verde:

        //campioni per la patch i
        NY= (int) Math.round((q.y*samps.y)+NprevY*(-1));

        //per ogni campione sull'elemento i
        for (int j=0; j<NY; j++) {

          Point3D dir;

          float rndX = Utilities.generateRandom(s);
          float rndY = Utilities.generateRandom(s);
          float rndZ = Utilities.generateRandom(s);

          float rndPhi=2* Utilities.MATH_PI *(rndX);
          float rndTeta=(float) Math.acos(Math.sqrt(
                  rndY));

          Point3D rndPoint=null;
          //punto scelto uniformemente nella patch i:
          rndPoint= o.randomPoint(rndX, rndY,rndZ);

          //base ortonormale
          Point3D u,v;
          Point3D w=null;

          w=o.normal(rndPoint);

          //vettore up (simile a (0,1,0))
          Point3D up=new Point3D(0.0015f,1.0f,0.021f);
          v=w.crossProduct(up);
          v=v.getNormalizedPoint();
          u=v.crossProduct(w);

          float cosRndPhi=(float) Math.cos(rndPhi);
          float sinRndTeta=(float) Math.sin(rndTeta);
          float sinRndPhi=(float) Math.sin(rndPhi);
          float cosRndTeta=(float) Math.cos(rndTeta);
          dir=u.multiplyScalar(cosRndPhi*sinRndTeta).
                  add(v.multiplyScalar(
                          sinRndPhi*sinRndTeta)).add(
                  w.multiplyScalar(cosRndTeta));
          dir=dir.getNormalizedPoint();

          //creo il raggio per scegliere la patch j
          //con probabilita' uguale al fattore di forma
          //tra la patch i e quella j
          Ray ffRay=new Ray(rndPoint,dir);
          objX[i]=null;

          if(utilities.intersect(ffRay, objX[i])) {
            utilities.inters = utilities.inf;
            objX[i]= utilities.intersObj;
            utilities.intersObj =null;
            objX[i].P.y=objX[i].P.y
                    + RenderAction.material[objX[i].matId].diffusionColor.y*(Prtot.y)/(samps.y);
          }
        }//fine for per la compoente verde
        NprevY=NprevY+NY;

        //componente blu:

        //campioni per la patch i
        NZ= (int) Math.round((q.z*samps.z)+NprevZ*(-1));

        //per ogni campione sull'elemento i
        for (int j=0; j<NZ; j++){
          Point3D dir;
          float rndX=0.0f;
          float rndY=0.0f;
          float rndZ=0;

          rndX= Utilities.generateRandom(s);
          rndY= Utilities.generateRandom(s);
          rndZ= Utilities.generateRandom(s);

          //direzione casuale
          float rndPhi=2* Utilities.MATH_PI *(rndX);
          float rndTeta=(float) Math.acos(Math.sqrt(rndY));

          //punto scelto uniformemente nella patch i
          Point3D rndPoint=null;
          rndPoint= o.randomPoint(rndX, rndY,rndZ);

          //base ortonormale
          Point3D u,v;
          Point3D w=null;

          w=o.normal(rndPoint);

          //vettore up (simile a (0,1,0))
          Point3D up=new Point3D(0.0015f,1.0f,0.021f);
          v=w.crossProduct(up);
          v=v.getNormalizedPoint();
          u=v.crossProduct(w);

          float cosRndPhi=(float) Math.cos(rndPhi);
          float sinRndTeta=(float) Math.sin(rndTeta);
          float sinRndPhi=(float) Math.sin(rndPhi);
          float cosRndTeta=(float) Math.cos(rndTeta);
          dir = u.multiplyScalar(cosRndPhi*sinRndTeta)
                  .add(v.multiplyScalar(sinRndPhi*sinRndTeta))
                  .add(w.multiplyScalar(cosRndTeta));

          dir=dir.getNormalizedPoint();

          //creo il raggio d'ombra dal punto di
          //intersezione ad un punto a caso sull'
          //emisfero
          Ray ffRay=new Ray(rndPoint,dir);
          objX[i]=null;

          if(utilities.intersect(ffRay,objX[i])) {
            utilities.inters = Utilities.inf;
            objX[i]= utilities.intersObj;
            utilities.intersObj =null;
            objX[i].P.z = objX[i].P.z
                    + RenderAction.material[objX[i].matId].diffusionColor.z*(Prtot.z)/(samps.z);
          }
        }//fine for per la componente blu

        NprevZ=NprevZ+NZ;
      }//fine for per le patch della scena SAS1

      //una volta terminata la fase di shooting si
      //riaggiornano le componenti di Potenza residua,
      //Pr, Potenza power, e si azzerano le potenze
      //residue parziali salvate negli oggetti
      //azzeramento della potenza totale:

      Prtot=new Point3D(0);

      for(int i=0;i<nObj;i++) {
        //aggiornamento delle Potenze (vengono aggiunte le potenze residue totali immagazzinate dalle patch durante
        //il processo)
        P[i]=P[i].add(RenderAction.globalObjects.get(i).P);
        Pr[i].copy(RenderAction.globalObjects.get(i).P);  //aggiornamento delle potenze residue totali
        RenderAction.err += Math.pow(Pr[i].average(),2);  //calcolo dell'errore
        Prtot=Prtot.add(Pr[i]); //calcolo dell'energia residua totale
        RenderAction.globalObjects.get(i).P.copy(new Point3D());  //azzeramento della potenza residua parziale contenuta nella patch
      }

      RenderAction.steps++;
    }

    //I valori ottenuti vengono salvati su ciascuna
    //patch nella variabile power (in cui durante il processo
    //veniva salvata la potenza residua parziale)
    for(int i=0; i<nObj; i++) {
      (RenderAction.globalObjects.get(i).P).copy(P[i]);
    }
   }

  Point3D photonRadiance(Ray r, Obj objX, PhotonBox[] Tree, double photond_2, int nph){
    Point3D radianceOutput = new Point3D();

    //carico l'ID del materiale
    int matId= objX.matId;

    //carico la normale dell'oggetto
    Point3D n1= objX.normal(r.o);

    //fotoni trovati nelle vicinanze del punto in esame

    Hashtable<Double,Photon> nearPh = new Hashtable<>();

    utilities.locate_photons(nearPh,r.o,1,objX,Tree, photond_2,nph);

    //per ogni fotone trovato
    for (Map.Entry<Double, Photon> entry : nearPh.entrySet()) {
      //raggio di entrata del fotone
      Ray psi= new Ray(r.o, entry.getValue().direction);

      //calcolo della BRDF
      Point3D BRDF= RenderAction.material[matId].C_T_BRDF(psi,r,n1);

      //distanza del fotone dal punto
      double dist= entry.getKey();

      double W=1-(Math.sqrt(dist)/((1.1)*Math.sqrt(photond_2)));
      //stima della radianza nel punto
      radianceOutput = radianceOutput.add(BRDF.multiplyComponents(entry.getValue().power).multiplyScalar(Utilities.MATH_PI).multiplyScalar(1/ photond_2).multiplyScalar(W));
    }

    return radianceOutput;
  }

  Point3D multiPassPhotonRadiance(Ray r, int x, int y, Obj o){

    Point3D radianceOutput = new Point3D();

    //illuminazione generata
    //radianceOutput=radianceOutput+Le(r,o);

    //illuminazione diretta
    radianceOutput = radianceOutput.add(directIllumination(r,o,x,y));

    //illuminazione indiretta
    radianceOutput = radianceOutput.add(photonIndirect(r,o,x,y));

    //illuminazione caustiche
    radianceOutput=radianceOutput.add(photonRadiance(r,o, RenderAction.causticTree, RenderAction.causticSearchDisc, RenderAction.nCausticSearch));

    return radianceOutput;
  }

  Point3D photonIndirect(Ray r, Obj o, int x, int y){
    //valore che resitituirò alla fine del processo
    Point3D radianceOutput = new Point3D();

    //normale dell'oggetto in esame
    Point3D n1 = o.normal(r.o);

    int mId = o.matId;

    for(int s = 0; s < RenderAction.aoSamps; s++){

      //quindi distribuito uniformemente sull'emisfero
      Point3D dir;
      double rndX=0.0f;
      double rndY=0.0f;

      rndX = Utilities.generateRandom(s+1);
      rndY = Utilities.generateRandom(s+1);

      //distribuisco i numeri random sull'emisfero
      double rndPhi = 2*Utilities.MATH_PI*(rndX);
      double rndTeta = Math.acos(Math.sqrt(rndY));

      // Create onb (ortho normal basis) on iP punto di intersezione
      Point3D u,v,w;
      w = n1;
      //vettore up (simile a (0,1,0))
      Point3D up = new Point3D(0.0015f,1.0f,0.021f);
      v = w.crossProduct(up);
      v = v.getNormalizedPoint();
      u = v.crossProduct(w);

      dir = u.multiplyScalar(Math.cos(rndPhi)*Math.sin(rndTeta)).add(v.multiplyScalar(Math.sin(rndPhi)*Math.sin(rndTeta))).add(w.multiplyScalar(Math.cos(rndTeta)));
      dir = dir.getNormalizedPoint();

      //creo il raggio dal punto di intersezione ad un punto a caso sull'emisfero
      Ray reflRay = new Ray(r.o,dir);
      double t= Utilities.inf;
      Obj objX = null;

      // il metodo và avanti solo se interseca un oggetto e se questo oggetto non è una luce ( poichè la luminosità diretta l'abbiamo già considerata)
      if((utilities.intersect(reflRay, objX))
          &&(RenderAction.material[objX.matId].emittedLight.max()==0)
          &&(RenderAction.material[objX.matId].diffusionColor.max()>0)) {
        t = utilities.inters;
        objX = utilities.intersObj;

        utilities.inters = Utilities.inf;
        utilities.intersObj = null;

        Point3D iP = reflRay.o.add(reflRay.d.multiplyScalar(t));
        Ray r2 = new Ray(iP,reflRay.d.multiplyScalar(-1));
        Point3D BRDF= RenderAction.material[mId].C_T_BRDF(reflRay,r,n1);
        radianceOutput=radianceOutput.add(photonRadiance(r2, objX, RenderAction.KdTree, RenderAction.photonSearchDisc, RenderAction.nPhotonSearch).multiplyComponents(BRDF).multiplyScalar(Utilities.MATH_PI));

      }
    }

    radianceOutput=radianceOutput.divideScalar(RenderAction.aoSamps);
    return radianceOutput;
  }

  void calculatePhotonMapping() {
    int liv = 0;

    //calcola numero di photonbox
    for(int i=1; i < RenderAction.Kdepth; i++){
      RenderAction.power += Math.pow(2, i);
    }

    RenderAction.KdTree = new PhotonBox[RenderAction.power +1];
    RenderAction.causticTree = new PhotonBox[RenderAction.power +1];

    //TODO multithread optimisation required
    //vengono emessi i fotoni dalle luci e fatti rimbalzare all'interno della scena
    emitPhotons();

    if(RenderAction.causticPhoton > 0 && Utilities.checkRefractionObjects()) {
      caustic();
    }

    RenderAction.KdTree[0] = new PhotonBox(RenderAction.min, RenderAction.max, RenderAction.photons);
    RenderAction.causticTree[0] = new PhotonBox(RenderAction.min, RenderAction.max, RenderAction.caustics);

    balance(RenderAction.KdTree,1,liv);
    balance(RenderAction.causticTree,1,liv);
  }

  //funzione che effette fotoni in direzioni casuali, campionando uniformemente un emisfero.
  //i suoi argomenti sono il vettore dei fotoni
  void emitPhotons() {
    float random1;
    float random2;
    float random3;
    Obj objX = null;

    for (int i = 0; i < RenderAction.lights.size(); i++) {
      //carichiamo l'area della luce
      float area = RenderAction.lights.get(i).areaObj;
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
        float rndPhi = 2*Utilities.MATH_PI*(random1);
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

  void photonScatter(Obj objX, int n_, Photon p) {
    //carichiamo il numero massimo di rimbalzi previsti per un fotone
    float MAX = Utilities.MAX_DEPTH_PHOTON;
    //ci ricaviamo quindi il peso da utilizzare nella roulette russa
    double peso=(MAX-n_)/MAX;

    //carichiamo l'ID del materiale dell'oggetto colpito
    int mId = objX.matId;

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
    Point3D n = objX.normal(p.position);
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
      rndls= Math.random();
      rndlt= Math.random();

      //distribuiamo i numeri sull'emisfero
      double rndPhi = 2*Utilities.MATH_PI*(rndls);
      double rndTeta = Math.acos(Math.sqrt(rndlt));

      // Create onb (ortho normal basis) on iP punto di intersezione
      Point3D u,v,w;
      w = objX.normal(p.position);
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
      // Ray refraction based on normal
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

  void caustic() {
    float rnd1;
    float rnd2;
    float rnd3;
    Obj objX;

    int nP=0;
    while(nP < RenderAction.causticPhoton) {    //per ogni campione
      System.out.println("Calcolo campioni caustiche: " + nP);

      rnd1= Utilities.generateRandom(RenderAction.loadedBoxes)*(RenderAction.lights.size());

      //TODO Fix OutOfBounds
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
        float dTheta= Utilities.MATH_PI/(2*RenderAction.projectionResolution);
        float dPhi=(2*Utilities.MATH_PI)/ RenderAction.projectionResolution;

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
          float scale= totSolidAngle/2*Utilities.MATH_PI;

          //potenza scalata
          Point3D P2 = P.multiplyScalar(scale);

          nP++;

          int nS=0;

          while(nS < RenderAction.aoCausticPhoton) {
            //campiono uniformemente la mappa di proiezione
            //TODO fix >=1.0 random value
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
                //aumento il numero di fotoni sparati per l'emisfero
                //nS++;

                //creiamo il nuovo fotone e ne calcoliamo i rimbalzi nella scena
                Point3D iP=pRay.o.add(pRay.d.multiplyScalar(t));
                Photon p2 = new Photon(iP,dir.multiplyScalar(-1),P2);

                causticScatter(objX, 0, p2);
              }
            }

            nS++;
          }
        }
      }
    }
  }

  void causticScatter(Obj objX, int n_, Photon p){
    int mId = objX.matId;

    if(RenderAction.material[mId].diffusionColor.max()>0){
      RenderAction.caustics.add(p);
    }

    n_++;
    if(n_< Utilities.MAX_DEPTH_CAUSTIC){
      Obj objY = null;

      if((RenderAction.material[mId].refractionColor.max()>0)&&(RenderAction.material[mId].absorptionCoefficient.max()==0)) {
        Point3D n = objX.normal(p.position);

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
          // Ray refraction based on normal
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

  void balance(PhotonBox[] Tree, int index, int liv) {
    liv++;

    if(liv < RenderAction.Kdepth) {
      int dim = Tree[index-1].dim;
      double median= Tree[index-1].planePos;
      double n = Tree[index-1].nph;
      ArrayList<Photon> ph= Tree[index-1].ph;

      Point3D min= Tree[index-1].V[0];
      Point3D max= Tree[index-1].V[1];

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

          Tree[(2*index)-1] = new PhotonBox(min, new Point3D(median,max.y,max.z),ph1);
          Tree[(2*index)] = new PhotonBox(new Point3D(median,min.y,min.z), max, ph2);

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

          Tree[(2*index)-1] = new PhotonBox(min, new Point3D(max.x,median,max.z), ph1);
          Tree[2*index] = new PhotonBox(new Point3D(min.x,median,min.z), max, ph2);

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

          Tree[(2*index)-1] = new PhotonBox(min, new Point3D(max.x,max.y,median), ph1);
          Tree[2*index] = new PhotonBox(new Point3D(min.x,min.y,median), max, ph2);
      }

      RenderAction.photonBoxNum += 2;

      balance(Tree,2*index,liv);
      balance(Tree,(2*index)+1,liv);
    }
  }
}
