import java.util.*;

//classe che definisce un Box (e i suoi paramenti): 
//un contenitore parallelepipedico con all'
//interno gli elementi della scena, che puo'
//essere ulteriormente suddiviso in altri box figli
//In questo modo si puo' avere una partizione 
//sempre piu' fine, in box sempre piu' piccoli 
//che contengono gli elementi della scena 
public class Box {
	//array dei vertici che definiscono il box
  Point3D[] V = new Point3D[2];
  //il parametro lato ci dice il piano con cui il
  //box sara' diviso in due
  //corrispondenze piano:valore
  //xy:0 yz:1 xz:2    cioe'
  //lato=0 taglio con il piano z,
  //l=1 taglio con il piano x,
  //l=2 taglio con il piano y
  short side =0;

  //array degli oggetti che il box contiene
  ArrayList<Obj> objects;

  //Box figli: i due "sottobox" in cui il box
  //iniziale viene diviso. Nel caso rimangano Null il
  //Box non ha nessun figlio
  Box leaf1;;
  Box leaf2;;

  //costruttore box
  public Box(Point3D min, Point3D max, short l){
      V[0]=min;
      V[1]=max;
      side =l;
  }

  //BSP (binary space partition): dato un box lo ripartisce
  //in 2 box identici, tagliando il box di partenza con un
  //piano indicato dal parametro "lato" della classe Box
  //restituisce lo stesso box di partenza, a cui assegna
  //pero' i due sottobox generati ai parametri leaf1 e
  //leaf2, che in partenza erano settati a NULL
  static Box makeChild(Box B) {
    Point3D min= B.V[0];
    Point3D max= B.V[1];
    //l indica il piano con cui si vuole tagliare a
    //meta' il box (vedere la classe Box per la
    //definizione di lato)
    int l= B.side;

    //taglio con il piano z=(min.z+max.z)/2 a meta' del
    //Bound
    if(l==0){
        //vengono costruiti i box leaf1 e leaf2 del box
      B.leaf1= new Box(min,new Point3D(max.x,max.y,(
              min.z+max.z)/2),(short)1);
      B.leaf2= new Box(new Point3D(min.x,min.y,(
              min.z+max.z)/2),max,(short)1);
    }
    //taglio con il piano x=(min.x+max.x)/2 a meta' del
    //Bound
    if(l==1){
        //vengono costruiti i box leaf1 e leaf2 del box
      B.leaf1= new Box(min,new Point3D((min.x+max.x)/2,
              max.y,max.z),(short)2);;
      B.leaf2= new Box(new Point3D((min.x+max.x)/2,min.y,
              min.z),max,(short)2);
    }
    //taglio con il piano y=(min.y+min.y)/2 a meta' del
    //bound
    if(l==2){
        //vengono costruiti i box leaf1 e leaf2 del box
      B.leaf1= new Box(min,new Point3D(max.x,(min.y+
              max.y)/2,max.z),(short)0);;
      B.leaf2= new Box(new Point3D(min.x,(min.y+max.y)/2,
              min.z),max,(short)0);
    }

    //restituisce il Box di partenza, ma con le leaf
    //aggiornate
    return B;
  }

  //metodo iterativo che crea una partizione spaziale della
  //scena e divide gli oggetti di un box padre tra i suoi
  //due box figli.
  //il metodo continua fino a quando non si raggiunge il
  //valore depth
  //notiamo che la variabile liv (livello di profondita'
  //all'interno dell'albero) e' globale e viene
  //continuamente aggiornata nei passaggi
  static Box setPartition(Box b) {
    //procede solo se il box non e' nullo, il livello di
    //profondita' non ha superato il parametro depth e se
    //ci sono almeno un numero di oggetti maggiore del
    //valore di sogliaBox dentro al box
    if((RenderAction.depthLevel < RenderAction.depth) && (b!=null) && (b.objects.size() > RenderAction.sogliaBox)){
      //aumentiamo il livello di profondita' dell'albero
      RenderAction.depthLevel++;

      //crea i figli del box padre
      b = makeChild(b);
      //assegna ai figli (foglie dell'albero) gli
      //oggetti appartenti al box padre
      b.setLeafObj();

      //continua la partizione iterando il procedimento
      ///finche' non si raggiunge il livello di massima
      //profondita'
      setPartition(b.leaf1);
      setPartition(b.leaf2);

      //variabile utilizzata per visualizzare lo stato
      //di caricamento dei box
      RenderAction.loadedBoxes++;
      System.out.println("box caricati:  "+ RenderAction.loadedBoxes
              +"  su un massimo di  "+ RenderAction.maxPartitions
              +"  (ogni box contenente minimo  "
              + RenderAction.sogliaBox+"  oggetti)");
      //una volta finita la partizione dei figli
      //ritorna al livello iniziale
      RenderAction.depthLevel--;
    }
    //Il box di ritorno e' aggiornato con le partizioni
    //richieste
    return b;
  }

  //settiamo gli oggetti contenuti nel box
  void setObjects(ArrayList<Obj> o){
      objects=o;
  }

  //in questo metodo si verifica se esiste
  //l'intersezione di un raggio con il box
  boolean intersect(Ray r){
  	//inizializziamo a + e - infinito i piani
  	//near e far
    float tNear = Float.NEGATIVE_INFINITY;
    float tFar = Float.POSITIVE_INFINITY;
    //array con le componenti della direzione del
    //raggio
    float[] direction = {
            r.d.getX(),
            r.d.getY(),
            r.d.getZ()
    };
    //array con le componenti dell'origine del
    //raggio
    float[] origin = {
            r.o.getX(),
            r.o.getY(),
            r.o.getZ()
    };

    //array con le componenti dei vertici del box
    //V[0] e V[1]
    float[] min ={
            V[0].getX(),
            V[0].getY(),
            V[0].getZ()
    };
    float[] max = {
            V[1].getX(),
            V[1].getY(),
            V[1].getZ()
    };

    //per ogni componente faccio vari controlli
    //per capire se c'e' intersezione:
    for (int i = 0; i < 3; i++) {
      //se nella direzione i non c'e' variazione
      //e l'origine e' fuori dal box non puo'
      //esserci intersezione
      if (direction[i] == 0){
        if((origin[i] < min[i]) || (origin[i] > max[i])) {
          return false;
        }
      } else {
        //si definiscono le intersezioni piu'
        //vicina e piu' lontana
        float T1= (min[i] -origin[i])/direction[i];
        float T2= (max[i] -origin[i])/direction[i];
        //ordina dal piu' piccolo (T1) al piu'
        //grande (T2)
        if(T1>T2){
          //swap(T1,T2);
          float app=T2;
          T2=T1;
          T1=app;
        }
        //voglio il t vicino piu' grande
        if(T1>tNear)
          tNear=T1;
        //voglio il t lontano piu' piccolo
        if(T2<tFar)
          tFar=T2;
        //non c'e' intersezione tra i due
        //segmenti
        if(tNear>tFar){
          return false;
        }
        //il raggio interseca nella direzione
        //opposta
        if(tFar<0){
          return false;
        }
      }
    }

    return true;
  }
 //in questo metodo si determinano quali
  //oggetti siano contenuti all'interno di uno dei
  //figli del box
  void setLeafObj() {
    //creazione di due arrayList (non uso array
  	//perche' non posso sapere dall'inizio quanti
  	//oggetti conterranno le suddivisioni del box)
  	ArrayList<Obj> leaf1Obj = new ArrayList<>();
  	ArrayList<Obj> leaf2Obj = new ArrayList<>();

  	//per ogni oggetto presente nel box padre
    for (Obj object : objects) {
      //vogliamo scoprire in quale leaf e'
      //l'oggetto i-esimo
      boolean inleaf1 = false;  //flag primo figlio leaf1
      boolean inleaf2 = false;  //flag secondo figlio leaf2

      if (object.t != null) {  //se l'oggetto e' un triangolo
        //si carica il triangolo
        Triangle t = object.t;
        //per ogni vertice
        for (int j = 0; j < 3; j++) {
          //se il box e' stato dimezzato
          //rispetto all'asse z
          //cio' che discriminera' in quale
          //leaf sta' il vertice e' la
          //coordinata z (le altre restano
          //uguali)
          if (side == 0) {
            if (t.vertices[j].z < leaf1.V[1].z) {
              //il vertice e' nel box leaf1
              inleaf1 = true;
            } else {
              //il vertice e' nel box leaf2
              inleaf2 = true;
            }
          }
          //se il box e' stato dimezzato rispetto
          //all'asse x
          if (side == 1) {
            if (t.vertices[j].x < leaf1.V[1].x) {
              inleaf1 = true;
            } else {
              inleaf2 = true;
            }
          }
          //se il box e' stato dimezzato rispetto
          //all'asse y
          if (side == 2) {
            if (t.vertices[j].y < leaf1.V[1].y) {
              inleaf1 = true;
            } else {
              inleaf2 = true;
            }
          }
        }
      }

      // se l'oggetto e' una sfera si procede
      //diversamente
      if (object.s != null) {
        //si carica la sfera
        Sphere s = object.s;
        //si carica il raggio della sfera
        float rad = s.rad;
        //se il box e' stato dimezzato rispetto
        //all'asse z
        if (side == 0) {
          //se mi trovo all'interno della leaf1
          if (s.p.z - rad < leaf1.V[1].z) {
            inleaf1 = true;
          }
          //se mi trovo all'interno della leaf2
          if (s.p.z + rad > leaf1.V[0].z) {
            inleaf2 = true;
          }
        }
        //se il box e' stato dimezzato rispetto
        //all'asse x
        if (side == 1) {
          if (s.p.x - rad < leaf1.V[1].x) {
            inleaf1 = true;
          }
          if (s.p.x + rad > leaf1.V[0].x) {
            inleaf2 = true;
          }
        }
        //se il box e' stato dimezzato rispetto
        //all'asse y
        if (side == 2) {
          if (s.p.y - rad < leaf1.V[1].y) {
            inleaf1 = true;
          }
          if (s.p.y + rad > leaf1.V[0].y) {
            inleaf2 = true;
          }
        }
      }

      //l'oggetto e' caricato dentro l'array del box
      //figlio in cui si trova
      //nel caso che questo fosse presente in
      //entrambi i box, esso sara' caricato due volte
      if (inleaf1) {
        leaf1Obj.add(object);
      }
      if (inleaf2) {
        leaf2Obj.add(object);
      }
    }

    //una volta conosciuta la grandezza degli array dei
    //box figli possiamo crearli della giusta
    //dimensione abbandonando l'arrayList:questo
    //permette di effettuare una piu' efficente
    //gestione della memoria
    leaf1.objects = new ArrayList<>();
    //per ogni oggetto, salvo l'oggetto dell'arrayList
    //dentro a leaf1.objects
    leaf1.objects.addAll(leaf1Obj);

    //faccio lo stesso per leaf2
    leaf2.objects = new ArrayList<>();
    leaf2.objects.addAll(leaf2Obj);
  }
}
