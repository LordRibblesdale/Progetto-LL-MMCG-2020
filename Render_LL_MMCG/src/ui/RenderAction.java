package ui;

import partition.Box;
import partition.PhotonBox;
import primitive.*;
import renderer.JacobiStocClass;
import renderer.PhotonScatterClass;
import renderer.Utilities;
import renderer.Renderer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static renderer.PhotonMappingClass.calculatePhotonMapping;

/* Classe nata per gestire l'intero processo del rendering.
 * Inizializza le variabili necessarie e reindirizza ai vari
 *  metodi di rendering in base alle scelte dell'utente.
 *
 * Il principale problema consiste nell'accesso a questa
 *  classe, da parte delle altre classi, tramite accessi
 *  statici. In aggiunta, si inizializzano i materiali tramite
 *  un array material da questa classe, il che richiede ogni
 *  volta l'accesso a RenderAction.
 */

public class RenderAction implements Properties, ModelerProperties {
  // Vettore dei materiali, per i modelli predefiniti di scena
    public static Material[] material = {
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

  /* Controlli booleani per accedere ai vari metodi per la scelta del
   *  tipo di rendering da effettuare
   *  -> Jacobi stocastico
   *  -> Final Gathering
   *  -> Photon Mapping
   */
  public static boolean doJacobi = false;
  public static boolean doFinalGathering = false;
  public static boolean doPhotonMapping = false;

  /* Queste variabili assegnano solo agli oggetti predefiniti
   *  (e quelli aggiunti nel modellatore) lo stesso tipo di materiale
   *   senza poter scegliere il tipo di materiale
   * Può essere corretto creando una variabile di controllo nell'oggetto (Material)
   *  dato che il renderer.Renderer già è capace di determinare
   *  le proprietà del materiale selettivamente (per la presenza della variabile matId nell'oggetto)
   */
  // Giada traslucente
  private static boolean translucentJade = false;
  // Giada diffusiva
  private static boolean diffusiveJade = false;
  // Vetro
  private static boolean glass = false;

  // Variabile di controllo per il posizionamento delle sfere
  /* Questa variabile può essere eliminata nel momento in cui il modellatore può
   *  gestire autonomamente la presenza e la posizione degli oggetti creati
   *  dall'utente
   */
  public static boolean aligned = false;

  private static int SPHERES = 3;	// Numero delle sfere create in via predefinita

  // Direzione dal punto di vista (eye) verso l'oggetto
  // La variabile verrà definita in corso d'opera
  static Point3D lookAt = new Point3D();

  // Posizione della camera rispetto al centro della scena (0, 0, 0)
  static Point3D eye = new Point3D(0.0f, 2.0f, 12.0f);

  /* Punto di messa a fuoco della camera (può essere modificato per definire la
   *  posizione della messa a fuoco
   */
  static Point3D focusPoint = new Point3D(0.0f);

  //TODO add variables in UI
  public static int width = 1080;	//larghezza dell'immagine
  public static int height = 720;	//altezza dell'immagine

  //costruttore della fotocamera
  //imposto la fotocamera che guarda il centro
  //dell'oggetto ed e' posizionata davanti
  static Camera cam;
  static Point3D focalPoint;
  static float filmDistance = 700;	// Lunghezza focale
  /* Per una resa visiva del motore, la lunghezza focale impostata a 700 fa si che l'apertura
   *  del diaframma sia molto bassa per avere un piano di messa a fuoco più esteso
   */

  /* Densità triangoli nella stanza
   * Variabile che gestisce il grado di tassellazione della scena suddividendo le mesh con
   *  splitMeshes()
   */
  static int scenePrecision = 0;

  // Contatore di suddivisione della scena (che dovrà essere <= maxDepth
  public static int depthLevel = 0;
  // Profondità massima di suddivisione dell'OCTree
  public static int maxDepth = 14;	//TODO add this variable to a specific menu

  // Punti per il calcolo delle dimensioni della scena (creata dinamicamente in base al numero di oggetti)
  public static Point3D max = null;
  public static Point3D min = null;

  // Variabile per la suddivisione della scena tramite BSP
  // BSP: suddivisione dello spazio binaria secondo una struttura ad albero
  public static partition.Box bound;

  static ArrayList<Sphere> spheres;

  // Variabile globale in cui verranno salvati gli oggetti della scena
  static ArrayList<Obj> objects;        // Come vettore di supporto
  public static ArrayList<Obj> globalObjects;  // Come variabile globale

  // Variabile globale per le luci della scena
  public static ArrayList<Obj> lights;

  // Array che contiene tutti i pixel (RGB) dell'immagine
  /* E' possibile modificare l'array come array bidimensionale per una implementazione
   *  piu semplice nei cicli
   */
  public static Point3D[] image = new Point3D[width * height];

  // Vettori per i campioni casuali per la fotocamera (x, y)
  public static int[] samplesX = new int[width * height];
  public static int[] samplesY = new int[width * height];

  // Vettori per i campioni casuali per l'illuminazione diretta
  public static int[] dirSamples1 = new int[width * height];
  public static int[] dirSamples2 = new int[width * height];
  public static int[] dirSamples3 = new int[width * height];

  // Vettori per i campioni casuali per l'illuminazione indiretta
  public static int[] aoSamplesX = new int[width * height];
  public static int[] aoSamplesY = new int[width * height];

  // Vettori per i campioni di riflessione e rifrazione
  public static int[] refSamples1 = new int[width * height];
  public static int[] refSamples2 = new int[width * height];

  // Background: lo impostiamo come bianco
  public static Point3D background = new Point3D(1.0f,1.0f,1.0f);

  // Campioni del pixel: numero di campioni per ogni pixels
  // Si tratta dei punti nei pixel attraverso cui faremo passare raggi
  public static int samps;

  public static int aoSamps;   // Campioni per l'illuminazione indiretta
  public static int dirSamps;	// Campioni illuminazione diretta (non ricorsivo)
  public static int refSamps;	// Campioni scelti per le riflessioni e le rifrazioni

  // Lista di fotoni "sparati"
  public static ArrayList<Photon> photons = new ArrayList<>();
  // Lista di fotoni per caustiche "sparati"
  public static ArrayList<Photon> caustics = new ArrayList<>();
  // Suddivisione dello spazio secondo KDTree per la mappa fotonica
  public static PhotonBox[] kdTree;
  // Suddivisione dello spazio secondo KDTree per la mappa di caustiche
  public static PhotonBox[] causticTree;
  // KDTree: suddivisione dello spazio in sezioni K-dimensionali in una struttura ad albero
  //Profondità della suddivisione dello spazio per la mappa fotonica //TODO add this variable to a specific menu
  public static int kDepth = 17;
  // Numero di fotoni da inviare nello spazio
  public static int nPhoton;
  // Numero di fotoni specifici per le caustiche da inviare agli oggetti trasparenti o traslucidi
  public static int causticPhoton;
  // Numero di fotoni per le caustiche per l'illuminazione indiretta
  public static int aoCausticPhoton;
  // Risoluzione della proiezione della mappa fotonica su un emisfero
  public static int projectionResolution;
  // Scalamento di potenza del fotone
  public static float scaleCausticPower = 1;
  //TODO add this variable in ui.PhotonPanel

  // Distanza al quadrato disco di ricerca dei fotoni
  public static double photonSearchDisc;
  public static double causticSearchDisc;

  // Numero di fotoni da ricercare
  public static int nPhotonSearch;
  public static int nCausticSearch;

  // Contatore di potenza del fotone in base alla suddivisione kDepth
  public static int power = 0;

  // Contatore di step raggiunti dal processo Jacobi Stocastico
  public static int steps = 0;
  // Stima dell'errore raggiunto dal processo Jacobi Stocastico
  public static double err = 0;
  // Step massimi per le iterazioni di Jacobi Stocastico
  public static int maxSteps;
  // Errore massimo nel processo di Jacobi Stocastico
  public static double maxErr;
  // Sample per Jacobi Stocastico, utilizzati per il calcolo con Monte Carlo
  public static int jacobiSamps;

  //soglia dei triangoli dentro ad un box se ce ne sono di
  //meno si ferma la partizione; si puo' regolare in base
  //al numero totale dei triangoli della scena
  public static int boxThreshold = 4;

  /* nRay indica il numero di rimbalzi all'interno della scena
   * E' una variabile globale in modo da potersi aggiornare
   *  all'interno del metodo radiance()
   * Sarebbe anche possibile dare ricorsivamente il numero di
   *  raggi raggiunto nel metodo stesso
   */
  public static int nRay = 0;

  // Altezza da aggiungere alla stanza in fase di rendering
  public static float hroom = 1.2f;

  // Indice del materiale della luce della stanza (secondo il vettore material)
  public static int matIdL=0;

  //se true la luce e' frontale. Se si cambia in true,
  //deccommentare le parti in createScene()
  public static boolean frontL = false;

  // Contatore per le box caricate durante la partizione spaziale della scena
  public static int loadedBoxes = 0;

  // Parametro per la ricerca sferica su una faccia piana in [0,1]
  public static double sphericalSearch = 1;

  /* Array di oggetti da inserire su scelta dell'utente
   * Array riempito all'interno del modellatore
   * Al momento sono aggiungibili solo sfere, ma se si estende il modellatore anche ad altri
   *  oggetti, è possibile estendere questo array a più oggetti
   */
  static ArrayList<Sphere> additionalSpheres = new ArrayList<>();

  RenderAction(int modelerProperties) {
    // Il valore booleano fa sapere al programma in che modo deve partire il render (se semplice o quello finale)
    doRender(modelerProperties);
  }

  private void doRender(int modelerProperties) {
    InterfaceInitialiser.label.setText("Creazione immagine in corso");
    InterfaceInitialiser.editPanel.setUI(false);

    // Inizializzo l'array nei quali conservare gli oggetti di scena
    //TODO rendere passaggio valido per oggetti generici
    int mIS = setMatIdSphere();
    spheres = new ArrayList<>();

    // Oggetti predefiniti
    for(int sph = 0; sph < SPHERES; sph++) {
      Point3D sPos = Sphere.setSpheresPosition(sph);

      //vettore costruttore delle sfere
      spheres.add(new Sphere(1, sPos, mIS));
    }

    // Aggiunta di oggetti dal modellatore
    if (additionalSpheres != null && !additionalSpheres.isEmpty()) {
      spheres.addAll(additionalSpheres);
      SPHERES = spheres.size();
    }

    /* Reimposta le variabili utilizzate
     * Il metodo risulta utile quando si avvia il render
     *  dopo aver utilizzato il modellatore (ripristino una condizione originale)
     *  e in caso si volesse rieseguire un render dopo averne effettuato uno in precedenza
     */
    resetVariables();

    if (modelerProperties == ENABLE_MODELER || modelerProperties == PREVIEW_ONLY) {
      // Impostazioni per un render veloce ma aggiungere altri metodi (ad esempio uno zBuffer sarebbe l'ideale)
      samps = 1;

      doJacobi = true;

      jacobiSamps = 175;
      maxSteps = 3;
      maxErr = 0.01f;
    } else if (modelerProperties == START_RENDERING) {
      // Impostazioni per il rendering effettivo
      samps = InterfaceInitialiser.editPanel.getSamps();

      switch (InterfaceInitialiser.editPanel.getMethod()) {
        case JACOBI_PANEL:
          doJacobi = true;

          jacobiSamps = InterfaceInitialiser.editPanel.jacobiPanel.getSamps();
          maxSteps = InterfaceInitialiser.editPanel.jacobiPanel.getMaxSteps();
          maxErr = InterfaceInitialiser.editPanel.jacobiPanel.getMaxErr();
          break;
        case FINAL_GATHERING_PANEL:
          doFinalGathering = true;

          aoSamps = InterfaceInitialiser.editPanel.finalGatheringPanel.getAOSamples();
          dirSamps = InterfaceInitialiser.editPanel.finalGatheringPanel.getDirSamples();
          refSamps = InterfaceInitialiser.editPanel.finalGatheringPanel.getRefSamples();
          break;
        case PHOTON_PANEL:
          doPhotonMapping = true;

          nPhoton = InterfaceInitialiser.editPanel.photonPanel.getPhotonNum();
          causticPhoton = InterfaceInitialiser.editPanel.photonPanel.getCausticNum();
          aoCausticPhoton = InterfaceInitialiser.editPanel.photonPanel.getAOCaustic();
          projectionResolution = InterfaceInitialiser.editPanel.photonPanel.getProjectionResolution();
          photonSearchDisc = InterfaceInitialiser.editPanel.photonPanel.getPhotonSearchDisc();
          nPhotonSearch = InterfaceInitialiser.editPanel.photonPanel.getPhotonSearchNum();
          causticSearchDisc = InterfaceInitialiser.editPanel.photonPanel.getCausticSearchDisc();
          nCausticSearch = InterfaceInitialiser.editPanel.photonPanel.getCausticSearchNum();
          break;
      }
    }

    // Impostazione del materiale predefinito
    switch (InterfaceInitialiser.editPanel.getMaterial()) {
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

    // Impostazione delle posizioni predefinite
    switch (InterfaceInitialiser.editPanel.getPosition()) {
      case ALIGNED:
        aligned = true;
        break;
      case OVERLAPPED:
        aligned = false;
        break;
    }
    //TODO le opzioni predefinite possono essere modificate per raggiungere un livello di creazione dinamico
    initialiseMeshes(modelerProperties);

    //richiamo la funzione per il calcolo della radiosita'
    //della scena attraverso il metodo di Jacobi
    //stocastico

    if(doJacobi || InterfaceInitialiser.editPanel.finalGatheringPanel.getJacobiCheck()) {
      // Avvio il calcolo dell'energia uscente dal metodo di Jacobi stocastico
      JacobiStocClass.jacobiStoc(objects.size());
    }

    if (doPhotonMapping) {
      // Avvio il calcolo dell'energia ottenuta dai fotoni
      calculatePhotonMapping();
    }

    // Calcolo delle direzioni dei sample in anticipo
    for (int i = 0; i < (width * height); i++) {
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
      image[i] = new Point3D();
    }

    /* Avvio il render effettivo (automaticamente effettuerà un render completo
     *  o meno a seconda dei calcoli effettuati nelle righe precedenti
     */
    Renderer.calculateThreadedRadiance(cam);

    //Ora viene creata l'immagine (per il modellatore viene solo mostrata,
    // per il render invece viene salvata)
    if (modelerProperties == ENABLE_MODELER) {
      showImage(true);
    } else if (modelerProperties == START_RENDERING) {
      createImage();
      //showImage(false);

      InterfaceInitialiser.editPanel.setUI(true);
    }
  }

  static ArrayList<Sphere> getSpheresFromGlobalObjects() {
    ArrayList<Sphere> spheres = new ArrayList<>();

    for (Obj o : globalObjects) {
      if (o.s != null) {
        spheres.add(o.s);
      }
    }

    return spheres;
  }

  static Obj[] getEditableObjects() {
    ArrayList<Obj> objs = new ArrayList<>();

    for (Obj o : globalObjects) {
      if (o.s != null) {
        objs.add(o);
      } else if (o.t != null) {
        if (!o.t.isBorderMeshScene) {
          objs.add(o);
        }
      }
    }

    return objs.toArray(new Obj[0]);
  }

  void showImage(boolean isModeler) {
    new Preview(isModeler);
  }

  private void createImage() {
    //stringa contenente le informazioni da scrivere nel file immagine image.ppm
    StringBuilder matrix = new StringBuilder();

    //iniziamo la stringa matrix con valori di settaggi richiesti
    matrix.append("P3\n").append(width).append("\n").append(height).append("\n255\n");

    //Ora si disegna l'immagine: si procede aggiungendo
    //alla stringa matrix le informazioni contenute
    //nell'array image in cui abbiamo precedentemente
    //salvato tutti i valori di radianza
    for(int i = 0; i < width * height; i++) {
      //stampiamo la percentuale di completamento per
      //monitorare l'avanzamento della creazione
      //dell'immagine
      double percent = (i / (float)(width * height)) * 100;
      double percentFloor=Math.floor(percent);
      double a=percent-percentFloor;
      if(a==0.0) {
        InterfaceInitialiser.label.setText("Percentuale di completamento immagine: "
            + new DecimalFormat("###.##").format(percent));
      }

      //i valori di radianza devono essere trasformati
      //nell'intervallo [0,255] per rappresentare la
      //gamma cromatica in valori RGB
      matrix.append(Utilities.toInt(image[i].x))
          .append(" ")
          .append(Utilities.toInt(image[i].y))
          .append(" ")
          .append(Utilities.toInt(image[i].z))
          .append("  ");
    }

    InterfaceInitialiser.label.setText("Immagine completata!");

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

  static void initialiseMeshes(int modelPreview) {
    //dovendo disegnare 3 sfere e la stanza definisco un
    //array di 2 primitive.Mesh: nella prima delle due mesh (per
    //meshes[0]) aggiungiamo le 3
    //sfere richiamando il metodo caricaSphere

    //array di mesh della scena
    ArrayList<Mesh> meshes = new ArrayList<>(2);
    meshes.add(new Mesh(
        modelPreview == PREVIEW_ONLY ? getSpheresFromGlobalObjects() : spheres));

    //inizializzo dei fittizi massimo e minimo, che mi
    //serviranno per definire i valori di max e min
    Point3D oldMin = new Point3D(Float.POSITIVE_INFINITY);
    Point3D oldMax = new Point3D(Float.NEGATIVE_INFINITY);

    //trovo le dimensioni della scena (tralasciando la
    //stanza in cui gli oggetti sono contenuti)
    for (Mesh tmpObjects : meshes) {
      max = Obj.getBoundMax(tmpObjects.objects, oldMax);
      min = Obj.getBoundMin(tmpObjects.objects, oldMin);
    }

    //definisco e calcolo il punto in cui guarda
    //l'osservatore: il centro della scena
    Point3D center = (max.add(min)).multiplyScalar(0.5f)
        .subtract(new Point3D(0, 0.8, 0));

    //parametri della fotocamera con sistema di riferimento centrato nella scena:
    //absolutePos e' vero se stiamo guardando proprio al centro della scena
    boolean absolutePos = false;

    if (modelPreview == ENABLE_MODELER || modelPreview == PREVIEW_ONLY) {
      lookAt = new Point3D();
    }

    //TODO check variable usage
    if(!absolutePos) {
      lookAt = lookAt.add(center);
    }

    //l'osservatore si trova nel punto camPosition
    Point3D camPosition = center.add(eye);

    // Inizializzo il punto di messa a fuoco
    focalPoint = center.add(focusPoint);

    cam = new Camera(camPosition, lookAt,
        new Point3D(0.00015f,1.00021f,0.0f),
        width, height, filmDistance);

    //Abbiamo ora a disposizione tutti gli elementi
    //necessari per costruire la stanza
    //Creo la stanza in cui mettere l'oggetto (per
    //visualizzare l'illuminazione globale)
    //La carico come ultima mesh
    meshes.add(new Mesh(max, min));

    //nel nostro caso scenePrecision=0, quindi non si entra
    //mai in questo ciclo
    for(int q = 0; q < scenePrecision; q++) {
      meshes.get(meshes.size()-1).splitMeshes();
    }

    //A questo punto consideriamo l'intero array di mesh,
    //ora composto da oggetti+stanza e aggiorno i valori
    //della grandezza della stanza, usando di nuovo i
    //metodi getBoundMin e getBoundMax.
    oldMax = max;
    oldMin = min;
    max = Obj.getBoundMax(meshes.get(meshes.size()-1).objects, oldMax);
    min = Obj.getBoundMin(meshes.get(meshes.size()-1).objects, oldMin);

    //vettore che conterra' gli oggetti della scena
    objects = new ArrayList<>();
    //vettore che contiene solo le luci della scena
    lights = new ArrayList<>();

    for (Mesh tmpMesh : meshes) {
      //e carico tutto nella lista globale degli oggetti
      for (int j = 0; j < tmpMesh.objects.size(); j++) {
        objects.add(tmpMesh.objects.get(j));
        //se l'oggetto e' una luce la carico dentro
        //l'array delle luci
        if (material[tmpMesh.objects.get(j).matId].emittedLight.max() > 0) {
          lights.add(tmpMesh.objects.get(j));
        }
      }
    }

    // Aggiungo un oggetto (triangolo) predefinito nella scena, uno specchio
    if (modelPreview == ENABLE_MODELER) {
      objects.add(new Obj(new Triangle(
          new Point3D(-5.5, 0, 1.5),
          new Point3D(-6.2, 0, 2),
          new Point3D(-5.75, 1, 1.75),
          7)));
    } else {
      for (Obj o : globalObjects) {
        if (o.t != null && !o.t.isBorderMeshScene) {
          objects.add(o);
        }
      }
    }

    //depthLevel e' il livello di profondita' all'interno dell'albero
    depthLevel = 0;

    //creo il Bounding partition.Box
    //Bound e' il primo elemento dell'albero che contiene tutti gli oggetti della scena
    bound = new partition.Box(min, max, 0);
    bound.setObjects(objects);

    //crea il tree: si richiama il metodo setPartition()
    //per dividere gli oggetti del box padre nei box figli
    bound = Box.setPartition(bound);

    // Fattore di scala per la messa a fuoco in base al
    // punto focale e alla camera tramite trasformazione prospettica
    cam.fuoco = (focalPoint.z-cam.eye.z)/(cam.W.z*(-cam.d));

    //salviamo gli oggetti della scena nella variabile
    //globale globalObjects in modo da poterli aggiornare
    //in jacobiStoc()
    globalObjects = new ArrayList<>();
    globalObjects.addAll(objects);
  }

  private void resetVariables() {
    // Reimpostazione delle variabili allo stato originale
    doJacobi = false;
    doFinalGathering = false;
    doPhotonMapping = false;

    translucentJade=false;
    diffusiveJade=false;
    glass=false;
    aligned=false;

    lookAt =new Point3D(0.0f);

    focusPoint=new Point3D(0.0f);

    scenePrecision = 0;
    lights = new ArrayList<>();

    depthLevel = 0;

    max = null;
    min = null;

    globalObjects = new ArrayList<>();

    samplesX=new int[width * height];
    samplesY=new int[width * height];

    aoSamplesX=new int[width * height];
    aoSamplesY=new int[width * height];

    dirSamples1=new int[width * height];
    dirSamples2=new int[width * height];
    dirSamples3=new int[width * height];

    refSamples1=new int[width * height];
    refSamples2=new int[width * height];

    image = new Point3D[width * height];

    photons = new ArrayList<>();
    caustics = new ArrayList<>();

    steps = 0;
    nRay = 0;
    err = 0;

    boxThreshold =4;

    frontL=false;
    loadedBoxes = 0;
    sphericalSearch = 1;
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
}
