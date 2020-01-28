import java.util.ArrayList;

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

  //metodo che genera randomicamente un valore in [0,1]
  //utilizzando x come seme
  static float generateRandom(int x) {
    double a = Math.floor(Math.random() * (x+1));
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
