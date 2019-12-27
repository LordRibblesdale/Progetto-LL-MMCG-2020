import java.util.ArrayList;

public class Renderer {
  Renderer() {}
  //Metodo per il calcolo dell'illuminazione diretta, cioe'
  //il contributo che arriva all'oggetto direttamente dalla
  //fonte di luce
  //I parametri in input sono:
  //r: raggio di entrata
  //o: oggetto dal quale partira' il nuovo raggio
  //x e y indici del seme iniziale per la generazione
  //di numeri randomici
   static Point3D directIllumination(Ray r, Obj o, int x, int y) {
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
      objX=null;

    //per ogni luce
    for (int i = 0; i < Main.nLight; i++) {

      //carico l'area della luce in esame
      float area = Main.lights[i].areaObj;

      //per ogni s campione della luce tra i dirsamps
      //campioni totali per l'illuminazione diretta
      for (int s = 0; s < Main.dirsamps; s++) {

        //controllo per decidere se calcolare la
        //BRDF o la BSSRDF, quindi la calcoliamo
          //nel punto osservato
        //inizializziamo B che e' la variabile in
        //cui salviamo il valore di BRDF o la
        //BSSRDF
        Point3D B;
        //Caso BSSRDF
        if(Main.material[mId].translucent) {
          //zv e zr sono scelti arbitrariamente in
          //modo tale da essere sufficientemente
          //vicini alla superficie
          float zv=0.005f;
          float dv;
          float zr=0.0025f;
          float dr;
          dv=(float) (Math.random() * (150-90)+90);
            //lo divido per non avere un numero troppo
            //grande
          dv=dv/10000f;
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
          int tt =x+y* Main.w;
          //allora faccio l'if per tt<w*h cosi' da
          //accertarmi che non sia considerato l'indice
          //w*h-esimo
          if(tt< Main.w* Main.h) {
            rnd1 = Utilities.generateRandom(Main.dirSamples1[tt]);
            rnd2 = Utilities.generateRandom(Main.dirSamples2[tt]);
            rnd3 = Utilities.generateRandom(Main.dirSamples3[tt]);
          }

          //genero due angoli casuali
          float rndPhi = 2 * Utilities.MATH_PI *(rnd1);
          float rndTeta = (float)Math.acos((float)Math.
              sqrt(rnd2));
          float cosP=(float)Math.cos(rndPhi);
          float cosT=(float)Math.cos(rndTeta);
          float sinP=(float)Math.sin(rndPhi);
          float sinT=(float)Math.sin(rndTeta);
          float px=r.o.x+l*cosP*cosT;
          float py=r.o.y+l*cosP*sinT;
          float pz=r.o.z+l*sinT;
          Point3D newPoint=new Point3D(px,py,pz);


          //si carica il punto campionato sulla luce
          Point3D p = Main.lights[i].randomPoint(rnd1,rnd2,rnd3);

          //la direzione e' quella che congiunge il punto
          //r.o al punto campionato
          Point3D dir = (p.subtract(newPoint));
          //salviamo la distanza tra i due punti
          float norma = dir.normalize();
          dir = dir.getNormalizedPoint();
          //creazione del raggio d'ombra diretto verso
          //la luce
          float cosTheta=r.d.dotProduct(n1);
          Point3D Ftheta= Main.material[mId].getFresnelCoefficient(
              cosTheta);

          //peso la direzione in base al fattore di Fresnel
          //ma verifico che la direzione formi un angolo
                //di massimo 90 gradi con la normale
          boolean okdir=true;
          while(okdir) {
               dir.x=dir.x*Ftheta.x;
               dir.y=dir.y*Ftheta.y;
               dir.z=dir.z*Ftheta.z;

               if(dir.dotProduct(n1)>0)
                    okdir=false;
               //altrimenti si cerca una nuova direzione
          }//fine while(okdir)

          Ray directRay= new Ray(newPoint, dir);

          //viene inizializzato l'oggetto che il raggio
          //intersechera' con l'oggetto che il raggio
          //punta
          objX = Main.lights[i];
          //si inizializza la massima distanza a cui il
          //raggio puo' arrivare
          float t = Main.inf;

          //verifica del fattore di visibilita'
          if (Utilities.intersect(directRay, objX)) {
            Main.inters= Main.inf;
            objX= Main.intersObj;
            Main.intersObj=null;
            //vengono caricati i dati della luce:
            //normale nel punto p
            Point3D n2 = Main.lights[i].normal(p);
            //identificativo del materiale della luce
            int lid = Main.lights[i].matId;
            //calcoliamo la BSSRDF
            float cosPsi=directRay.d.dotProduct(n1);
            Point3D Fpsi= Main.material[mId].getFresnelCoefficient(
                cosPsi);
            //float cosTheta=r.d.dot(n1);
            //float3 Ftheta=material[mId].getFresn(
            //		cosTheta);
            Point3D one=new Point3D(1.0f);


            //pi4=4*3.14
            Point3D pi4=new Point3D(4*Utilities.MATH_PI);
            //i valori si sigmas e sigmaa sono specifici per
            //la giada
            Point3D sigmas=new Point3D(0.657f,0.786f,0.9f);
            Point3D sigmaa=new Point3D(0.2679f,0.3244f,0.1744f);

            Point3D sigmat=sigmaa.add(sigmas);
            Point3D sigmatr=(sigmaa.multiplyComponents(sigmat)).
                      multiplyScalar(3.0f);
            sigmatr= Point3D.getSquareCompPoint(sigmatr);
            Point3D alpha=sigmas.divideComponents(sigmat);

            Point3D expdr=sigmatr.multiplyScalar(dr*-1.0f);
            float dr3=dr*dr*dr;
            Point3D edivdr=(Point3D.exponent(expdr)).
                  divideScalar(dr3);

            Point3D expdv=sigmatr.multiplyScalar(dv*-1.0f);
            float dv3=dv*dv*dv;
            Point3D edivdv=(Point3D.exponent(expdv)).
                  divideScalar(dv3);
            Point3D rPart=(((sigmatr.multiplyScalar(dr)).
                      add(one)).multiplyComponents(edivdr)).
                      multiplyScalar(zr);
            Point3D vPart=(((sigmatr.multiplyScalar(dv)).
                      add(one)).multiplyComponents(edivdv)).
                      multiplyScalar(zv);

            Point3D Rd=(alpha.divideComponents(pi4)).multiplyComponents(rPart.
                      add(vPart));

            B=(Rd.multiplyComponents(Fpsi).multiplyComponents(Ftheta)).
                      divideScalar(Utilities.MATH_PI);


            //enfatizzo il colore verde
            B.x=B.x*0.31f;
            B.y=B.y*0.65f;
            B.z=B.z*0.246f;

            //calcolo dell'illuminazione diretta
            //vengono definiti i seguenti float3 per
            //leggibilita' del risultato
            float dirN1N2=(-dir.dotProduct(n1))*(dir.dotProduct(n2));
            float norma2=(float) Math.pow(norma, 2);
            radianceOutput = radianceOutput.
                    add(Main.material[lid].emittedLight.
                            multiplyComponents(B).multiplyScalar(area).
                            multiplyScalar(dirN1N2)).
                    divideScalar(norma2);

          }
        } else { //Caso BRDF
          int tt =x+y* Main.w;
          if(tt< Main.w* Main.h) {
            rnd1 = Utilities.generateRandom(Main.dirSamples1[tt]);
            rnd2 = Utilities.generateRandom(Main.dirSamples2[tt]);
            rnd3 = Utilities.generateRandom(Main.dirSamples3[tt]);
          }

          //si carica il punto campionato sulla luce
          Point3D p = Main.lights[i].randomPoint(rnd1,rnd2,rnd3);

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
          objX = Main.lights[i];
          //si inizializza la massima distanza a cui il
          //raggio puo' arrivare
          float t = Main.inf;

          //verifica del fattore di visibilita'
          if (Utilities.intersect(directRay, objX)) {
             Main.inters= Main.inf;
             objX= Main.intersObj;
             Main.intersObj=null;
            //vengono caricati i dati della luce:
            //normale nel punto p
            Point3D n2 = Main.lights[i].normal(p);
            //identificativo del materiale della luce
            int lid = Main.lights[i].matId;
            //calcoliamo la BRDF
            B = Main.material[mId].C_T_BRDF(
                directRay, r, n1);

            //calcolo dell'illuminazione diretta
            //vengono definiti i seguenti float3 per
            //leggibilita' del risultato
            float dirN1N2=(-dir.dotProduct(n1))*(dir.dotProduct(n2));
            float norma2=(float) Math.pow(norma, 2);
            //radianceOutput = radianceOutput +
            //+ material[lid].Le.mult(BRDF)*(area)*
            //*(-dir.dot(n1)*dir.dot(n2)) / (pow(
            //(norma, 2));
            radianceOutput = radianceOutput.
                    add(Main.material[lid].emittedLight.
                            multiplyComponents(B).multiplyScalar(area).
                            multiplyScalar(dirN1N2)).
                    divideScalar(norma2);

          }

        }

        }

    }

    //dividiamo per tutti i sample utilizzati nell'
    //estimatore di Monte Carlo
    radianceOutput = radianceOutput.divideScalar(Main.dirsamps* Main.nLight);
    return radianceOutput;
  }

  //metodo per l'illuminazione indiretta
  //I parametri in input sono:
  //r: raggio di entrata
  //o: oggetto dal quale partira' il nuovo raggio
  //x e y indici del seme iniziale per la generazione
  //di numeri randomici
  static Point3D finalIndirect(Ray r, Obj o, int x, int y) {
    //inizializzo a 0 il valore che resitituiro' alla fine
    //del processo
    Point3D radianceOutput = new Point3D();

    //normale dell'oggetto in esame
    Point3D n1 = o.normal(r.o);

    int mId = o.matId;

    //per ogni s campione della luce tra gli aosamps
    //campioni totali per l'illuminazione indiretta
    for (int s = 0; s < Main.aosamps; s++) {

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
      int tt =x+y* Main.w;
      //allora faccio l'if per tt<w*h cosi' da
      //accertarmi che non sia considerato l'indice
      //w*h-esimo
      if(tt< Main.w* Main.h)
      {
        rndX = Utilities.generateRandom(Main.aoSamplesX[tt]);
        rndY = Utilities.generateRandom(Main.aoSamplesY[tt]);
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
      dir = (u.multiplyScalar(cosPhi*sinTeta)).
              add(v.multiplyScalar(sinPhi*
          sinTeta)).add(w.multiplyScalar(
          cosTeta));
      dir=dir.getNormalizedPoint();

      //creo il raggio dal punto di intersezione ad un
      //punto a caso sull'emisfero
      Ray reflRay=new Ray(r.o, dir);
      float t = Main.inf;
      Obj objX;
  objX=null;
      // il metodo va' avanti solo se interseca un
  //oggetto e se questo oggetto non e' una luce
  //(poiche' la luminosita' diretta l'abbiamo gia'
  //considerata)
      if (Utilities.intersect(reflRay, objX)) {

        t= Main.inters;
            Main.inters= Main.inf;
            objX= Main.intersObj;
            Main.intersObj=null;
            if(Main.material[(objX).matId].emittedLight.max() == 0)
            {
              float _area = 1 / (objX).area();
              radianceOutput = radianceOutput.
                      add(((objX).P).multiplyComponents(
                Main.material[mId].diffusionColor).multiplyScalar
                (_area));
            }
      }

    }

    radianceOutput = radianceOutput.divideScalar(
        Main.aosamps);
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
  static Point3D finalGathering(Ray viewRay, int x, int y, Obj o) {
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
  public static Point3D radiance(Ray r, Obj o, int x, int y) {

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
    float cos_i= r.d.dotProduct(n1);

    //fattore di Fresnel
    Point3D Fresn = Main.material[mId].getFresnelCoefficient(cos_i);

    //si verifica se il materiale ha una componente speculare
    //e che non sia stato superato il numero massimo di
    //riflessioni
    if((Main.material[mId].reflectionColor.max()>0)&&
        (Main.nRay<Utilities.MAX_DEPTH+1)){

     //con questo controllo si evitano le riflessioni interne
     //al materiale
     if(cos_i>0){
     // riflessione del raggio in entrata rispetto alla
     //normale n1
     Point3D refl= Point3D.reflect(r.d,n1);
     //si verifica che il materiale sia perfettamente
     //speculare o che non sia la prima riflessione (si evita
     //in questo modo l'aumento esponenziale dei raggi nel
     //caso in cui ci siano riflessioni multiple tra specchi
     //imperfetti)
     if((Main.material[mId].refImperfection==0)||(Main.nRay>0)){
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
        float t= Main.inf;
        //definizione e inizializzoazione a null dell'oggetto
        //che si andra' ad intersecare
        Obj objX;
        objX=null;
        //intersezione del raggio con gli elementi della scena
        if(Utilities.intersect(reflRay,objX)){
          //pongo t uguale al valore di intersezione
          //memorizzato nella variabile globale inters
          t= Main.inters;
          //resetto inters uguale a inf in modo da avere
          //il giusto valore di partenza la prossima volta
          //che si utilizzera' il metodo intersect()
          Main.inters= Main.inf;
          //salvo nell'elemento i-esimo dell'array objX
          //l'elemento intersecato dal raggio ffRay
          objX= Main.intersObj;
          //resetto intersObj=null in modo da avere il
          //giusto valore di partenza la prossima volta che
          //si utilizzera' il metodo intersect()
          Main.intersObj=null;
          //si calcola il punto di intersezione
          Point3D iP=(reflRay.o).add((reflRay.d).
                  multiplyScalar(t));
          //si crea il nuovo raggio
          Ray r2=new Ray(iP,refl.multiplyScalar(-1));
          //si aumentano il numero di riflessioni per il
          //raytracing
          Main.nRay++;
          //si calcola la radianza riflessa utilizzando
          //ricorsivamente la funzione radiance
          radianceRefl=radiance(r2,objX,x,y).multiplyComponents(Main.material
              [mId].S_BRDF(Fresn));
          //si diminuiscono il numero di riflessioni
          Main.nRay--;
        }

     }//fine if per specchi imperfetti
     else
     {
       //si aumenta il numero di riflessioni una volta per
       //tutti i campioni
       Main.nRay++;
         //per ogni campione
         for(int s = 0; s< Main.refSample; s++)
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
                random1= Utilities.generateRandom(Main.refSamples1[x+y* Main.w]);
                random2= Utilities.generateRandom(Main.refSamples2[x+y* Main.w]);

                //Inverse Cumulative Distribution Function
                //del coseno modificata
                //creiamo la base ortonormale per generare
                //la direzione del raggio riflesso reflRay
                float rndPhi=2*Utilities.MATH_PI *(random1);
                float rndTeta=(float) Math.acos((float)
                    Math.pow(random2, Main.material[mId].
                    refImperfection));

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
                dir=(u.multiplyScalar(cosPhi*sinTeta)).
                        add(v.multiplyScalar(sinPhi*
                    sinTeta)).add(w.
                        multiplyScalar(cosTeta));

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
             float t= Main.inf;
             Obj objX;
             objX=null;
             //intersezione con gli oggetti della scena
             if(Utilities.intersect(reflRay, objX)){
              t= Main.inters;
              Main.inters= Main.inf;
              objX= Main.intersObj;
              Main.intersObj=null;
              //punto di intersezione del raggio con
              //l'oggetto objX:
                Point3D iP=(reflRay.o).add(
                    (reflRay.d).multiplyScalar(t));
                //nuovo raggio:
                Ray r2=new Ray(iP,refl.multiplyScalar(-1));

                //calcolo della radianza riflessa
                radianceRefl=radianceRefl.add(
                    radiance(r2,objX,x,y).multiplyComponents(
                    Main.material[mId].S_BRDF(Fresn)));
             }
         }
         Main.nRay--;
         //divido per il numero di raggi che sono stati creati
         radianceRefl=radianceRefl.divideScalar(
             (float) Main.refSample);

      }
     }
  }


  //rifrazione
  //si verifica che l'oggetto abbia Kg>0, non sia un
  //metallo e non si siano superato il numero massimo di
  //riflessioni del ray tracer
  if((Main.material[mId].refractionColor.max()>0)&&(Main.nRay<Utilities.MAX_DEPTH
      +1)&&(Main.material[mId].absorptionCoefficient.max()==0))
  {
  //si verifica che l'indice di rifrazione sia uguale per
  //tutte le componenti RGB
  //in questo caso il calcolo per la rifrazione sara'
  //semplificato
  if((Main.material[mId].refractionIndexRGB.x== Main.material[mId].refractionIndexRGB.y)&&
       (Main.material[mId].refractionIndexRGB.x== Main.material[mId].refractionIndexRGB.z))
  {
    //direzione del raggio rifratto:
    Point3D dir=new Point3D();
    //calcolo della direzione per il raggio rifratto:
    dir= Point3D.getRefraction(r.d,n1,
        Main.material[mId].refractionIndexRGB.x);
    //se c'e' effettivamente una rifrazione non interna
    if(dir!=new Point3D(-1.0f)){

      //viene creato il raggio rigratto
      Ray refrRay=new Ray(r.o,dir);

      //si verifica il parametro di imperfezione del
      //materiale
      if((Main.material[mId].refImperfection==0)||(Main.nRay>0))
      {
          float t= Main.inf;
          Obj objX;
          objX=null;

          if(Utilities.intersect(refrRay, objX))
          {
            t= Main.inters;
            Main.inters= Main.inf;
            objX= Main.intersObj;
            Main.intersObj=null;
            Point3D iP=(refrRay.o).add(
                 refrRay.d.multiplyScalar(t));
            Ray r2=new Ray(iP,dir.multiplyScalar(-1));

            Main.nRay++;
            //calcolo della radianza rifratta
            radianceRefr=radiance(r2,objX,x,y).multiplyComponents(
                 Main.material[mId].T_BRDF(Fresn));
            Main.nRay--;
          }

      }
      else{

        Main.nRay++;
          //per ogni sample
          for(int s = 0; s< Main.refSample; s++){

              float random1;
              float random2;
              //flag di controllo sulla direzione creata
              boolean okdir=true;
              //float3 dir;

              while(okdir){

               //variabili aleatorie uniformi in
               //[0,1]
               random1= Utilities.generateRandom(
                  Main.refSamples1[x+y* Main.w]);
               random2= Utilities.generateRandom(
                  Main.refSamples1[x+y* Main.w]);

               //distribuisco i numeri random sull'
               //emisfero
               float rndPhi=2*Utilities.MATH_PI *(
                  random1);
               float rndTeta=(float) Math.acos(
                  (float) Math.pow(random2,
                  Main.material[mId].refImperfection));

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
               dir=(u.multiplyScalar(cosPhi*sinTeta)).
                       add(v.multiplyScalar(
                  sinPhi*sinTeta)).add(
                  w.multiplyScalar(cosTeta));
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

              float t= Main.inf;
              Obj objX;
              objX=null;

              if(Utilities.intersect(refrRay, objX)){
                t= Main.inters;
                Main.inters= Main.inf;
                objX= Main.intersObj;
                Main.intersObj=null;
                  //punto di intersezione
                  Point3D iP=(refrRay.o).add((
                      refrRay.d).multiplyScalar(t));
                  Ray r2=new Ray(iP,dir.multiplyScalar(
                      -1));

                  //calcolo della radianca rifratta
                  radianceRefr=radianceRefr.add
                      (radiance(r2,objX,x,y).multiplyComponents(
                      Main.material[mId].T_BRDF(Fresn)));
              }
          }
          Main.nRay--;
          //si mediano i contributi di tutti i raggi
          //usati
          radianceRefr=radianceRefr.divideScalar(
              (float) Main.refSample);

        }
      }
    }
    //se l'indice di rifrazione e' diverso nelle 3
    //componeneti RGB allora si devono calcolare 3 raggi
    //uno per ogni componente. In questo caso pero' per
    //facilitare il calcolo non viene considerato l'indice
    //di imperfezione del materiale
    else{

    // rifrazone del raggio basata sulla normale
    // Raggio utilizzato per la rifrazione
    Ray[] refrRay= new Ray[3];
    refrRay[0]=new Ray();
    refrRay[1]=new Ray();
    refrRay[2]=new Ray();
    refrRay[0].o=r.o;
    refrRay[1].o=r.o;
    refrRay[2].o=r.o;
    float K[]={0,0,0};

    //calcolo dei 3 raggi rifratti
    refrRay= Point3D.getRefraction(refrRay,r.d,n1, Main.material[mId].
                refractionIndexRGB);

    //carichiamo la brdf sul vettore g[] di 3 elementi
    //cosi da accedervi piu' facilmente
    Point3D brdf= Main.material[mId].T_BRDF(Fresn);
    float g[]={brdf.x,brdf.y,brdf.z};
    Main.nRay++;
    //per ogni componente RGB
    for(int i=0;i<3;i++){
    //si verifica che non ci sia stata riflessione
          //totale
    if(refrRay[i].depth!=0){

    float t= Main.inf;
    Obj objX;
    objX=null;

    if(Utilities.intersect(refrRay[i], objX)){
                  t= Main.inters;
                  Main.inters= Main.inf;
                  objX= Main.intersObj;
                  Main.intersObj=null;
                  Main.nRay++;
    Point3D iP=(r.o).add((refrRay[i].
                        d).multiplyScalar(t));
    Ray r2=new Ray(iP,refrRay[i].d.
                            multiplyScalar(-1));

    //viene calcolato il valore di radianza
    //del raggio
    Point3D pr= radiance(r2,objX,x,y);
    //si deve ora estrapolare la componente
    //i di tale radianza
    float[] rg=new float[3];
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
    Main.nRay--;
    }
    }

    //algoritmi per il calcolo dell'illuminazione globale:
    //si controlla che il materiale abbia componente Kd>0 in
    //almeno una delle 3 componenti o che il coefficiente
    //slope per la rugosita' della superficie sia >0 o che
    //il materiale emetta luce
    if((Main.material[mId].diffusionColor.max()>0)||(Main.material[mId].slope>0)||
        (Main.material[mId].emittedLight.max()>0)){


    //metodo di Jacobi stocastico e final gathering:
    if (Main.jacob && Main.Fg) {

          float areaInverse= (1.0f)/((o).areaObj);
        Point3D L=((o).P).multiplyScalar(areaInverse);
        Point3D f= finalGathering(r, x, y, o);
        return f.add(radianceRefr).
                add(radianceRefl);

      }
      //metodo di Jacobi stocastico:
      if(Main.jacob){
        float areaInverse= (1.0f)/((o).areaObj);
        Point3D L=((o).P).multiplyScalar(areaInverse);

        //si somma alla radianza emessa dalla patch
        //(pesata per l'area), la radianza data dalla
        //riflessione e dalla rifrazione, quindi si
        //restituisce il valore di radianza calcolato in
        //questo modo
    return L.add(radianceRefr).add(
                radianceRefl);

  }

//se non e' stato impostato nessuno di tali metodi
//allora viene restituito il colore nero
return new Point3D();

}//fine if
else{
//se il materiale e' riflettente o trasparente
return radianceRefl.add(radianceRefr);
}
}

  //metodo che restituisce la radianza emessa dall'oggetto
  //o in direzione r:
  static Point3D emittedObjRadiance(Obj o) {
    //carico l'indice del material
    int mId = o.matId;

    //dichiato e inizializzo a 0 il valore in uscita
    Point3D radianceOutput = new Point3D();

    //con il seguente if si controlla se il materiale
    //emette effettivamente luce
    if (Main.material[mId].emittedLight.max() > 0) {
      float Ler = Main.material[mId].emittedLight.max();
      Point3D Ler3=new Point3D(Ler);
      //con il metodo clamp3 si evita che la radianza in
      //uscita superi il valore massimo di radianza: 1
      Point3D.clamp3(Ler3);
      radianceOutput = Ler3;
    }
    return radianceOutput;
  }

  //funzione per il calcolo della radiosita' della scena:
  //in questa funzione si utilizza il metodo di Jacobi
  //stocastico per calcolare il valore di potenza di ogni
  //patch della scena
  //si usa la variabile globale GlobalObject in modo da
  //conservare i valori aggiornati delle potenze dei vari
  //oggetti
  void jacobiStoc(int nObj) {
    ArrayList<ParallelProcessRadiance> parallelisation = null;
    //TODO controllare probabili ottimizzazioni

    //definizco la potenza residua totale delle patch:
    Point3D Prtot=new Point3D();
    //definisco la potenza di ogni patch della scena:
    Point3D[] P=new Point3D[nObj];

    //definisco la potenza residua di ogni patch della
    //scena:
    Point3D[] Pr=new Point3D[nObj];

    //inizializzo a (0,0,0) P e Pr
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
      float area= Main.GlobalObjects[i].areaObj;
      //se l'area e' piu' piccola della precisione di
      //calcolo allora impostiamo l'area a 0
      if(area<Utilities.EPSILON) {area=0;}

      //potenza della luce
      //Potenza emessa dalla patch i:
      //(potenza emessa)*(pi greco)*(area)
      Point3D LP= Main.material[Main.GlobalObjects[i].matId].emittedLight.
                      multiplyScalar(Utilities.MATH_PI).
                      multiplyScalar(area);

      //viene calcolata la potenza totale iniziale:
      //(potenza residua della patch)+
      //+(potenza emessa dalla patch)
      Prtot=Prtot.add(LP);

      //viene salvata nell'array P la potenza dell'
      //elemento i
      P[i].copy(LP);

      //viene salvata nell'array Pr la potenza residua
      //dell'elemento i
      Pr[i].copy(LP);

      //viene calcolato l'errore di approssimazione
      //(con cui e' possibile fermare il metodo)
      Main.err=(float) (Main.err+Math.pow(LP.average(),2));
    }//fine for

    //dopo aver aggiunto dei quadrati dobbiamo fare la
    //radice del risultato finale
    Main.err= (float) Math.sqrt(Main.err);

    //iterazioni del metodo di Jacobi:
    //Si continuano le iterazioni finche' l'energia
    //rilasciata non diventa minore di un certo valore
    ///(verificato dalla variabile err)
    //o gli steps superano gli steps massimi (verificato
    //dalla variabile maxsteps).
    //Devono essere vere entrambe

    Main.steps = 0;

    while ((Main.err > Main.maxerr) && (Main.steps < Main.maxsteps)) {
      //percentuale di completamento
      System.out.println("completamento jacobi "+(Main.steps/(float) Main.maxsteps)*100);
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
      float Prt= Prtot.x+Prtot.y+Prtot.z;
      //campioni distribuiti in base alla potenza
      //totale di ciascuna componente RGB che chiamiamo
      //qui (x,y,z)

      Point3D samps=new Point3D(Main.Jacobisamps*Prtot.x,
              Main.Jacobisamps*Prtot.y, Main.Jacobisamps*Prtot.z);
      samps=samps.divideScalar(Prt);
      //parametro che ci permette di contare, quindi
      //utilizzare tutti i campioni che erano stati
      //previsti

      Point3D q=new Point3D();
      //per ogni patch della scena

      for(int i=0;i<nObj;i++) {
        //salviamo nella variabile locale o l'oggetto
        //i-esimo dell'array GlobalObjects
        Obj o= Main.GlobalObjects[i];

        //probabilita' con cui viene scelta la patch i
        Point3D pi=(Pr[i].divideComponents(Prtot));

        q=q.add(pi);

        //componente rossa:

        //calcoliamo il numero di campioni utilizzati
        //per la patch i in base alla sua potenza
        //e anche in base a quanti campioni sono gia'
        //stati usati
        NX=Math.round((q.x*samps.x)+NprevX*(-1));

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
          float rndX=0.0f;
          float rndY=0.0f;
          float rndZ=0.0f;
          rndX= Utilities.generateRandom(s);
          rndY= Utilities.generateRandom(s);
          rndZ= Utilities.generateRandom(s);

          //Inverse Cumulative Distribuction Function
          //creiamo la base ortonormale per generare
          //la direzione del raggio ffRay
          float rndPhi=2*Utilities.MATH_PI *(rndX);
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

          if(Utilities.intersect(ffRay, objX[i])){
            //resetto inters uguale a inf in modo da
            //avere il giusto valore di partenza la
            //prossima volta che si utilizzera' il
            //metodo intersect()
            Main.inters= Main.inf;
            //salvo nell'elemento i-esimo dell'array
            //objX l'elemento intersecato dal raggio
            //ffRay
            objX[i]= Main.intersObj;
            //resetto intersObj=null in modo da avere
            //il giusto valore di partenza la prossima
            //volta che si utilizzera' il
            //metodo intersect()
            Main.intersObj=null;
            //salviamo la potenza rilasciata all'interno
            //della struttura dell'oggetto: essa si
            //sommera' con la potenza residua parziale
            //che l'oggetto ha raggiunto finora; solo
            //alla fine del processo infatti avremo
            //la potenza residua totale della patch
            objX[i].P.x = objX[i].P.x
                    + Main.material[objX[i].matId].diffusionColor.x*(Prtot.x)/(samps.x);
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
        NY=Math.round((q.y*samps.y)+NprevY*(-1));

        //per ogni campione sull'elemento i
        for (int j=0; j<NY; j++) {

          Point3D dir;
          float rndX=0.0f;
          float rndY=0.0f;
          float rndZ=0;

          rndX= Utilities.generateRandom(s);
          rndY= Utilities.generateRandom(s);
          rndZ= Utilities.generateRandom(s);

          float rndPhi=2*Utilities.MATH_PI *(rndX);
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

          if(Utilities.intersect(ffRay, objX[i])) {
            Main.inters= Main.inf;
            objX[i]= Main.intersObj;
            Main.intersObj=null;
            objX[i].P.y=objX[i].P.y
                    + Main.material[objX[i].matId].diffusionColor.y*(Prtot.y)/(samps.y);
          }
        }//fine for per la compoente verde
        NprevY=NprevY+NY;

        //componente blu:

        //campioni per la patch i
        NZ=Math.round((q.z*samps.z)+NprevZ*(-1));

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
          float rndPhi=2*Utilities.MATH_PI *(rndX);
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

          if(Utilities.intersect(ffRay,objX[i])) {
            Main.inters= Main.inf;
            objX[i]= Main.intersObj;
            Main.intersObj=null;
            objX[i].P.z = objX[i].P.z
                    + Main.material[objX[i].matId].diffusionColor.z*(Prtot.z)/(samps.z);
          }
        }//fine for per la componente blu

        NprevZ=NprevZ+NZ;
      }//fine for per le patch della scena SAS1

      //una volta terminata la fase di shooting si
      //riaggiornano le componenti di Potenza residua,
      //Pr, Potenza P, e si azzerano le potenze
      //residue parziali salvate negli oggetti
      //azzeramento della potenza totale:

      Prtot=new Point3D(0);

      for(int i=0;i<nObj;i++) {
        //aggiornamento delle Potenze (vengono aggiunte le potenze residue totali immagazzinate dalle patch durante
        //il processo)
        P[i]=P[i].add(Main.GlobalObjects[i].P);
        Pr[i].copy(Main.GlobalObjects[i].P);  //aggiornamento delle potenze residue totali
        Main.err=(float) (Main.err+Math.pow(Pr[i].average(),2));  //calcolo dell'errore
        Prtot=Prtot.add(Pr[i]); //calcolo dell'energia residua totale
        Main.GlobalObjects[i].P.copy(new Point3D());  //azzeramento della potenza residua parziale contenuta nella patch
      }

      Main.steps++;
    }

    //I valori ottenuti vengono salvati su ciascuna
    //patch nella variabile P (in cui durante il processo
    //veniva salvata la potenza residua parziale)
    for(int i=0; i<nObj; i++) {
      (Main.GlobalObjects[i].P).copy(P[i]);
    }
   }
}
