import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class RenderAction implements Properties {
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

  // vettore per gli indici dei materiali delle sfere
  // si fa corrispondere alla sfera i-esima del vettore
  // spheres definito poco sotto, il materiale di
  // indice corrispondente nel vettore material
  private static int[] matIdSphere = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

  private static Sphere[] spheres = {	//vettore costruttore delle sfere
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
  static boolean doJacobi = false;
  static boolean doFinalGathering = false;
  static boolean doPhotonFinalGathering = false;
  static boolean doMultiPassPhotonMapping = false;

  //translucentJade=true se si vuole una
  //visualizzazione con BSSRDF
  private static boolean translucentJade=false;
  //diffusiveJade=true se vogliamo una giada
  //"diffusiva"
  private static boolean diffusiveJade=false;
  private static boolean glass=false;
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
  static Point3D max = null;
  static Point3D min = null;
  static Box bound;	//primo elemento della lista di Box (usato per la BSP)
  static int depth=14;	//profondita' dell'octree

  static int Kdepth = 17;

  //massimi box (cioe' massima profondita') nell'albero
  //per la partizione spaziale della scena
  static int maxPartitions = 0;

  //variabile globale in cui verranno salvati gli oggetti
  //della scena (in modo da poter essere aggionati nei
  //metodi richiamati)
  static ArrayList<Obj> globalObjects;

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
  static int samps;

  //campioni (numero di raggi) per l'illuminazione indiretta
  //(ricorsivo in global illumination, non ricorsivo in
  //final gathering)
  static int aoSamps;
  static int dirSamps;	//campioni (numero di raggi) illuminazione diretta (non ricorsivo)
  static int refSamps;	//campioni scelti per le riflessioni e le rifrazioni
  static int jacobiSamps;	//sample per lo Stochastic Jacobi, utilizzati per il calcolo con Monte Carlo

  static ArrayList<Photon> photons = new ArrayList<>();
  static ArrayList<Photon> caustics = new ArrayList<>();
  static PhotonBox[] KdTree;
  static PhotonBox[] causticTree;
  static int nPhoton = 100; // Sample per la massima ricorsività del photon mapping
  static int causticPhoton = 100; // Sample per la massima ricorsività del photon mapping
  static int aoCausticPhoton = 150; //Sample per la massima ricorsività del photon mapping
  static int projectionResolution = 300;  //Sample per la mappa di proiezione
  static float scaleCausticPower = 1; //scalamento di potenza del fotone

  //distanza al quadrato disco di ricerca dei fotoni
  static double photonSearchDisc;
  static double causticSearchDisc;

  static int power = 0;
  static int photonBoxNum = 0;

  //numero di fotoni da ricercare
  static int nPhotonSearch;
  static int nCausticSearch;

  static int steps;	//numero di step raggiunti dal processo Jacobi Stocastico
  static float err;	//stima dell'errore raggiunto dal processo Jacobi Stocastico
  static int maxSteps;	//step massimi per le iterazioni di Jacobi Stocastico
  static double maxErr;	//errore massimo nel processo di Jacobi Stocastico

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

  //parametro per la ricerca sferica su una faccia piana in [0,1]
  static double sphericalSearch = 1;

  private Renderer renderer;
  private Utilities utilities;

  RenderAction() {
    utilities = new Utilities();
    renderer = new Renderer(utilities);

    actionPerformed();
  }

  private void actionPerformed() {
    Main.editPanel.disableUI();

    jacobiSamps = Main.editPanel.jacobiPanel.getSamps();
    maxSteps = Main.editPanel.jacobiPanel.getMaxSteps();
    maxErr = Main.editPanel.jacobiPanel.getMaxErr();

    aoSamps = Main.editPanel.finalGatheringPanel.getAOSamples();
    dirSamps = Main.editPanel.finalGatheringPanel.getDirSamples();
    refSamps = Main.editPanel.finalGatheringPanel.getRefSamples();

    nPhoton = Main.editPanel.photonPanel.getPhotonNum();
    causticPhoton = Main.editPanel.photonPanel.getCausticNum();
    aoCausticPhoton = Main.editPanel.photonPanel.getAOCaustic();
    projectionResolution = Main.editPanel.photonPanel.getProjectionResolution();
    photonSearchDisc = Main.editPanel.photonPanel.getPhotonSearchDisc();
    nPhotonSearch = Main.editPanel.photonPanel.getPhotonSearchNum();
    causticSearchDisc = Main.editPanel.photonPanel.getCausticSearchDisc();
    nCausticSearch = Main.editPanel.photonPanel.getCausticSearchNum();

    Main.label.setText("Creazione immagine in corso");

    switch (Main.editPanel.getMethod()) {
      case JACOBI_PANEL:
        doJacobi = true;
        break;
      case FINAL_GATHERING_PANEL:
        doFinalGathering = true;
        break;
      case PHOTON_PANEL:
        doPhotonFinalGathering = true;
        break;
    }

    switch (Main.editPanel.getMaterial()) {
      case TRANSLUCENT_JADE:
        translucentJade = true;
        break;
      case DIFFUSIVE_JADE:
        diffusiveJade = true;
        break;
      case GLASS:
        glass = true;
        break;
    }

    switch (Main.editPanel.getPosition()) {
      case ALIGNED:
        aligned = true;
        break;
      case OVERLAPPED:
        aligned = false;
        break;
    }

    int mIS = setMatIdSphere();
    for(int sph = 0; sph < SPHERE_NUM; sph++)
      matIdSphere[sph]=mIS;

    Point3D sPos0 = Sphere.setSpheresPosition(0);
    Point3D sPos1 = Sphere.setSpheresPosition(1);
    Point3D sPos2 = Sphere.setSpheresPosition(2);
    //vettore costruttore delle sfere
    spheres[0] = new Sphere(1,sPos0);
    spheres[1] = new Sphere(1,sPos1);
    spheres[2] = new Sphere(1,sPos2);

    //dovendo disegnare 3 sfere e la stanza definisco un
    //array di 2 Mesh: nella prima delle due mesh (per
    //meshes[0]) aggiungiamo le 3
    //sfere richiamando il metodo caricaSphere
    meshes = new ArrayList<>(2);
    meshes.add(new Mesh(SPHERE_NUM, spheres, matIdSphere));

    //inizializzo massimo e minimo punto della scena

    //inizializzo dei fittizi massimo e minimo, che mi
    //serviranno per definire i valori di max e min
    Point3D oldMin=new Point3D(Float.POSITIVE_INFINITY);
    Point3D oldMax=new Point3D(Float.NEGATIVE_INFINITY);

    //trovo le dimensioni della scena (tralasciando la
    //stanza in cui gli oggetti sono contenuti)
    for (Mesh tmpObjects : meshes) {
      max = Obj.getBoundMax(tmpObjects.objects, oldMax);
      min = Obj.getBoundMin(tmpObjects.objects, oldMin);
    }

    //definisco e calcolo il punto in cui guarda
    //l'osservatore: il centro della scena
    Point3D center= (max.add(min)).multiplyScalar(0.5f).subtract(new Point3D(0, 0.8, 0));

    if(!absolutePos) {
      lookat = lookat.add(center);
    }

    //l'osservatore si trova nel punto camPosition
    Point3D camPosition = center.add(eye);
    //calcolo del punto di messa a fuoco pf
    Point3D pf = new Point3D();
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
    max=Obj.getBoundMax(meshes.get(meshes.size()-1).objects, oldMax);
    min=Obj.getBoundMin(meshes.get(meshes.size()-1).objects, oldMin);

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
    for(int i=1; i < depth; i++){
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
    utilities.inters= Utilities.inf;

    //richiamo la funzione per il calcolo della radiosita'
    //della scena attraverso il metodo di Jacobi
    //stocastico

    if(doJacobi) {
      renderer.jacobiStoc(objects.size());
    }

    //aggiorno la variabile locale sceneObjects con i
    //valori ora contenuti in globalObjects
    sceneObjects.clear();
    sceneObjects.addAll(globalObjects);

    // Aggiunta del photon mapping
    if (doPhotonFinalGathering || doMultiPassPhotonMapping) {
      renderer.calculatePhotonMapping();
    }

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

    /*
    //Stampiamo le varie informazioni
    System.out.println("Samples: "+ samps + " " + aoSamps);
    System.out.println("Origin: x "+cam.eye.x+" y "+
        cam.eye.y+" z "+cam.eye.z);
    System.out.println("Direction: x "+cam.lookAt.x+" y "+
        cam.lookAt.y+" z "+cam.lookAt.z);
    System.out.println("Up: x "+cam.up.x+" y "+cam.up.y+
        " z "+cam.up.z);
    System.out.println("D: "+cam.d+" W: "+cam.width+
        " H: "+cam.height);
    */

    cam.aperturaDiaframma = ap;// =0

    //l'istruzione seguente calcola il fattore di scala
    //con una trasformazione perospettica che porta
    //l'origine della posizione della fotocamera e il
    //vettore dell'osservatore al vettore della fotocamera
    cam.fuoco = (pf.z - cam.eye.z) / (cam.W.z*(-cam.d));

    System.out.println("Fuoco: " + cam.fuoco);
    System.out.println("Apertura diaframma: " + cam.aperturaDiaframma);

    renderer.calculateThreadedRadiance(cam);

    //Ora viene creata l'immagine
    createImage();
  }

  //metodo che imposta, a seconda della scelta
  //effettuata dall'utente, il materiale
  //appropriato alle prime tre sfere
  private int setMatIdSphere() {
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

  private void createImage() {
    //stringa contenente le informazioni da scrivere nel
    //file immagine image.ppm
    StringBuilder matrix = new StringBuilder();

    //iniziamo la stringa matrix con valori di settaggio
    //richiesti
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

    //Main.label.setText("Immagine completata!");

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
