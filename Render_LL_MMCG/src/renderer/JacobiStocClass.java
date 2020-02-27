package renderer;

import primitive.Obj;
import primitive.Point3D;
import primitive.Ray;
import ui.InterfaceInitialiser;
import ui.RenderAction;

import java.text.DecimalFormat;

public class JacobiStocClass {
  //Metodo per il calcolo della radiosita' della scena:
  //in questa funzione si utilizza il metodo di Jacobi
  //stocastico per calcolare il valore di potenza di ogni
  //patch della scena
  //si usa la variabile globale GlobalObject in modo da
  //conservare i valori aggiornati delle potenze dei vari
  //oggetti

  /* Ci sono diversi problemi presenti nel metodo:
   *  -> la scelta del numero di sample da inviare per
   *      Jacobi stocastico (da variare in base alla potenza
   *      della scena)
   *  -> la suddivisione in varie patches della scena
   *      (al momento formata da sole semplici patches, rendendo
   *       l'intero render troppo approssimativo)
   */

  public static void jacobiStoc(int nObj) {
    Utilities utilities = new Utilities();

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
      double area= RenderAction.globalObjects.get(i).areaObj;
      //se l'area e' piu' piccola della precisione di
      //calcolo allora impostiamo l'area a 0
      if(area < Utilities.EPSILON) {
        area=0;
      }

      //potenza della luce
      //Potenza emessa dalla patch i:
      //(potenza emessa)*(pi greco)*(area)
      Point3D LP= RenderAction.material[RenderAction.globalObjects.get(i).matId].emittedLight
          .multiplyScalar(Utilities.MATH_PI)
          .multiplyScalar(area);

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

    /* jacobiSamps possono essere impostati secondo la geometria della scena,
     *  così da generare i campioni
     */
    while ((RenderAction.err > RenderAction.maxErr)
        && (RenderAction.steps < RenderAction.maxSteps)) {
      //percentuale di completamento
      InterfaceInitialiser.label.setText("Completamento Jacobi "
          + new DecimalFormat("###.##")
          .format((RenderAction.steps/(float) RenderAction.maxSteps)*100));
      //viene inizializzato un seme iniziale casuale
      //da 0 a 30000
      int s =(int) Math.floor(Math.random()*(30001));
      //inizializzo per le tre componenti una variabile
      //per il numero di sample utilizzati per la patch
      //(N*) e una per il numero di sample utilizzati
      //finora (Nprev*)
      int NprevX=0;
      int NprevY=0;
      int NprevZ=0;
      int NX, NY, NZ;

      //potenza residua totale nelle tre componenti
      double Prt= Prtot.x+Prtot.y+Prtot.z;
      //campioni distribuiti in base alla potenza
      //totale di ciascuna componente RGB che chiamiamo
      //qui (x,y,z)

      Point3D samps = new Point3D(
          RenderAction.jacobiSamps*Prtot.x,
          RenderAction.jacobiSamps*Prtot.y,
          RenderAction.jacobiSamps*Prtot.z);
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

        //componente R:

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
          float rndTeta=(float) Math.acos(Math.sqrt(rndY));

          //dichiaro e inizializzo un punto scelto
          //uniformemente sulla patch i
          Point3D rndPoint = o.randomPoint(rndX, rndY, rndZ);

          //Si crea ora la base ortonormale
          Point3D u;
          Point3D v;
          //settiamo width come la normale all'oggetto nel
          //punto rndPoint
          Point3D w = o.normal(rndPoint);

          //vettore up (simile a (0,1,0))
          Point3D up=new Point3D(0.0015f,1.0f,0.021f);
          //il prodotto vettoriale tra width e up mi genera
          //il vettore v normale a entambi, che
          //normalizzo
          v=w.crossProduct(up);
          v=v.getNormalizedPoint();
          //il prodotto vettoriale tra v e width mi genera
          //il vettore u normale a entambi
          //dal momento che i vettori v e width sono gia'
          //normali, non c'e' bisogno di noemalizzare
          //il vettore u)
          u=v.crossProduct(w);

          //ora che abbiamo la base ortonormale,
          //possiamo calcolare la direzione dir.
          //salvo in delle cariabili i calori di seno
          //e coseno necessari per il calcolo di dir
          float cosRndPhi=(float) Math.cos(rndPhi);
          float sinRndTeta=(float) Math.sin(rndTeta);
          float sinRndPhi=(float) Math.sin(rndPhi);
          float cosRndTeta=(float) Math.cos(rndTeta);
          //poi normalizzato
          dir = u.multiplyScalar(cosRndPhi*sinRndTeta)
              .add(v.multiplyScalar(sinRndPhi*sinRndTeta))
              .add(w.multiplyScalar(cosRndTeta));
          dir = dir.getNormalizedPoint();

          //creo il raggio per scegliere la patch j
          //con probabilita' uguale al fattore di forma
          //tra la patch i e quella j
          Ray ffRay=new Ray(rndPoint,dir);

          //inizializzo a null l'oggetto intersecato
          objX[i]=null;

          //l'oggetto intersecato e' proprio la patch j
          //su cui sara' rilasciata la potenza totale
          //della componente rossa

          if(utilities.intersect(ffRay, objX[i])) {
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
            utilities.intersObj = null;
            //salviamo la potenza rilasciata all'interno
            //della struttura dell'oggetto: essa si
            //sommera' con la potenza residua parziale
            //che l'oggetto ha raggiunto finora; solo
            //alla fine del processo infatti avremo
            //la potenza residua totale della patch
            objX[i].P.x +=
                RenderAction.material[objX[i].matId].diffusionColor.x*(Prtot.x)/(samps.x);
          }
        }//fine for per la compoente rossa
        //aggiorniamo il numero di campioni usati per
        //questa componente
        NprevX += NX;

        //componente G:

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

          //punto scelto uniformemente nella patch i:
          Point3D rndPoint = o.randomPoint(rndX, rndY, rndZ);

          //base ortonormale
          Point3D u,v;
          Point3D w = o.normal(rndPoint);

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
            utilities.inters = Utilities.inf;
            objX[i]= utilities.intersObj;
            utilities.intersObj =null;
            objX[i].P.y +=
                RenderAction.material[objX[i].matId].diffusionColor.y*(Prtot.y)/(samps.y);
          }
        }//fine for per la compoente verde
        NprevY += NY;

        //componente B:

        //campioni per la patch i
        NZ= (int) Math.round((q.z*samps.z)+NprevZ*(-1));

        //per ogni campione sull'elemento i
        for (int j=0; j<NZ; j++){
          Point3D dir;
          float rndX;
          float rndY;
          float rndZ;

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
          Point3D w = o.normal(rndPoint);

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
          objX[i] = null;

          if(utilities.intersect(ffRay,objX[i])) {
            utilities.inters = Utilities.inf;
            objX[i]= utilities.intersObj;
            utilities.intersObj = null;
            objX[i].P.z +=
                RenderAction.material[objX[i].matId].diffusionColor.z*(Prtot.z)/(samps.z);
          }
        }//fine for per la componente blu

        NprevZ += NZ;
      }//fine for per le patch della scena SAS1

      //una volta terminata la fase di shooting si
      //riaggiornano le componenti di Potenza residua,
      //Pr, Potenza power, e si azzerano le potenze
      //residue parziali salvate negli oggetti
      //azzeramento della potenza totale:

      Prtot=new Point3D(0);

      for(int i=0; i < nObj; i++) {
        //aggiornamento delle Potenze (vengono aggiunte le potenze residue totali
        // immagazzinate dalle patch durante il processo,
        // secondo la radiosità basata su potenza incrementale)
        P[i] = P[i].add(RenderAction.globalObjects.get(i).P);
        //aggiornamento delle potenze residue totali
        Pr[i].copy(RenderAction.globalObjects.get(i).P);
        //calcolo dell'errore
        RenderAction.err += Math.pow(Pr[i].average(),2);
        //calcolo dell'energia residua totale
        Prtot = Prtot.add(Pr[i]);
        //azzeramento della potenza residua parziale contenuta nella patch
        RenderAction.globalObjects.get(i).P.copy(new Point3D());
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
}
