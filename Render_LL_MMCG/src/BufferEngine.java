class BufferEngine {
  public static Point3D[][] pixelMatrix;
  public static double[][] zBuffer;
  public static int f, w, h;
  public static Point3D[] p = new Point3D[4];
  public static Point3D view;

  BufferEngine(int width, int height, int focal){
    //Costruttore del renderer
    w = width;
    h = height;
    f = focal;
    view = new Point3D(0,0,f);
    pixelMatrix = new Point3D[w][h];
    zBuffer = new double[w][h];

    render();
  }

  static void render(){
    // render() esegue tutte le funzioni necessarie a fere il
    //rendering della scena
    // inizializzo la matrice dei pixel con il colore dello
    //    sfondo
    // e quella dello z-buffer con la profondità massima

    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        pixelMatrix[i][j] = new Point3D(0, 0, 0);
        zBuffer[i][j] = 99999;
      }
    }

    // per ogni triangolo della scena, calcolo il suo
    //rendering
    for (int i = 0; i < RenderAction.approxSceneObjects.size(); i++){
      render(RenderAction.approxSceneObjects.get(i));
    }
  }

  private static void render(Triangle tr){
    // Questa funzione trova la proiezione del triangolo tr sul viewplane
    // e aggiorna i valori delle matrici pixelMatrix e zBuffer

    int matId = tr.matId;

    double y;

    Point3D n = tr.n; // normale del triangolo tr

    //TODO fix backface culling
    /*
    if (tr.n.dotProduct(RenderAction.cam.lookAt) > 0) {
      return; // backface culling
    }
    */

    System.out.println(matId);

    //calcolo delle proiezioni dei tre vertici del triangolo
    //sul view plane

    p[1] = tr.vertices[0].project(f);
    p[2] = tr.vertices[1].project(f);
    p[3] = tr.vertices[2].project(f);

    // eseguo scambi per far sì che p[1] sia il vertice piu’
    //in alto sul viewplane (coordinata y più bassa),
    // p[2] al centro e p[3] sia il più basso

    if (p[1].y > p[2].y) {
      swap(1,2);
    }

    if (p[1].y > p[3].y) {
      swap(1,3);
    }

    if (p[2].y > p[3].y) {
      swap(2,3);
    }

    double zSlopeX = -n.x / n.z;

    // è l’incremento di z per
    //ogni spostamento di 1 pixel lungo l’asse x

    double zSlopeY = -n.y / n.z;
    // è l’incremento di z per ogni spostamento di 1 pixel lungo l’asse y
    /* Adesso occorre fare un clipping del triangolo
    proiettato. Infatti la funzione scan() che disegna
    il triangolo su pixelMatrix funziona solo se uno dei
    lati è parallelo all’asse x del view plane.
    Si opera dunque un taglio orizzionatle all’altezza del
    vertice p[2] ottentedo due triangol (uno
    superiore e uno inferiore), entrambi con un lato
    orizzontale (in comune)
    */

    // Caso banale: se due vertici hanno la stessa y, allora
    //un lato è orizzontale. Posso dunque eseguire
    // direttamente la funzione scan() e disegnare l’intero
    //    triangolo

    if (p[1].y == p[2].y) {
      if (p[1].x > p[2].x) {
        swap(1,2);
      }

      scan(p[3], p[1], p[2], -1, zSlopeX, zSlopeY, tr, matId);
      return;
    }

    // Nel caso generale devo trovare l’intersezione del triangolo proiettato sul viewplane (2D) con
    // la retta orizzontale passante per p[2]. Un punto d’interzezione è ovviamente p[2], l’altro
    // si trova sul lato opposto e si ottiene tramite interpolazione lineare

    y = (p[2].y - p[1].y) / (p[1].y - p[3].y); //coefficiente di interpolazione lineare

    p[0] = new Point3D(y*(p[1].x - p[3].x) + p[1].x , p[2].y,
        y*(p[1].z - p[3].z) + p[1].z); // punto d’intersezione

    // Ora bisogna disegnare i due triangoli (ora entrambi
    //con un lato orizzontale).
    // Opero degli scambi affinche’ p[1],p[2],p[0] sia il
    //triangolo superiore e
    // p[3],p[2],p[0] quello inferiore

    if (p[2].x > p[0].x) {
      swap(2,0);
    }

    // si possono ora disegnare le due porzioni di triangolo
    //con la funzione scan()

    scan(p[1], p[2], p[0], 1, zSlopeX, zSlopeY, tr, matId);
    scan(p[3], p[2], p[0], -1, zSlopeX, zSlopeY, tr, matId);
  }

  private static int round(double x){
    // round() arrotonda valori double all’intero piu’ vicino
    // e’ utilizzata per passare dalle corrdinate dello
    //spazio (double)
    // alle coordinate della pixelMatrix (int)

    return (Math.round(x) > 255 ? 255 : (int) (Math.round(x)));
  }

  private static void scan(Point3D p1, Point3D p2, Point3D p3, int v,
                           double zSlopeX, double zSlopeY, Triangle tr, int matId) {

    /* scan() trova, con il calolo incrementale, tutti i
    pixel interni del triangolo p1,p2,p3 e per ognuno
    di essi richiama draw(), la funzione che aggiorna di
    fatto le matrici pixelMatrix e zBuffer.


    Il valore ’v’ indica se si sta operando lo scan di un
    triangolo superiore (+1) o inferiore (-1) del
        clipping
    */
    // Se si considera la retta che passa lungo il lato
    //sinistro del triangolo,
    // ls rappresente l’incremento di x per ogni
    //    spostamento di 1 pixel lungo l’asse y del view plane
    // (dunque l’inverso del coefficiente angolare)

    double ls = (p1.x - p2.x) / (p1.y - p2.y);

    // rs rappresenta la stessa quantita’ per il lato destro
    //del triangolo

    double rs = (p1.x - p3.x) / (p1.y - p3.y);
    double x, y, z, xl, xr, zx;

    Point3D c = getShade(tr, RenderAction.material[matId].diffusionColor, matId);

    // trovo il
    //valore d’illuminazione del triangolo nelle scena
    // Nel caso in cui v=+1, si sta facendo lo scan del
    //triangolo superiore, dall’alto verso il basso
    // (dunque dal vertice in alto verso il lato orizzontale)

    if(v==1){
      z = p1.z;
      xl = xr = p1.x;
      for(y=p1.y; (y)<=p3.y; y++){
        // questo for() viene eseguito per ogni riga del
        //triangolo
        zx = z;

        for(x=xl; x<=xr; x++) {
          //questo for() viene eseguito per ogni pixel
          // della stessa riga
          // con draw(), scrivo sulla matrice di pixel il
          //valore del colore c nel punto x,y del
            //viweplane
          // e aggiorno in tali coordinate anche il valore
          //dello zBuffer
          draw(x, y, zx, c);

          // ottengo il nuovo valore di profondit\‘{a} per
          //la prossima iterazione (pixel di destra)
          zx += zSlopeX;
        }

        z += ls*zSlopeX + zSlopeY; // ottengo il nuovo
        //valore di profondit\‘{a} per la prossima
        //iterazione (riga sottostante)
        xl += ls; // ottengo l’estremo sinistro dei valori di x per la prossima riga
        xr += rs; // ottengo l’estremo destro dei valori di x per la prossima riga
        // xl e xr sono gli estremi della riga per il ciclo interno, sono usati nella condizione
        // di permanenza del ciclo for() interno
      }
    }

    // Nel caso in cui v=-1, si sta facendo lo scan del triangolo inferiore, dall’alto verso il basso
    // (dunque dal lato orizzontale verso il vertice in basso) le operazioni sono analoghe

    if(v == -1) {
      z = p3.z;
      xl = p2.x;
      xr = p3.x;

      for (y = p3.y; y <= p1.y; y++) {
        zx = z;

        for(x = xl; x <= xr; x++) {
          draw(x, y, zx, c);
          zx += zSlopeX;
        }
        z += ls*zSlopeX;
        z += zSlopeY;
        xl += ls;
        xr += rs;
      }
    }
  }

  private static void draw(double xd, double yd, double z, Point3D c) {
    // Se necessario, la funzione draw() aggiorna pixelMatrix con il colore c nel punto (xd,yd)
    // e inoltre aggiorna lo zBuffer nello stesso punto con il valore z
    // passo dalle coordinate dello spazio a quelle del viewplane (in pixel)

    int x = round(xd + w/2f);
    int y = round(yd + h/2f);

    /* Se le coordinate (x,y) del pixel sono contenute nel viewplane
      e la coordinata z di profondita’ e’ minore di quella gia’ presente nello
      zBuffer, allora aggiorno i valori nelle due matrici con il colore e la profondita’
      passate come argomento
    */
    if (x < w && x >= 0 && y >= 0 && y < h && z < zBuffer[x][y]) {
      zBuffer[x][y] = z;
      pixelMatrix[x][y] = c;
    }
  }

  private static void swap(int i, int j){
    // swap() e’ una funzione che scambia i nomi (puntatori)
    // di due oggetti di tipo Point
    Point3D t = p[i];
    p[i] = p[j];
    p[j] = t;
  }

  public static Point3D getShade(Triangle tr, Point3D c, int matId){
    // getShade() ottiene l’illuminazione del trianagolo tr
    //    nella scena, calcolando
    // il contributo ambientale, diffusivo e riflessivo
    //    triangolo

    return getShade(tr.center, tr.n, c,
        RenderAction.material[matId].diffusionColor.normalize(),
        RenderAction.material[matId].reflectionColor.normalize());
  }

  public static Point3D getShade(Point3D p, Point3D N, Point3D clr, double kDif, double kRef){
    // getShade ottiene l’illuminazione del punto p nella
    //scena, con normale N,
    // calcolando il contributo ambientale, diffusivo
    //(Lambert) e riflessivo (Phong)
    // Per il solo z-buffer, questa classe non serviva: lo
    //z-buffer possiede come input
    // il colore di ciascun triangolo, senza doverlo ricavare
    //dai dati geometrici 3D,
    // ossia dalla posizione di osservatore, luci e versore
        //normale.
    // Ma qui passiamo allo z-buffer, per ciascun poligono,
            //un colore appropriato:
    // quello che si ottiene da questi dati geometrici
    //applicando la equazione di
    // illuminazione di Phong. Quindi questo metodo di
    //z-buffer e’ in realta’ una
    // accelerazione tramite z-buffer del Ray Tracing (non
   // ricorsivo).
    // inizializzazione di variabili
    double intensity;
    double d, att, diff=0,shadeDiff=0,shadeRef=0,ref=0;
    Obj light = null;
    Point3D amb = new Point3D(255); // amb e’ il colore della luce
    //ambientale
    Point3D shade;
    //nella sua normale
    Point3D shadeDiffFinal;
    Point3D shadeRefFinal;
    Point3D L, V;
    Point3D o = new Point3D(0,0,-f); // o rappresenta il centro
    //di proiezione, ed e’ dunque l’origine
    // di tutti i raggi di vista

    for(int i=0; i<RenderAction.lights.size(); i++){
      // per ogni luce della scena, calcolo il suo
      //contributo d’illuminazione
      light = RenderAction.lights.get(i);
      // V e’ il vettore che congiunge il centro del
      // triangolo al centro di proiezione o
      V = o.subtract(p);
      // L e’ il vettore che congiunge il centro del
      //triangolo alla posizione della luce
      // (chiamato anche ’raggio d’ombra’)
      L = light.P.subtract(p);
      // d e’ la distanza del triangolo dalla luce
      d = L.normalize();
      // att e’ il coefficiente di attenuazione della luce
      //dato dalla distanza d
      att = light.getAtt(d);

      if(L.dotProduct(N)<0){
        // Se la superficie del triangolo non ’vede’ la
        // luce,
        // non calcolo affatto il contributo di questa
       //     sorgente e
        // salto direttamente alla prossima luce
        continue;
      }

      intensity = light.i * att; // intensity e’
      //l’intensit\‘{a} luminosa che raggiunge il punto
      L = L.getNormalizedPoint(); // normalizzo il raggio d’ombra
      V = V.getNormalizedPoint(); // normalizzo il raggio di vista
      // L e V sono ora versori
      /* diff e’ il contributo d’illuminazione, diffusivo
      calcolato con la formula
      di Lambert: diff = <L,N>. Notare che a questo punto
      della funzione diff non
      puo’ essere negativo per il controllo effettuato
      prima */
      diff = L.dotProduct(N);
      shadeDiff += diff*intensity; // incremento il
      //contributo diffusivo
      /* ref e’ il contributo di luce riflettente calcolato
      con la formula
      di Phong: ref = 2 <N,L> <N,V> - <L,V> */
      ref = 2*N.dotProduct(L)*N.dotProduct(V) - L.dotProduct(V);
      // se ref e’ positivo, incremento l’illuminazione di
      // ref, moltiplicata per il coefficiente di
      // riflessione al punto
      if(ref>0) shadeRef += ref*ref*intensity;
    }

    shadeDiff *= kDif; // moltiplico diff per il
    //coefficiente diffusivo del punto e quello
    //d’attenuazione
    shadeRef *= kRef; // moltiplico diff per il
    //coefficiente diffusivo del punto e quello
    //d’attenuazione
    shadeDiffFinal = clr.multiplyScalar(shadeDiff);
    shadeRefFinal = new Point3D(shadeRef, shadeRef, shadeRef);
    shade = shadeRefFinal.add(shadeDiffFinal);
    shade = shade.add((amb.multiplyComponents(clr)).multiplyScalar(kDif)); //
    //incremento del contributo ambientale
    // normalizzo a valori compresi fra 0 e 255
    shade = shade.multiplyScalar(255);
    // contengo l’illuminazione fra il valore massimo (255) e
    //minimo (0)
    shade = Point3D.clamp3C(shade);
    //intShade = round(shade); // arrotondo all’intero piu’
    // vicino per la codifica RGB

    //noinspection SuspiciousNameCombination
    return new Point3D(round(shade.x), round(shade.y), round(shade.z));
    // restituisco l’illuminazione finale
  }

  public Point3D[][] getPixelMatrix() {
    return pixelMatrix;
  }
}