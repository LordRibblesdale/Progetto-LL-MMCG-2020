import java.util.ArrayList;

//classe per definire una mesh: una raccolta di vertici, 
//lati e facce che definiscono la forma, quindi la
//modellazione solida di un oggetto poliedrico.
//Le facce di solito sono costituite da triangoli 
//(maglia triangolare), quadrilateri o altri poligoni 
//convessi semplici, poiche' cio' semplifica il rendering, 
//ma puo' anche essere composto da poligoni concavi piu' 
//generali e sfere. 
//Nel nostro caso considereremo solo sfere o triangoli.
//La classe contiene anche un metodo per settare il 
//materiale di una mesh e un metodo per spezzare in 
//due parti uguali ogni triangolo della mesh (e' ovvio che 
//il metodo non funziona per mesh di sfere) 
public class Mesh {
  Utilities utilities;
  
  //array di puntatori ad oggetti
  public ArrayList<Obj> objects;

  int[] matIdRoom = {	//vettore per gli indici dei materiali delle pareti della stanza
      3, //sinistra
      4, //inferiore
      13, //posteriore
      3, //destra
      4, //superiore
      4  //frontale
  };

  private Mesh() {
    utilities = new Utilities();
  }

  Mesh(ArrayList<Sphere> spheres, ArrayList<Integer> matIdSphere) {
    this();
    loadSphere(spheres, matIdSphere);
  }

  Mesh(Point3D max, Point3D min) {
    this();
    createScene(max, min);
  }

  // costruttore Mesh
  Mesh(ArrayList<Obj> o){
    //array di oggetti contenuti nella mesh
    objects=o;
  }

  //si crea la stanza con grandezza dipendente da max e min
  //per stanza si intende un ambiente parallelepipedico
  //avente pareti (composte da due triangoli per ciascuna
  //parete) di un materiale scelto tra quelli presenti
  //nell'array dei materiali, una luce nel nostro caso
  //collocata sul soffitto
  void createScene(Point3D max, Point3D min){
    //uso le variabili maxx e minn che hanno i valori di 
    //max e min per non aggiornarli dopo l'utilizzo del 
    //metodo
    Point3D maxx=new Point3D();
    maxx.copy(max);
    Point3D minn=new Point3D();
    minn.copy(min);
    
    double maxhroom = maxx.y + RenderAction.hroom;
    maxx.y = max.y;
    
    //definisco un array per gli 8 vertici della stanza
    //a max y aggiungo sempre hroom 
    //vertici: sinistra=min.x, destra=max.x; basso=min.y,
    //alto=max.y; dietro=min.z, davanti=max.z
    Point3D[] v ={
          //sinistra, in basso, dietro
    minn,
    //sinistra, in alto, dietro
    new Point3D(minn.x,maxhroom,minn.z),
    //sinistra, in basso, davanti
    new Point3D(minn.x,minn.y,maxx.z),
    //destra, in basso, dietro
    new Point3D(maxx.x,minn.y,minn.z),
    //destra, in alto, davanti
    new Point3D(maxx.x,maxhroom,maxx.z),
    //sinistra, in alto, davanti
    new Point3D(minn.x,maxhroom,maxx.z),
    //destra, in alto, dietro
    new Point3D(maxx.x,maxhroom,minn.z),
    //destra, in basso, davanti
    new Point3D(maxx.x,minn.y,maxx.z),
    };
  
    //Dal momento che frontWall=false, si creano 12 
    //triangoli per comporre la stanza: 10 per la stanza
    //(a cui manca appunto la faccia frontale per 
    //permettere di vedere all'interno) e 2 per la luce 
    //parallela al soffitto (perche' frontL=false)
    ArrayList<Triangle> Troom=new ArrayList<Triangle>();
    //array per gli oggetti che compongono la stanza
    objects = new ArrayList<>(12);
    
    //inseriamo la luce
    //c e' la posizione centrale della stanza
    Point3D c=((maxx.add(minn)).multiplyScalar(
            0.5f));
    //dichiaro l'array Lv che serve per i vertici della 
    //luce e lo inizializzo a (0,0,0)
    Point3D[] Lv=new Point3D[8];
    for(int i=0; i<8; i++) {
      Lv[i]=new Point3D();
    }
    
    for(int i=0; i<8; i++) {  //dilatazione della luce
      Lv[i].copy(v[i]);  //copio in Lv i vertici della stanza
      //faccio poi le dovute dilatazioni nella x
      //Lv[i].x=Lv[i].x+(Lv[i].x-c.x)*scaleL.x;
      
      //frontL e' false quindi entra in questo if (la 
      //luce non e' frontale): si fanno quindi le 
      //dovute dilatazioni nella z  
      
      /*
      if(!Main.frontL){
        //Lv[i].z=Lv[i].z+(Lv[i].z-c.z)*scaleL.z;
      }
        else{
            if( (i==4 )|| (i==5) ){
                Lv[i].y=Lv[i].y+hroom*scaleL.y;
            }
        }
      */
  }
  
    //anche in questo caso copio i valori in delle 
    //variabili che non si aggiornano
    Point3D translateLL;
    Point3D Lv1=new Point3D();
    Lv1.copy(Lv[1]);
    Point3D Lv4=new Point3D();
    Lv4.copy(Lv[4]);
    Point3D Lv5=new Point3D();
    Lv5.copy(Lv[5]);
    Point3D Lv6=new Point3D();
    Lv6.copy(Lv[6]);
  
    if(!RenderAction.frontL){
      //definisco i vertici dei triagoli della stanza, 
      //traslandoli sulla y del valore 
      //utilities.EPS=0.01f per evitare di avere
      //problemi di aliasing dati dal linee 
      //perfettamente dritte nella fase di rendering
      translateLL = new Point3D(0, -Utilities.EPSILON, 0);
      Point3D Lv1tr = (Lv1).add(translateLL);
      Point3D Lv4tr = (Lv4).add(translateLL);
      Point3D Lv5tr = (Lv5).add(translateLL);
      Point3D Lv6tr = (Lv6).add(translateLL);
      //si creano e si aggiungono i triangoli per la 
      //luce
      Triangle Tr0=new Triangle(Lv4tr,Lv5tr,Lv6tr);
      Troom.add(0,Tr0);
      Triangle Tr1=new Triangle(Lv1tr,Lv6tr,Lv5tr);
      Troom.add(1,Tr1);
    }
  
    //dilatazione delle pareti
    for (int i = 0; i < 8; i++) {
      v[i].x=v[i].x+(v[i].x-c.x)*1.5f;
      v[i].z=v[i].z+(v[i].z-c.z)*1.5f;
    }
  
    //si creano e si aggiungono i triangoli per le 
    //pareti
    //faccia laterale sinistra
    Triangle Tr2=new Triangle(v[0],v[1],v[2]);
    Troom.add(2,Tr2);
    Triangle Tr3=new Triangle(v[5],v[2],v[1]);
    Troom.add(3,Tr3);
    //faccia inferiore
    Triangle Tr4=new Triangle(v[0],v[2],v[3]);
    Troom.add(4,Tr4);
    Triangle Tr5=new Triangle(v[7],v[3],v[2]);
    Troom.add(5,Tr5);
    //faccia posteriore
    Triangle Tr6=new Triangle(v[0],v[3],v[1]);
    Troom.add(6,Tr6);
    Triangle Tr7=new Triangle(v[6],v[1],v[3]);
    Troom.add(7,Tr7);
    //faccia laterale destra
    Triangle Tr8=new Triangle(v[4],v[6],v[7]);
    Troom.add(8,Tr8);
    Triangle Tr9=new Triangle(v[7],v[6],v[3]);
    Troom.add(9,Tr9);
    //faccia superiore
    Triangle Tr10=new Triangle(v[4],v[5],v[6]);
    Troom.add(10,Tr10);
    Triangle Tr11=new Triangle(v[1],v[6],v[5]);
    Troom.add(11,Tr11);
  
    //imposto i materiali della stanza
    objects.add(new Obj(Troom.get(0), RenderAction.matIdL));
    objects.add(new Obj(Troom.get(1), RenderAction.matIdL));
    objects.add(new Obj(Troom.get(2), matIdRoom[0]));
    objects.add(new Obj(Troom.get(3), matIdRoom[0]));
    objects.add(new Obj(Troom.get(4), matIdRoom[1]));
    objects.add(new Obj(Troom.get(5), matIdRoom[1]));
    objects.add(new Obj(Troom.get(6), matIdRoom[2]));
    objects.add(new Obj(Troom.get(7), matIdRoom[2]));
    objects.add(new Obj(Troom.get(8), matIdRoom[3]));
    objects.add(new Obj(Troom.get(9), matIdRoom[3]));
    objects.add(new Obj(Troom.get(10), matIdRoom[4]));
    objects.add(new Obj(Troom.get(11), matIdRoom[4]));

    //viene resituita una mesh che contiene i triangoli
    //che costituiscono la stanza e la luce

  }

  void loadSphere(ArrayList<Sphere> spheres, ArrayList<Integer> matIdSphere) {
    //crea una mesh costituita da n sfere
    //creo un array di Obj di n elementi
      objects = new ArrayList<>();

      //per ogni elemento creo un oggetto che ha l'elemento
      //i-esimo dell'array di Sphere[] spheres e l'elemento
      //i-esimo di int[] matIdSphere (che considerera'
      //l'i-esimo materiale)
      for(int i=0; i < spheres.size(); i++) {
        objects.add(new Obj(spheres.get(i), matIdSphere.get(i)));
      }

      //viene restituita una mesh delle sfere create
  }

  //metodo per impostare il materiale degli oggetti 
  //presenti nella mesh
  
  void setMaterial(int m){
    for (Obj object : objects) {
      object.setMaterial(m);
    }
  }
    
  //funzione valida solo per Mesh di triangoli
  //il metodo suddivide i triangoli della Mesh in due,
  //duplicando quindi il numero di triangoli della Mesh
  //nel nostro caso sceneDepth=0, quindi non si usa mai
  //questo metodo
  void splitMeshes(){
    //nuovo array dei triangoli
    ArrayList<Obj> objects2 = new ArrayList<>(objects.size()*2);
    for (Obj object : objects) {
      //valido solamente su triangoli
      if (object.t != null) {
        //carico i vertici del triangolo
        Point3D[] v = object.t.vertices;
        //creo i lati del triangolo
        Point3D l[] = {v[2].subtract(v[0]),
            v[0].subtract(v[1]), v[1].
            subtract(v[2])};
        //cerco il lato piu' lungo del triangolo
        int pos = 0;
        if (l[1].normalize() > l[pos].normalize()) pos = 1;
        if (l[2].normalize() > l[pos].normalize()) pos = 2;

        //cerco il punto a meta' del lato piu'
        //lungo del triangolo
        float val = (float) (l[pos].normalize() * 0.5);
        //normalizzo il lato piu' lungo
        l[pos].getNormalizedPoint();
        //carico i nuovi vertici del triangolo
        Point3D[] nv = {v[(pos + 1) % 3], v[(pos + 2) % 3],
            v[pos], l[pos].multiplyScalar(val).
            add(v[pos])};
        //creo i nuovi due triangoli
        objects2.add(new Obj(new Triangle(nv[0], nv[1], nv[3]), object.matId));
        objects2.add(new Obj(new Triangle(nv[0], nv[3], nv[2]), object.matId));
      }
    }

    //aggiorno l'array degli oggetti e il numero di
    //oggetti della mesh
    objects= objects2;
  }
}
