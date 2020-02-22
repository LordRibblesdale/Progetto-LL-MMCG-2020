import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

class RenderAction implements Properties, ModelerProperties {
  // Vettore dei materiali, per i modelli predefiniti di scena
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

  /* Questo fattore può essere ottimizzato assegnando a ogni oggetto
   *  (in questo caso sfere), l'indice o addirittura l'oggetto Material
   *  all'oggetto stesso e far richiamare il materiale dell'oggetto
   *  invece di chiamare l'indice di un array
   */
  private static ArrayList<Integer> matIdSphere = new ArrayList<>();

  static ArrayList<Sphere> spheres;

  /* Controlli booleani per accedere ai vari metodi per la scelta del
   *  tipo di rendering da effettuare
   *  -> Jacobi stocastico
   *  -> Final Gathering
   *  -> Photon Mapping
   */
  static boolean doJacobi = false;
  static boolean doFinalGathering = false;
  static boolean doPhotonMapping = false;

  /* Queste variabili assegnano solo agli oggetti predefiniti (e quelli aggiunti nel modellatore)
   *  lo stesso tipo di materiale senza poter scegliere il tipo di materiale
   * Può essere corretto creando una variabile di controllo nell'oggetto (Material)
   *  dato che il Renderer già è capace di determinare le proprietà del materiale selettivamente
   *  (per la presenza della variabile matId nell'oggetto)
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
  static boolean aligned = false;

  private static int SPHERES = 3;	// Numero delle sfere create in via predefinita

  // Direzione dal punto di vista (eye) verso l'oggetto
  // La variabile verrà definita in corso d'opera
  private static Point3D lookAt = new Point3D();

  // Posizione della camera rispetto al centro della scena (0, 0, 0)
  private static Point3D eye = new Point3D(0.0f, 2.0f, 12.0f);

  /* Punto di messa a fuoco della camera (può essere modificato per definire la
   *  posizione della messa a fuoco
   */
  private static Point3D focusPoint = new Point3D(0.0f);

  //TODO add variables in UI
  static int width = 1080;	//larghezza dell'immagine
  static int height = 720;	//altezza dell'immagine
  private static float filmDistance = 700;	// Lunghezza focale
  /* Per una resa visiva del motore, la lunghezza focale impostata a 700 fa si che l'apertura
   *  del diaframma sia molto bassa per avere un piano di messa a fuoco più esteso
   */
  private static float aperture = 5;  // Apertura diaframma della fotocamera

  /* Densità triangoli nella stanza
   * Variabile che gestisce il grado di tassellazione della scena suddividendo le mesh con
   *  splitMeshes()
   */
  private static int scenePrecision = 0;

  // Contatore di suddivisione della scena (che dovrà essere <= maxDepth
  static int depthLevel = 0;
  // Profondità massima di suddivisione dell'OCTree
  static int maxDepth = 14;	//TODO add this variable to a specific menu

  // Punti per il calcolo delle dimensioni della scena (creata dinamicamente in base al numero di oggetti)
  static Point3D max = null;
  static Point3D min = null;

  // Variabile per la suddivisione della scena tramite BSP
  // BSP: suddivisione dello spazio binaria secondo una struttura ad albero
  static Box bound;

  // Variabile globale in cui verranno salvati gli oggetti della scena
  static ArrayList<Obj> globalObjects;

  // Variabile globale per le luci della scena
  static ArrayList<Obj> lights;

  // Array che contiene tutti i pixel (RGB) dell'immagine
  /* E' possibile modificare l'array come array bidimensionale per una implementazione
   *  piu semplice nei cicli
   */
  static Point3D[] image = new Point3D[width * height];

  static int[] samplesX = new int[width * height];	  // Vettori per i campioni casuali per la fotocamera (x, y)
  static int[] samplesY = new int[width * height];

  static int[] dirSamples1 = new int[width * height];	// Vettori per i campioni casuali per l'illuminazione diretta
  static int[] dirSamples2 = new int[width * height];
  static int[] dirSamples3 = new int[width * height];

  static int[] aoSamplesX = new int[width * height];	// Vettori per i campioni casuali per l'illuminazione indiretta
  static int[] aoSamplesY = new int[width * height];

  static int[] refSamples1 = new int[width * height];	// Vettori per i campioni di riflessione e rifrazione
  static int[] refSamples2 = new int[width * height];

  static Point3D background = new Point3D(1.0f,1.0f,1.0f);	// Background: lo impostiamo come bianco

  // Campioni del pixel: numero di campioni per ogni pixels
  // Si tratta dei punti nei pixel attraverso cui faremo passare raggi
  static int samps;

  static int aoSamps;   // Campioni per l'illuminazione indiretta
  static int dirSamps;	// Campioni illuminazione diretta (non ricorsivo)
  static int refSamps;	// Campioni scelti per le riflessioni e le rifrazioni

  static ArrayList<Photon> photons = new ArrayList<>();   // Lista di fotoni "sparati"
  static ArrayList<Photon> caustics = new ArrayList<>();  // Lista di fotoni per caustiche "sparati"
  static PhotonBox[] kdTree;        // Suddivisione dello spazio secondo KDTree per la mappa fotonica
  static PhotonBox[] causticTree;   // Suddivisione dello spazio secondo KDTree per la mappa di caustiche
  // KDTree: suddivisione dello spazio in sezioni K-dimensionali in una struttura ad albero
  static int kDepth = 17;             //Profondità della suddivisione dello spazio per la mappa fotonica //TODO add this variable to a specific menu
  static int nPhoton;                 // Numero di fotoni da inviare nello spazio
  static int causticPhoton;           // Numero di fotoni specifici per le caustiche da inviare agli oggetti trasparenti o traslucidi
  static int aoCausticPhoton;         // Numero di fotoni per le caustiche per l'illuminazione indiretta
  static int projectionResolution;    // Risoluzione della proiezione della mappa fotonica
  static float scaleCausticPower = 1; // Scalamento di potenza del fotone
  //TODO add this variable in PhotonPanel

  // Distanza al quadrato disco di ricerca dei fotoni
  static double photonSearchDisc;
  static double causticSearchDisc;

  // Numero di fotoni da ricercare
  static int nPhotonSearch;
  static int nCausticSearch;

  // Contatore di potenza del fotone in base alla suddivisione kDepth
  static int power = 0;

  static int steps = 0;	    // Contatore di step raggiunti dal processo Jacobi Stocastico
  static double err = 0;    // Stima dell'errore raggiunto dal processo Jacobi Stocastico
  static int maxSteps;	    // Step massimi per le iterazioni di Jacobi Stocastico
  static double maxErr;     // Errore massimo nel processo di Jacobi Stocastico
  static int jacobiSamps;   // Sample per Jacobi Stocastico, utilizzati per il calcolo con Monte Carlo

  //soglia dei triangoli dentro ad un box se ce ne sono di
  //meno si ferma la partizione; si puo' regolare in base
  //al numero totale dei triangoli della scena
  static int boxThreshold = 4;

  /* nRay indica il numero di rimbalzi all'interno della scena
   * E' una variabile globale in modo da potersi aggiornare all'interno del metodo radiance()
   * Sarebbe anche possibile dare ricorsivamente il numero di raggi raggiunto nel metodo stesso
   */
  static int nRay = 0;

  static float hroom=1.2f;	// Altezza da aggiungere alla stanza in fase di rendering

  static int matIdL=0;	// Indice del materiale della luce della stanza (secondo il vettore material)

  //se true la luce e' frontale. Se si cambia in true,
  //deccommentare le parti in createScene()
  static boolean frontL = false;

  // Contatore per le box caricate durante la partizione spaziale della scena
  static int loadedBoxes = 0;

  // Parametro per la ricerca sferica su una faccia piana in [0,1]
  static double sphericalSearch = 1;

  /* Oggetto che contiene tutti i metodi di rendering e gestisce la fase di multithreading
   * E' necessario avere questa classe come oggetto invece di una statica per permettere
   *  di gestire variabili statiche e non statiche e di poter progettare la fase di MT
   */
  private Renderer renderer;

  /* Array di oggetti da inserire su scelta dell'utente
   * Array riempito all'interno del modellatore
   * Al momento sono aggiungibili solo sfere, ma se si estende il modellatore anche ad altri
   *  oggetti, è possibile estendere questo array a più oggetti
   */
  static ArrayList<Sphere> additionalSpheres = new ArrayList<>();

  RenderAction(int modelerProperties) {
    // Il valore booleano fa sapere al programma in che modo deve partire il render (se semplice o quello finale)
    renderer = new Renderer(new Utilities());

    doRender(modelerProperties);
  }

  private void doRender(int modelerProperties) {
    Main.label.setText("Creazione immagine in corso");
    Main.editPanel.setUI(false);

    // Inizializzo l'array nei quali conservare gli oggetti di scena e l'array di indici
    /* L'array di indici può essere cancellato in favore di una riscrittura di Sphere nel quale al posto
     *  dell'indice è presente il materiale, che verrà richiamato dai metodi vari
     */
    int mIS = setMatIdSphere();
    matIdSphere = new ArrayList<>();
    spheres = new ArrayList<>();

    // Oggetti predefiniti
    for(int sph = 0; sph < SPHERES; sph++) {
      matIdSphere.add(mIS);
      Point3D sPos = Sphere.setSpheresPosition(sph);

      //vettore costruttore delle sfere
      spheres.add(new Sphere(1, sPos));
    }

    // Aggiunta di oggetti dal modellatore
    if (additionalSpheres != null && !additionalSpheres.isEmpty()) {
      spheres.addAll(additionalSpheres);
      SPHERES = spheres.size();

      for (int i = 0; i < additionalSpheres.size(); i++) {
        matIdSphere.add(mIS);
      }
    }

    /* Reimposta le variabili utilizzate
     * Il metodo risulta utile quando si avvia il render dopo aver utilizzato il modellatore (ripristino una condizione
     *  originale) e in caso si volesse rieseguire un render dopo averne effettuato uno in precedenza
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
      samps = Main.editPanel.getSamps();

      switch (Main.editPanel.getMethod()) {
        case JACOBI_PANEL:
          doJacobi = true;

          jacobiSamps = Main.editPanel.jacobiPanel.getSamps();
          maxSteps = Main.editPanel.jacobiPanel.getMaxSteps();
          maxErr = Main.editPanel.jacobiPanel.getMaxErr();
          break;
        case FINAL_GATHERING_PANEL:
          doFinalGathering = true;

          aoSamps = Main.editPanel.finalGatheringPanel.getAOSamples();
          dirSamps = Main.editPanel.finalGatheringPanel.getDirSamples();
          refSamps = Main.editPanel.finalGatheringPanel.getRefSamples();
          break;
        case PHOTON_PANEL:
          doPhotonMapping = true;

          nPhoton = Main.editPanel.photonPanel.getPhotonNum();
          causticPhoton = Main.editPanel.photonPanel.getCausticNum();
          aoCausticPhoton = Main.editPanel.photonPanel.getAOCaustic();
          projectionResolution = Main.editPanel.photonPanel.getProjectionResolution();
          photonSearchDisc = Main.editPanel.photonPanel.getPhotonSearchDisc();
          nPhotonSearch = Main.editPanel.photonPanel.getPhotonSearchNum();
          causticSearchDisc = Main.editPanel.photonPanel.getCausticSearchDisc();
          nCausticSearch = Main.editPanel.photonPanel.getCausticSearchNum();
          break;
      }
    }

    // Impostazione del materiale predefinito
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

    // Impostazione delle posizioni predefinite
    switch (Main.editPanel.getPosition()) {
      case ALIGNED:
        aligned = true;
        break;
      case OVERLAPPED:
        aligned = false;
        break;
    }
    //TODO le opzioni predefinite possono essere modificate per raggiungere un livello di creazione dinamico

    //dovendo disegnare 3 sfere e la stanza definisco un
    //array di 2 Mesh: nella prima delle due mesh (per
    //meshes[0]) aggiungiamo le 3
    //sfere richiamando il metodo caricaSphere

    //array di mesh della scena
    ArrayList<Mesh> meshes = new ArrayList<>(2);
    meshes.add(new Mesh(spheres, matIdSphere));

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
    Point3D center = (max.add(min)).multiplyScalar(0.5f).subtract(new Point3D(0, 0.8, 0));

    //parametri della fotocamera con sistema di riferimento centrato nella scena:
    //absolutePos e' vero se stiamo guardando proprio al centro della scena
    boolean absolutePos = false;

    //TODO check variable usage
    if(!absolutePos) {
      lookAt = lookAt.add(center);
    }

    //l'osservatore si trova nel punto camPosition
    Point3D camPosition = center.add(eye);

    // Inizializzo il punto di messa a fuoco
    Point3D focalPoint = center.add(focusPoint);

    //costruttore della fotocamera
    //imposto la fotocamera che guarda il centro
    //dell'oggetto ed e' posizionata davanti
    Camera cam = new Camera(camPosition, lookAt, new Point3D(0.00015f,1.00021f,0.0f), width, height, filmDistance);

    //Abbiamo ora a disposizione tutti gli elementi
    //necessari per costruire la stanza
    //Creo la stanza in cui mettere l'oggetto (per
    //visualizzare l'illuminazione globale)
    //La carico come ultima mesh
    meshes.add(new Mesh(max,min));

    //nel nostro caso scenePrecision=0, quindi non si entra
    //mai in questo ciclo
    for(int q = 0; q < scenePrecision; q++) {
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
    objects.add(new Obj(new Triangle(new Point3D(-5.5, 0, 1.5), new Point3D(-6.2, 0, 2), new Point3D(-5.75, 1, 1.75)), 7));
;
    //liv e' il livello di profondita' all'interno
    //dell'albero
    depthLevel = 0;

    //creo il Bounding Box
    //Bound e' il primo elemento dell'albero che contiene
    //tutti gli oggetti della scena
    bound = new Box(min, max, 0);
    bound.setObjects(objects);

    //crea il tree: si richiama il metodo setPartition()
    //per dividere gli oggetti del box padre nei box figli
    bound = Box.setPartition(bound);

    //salviamo gli oggetti della scena nella variabile
    //globale globalObjects in modo da poterli aggiornare
    //in JacobiStoc()
    globalObjects = new ArrayList<>();
    globalObjects.addAll(objects);

    //richiamo la funzione per il calcolo della radiosita'
    //della scena attraverso il metodo di Jacobi
    //stocastico

    if(doJacobi) {
      // Avvio il calcolo dell'energia uscente dal metodo di Jacobi stocastico
      renderer.jacobiStoc(objects.size());
    }

    if (doPhotonMapping) {
      // Avvio il calcolo dell'energia ottenuta dai fotoni
      renderer.calculatePhotonMapping();
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

    cam.aperture = aperture;

    // Fattore di scala per la messa a fuoco in base al punto focale e alla camera tramite trasformazione prospettica
    cam.fuoco = (focalPoint.z-cam.eye.z)/(cam.W.z*(-cam.d));

    /* Avvio il render effettivo (automaticamente effettuerà un render completo o meno a seconda dei calcoli
     * effettuati nelle righe precedenti
     */
    renderer.calculateThreadedRadiance(cam);

    //Ora viene creata l'immagine (per il modellatore viene solo mostrata, per il render invece viene salvata)
    if (modelerProperties == ENABLE_MODELER) {
      showImage();
    } else if (modelerProperties == PREVIEW_ONLY) {

    } else if (modelerProperties == START_RENDERING) {
      createImage();

      Main.editPanel.setUI(true);
    }
  }

  void showImage() {
    // Interfaccia per mostrare l'immagine effettiva su schermo,
    boolean[] isRunning = {false};

    JDialog frame = new JDialog(Main.mainFrame, "Anteprima", true);
    frame.setMinimumSize(new Dimension(width, height));
    frame.setLocationRelativeTo(Main.editPanel);
    frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    JPanel imagePanel = new JPanel(null) {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
            g.setColor(image[j + i* width].toColor());
            g.drawLine(j, i, j, i);
          }
        }
      }
    };

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton insert = new JButton("Aggiungi sfere");
    insert.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Modeler(frame);
      }
    });

    JButton button = new JButton("Avvia render");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        isRunning[0] = true;

        frame.dispose();

        new Thread(new Runnable() {
          @Override
          public void run() {
            new RenderAction(START_RENDERING);
          }
        }).start();
      }
    });

    bottomPanel.add(insert);
    bottomPanel.add(button);

    frame.add(imagePanel);
    frame.add(bottomPanel, BorderLayout.PAGE_END);

    frame.setVisible(true);

    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        super.windowClosed(e);

        if (isRunning[0]) {
          frame.dispose();
        } else {
          System.exit(0);
        }
      }
    });
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
        Main.label.setText("Percentuale di completamento immagine: " + new DecimalFormat("###.##").format(percent));
      }

      //i valori di radianza devono essere trasformati
      //nell'intervallo [0,255] per rappresentare la
      //gamma cromatica in valori RGB
      matrix.append(Utilities.toInt(image[i].x)).append(" ").append(Utilities.toInt(image[i].y)).append(" ").append(Utilities.toInt(image[i].z)).append("  ");
    }

    Main.label.setText("Immagine completata!");

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
}
