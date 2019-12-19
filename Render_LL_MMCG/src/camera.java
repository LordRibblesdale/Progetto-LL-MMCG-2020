//classe che definisce una fotocamera e i suoi parametri
class camera {

	// Posizione della fotocamera
	public Point3D eye;
	// Direzione di vista
	public Point3D lookAt;
	// Up vector (generalmente Y)
	public Point3D up;
	// risoluzione sulla X
	public int width;
	// risoluzione sulla Y
	public int height;
	// Distanza del piano di messa a fuoco
	public float d;
	// coordinate locali della fotocamera 
	//(si intende il sistema di riferimento con la 
	//fotocamera nell'origine)
	public Point3D U,V,W;
	   
	public float aperturaDiaframma = 0;
	public float fuoco=0;
	
	//costruttore di default imposta la fotocamera 
	//nell'origine che guarda l'origine con distanza 
	//focale 35 
	public camera() {

		eye = new Point3D();
		lookAt= new Point3D();
		up=new Point3D(0.0f,0.0f,1.0f);
		width=320;
		height=240;
		d=35.0f;
		U = new Point3D();
		V = new Point3D();
		W = new Point3D();
		
	}
	
	//costruttore della fotocamera in cui il 
    //sistema ortonormale viene prima impostato a 0
	//per essere poi calcolato basandosi sulla posizione
	//della fotocamera eye (come centro) e sulla 
	//direzione in cui si guarda lookAt (come assi)
	public camera(Point3D neye, Point3D nlookAt,
                Point3D nup, int nwidth, int nheight,
                float nd) {
		eye=neye;
		lookAt=nlookAt;
		up=nup;
		width=nwidth;
		height=nheight;
		d=nd;
		U=new Point3D();
		V=new Point3D();
		W=new Point3D();
	
		// creo il sistema ortonormale della fotocamera 
		//(normalizzando tutti gli assi del sistema con 
		//il metodo .norm())
        //direzione verso cui guardo invertito per 
		//convenzione della computer graphics 
		//corrisponde alla profondita' z 
		W=eye.subtract(lookAt);
		W=W.getNormalizedPoint();
        
        // prendo -W (direzione sguardo) e faccio il 
		//prodotto vettoriale con il vettore up (che da'
		//l'inclinazione della camera) cosi da ottenere
		//un vettore ortogonale ad entrambi
		U=(W.multiplyScalar(-1.0f));
		U=U.crossProduct(up.getNormalizedPoint());
		U=U.getNormalizedPoint();
        //creo l'ultimo vettore ortogonale che e' gia' 
		//normalizzato poiche' i vettori di cui facciamo 
		//il prodotto vettoriale sono normalizzati
		//(non potevamo prendere esattamente l'up 
		//poiche' non e' per forza ortogonale alla 
		//direzione dello sguardo) 
		V=U.crossProduct(W);
	}

	public static void main(String[] args) {

	}

}
