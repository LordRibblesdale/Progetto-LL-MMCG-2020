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
  Point3D[] V=new Point3D[2];
  //il parametro lato ci dice il piano con cui il
  //box sara' diviso in due
  //corrispondenze piano:valore
  //xy:0 yz:1 xz:2    cioe'
  //lato=0 taglio con il piano z,
  //l=1 taglio con il piano x,
  //l=2 taglio con il piano y
  short lato=0;

  //array degli oggetti che il box contiene
  Obj[] objects;
  int nObj=0;

  //Box figli: i due "sottobox" in cui il box
  //iniziale viene diviso. Nel caso rimangano Null il
  //Box non ha nessun figlio
  Box leaf1;;
  Box leaf2;;

  //costruttore box
  public Box(Point3D min, Point3D max, short l){
      V[0]=min;
      V[1]=max;
      lato=l;
  }
    //settiamo gli oggetti contenuti nel box
  void setObjects(Obj[] o,int nO){
      objects=o;
      nObj=nO;
  }

  //in questo metodo si verifica se esiste
  //l'intersezione di un raggio con il box
  boolean intersect(Ray r){

  	//inizializziamo a + e - infinito i piani
  	//near e far
      float Tnear=Float.NEGATIVE_INFINITY;
      float Tfar=Float.POSITIVE_INFINITY;

      //array con le componenti della direzione del
      //raggio
      float d[]={r.d.x,r.d.y,r.d.z};
      //array con le componenti dell'origine del
      //raggio
      float o[]={r.o.x,r.o.y,r.o.z};

      //array con le componenti dei vertici del box
      //V[0] e V[1]
      float min[]={V[0].x,V[0].y,V[0].z};
      float max[]={V[1].x,V[1].y,V[1].z};

      //per ogni componente faccio vari controlli
      //per capire se c'e' intersezione:
      for(int i=0;i<3;i++){
      	//se nella direzione i non c'e' variazione
      	//e l'origine e' fuori dal box non puo'
      	//esserci intersezione
          if(d[i]==0){
              if((o[i]<min[i])||(o[i]>max[i])){
                  return false;
              }
          }
          else{
          	//si definiscono le intersezioni piu'
          	//vicina e piu' lontana
              float T1= (min[i] -o[i])/d[i];
              float T2= (max[i] -o[i])/d[i];
              //ordina dal piu' piccolo (T1) al piu'
              //grande (T2)
              if(T1>T2){
              	//swap(T1,T2);
              	float app=T2;
              	T2=T1;
              	T1=app;
              }
              //voglio il t vicino piu' grande
              if(T1>Tnear)
              	Tnear=T1;
              //voglio il t lontano piu' piccolo
              if(T2<Tfar)
              	Tfar=T2;

              //non c'e' intersezione tra i due
              //segmenti
              if(Tnear>Tfar){
              	return false;
              }

              //il raggio interseca nella direzione
              //opposta
              if(Tfar<0){
              	return false;
              }
             }
          }

      return true;
  }
 //in questo metodo si determinano quali
  //oggetti siano contenuti all'interno di uno dei
  //figli del box
  void setLeafObj(){

      //creazione di due arrayList (non uso array
  	//perche' non posso sapere dall'inizio quanti
  	//oggetti conterranno le suddivisioni del box)
  	ArrayList<Obj> leaf1_Obj = new ArrayList<Obj>();
  	ArrayList<Obj> leaf2_Obj = new ArrayList<Obj>();

      //per ogni oggetto presente nel box padre
      for(int i=0;i<nObj;i++){

      	//vogliamo scoprire in quale leaf e'
      	//l'oggetto i-esimo

          //flag primo figlio leaf1
          boolean inleaf1=false;
          //flag secondo figlio leaf2
          boolean inleaf2=false;

          //se l'oggetto e' un triangolo
          if(objects[i].t!=null){

              //si carica il triangolo
              Triangle t = objects[i].t;

              //per ogni vertice
              for (int j=0; j<3; j++){

                  //se il box e' stato dimezzato
              	//rispetto all'asse z
                  //cio' che discriminera' in quale
              	//leaf sta' il vertice e' la
              	//coordinata z (le altre restano
              	//uguali)
                  if(lato==0){

                  	if(t.vertices[j].z < leaf1.V[1].z){
                  		//il vertice e' nel box leaf1
                  		inleaf1=true;
                  	}
                  	else {
                          //il vertice e' nel box leaf2
                          inleaf2=true;
                      }
                  }
                  //se il box e' stato dimezzato rispetto
                  //all'asse x
                  if(lato==1){
                  	if(t.vertices[j].x<leaf1.V[1].x){
                  		inleaf1=true;
                  	}
                  	else {
                  		inleaf2=true;
                  	}
                  }
                  //se il box e' stato dimezzato rispetto
                  //all'asse y
                  if(lato==2){
                  	if(t.vertices[j].y<leaf1.V[1].y){
                  		inleaf1=true;
                  	}
                  	else {
                  		inleaf2=true;
                      }
                  }

              }
          }

          // se l'oggetto e' una sfera si procede
          //diversamente
          if(objects[i].s!=null){

              //si carica la sfera
              Sphere s = objects[i].s;

              //si carica il raggio della sfera
              float rad= s.rad;

              //se il box e' stato dimezzato rispetto
              //all'asse z
              if(lato==0){
              	//se mi trovo all'interno della leaf1
                  if(s.p.z-rad<leaf1.V[1].z){
              		inleaf1=true;
                  }
                  //se mi trovo all'interno della leaf2
                  if(s.p.z+rad>leaf1.V[0].z){
                  	inleaf2=true;
                  }
              }
              //se il box e' stato dimezzato rispetto
              //all'asse x
              if(lato==1){
              	if(s.p.x-rad<leaf1.V[1].x){
              		inleaf1=true;
                  }
                  if(s.p.x+rad>leaf1.V[0].x){
                  	inleaf2=true;
                  }
              }
              //se il box e' stato dimezzato rispetto
              //all'asse y
              if(lato==2){
              	if(s.p.y-rad<leaf1.V[1].y){
              		inleaf1=true;
                  }
                  if(s.p.y+rad>leaf1.V[0].y){
                  	inleaf2=true;
                  }
              }
         }

          //l'oggetto e' caricato dentro l'array del box
          //figlio in cui si trova
          //nel caso che questo fosse presente in
          //entrambi i box, esso sara' caricato due volte
          if(inleaf1) {
          	leaf1_Obj.add(objects[i]);
          }
          if(inleaf2) {
          	leaf2_Obj.add(objects[i]);
          }


      }

      //una volta conosciuta la grandezza degli array dei
      //box figli possiamo crearli della giusta
      //dimensione abbandonando l'arrayList:questo
      //permette di effettuare una piu' efficente
      //gestione della memoria
      int nO=(int)leaf1_Obj.size();
      leaf1.nObj=nO;
      leaf1.objects=new Obj[nO];

      //per ogni oggetto, salvo l'oggetto dell'arrayList
      //dentro a leaf1.objects
      for(int i=0; i<nO;i++){
          leaf1.objects[i]=leaf1_Obj.get(i);
      }
      //faccio lo stesso per leaf2
      nO=(int)leaf2_Obj.size();
      leaf2.nObj=nO;
      leaf2.objects=new Obj[nO];
      for(int i=0; i<nO;i++){
          leaf2.objects[i]=leaf2_Obj.get(i);
      }
  }
}
