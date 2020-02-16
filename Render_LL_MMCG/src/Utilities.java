import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Utilities {
	//DEFINIZIONE DI VARIABILI
	
	static final float EPSILON = 1.e-2f;
	static final float MATH_PI = 3.14159265358979323846f;
	//1 su pi greco
	static final float MATH_1_DIV_PI = 0.318309886183790671538f;
	static final float MATH_1_DIV_180 = 0.005555555690079927f;

	//massima ricorsivita' del ray tracing 
	static int MAX_DEPTH=100;

	//massima ricorsivita' del photon mapping
	static int MAX_DEPTH_PHOTON=100000;

	//massima ricorsivita' del ray tracing
	static int MAX_DEPTH_CAUSTIC=100;


  //variabile globale, utilizzata nel metodo intersect(),
  //in cui viene salvato l'oggetto intersecato da un
  //raggio considerato
  Obj intersObj = null;
  //variabile globale, utilizzata nel metodo intersect(),
  //in cui viene salvato punto di intersezione tra l'oggetto
  //e il raggio considerato
  static double inf=(float) 1e20;	//distanza massima dal raggio
  double inters = inf;//sarebbe t del metodo intersect;

  //Intersezione con i box della ripartizione spaziale.
  //Controllando in modo ricorsivo, cerso l'intersezione
  //piu' vicina nei 2 figli di ogni box che stiamo
  //considerando, fino a trovare il punto di intersezione
  //b: box da intersecare con il raggio,
  //r: raggio considerato


  boolean intersectBSP(Box b,Ray r){
    if(b.intersect(r)) {  //controllo che i figli non siano nulli (ovvero che abbia figli)
      if((b.leaf1 != null) && (b.leaf2 != null)) { //se non e' nullo ricomincia con i figli
        intersectBSP(b.leaf1,r);
        intersectBSP(b.leaf2,r);
      } else { //altrimenti se non ha figli:
        //vengono salvati nell'array objects tutti
        //gli oggetti contenuti nel box che stiamo
        //considerando
        ArrayList<Obj> objects= b.objects;

        //inizializziamo il punto di intersezione
        //con il raggio
        double d=0;

        for (Obj object : objects) {  //per ogni oggetto
          //nell'if si richiama il metodo intersect()
          //dell'oggetto in questione: ricordiamo che
          //questo metodo restituisce il valore della
          //distanza o -1.0f se non c'e' intersezione,
          //quindi nella prima condizione dell'if
          //controlliamo che effetticamente ci sia un'
          //intersezione e assegnamo questo valore a d.
          //nella seconda condizione controlliamo pero'
          //che questo valore di d appena calcolato
          //sia < del valore gia' memorizzato nella
          //variabile inters (dal momento che dobbiamo
          //salvare l'intersezione piu' vicina)
          if ((d = object.intersect(r)) >= 0.0f && d < inters) {
            //salviamo quindi il valore di d nella
            //variabile globale inters
            inters = d;
            //e salviamo l'oggetto intersecato nella
            //variabile globale intersObj
            intersObj = object;
          }
        }
      }
    } else {  //altrimenti se non si interseca il  box
      return false;
    }

      //dobbiamo anche controllare che il valore inters sia
      //rimasto minore della distanza massima del raggio inf
    //altrimenti l'intersezione e' troppo lontana quindi
    //non la consideriamo
    return inters < inf;
  }


  //metodo che serve a calcolare l'intersezione tra un
  //oggetto e un raggio, che richiama la funzione
  //inersectBSP
  boolean intersect(Ray r, Obj oi) {
    if((oi)==null) {
        //se l'oggetto e' nullo si richiama semplicemente
        //il metodo intersectBSP
        return intersectBSP(RenderAction.bound,r);
    } else {
      //se l'oggetto non e' nullo, si salva in un'altra
      //variabile o1 e poi si rende nullo, per poter
      //richiamare il metodo intersectBSP
      Obj o1=oi;
      oi=null;

      if(intersectBSP(RenderAction.bound,r)) {
        oi = intersObj;

        return (o1) == (oi);
      } else {
        return false;
      }
    }
  }

  // riflessione di un vettore i rispetto alla normale n
  Point3D reflect(Point3D i, Point3D n) {
    return ((n.multiplyScalar(2)).multiplyScalar(n.dotProduct(i)).subtract(i));
  }

  // rifrazione di un vettore i rispetto ad una normale n
  // in questa funzione la rifrazione non varia con la lunghezza d'onda
  Point3D refract(Point3D i, Point3D n, double ior) {

    //ci si accerta che il vettore in entrata sia normalizzato
    i = i.getNormalizedPoint();

    //si calcola il coseno tra la normale e il vettore entrante i
    // <n,i>
    double cos_theta_i = n.dotProduct(i);

    //si prende in esame l’indice di rifrazione ior del materiale
    //per semplificare il calcolo viene considerato solamente
    //il passaggio dal vuoto
    //non è quindi possibile con questa funzione modellare il passaggio di luce
    //tra due materiali con indice di rifrazione differente
    //indice di rifrazione nel vuoto / indice di rifrazione del materiale
    double eta = 1/ior;

    //se il coseno dell'angolo tra il vettore i e n è minore di 0 allora
    //dobbiamo invertire la normale e l'indice di rifrazione
    //ovvero il passaggio avverrà dal mezzo denso fino al vuoto
    // (di conseguenza il coseno diventerà positivo)
    if(cos_theta_i<0){
      cos_theta_i=-cos_theta_i;
      eta = 1.0/eta;
      n = n.multiplyScalar(-1);
    }

    //calcolo dei fattori necessari
    double sin2_theta_i=1-(cos_theta_i*cos_theta_i);
    double sin2_theta_t= eta*eta*sin2_theta_i;

    // se questo coefficente è minore di 0 allora avviene una riflessione totale e il resto del calcolo non viene effettuato
    double K= 1-sin2_theta_t;
    if(K<0){
      //riflessione totale
      return null;
    } else {
      //altrimenti si procede con il calcolo del vettore rifratto
      double cos_theta_t = Math.sqrt(K);
      return n.multiplyScalar(eta*cos_theta_i-cos_theta_t).subtract(i.multiplyScalar(eta));
    }
  }

  /// Refract a vector i on normal n based on external ior
  Ray[] refract(Ray[] rays, Point3D i, Point3D n, Point3D ior) {
    Ray[] t = rays;

    //utilizziamo la proprietà depth del raggio per verificare se c'è riflessione totale
    t[0].depth=1;
    t[1].depth=1;
    t[2].depth=1;

    i = i.getNormalizedPoint();
    // <n, i>
    double cos_theta_i=n.dotProduct(i);
    Point3D eta = new Point3D(1).divideComponents(ior);

    //se l'angolo di incidenza è superiore a pi/2 allora devo cambiare il verso della normale e cambiare indice di rifrazione (prendendo il suo inverso)
    if(cos_theta_i<0.0f){
      cos_theta_i=-cos_theta_i;
      n = n.multiplyScalar(-1.0f);
      eta = new Point3D(1).divideComponents(eta);
    }

    double sin2_theta_i=1-(cos_theta_i*cos_theta_i);

    Point3D sin2_theta_t= eta.multiplyComponents(eta).multiplyScalar(sin2_theta_i);

    Point3D K= new Point3D(1).subtract(sin2_theta_t);

    //componente rossa
    if (K.x<0.0f) {
      // TIR
      //radice negativa niente rifrazione
      t[0].depth=0;
    } else {
      double cos_theta_t= Math.sqrt(K.x);
      t[0].d= n.multiplyScalar(eta.x*cos_theta_i-cos_theta_t).subtract(i.multiplyScalar(eta.x));
    }

    //componente verde
    if (K.y<0.0f) {
      // TIR
      //radice negativa niente rifrazione
      t[1].depth=0;
    } else {
      double cos_theta_t= Math.sqrt(K.y);
      t[1].d= n.multiplyScalar(eta.y*cos_theta_i-cos_theta_t).subtract(i.multiplyScalar(eta.y));
    }

    //componente blu
    if (K.z<0.0f){
      // TIR
      //radice negativa niente rifrazione
      t[2].depth=0;
    } else {
      double cos_theta_t= Math.sqrt(K.z);
      t[2].d= n.multiplyScalar(eta.z*cos_theta_i-cos_theta_t).subtract(i.multiplyScalar(eta.z));
    }

    return t;
  }

  ArrayList<Point3D> Projection(Point3D p,Point3D n){
    ArrayList<Point3D> ProjectionMap = new ArrayList<>();

    Point3D u,v,w;
    w = n;
    Point3D up = new Point3D(0.0015f,1.0f,0.021f);
    v = w.crossProduct(up);
    v = v.getNormalizedPoint();
    u = v.crossProduct(w);

    //incremento
    float dTheta = (MATH_PI/2)/(RenderAction.ProjectionResolution);
    float dPhi=(2*MATH_PI)/ RenderAction.ProjectionResolution;

    //latitudine
    double Theta=0;
    double T=0;

    Obj objY = null;

    for(int i = 0; i < RenderAction.ProjectionResolution; i++) {
      //longitudine
      float Phi=0;

      for(int j = 0; j < RenderAction.ProjectionResolution; j++) {

        //direzione corrispondente all'angolo in esame
        Point3D dir = u.multiplyScalar((Math.cos(Phi)*Math.sin(Theta))).add(v.multiplyScalar(Math.sin(Phi)*Math.sin(Theta))).add(w.multiplyScalar((Math.cos(Theta))));

        Ray pRay= new Ray(p,dir);

        if(intersect(pRay, objY)){
          objY = intersObj;

          intersObj = null;
          inters = inf;

          //verifico la presenza di un materiale trasparente
          if(RenderAction.material[objY.matId].refractionColor.max() > 0) {
            Point3D angle = new Point3D(Phi,Theta,T);
            ProjectionMap.add(angle);
          }
        }

        Phi+=dPhi;
      }

      T+=dTheta;
      Theta=T*Math.sin(T);
    }

    return ProjectionMap;
  }

  void locate_photons(Map<Double, Photon> nearPh, Point3D iP, int index, Obj objX, PhotonBox[] Tree, double d_2, int nph){
    //si verifica che il box index non sia vuoto
    if(Tree[index-1].nph != 0){

      //si verifica che il nuovo indice non abbia ha superato la lunghezza dell'albero (P), in tal caso l'indice corrisponde ad un box all'estremità dell'albero
      if((2*index)+1< RenderAction.P){

        //viene caricato il punto di intersezione in un array di 3 elementi
        double[] pos = {iP.x, iP.y, iP.z};

        //si calcola la distanza del punto dal piano del nodo in esame
        double delta = pos[Tree[index-1].dim]-Tree[index-1].planePos;

        //a seconda della risposta si continua la ricerca nella foglia sinistra o nella foglia destra
        if(delta<0){

          //foglia sinistra:

          //si continua la ricerca solo se il box ha dei fotoni all'interno
          locate_photons(nearPh,iP,2*index,objX,Tree,d_2,nph);

          //viene verificato se la distanza dal piano è più piccola della distanza di ricerca
          if(Math.pow(delta,2)<d_2){

            //se lo è si deve cercare anche nella foglia destra
            locate_photons(nearPh,iP,2*index+1,objX,Tree,d_2,nph);

          }
        }else{

          //foglia destra (stesso procedimento invertito della foglia sinistra)
          locate_photons(nearPh,iP,2*index+1,objX,Tree,d_2,nph);

          if(Math.pow(delta,2)<d_2){

            locate_photons(nearPh,iP,2*index,objX,Tree,d_2,nph);
          }
        }
      } else {
        //se ci si trova all'estremità dell'albero

        //si caricano tutti i fotoni del box
        int n = Tree[index-1].nph;
        ArrayList<Photon> p = Tree[index-1].ph;

        //per ogni fotone
        for(int i=0;i<n;i++){

          //si verifica la distanza del fotone dal punto di intersezione
          Point3D dist = p.get(i).position.subtract(iP);


          if(objX.t != null){

            //se l'oggetto è un triangolo la ricerca sarà planare
            //in realtà per evitare fenomeni di aliasing su le facce piccole si è costretti a considerare un parametro che
            // ci permetta di effettuare la ricerca sul disco o su una sfera
            //questo parametro è definito come sphericalSearch
            //più questo parametro è grande più

            //proiezione del vettore dist sul piano tangente ad objX

            Point3D d= dist.multiplyScalar(1/dist.normalize());
            double projN = d.dotProduct(objX.normal(iP));
            double val;

            if(objX.area() < 1){
              val= 1/(RenderAction.sphericalSearch*objX.area());
            }else{
              val= 1/(RenderAction.sphericalSearch);
            }

            if(val < Utilities.EPSILON)
              val=0;

            if((projN<=val) && (projN >= -val)) {

              double d2p = dist.squareNorm();
              //verifico la distanza
              if(d2p<d_2){

                nearPh.put(d2p, p.get(i));

                if(nearPh.size() > nph){
                  Double value = (double) 0;
                  for (Map.Entry<Double, Photon> entry : nearPh.entrySet()) {
                    value = entry.getKey();
                  }

                  d_2 = value;
                }
              }
            }
          } else if (objX.s != null){

            //se l'oggetto è una sfera la ricerca sarà sferica
            //verifico la distanza
            double d2p=dist.squareNorm();
            //verifico la distanza
            if(d2p<d_2){
              nearPh.put(d2p, p.get(i));

              if(nearPh.size() > nph){
                Double value = (double) 0;
                for (Map.Entry<Double, Photon> entry : nearPh.entrySet()) {
                  value = entry.getKey();
                }

                d_2 = value;
              }
            }
          }
        }
      }
    }
  }

  static boolean checkRefractionObjects() {
    boolean isThereRefraction = false;

    for (Obj o : RenderAction.globalObjects) {
      if (RenderAction.material[o.matId].refractionColor.max() > 0) {
        isThereRefraction = true;
        break;
      }
    }

    return isThereRefraction;
  }

  //metodo che genera randomicamente un valore in [0,1]
  //utilizzando x come seme
  static float generateRandom(int x) {
    double a = Math.floor(Math.random()*(x+1));
    return (float)(a/x);  //normalizzazione per riportare il valore in [0,1]
  }

  //DEFINIZIONE DI FUNZIONI MATEMATICHE
	
	//funzione da gradi a radianti
	public float degreesToRadiants(float deg) {
		return deg* MATH_PI * MATH_1_DIV_180;
	}

	//funzione da radianti a gradi 
	public float radiantsToDegrees(float rad) {
		return rad*180.0f* MATH_1_DIV_PI;
	}

	//costringe un numero in [0,1]
	static double clamp(double x){
		return x<0 ? 0 : x>1 ? 1 : x;
	}

	//Transforma un float in [0,1], in un intero in [0,255]
	//Si utilizza con una gamma di 2.2, ovvero il numero 
	//viene portato a [0,1] dopodiche' viene elevato alla 
	//2.2: in questo modo il valore 0,218 si avvicina allo
	//0,5 ovvero viene portato a meta' della gamma dando 
	//piu' spazio ai colori scuri, i quali creano sempre 
	//piu' problemmi. Moltiplicando infine per 255 si 
	//estende per il range di bit preso in esame e la 
	//funzione ritorna l'approssimazione ad intero
	static int toInt(double x){
		return (int) (Math.pow(clamp(x), 1/2.2)*255);
	}
}
