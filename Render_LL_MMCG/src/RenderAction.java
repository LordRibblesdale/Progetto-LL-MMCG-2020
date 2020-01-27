import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class RenderAction extends AbstractAction {
  //vettore costruttore dei materiali
    static Material[] material = {
            //luce
            StandardMaterial.MATERIAL_LIGHT_WHITE,

            //diffusivi:
            StandardMaterial.MATERIAL_DIFFUSIVE_RED,
            StandardMaterial.MATERIAL_DIFFUSIVE_GREEN,
            StandardMaterial.MATERIAL_DIFFUSIVE_BLUE,
            StandardMaterial.MATERIAL_DIFFUSIVE_GRAY,
            StandardMaterial.MATERIAL_DIFFUSIVE_BLACK,

            //riflettenti:
            StandardMaterial.MATERIAL_REFLECTIVE_GLASS,
            StandardMaterial.MATERIAL_REFLECTIVE_PERFECT_GLASS,

            //materiali particolari:
            StandardMaterial.MATERIAL_COOK_TORRANCE_VIOLET,
            StandardMaterial.MATERIAL_STEEL,
            StandardMaterial.MATERIAL_DEEP_RED,
            StandardMaterial.MATERIAL_IMPERFECT_STEEL,
            StandardMaterial.MATERIAL_TRANSLUCENT_JADE,
            StandardMaterial.MATERIAL_DIFFUSIVE_PINK,
            StandardMaterial.MATERIAL_DIFFUSIVE_DEEP_GRAY,
            StandardMaterial.MATERIAL_DIFFUSIVE_JADE
    };

  //static int mIS=setMatIdSphere();
  // vettore per gli indici dei materiali delle sfere
  // si fa corrispondere alla sfera i-esima del vettore
  // spheres definito poco sotto, il materiale di
  // indice corrispondente nel vettore material
  static int[] matIdSphere ={
    1,//mIS,
    1,//mIS,
    1,//mIS,
    1,//7,
    1,//7,
    1,//7,
    1,//4,
    1,//4,
    1,//4,
    1,//4
  };

  static Sphere[] spheres = {	//vettore costruttore delle sfere
      new Sphere(1,new Point3D()),
      new Sphere(1,new Point3D()),
      new Sphere(1,new Point3D()),
      new Sphere(1,new Point3D(4.0f,0.1f,1.4f)),
      new Sphere(1,new Point3D(-3.0f,0.4f,2.3f)),
      new Sphere(1,new Point3D(-4.0f,2.0f,4.0f)),
      new Sphere(1,new Point3D(6.0f,5.3f,2.0f)),
      new Sphere(1,new Point3D(7.0f,3.4f,1.4f)),
      new Sphere(1,new Point3D(-7.0f,4.0f,2.1f)),
      new Sphere(1,new Point3D(-4.0f,0.9f,0.5f))
  };
  //metodo: scegliere una delle due flag per
  //visualizzare il rendering con il metodo di Jacobi
  //stocastico (doJacobi) o con il final gathering(doFinalGathering)
  static boolean doJacobi = true;
  static boolean doFinalGathering = false;
  //translucentJade=true se si vuole una
  //visualizzazione con BSSRDF
  static boolean translucentJade=false;
  //diffusiveJade=true se vogliamo una giada
  //"diffusiva"
  static boolean diffusiveJade=false;
  static boolean glass=false;
  static boolean aligned=false;

  static final int SPHERE_NUM = 3;	//numero delle sfere effettivamente considerate tra quelle definite in spheres[]
  ArrayList<Mesh> meshes;	//array di mesh della scena

  //parametri della fotocamera con sistema di riferimento centrato nella scena:
  //absolutePos e' vero se stiamo guardando proprio al centro della scena
  static boolean absolutePos=false;

  //punto guardato rispetto al centro della scena (0,0,0)
  //inizialmente coincide  con il centro ma poiche'
  //absolutePos=false lo cambieremo in seguito
  static Point3D lookat=new Point3D(0.0f);

  //punto in cui e' posizionata la fotocamera rispetto al
  //centro della scena (0,0,0)
  static Point3D eye=new Point3D(0.0f,2.0f,12.0f);

  //punto messo a fuoco rispetto al centro della scena
  //(0,0,0): inizialmente coincide  con il centro ma lo
  //cambieremo in seguito
  static Point3D focusPoint=new Point3D(0.0f);

  static int w=1080;	//larghezza dell'immagine
  static int h=720;	//altezza dell'immagine

  static float distfilm=700.0f;	//distanza dal centro della fotocamera al viewplane
  static float ap=0;	//apertura diaframma della fotocamera

  static int sceneDepth= 0;	//densita' triangoli nella stanza
  static ArrayList<Obj> lights;

  //liv e' il livello di profondita' all'interno dell'albero
  //per la partizione spaziale della scena
  static int depthLevel = 0;

  /// vettore in cui carichero' le luci (sono dei semplici
  //Obj che hanno pero' come materiale una luce)
  static Box bound;	//primo elemento della lista di Box (usato per la BSP)
  static int depth=14;	//profondita' dell'octree

  //massimi box (cioe' massima profondita') nell'albero
  //per la partizione spaziale della scena
  static int maxPartitions = 0;

  //variabile globale in cui verranno salvati gli oggetti
  //della scena (in modo da poter essere aggionati nei
  //metodi richiamati)
  static ArrayList<Obj> globalObjects;

  //variabile globale, utilizzata nel metodo intersect(),
  //in cui viene salvato l'oggetto intersecato da un
  //raggio considerato
  static Obj intersObj;

  //variabile globale, utilizzata nel metodo intersect(),
  //in cui viene salvato punto di intersezione tra l'oggetto
  //e il raggio considerato
  static float inters;//sarebbe t del metodo intersect;
  static float inf=(float) 1e20;	//distanza massima dal raggio

  static int[] samplesX=new int[w*h];	//campioni per la fotocamera
  static int[] samplesY=new int[w*h];

  static int[] aoSamplesX=new int[w*h];	//campioni per la luce indiretta
  static int[] aoSamplesY=new int[w*h];

  static int[] dirSamples1=new int[w*h];	//campioni per la luce diretta
  static int[] dirSamples2=new int[w*h];
  static int[] dirSamples3=new int[w*h];

  static int[] refSamples1=new int[w*h];	//campioni riflessioni/rifrazioni
  static int[] refSamples2=new int[w*h];

  static Point3D[] image = new Point3D[w*h];	//array che contiene tutti i pixel (rgb) dell'immagine
  static Point3D background = new Point3D(1.0f,1.0f,1.0f);	// Background: lo impostiamo come nero

  //campioni del pixel: numero di campioni per ogni pixels.
  //Si tratta dei punti nei pixel attraverso cui faremo
  //passare raggi
  static int samps=30;

  //campioni (numero di raggi) per l'illuminazione indiretta
  //(ricorsivo in global illumination, non ricorsivo in
  //final gathering)
  static int aosamps=1;

  static int dirsamps=1;	//campioni (numero di raggi) illuminazione diretta (non ricorsivo)
  static int refSample=100;	//campioni scelti per le riflessioni e le rifrazioni
  static int Jacobisamps=150000;	//sample per lo Stochastic Jacobi, utilizzati per il calcolo con Monte Carlo
  static int steps;	//numero di step raggiunti dal processo Jacobi Stocastico
  static float err;	//stima dell'errore raggiunto dal processo Jacobi Stocastico
  static int maxsteps=15;	//step massimi per le iterazioni di Jacobi Stocastico
  static float maxerr=0.001f;	//errore massimo nel processo di Jacobi Stocastico

  //soglia dei triangoli dentro ad un box se ce ne sono di
  //meno si ferma la partizione; si puo' regolare in base
  //al numero totale dei triangoli della scena
  static int sogliaBox=4;

  //nRay indica il numero di rimbalzi all'interno della
  //scena; e' una variabile globale in modo da potersi
  //aggiornare all'interno del metodo radiance()
  static int nRay;

  //peso che gestisce la distanza delle pareti dall'oggetto:
  //1 se distanza = diametro oggetto
  static float scaleX=1.5f;
  static float scaleZ=1.5f;

  static float hroom=1.2f;	//altezza da aggiungere alla stanza

  static int matIdL=0;	//indice del materiale della luce della stanza

  //se true la luce e' frontale. Se si cambia in true,
  //deccommentare le parti in createScene()
  static boolean frontL=false;

  //variabile utilizzata per visualizzare lo stato di
  //caricamento dei box durante la partizione spaziale
  //della scena
  static int loadedBoxes = 0;	//rappresenta i box che sono stati caricati

  private static int[] bool;

  private Renderer renderer;

  RenderAction(int[] bool) {
    RenderAction.bool = bool;

    renderer = new Renderer();
  }

  public void actionPerformed(ActionEvent e) {
    Main.ok_button.setEnabled(false);
    Main.tf.setText("Creazione immagine in corso");

    //TODO: optimise here
    //Metodo
    if(bool[0]==1)
      doFinalGathering =true;
    else if(bool[0]==0)
      doFinalGathering =false;

    //Materiale
    if(bool[1]==1)
      translucentJade=true;
    else if(bool[1]==0)
      translucentJade=false;
    if(bool[2]==1)
      diffusiveJade=true;
    else if(bool[2]==0)
      diffusiveJade=false;
    if(bool[3]==1)
      glass=true;
    else if(bool[3]==0)
      glass=false;

    //Posizione
    if(bool[4]==1)
      aligned=true;
    else if(bool[4]==0)
      aligned=false;


    int mIS=setMatIdSphere();
    for(int sph = 0; sph< SPHERE_NUM; sph++)
      matIdSphere[sph]=mIS;

    Point3D sPos0= Sphere.setSpheresPosition(0);
    Point3D sPos1= Sphere.setSpheresPosition(1);
    Point3D sPos2= Sphere.setSpheresPosition(2);
    //vettore costruttore delle sfere
    spheres[0]=new Sphere(1,sPos0);
    spheres[1]=new Sphere(1,sPos1);
    spheres[2]=new Sphere(1,sPos2);

    //dovendo disegnare 3 sfere e la stanza definisco un
    //array di 2 Mesh: nella prima delle due mesh (per
    //meshes[0]) aggiungiamo le 3
    //sfere richiamando il metodo caricaSphere
    meshes = new ArrayList<>(2);
    meshes.add(new Mesh(SPHERE_NUM, spheres, matIdSphere));

    //inizializzo massimo e minimo punto della scena
    Point3D max = null;
    Point3D min = null;
    //inizializzo dei fittizi massimo e minimo, che mi
    //serviranno per definire i valori di max e min
    Point3D oldMin=new Point3D(Float.POSITIVE_INFINITY);
    Point3D oldMax=new Point3D(Float.NEGATIVE_INFINITY);

    //trovo le dimensioni della scena (tralasciando la
    //stanza in cui gli oggetti sono contenuti)
    for(int i = 0; i < meshes.size(); i++) {
      Mesh tmpObjects = meshes.get(i);
      max=Obj.getBoundMax(tmpObjects.objects, oldMax);
      min=Obj.getBoundMin(tmpObjects.objects, oldMin);
    }

    //definisco e calcolo il punto in cui guarda
    //l'osservatore: il centro della scena
    Point3D center= (max.add(min)).multiplyScalar(0.5f);

    if(!absolutePos)
    {
      lookat = lookat.add(center);
    }

    //l'osservatore si trova nel punto camPosition
    Point3D camPosition= center.add(eye);
    //calcolo del punto di messa a fuoco pf
    Point3D pf = new Point3D();
    //pf= center+(0,0,0)
    pf.copy(center.add(focusPoint));

    //costruttore della fotocamera
    //imposto la fotocamera che guarda il centro
    //dell'oggetto ed e' posizionata davanti
    Camera cam = new Camera(camPosition, lookat, new Point3D(0.00015f,1.00021f,0.0f), w, h, distfilm);

    //Abbiamo ora a disposizione tutti gli elementi
    //necessari per costruire la stanza
    //Creo la stanza in cui mettere l'oggetto (per
    //visualizzare l'illuminazione globale)
    //La carico come ultima mesh
    meshes.add(new Mesh(max,min));

    //nel nostro caso sceneDepth=0, quindi non si entra
    //mai in questo ciclo
    for(int q=0;q < sceneDepth; q++) {
      meshes.get(meshes.size()-1).splitMeshes();
    }

    //A questo punto consideriamo l'intero array di mesh,
    //ora composto da oggetti+stanza e aggiorno i valori
    //della grandezza della stanza, usando di nuovo i
    //metodi getBoundMin e getBoundMax.
    oldMax=max;
    oldMin=min;
    max=Obj.getBoundMax(meshes.get(meshes.size()-1).objects,oldMax);
    min=Obj.getBoundMin(meshes.get(meshes.size()-1).objects,oldMin);

    //vettore che conterra' gli oggetti della scena
    ArrayList<Obj> objects = new ArrayList<>();
    ArrayList<Obj> sceneObjects = new ArrayList<>();
    //vettore che contiene solo le luci della scena
    lights = new ArrayList<>();

    for (Mesh tmpMesh : meshes) {
      //e carico tutto nella lista globale degli oggetti
      for (int j = 0; j < tmpMesh.objects.size(); j++) {
        objects.add(tmpMesh.objects.get(j));
        sceneObjects.add(tmpMesh.objects.get(j));
        //se l'oggetto e' una luce la carico dentro
        //l'array delle luci
        if (material[tmpMesh.objects.get(j).matId].emittedLight.max() > 0) {
          lights.add(tmpMesh.objects.get(j));
        }
      }
    }

    //costruzione del Kd-tree che ripartisce la scena

    //l=0: iniziamo a partizionare col piano xy
    short l=0;
    //liv e' il livello di profondita' all'interno
    //dell'albero
    depthLevel =0;

    //creo il Bounding Box
    //Bound e' il primo elemento dell'albero che contiene
    //tutti gli oggetti della scena
    bound = new Box(min, max, l);

    bound.setObjects(objects);

    //inizializzo la variabile S che e' la massima
    //profondita' dell'albero
    for(int i=1; i<depth; i++){
      maxPartitions +=Math.pow(2,i);
    }

    //crea il tree: si richiama il metodo setPartition()
    //per dividere gli oggetti del box padre nei box figli
    bound = Box.setPartition(bound);

    //salviamo gli oggetti della scena nella variabile
    //globale globalObjects in modo da poterli aggiornare
    //in JacobiStoc()
    globalObjects = new ArrayList<>();
    globalObjects.addAll(sceneObjects);

    //la definizione di inters mi serve per il metodo
    //intersectBPS che utilizza questa variabile
    inters=inf;

    //richiamo la funzione per il calcolo della radiosita'
    //della scena attraverso il metodo di Jacobi
    //stocastico
    if(!doFinalGathering)
      renderer.jacobiStoc(objects.size());

    //aggiorno la variabile locale sceneObjects con i
    //valori ora contenuti in globalObjects
    //sceneObjects.
    sceneObjects.clear();
    sceneObjects.addAll(globalObjects);

    //nota: rand() in C++ e' un numero random tra 0 e
    //RAND_MAX=2147483647: qua usero'
    //Math.random() * (fine-iniz+1)) + iniz
    //cioe' Math.random()* (2147483648)
    for (int i = 0; i < (w*h); i++) {
      //creiamo i campioni necessari per:

      //la fotocamera
      samplesX[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);
      samplesY[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);

      //la luce indiretta
      aoSamplesX[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);
      aoSamplesY[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);

      //la luce diretta
      dirSamples1[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);
      dirSamples2[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);
      dirSamples3[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);

      //riflessioni/rifrazioni
      refSamples1[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);
      refSamples1[i] = (int) (Math.random()*(Integer.MAX_VALUE) +1);

      //inizializzo l'immagine nera
      image[i]=new Point3D();
    }

    //Stampiamo le varie informazioni
    System.out.println("Samples: "+samps+" "+aosamps);
    System.out.println("Origin: x "+cam.eye.x+" y "+
        cam.eye.y+" z "+cam.eye.z);
    System.out.println("Direction: x "+cam.lookAt.x+" y "+
        cam.lookAt.y+" z "+cam.lookAt.z);
    System.out.println("Up: x "+cam.up.x+" y "+cam.up.y+
        " z "+cam.up.z);
    System.out.println("D: "+cam.d+" W: "+cam.width+
        " H: "+cam.height);

    cam.aperturaDiaframma = ap;// =0

    //l'istruzione seguente calcola il fattore di scala
    //con una trasformazione perospettica che porta
    //l'origine della posizione della fotocamera e il
    //vettore dell'osservatore al vettore della fotocamera
    cam.fuoco = (pf.z - cam.eye.z) / (cam.W.z*(-cam.d));

    System.out.println("Fuoco: "+cam.fuoco);
    System.out.println("Apertura diaframma: "+cam.aperturaDiaframma);

    //per tutte le righe
    for(int y = 0; y <= h; y++) {
      //stampiamo la percentuale di completamento per
      //monitorare l'avanzamento del rendering
      double percentY = ((float)y / (float)h) * 100;
      System.out.println("percentuale di completamento " + "radianza:	 "+percentY);

      //per tutte le colonne
      for(int x = 0; x <= w; x++)
      {
        // Ora siamo nel pixel
        // r e' la radianza: in questo caso e' tutto nero
        // Radianza della scena
        Point3D sceneRadiance = new Point3D(0.0f);

        // Loop per ogni campione
        for (int s = 0; s < samps; s++) {
          //inizializiamo un raggio per la camera
          Ray cameraRay;

          //transformazione delle variabili x e y in
          //float corrispondono alla posizione che
          //cameraRay deve raggiungere
          float raster_x = (float)x;
          float raster_y = (float)y;

          //origine del raggio della fotocamera
          Point3D origin=new Point3D();
          origin.copy(cam.eye);

          //se ho piu' di un campione allora
          //distribuisco gli altri campioni in modo
          //casuale
          if (s > 0) {
            float rndX=0;
            float rndY=0;

            //utilizzo questa variabile tt perche' non
            //posso usare il valore x+y*w nell'array
            //samplesX[], altrimenti l'ultimo indice
            //sarebbe fuori dal range (ricordo che la
            //misura e' w*h ma gli indici vanno da 0 a
            //w*h-1)
            int tt =x+y*w;
            //allora faccio l'if per tt<w*h cosi' da
            //accertarmi che non sia considerato l'indice
            //w*h-esimo
            if(tt<w*h)
            {
              // gli passo il numero random da cui
              //siamo partiti all'interno del pixel
              rndX = Utilities.generateRandom(samplesX[tt]);
              rndY = Utilities.generateRandom(samplesY[tt]);
            }

            //prendiamo un punto a caso su un disco di
            raster_x += Math.cos(2 * Utilities.MATH_PI *
                rndX)*cam.aperturaDiaframma*rndY;
            raster_y += Math.sin(2 * Utilities.MATH_PI *
                rndX)*cam.aperturaDiaframma*rndY;
            Point3D camUFuoco=cam.U.multiplyScalar(cam.
                fuoco*(x - raster_x));
            Point3D camVFuoco=cam.V.multiplyScalar(cam.
                fuoco*(y - raster_y));

            origin = origin.add(camUFuoco).
                add(camVFuoco);
          }

          // prediamo la direzione della fotocamera
          Point3D ray_direction;
          //ray_direction e' calcolato con l'ONB(base
          //ortonormale) della fotocamera
          //il raggio dalla fotocamera al campione sara'
          //data dalla combinazione lineare dell'ONB
          //della fotocamera
          //centro il piano rispetto alla fotocamera
          //sottraendo w/2 alla componente in x e h/2
          //alla componente in y infine la distanza z
          //tra la fotocamera e il piano e' cam.d

          //ray_direction=U*(raster_x-w/2)+
          //+V*(raster_y-h/2)+W*(-cam.d)
          ray_direction = (cam.U.multiplyScalar(
              raster_x - 0.5f*w)).add(
              cam.V.multiplyScalar(raster_y -
                  0.5f*h)).add(cam.W.
              multiplyScalar(-cam.d));
          ray_direction=ray_direction.getNormalizedPoint();

          //Ora si crea il raggio della fotocamera
          cameraRay = new Ray(origin, ray_direction);

          //dichiaro e inizializzo la variabile t in cui
          //salveremo il punto di intersezione fra
          //l'oggetto considerato  e cameraRay
          float t = inf;
          //inizializzo a null l'oggetto intersecato
          //dal raggio
          Obj o=null;
          //intersezione del raggio con gli elementi
          //della scena:
          if(Utilities.intersect(cameraRay, o)) {
            //pongo t uguale al valore di intersezione
            //memorizzato nella variabile globale inters
            t=inters;
            //resetto inters uguale a inf in modo da
            //avere il giusto valore di partenza la
            //prossima volta che si utilizzera'
            //il metodo intersect()
            inters=inf;
            //salvo nella variabile o objX l'elemento
            //intersecato dal raggio cameraRay
            o=intersObj;
            //resetto intersObj=null in modo da avere
            //il giusto valore di partenza la prossima
            //volta che si utilizzera' il metodo
            //intersect()
            intersObj=null;
            //si calcola il punto di intersezione
            Point3D iP = (cameraRay.o).add(
                cameraRay.d.multiplyScalar(t));
            //viene creato il primo raggio per il
            //calcolo della radianza
            //questo raggio parte dal punto ed e'
            //diretto verso l'osservatore
            Ray first=new Ray(iP, (cameraRay.d).
                multiplyScalar(-1));
            //si aggiunge alla variabile r il contributo
            //di radianza del punto considerato
            sceneRadiance = sceneRadiance.add(renderer.radiance(first, o, x, y));
          }
          //se non si interseca nessun oggetto si
          //aggiunge alla variabile r il colore di
          //background (nero)
          else
          {
            sceneRadiance = sceneRadiance.add(background);
          }
        }
        //divido per il numero di campioni del pixel
        sceneRadiance = sceneRadiance.divideScalar((float)samps);
        sceneRadiance.multiplyScalar(0.3f);
        // A questo punto si crea un'immagine basata sui
        //valori di radianza r

        //le componenti RGB del vettore r vengono tagliate
        //se non comprese in [0,1] dopodiche' vengono
        //caricate nel vettore image
        //nota: per ogni y che aumenta abbiamo gia'
        //caricato w pixel

        //utilizzo questa variabile tt perche' non posso
        //usare il valore x+y*w nell'array image[w*y],
        //altrimenti l'ultimo indice sarebbe fuori dal
        //range (ricordo che la misura e' w*h ma gli
        //indici vanno da 0 a w*h-1)
        int tt =x+y*w;
        //allora faccio l'if per tt<w*h cosi' da
        //accertarmi che non sia considerato l'indice
        //w*h-esimo
        if(tt<w*h)
        {
          image[x+y*w].x = Point3D.clamp(sceneRadiance.x);
          image[x+y*w].y = Point3D.clamp(sceneRadiance.y);
          image[x+y*w].z = Point3D.clamp(sceneRadiance.z);
        }
      }
    }

    //Ora viene creata l'immagine
    createImage();
  }

  //metodo che imposta, a seconda della scelta
  //effettuata dall'utente, il materiale
  //appropriato alle prime tre sfere
  int setMatIdSphere() {
    int translucentJadeIndex=12;
    int diffusiveJadeIndex=15;
    int glassIndex=7;
    int one=1;

    if(translucentJade)
      return translucentJadeIndex;
    else if(diffusiveJade)
      return diffusiveJadeIndex;
    else if(glass)
      return glassIndex;

    return one;
  }

  void createImage() {
    //stringa contenente le informazioni da scrivere nel
    //file immagine image.ppm
    //String matrix="";
    StringBuilder matrix = new StringBuilder();

    //iniziamo la stringa matrix con valori di settaggio
    //richiesti
    //matrix +="P3\n" + w + "\n" + h + "\n255\n";
    matrix.append("P3\n").append(w).append("\n").append(h).append("\n255\n");

    //Ora si disegna l'immagine: si procede aggiungendo
    //alla stringa matrix le informazioni contenute
    //nell'array image in cui abbiamo precedentemente
    //salvato tutti i valori di radianza
    for(int i = 0; i <w*h; i++) {
      //stampiamo la percentuale di completamento per
      //monitorare l'avanzamento della creazione
      //dell'immagine
      double percent = ((float)i / (float)(w*h)) * 100;
      double percentFloor=Math.floor(percent);
      double a=percent-percentFloor;
      if(a==0.0)
      {
        System.out.println("percentuale di completamento "
            + "immagine: "+percent);
      }

      //StringBuilder matrix

      //i valori di radianza devono essere trasformati
      //nell'intervallo [0,255] per rappresentare la
      //gamma cromatica in valori RGB
      matrix.append(Utilities.toInt(image[i].x)).append(" ").append(Utilities.toInt(image[i].y)).append(" ").append(Utilities.toInt(image[i].z)).append("  ");
    }

    Main.tf.setText("Immagine completata!");

    //nome del file in cui si andra' a salvare l'immagine
    //di output
    String filename = "image.ppm";

    //Per finire viene scritto il file con con permesso
    //di scrittura e chiuso.
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(filename);
      fos.write(matrix.toString().getBytes());
      fos.close();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}
