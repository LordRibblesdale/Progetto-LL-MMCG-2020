//questa classe definisce un float3: esso puo'  
//rappresentare un punto o un vettore in R3 a seconda
//delle esigenze
//Dal momento che la scena che vogliamo renderizzare
//sara' una stanza nello spazio R3, questa classe sara'
//molto utilizzata, insieme anche alle sue funzioni che si
//occupano di varie trasformazioni o operazioni con i
//float3

public class Point3D {
	//ha come parametri soltanto le tre componenti di un 
	//float in uno spazio a 3 dimensioni
	private float x;
	private float y;
	private float z;
	
	//costruttori
	public Point3D() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}

	public Point3D(float x_, float y_, float z_) {
		x = x_;
		y = y_;
		z = z_;
	}
	
	public Point3D(float x_) {
		x = x_;
		y = x_;
		z = x_;
	}
	
	//metodi set e get
	public void setX(float newX) {
		x = newX;
	}

	public void setY(float newY) {
		y=newY;
	}

	public void setZ(float newZ) {
		z=newZ;
	}
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}
	
	//addizione componente componente tra 2 vettori in R3
	public Point3D add(Point3D b) {
		Point3D a=new Point3D(x,y,z);
		a.x=a.x+b.x;
		a.y=a.y+b.y;
		a.z=a.z+b.z;
		return a;
	}
	
	//sottrazione componente componente tra 2 vettori in R3
	public Point3D subtract(Point3D b)	{
		Point3D a=new Point3D(x,y,z);
		a.x=a.x-b.x;
		a.y=a.y-b.y;
		a.z=a.z-b.z;
		return a;
	}
	
	//moltiplicazione tra ogni componente di un vettore 
	//in R3 con uno scalare b
	public Point3D multiplyScalar(float b)	{
		Point3D a=new Point3D(x,y,z);
		a.x=a.x*b;
		a.y=a.y*b;
		a.z=a.z*b;
		return a;
	}
	
	//divisione tra ogni componente di un vettore in R3 
	//con uno scalare b
	public Point3D divideScalar(float b)	{
		Point3D a=new Point3D(x,y,z);
		a.x=a.x/b;
		a.y=a.y/b;
		a.z=a.z/b;
		return a;
	}
    
	//moltiplicazione componente componente tra 2 
	//vettori in R3
	public Point3D multiplyComponents(Point3D b)	{
		Point3D a=new Point3D(x,y,z);
		a.x=a.x*b.x;
		a.y=a.y*b.y;
		a.z=a.z*b.z;
		return a;
	}
 
	//divisione componente componente tra 2 vettori in R3
	public Point3D divideComponents(Point3D b)	{
		Point3D a=new Point3D(x,y,z);
		a.x=a.x/b.x;
		a.y=a.y/b.y;
		a.z=a.z/b.z;
		return a;
		
	}	    
			
	//modulo
	void abs() {
		if (x<0) x=-x;
		if (y<0) y=-y;
		if (z<0) z=-z;
	}
	
	//norma euclidea
	float normalize() {
		Point3D a=new Point3D(x,y,z);
		return (float) Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
	}

	//norma al quadrato
	float squareNorm() {
   	Point3D a=new Point3D(x,y,z);
		return a.x*a.x+a.y*a.y+a.z*a.z;
   }

  //media delle componenti di un vettore in R3
  float average() {
   	Point3D a=new Point3D(x,y,z);
		return (a.x+a.y+a.z)/3;
	}
    
  //norma del vettore
  Point3D getNormalizedPoint() {
  	Point3D a=new Point3D(x,y,z);
  	float n = a.normalize();
  	float ax=(a.x)/n;
  	float ay=(a.y)/n;
  	float az=(a.z)/n;
		return new Point3D(ax,ay,az);
  }
    
  // Prodotto scalare
 	float dotProduct(Point3D b) {
 		Point3D a=new Point3D(x,y,z);
		return a.x*b.x+a.y*b.y+a.z*b.z; 
	}

 	// Prodotto vettoriale
 	Point3D crossProduct(Point3D b) {
 		Point3D a=new Point3D(x,y,z);
		return new Point3D(a.y*b.z-a.z*b.y,
				a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x);
 	}

 	//Cerca la componente massima di un vettore in R3
 	float max() {
 		float max=x; 
 		if(y>x){max=y;} 
 		if(z>max){max=z;} 
 		return max;
 	}
	   
 	//copia del vettore b passato come parametro
 	void copy(Point3D b) {
 		x = b.getX();
 		y = b.getY();
 		z = b.getZ();
	}
	    
 	//verificare se sono uguali due vettori
	boolean equal(Point3D b) {
		return (b.x == x) && (b.y == y) && (b.z == z);
	}
    
	//radice del vettore in R3 passato come parametro
	//(fa la radice di ogni componente)
	public Point3D getSquareCompPoint(Point3D b) {
		Point3D a=new Point3D();
		a.x=(float) Math.sqrt(b.x);
		a.y=(float) Math.sqrt(b.y);
		a.z=(float) Math.sqrt(b.z);

		return a;
	}
    
	// riflessione di un vettore i rispetto alla normale
	//n: R=2<N,I>N-I
	Point3D reflect(Point3D i, Point3D n){
		return n.multiplyScalar(n.dotProduct(i))
						.multiplyScalar(2.0f)
						.subtract(i);
	}

  // rifrazione di un vettore i rispetto ad una normale
  //n: T=N(ior<N,I>-sqrt(1-(ior)^2(1-(<N,I>)^2)))-ior*I
  // in questa funzione la rifrazione non varia con la
  //lunghezza d'onda

  Point3D getRefraction(Point3D direction, Point3D normal, float refractionIndex){
		// direction definito come "i" nei commenti
		// normal definito come "n" nei commenti

  	//ci si accerta che il vettore in entrata sia
  	//normalizzato
  	direction.getNormalizedPoint();

  	//si calcola il coseno tra la normale e il vettore
  	//entrante i: <n,i>
  	float cosThetaI = normal.dotProduct(direction);

  	//si prende in esame l'indice di rifrazione ior
  	//del materiale. Per semplificare il calcolo viene
  	//considerato solamente il passaggio dal vuoto,
  	//non e' quindi possibile con questa funzione
  	//modellare il passaggio di luce tra due materiali
  	//con indice di rifrazione differente
  	//indice di rifrazione nel vuoto (=1) / indice di
  	//rifrazione del materiale
  	float eta=1/refractionIndex;

  	//se il coseno dell'angolo tra il vettore i e n e'
  	//minore di 0 allora dobbiamo invertire la normale
  	//e l'indice di rifrazione, ovvero il passaggio
  	//avverra' dal mezzo denso fino al vuoto
  	// (di conseguenza il coseno diventera' positivo)
  	if (cosThetaI<0) {
  		cosThetaI=-cosThetaI;
  		eta=(float) (1.0/eta);
  		normal = normal.multiplyScalar(-1);
  	}

  	//calcolo dei fattori necessari
  	float sin2ThetaI = 1-(cosThetaI*cosThetaI);
  	float sin2ThetaT = eta*eta*sin2ThetaI;

  	// se questo coefficente e' minore di 0 allora
  	//avviene una riflessione totale e il resto del
  	//calcolo non viene effettuato
  	float K= 1 - sin2ThetaT;
  	if (K<0) {
  		//riflessione totale
			return new Point3D(-1.0f);
  	}	else {
  		//altrimenti si procede con il calcolo del
  		//vettore rifratto
  		float cosThetaT= (float) Math.sqrt(K);
  		float angle = eta*cosThetaI - cosThetaT;
			return normal.multiplyScalar(angle)
							.subtract(direction.multiplyScalar(eta));
  	}
  }

  // rifrazione di un vettore i rispetto ad una normale
  //n: T=N(ior<N,I>-sqrt(1-(ior)^2(1-(<N,I>)^2)))-ior*I
  //restituisce Ray[]t in cui t[0]=R, t[1]=G, t[2]=B
  static Ray[] getRefraction(Ray[] rays, Point3D direction, Point3D normal,
													 Point3D refractionIndex) {
		//utilizziamo la proprieta' depth del raggio per
  	//verificare se c'e' riflessione totale
		rays[0].depth = 1;
		rays[1].depth = 1;
		rays[2].depth = 1;

		direction.getNormalizedPoint();
		// <n, i>
  	float cosThetaI = normal.dotProduct(direction);
  	Point3D eta = new Point3D(1.0f).divideComponents(refractionIndex);
     //se l'angolo di incidenza e' superiore a pi/2
  	//allora devo cambiare il verso della normale e
  	//cambiare indice di rifrazione (prendendo il suo
  	//inverso)

  	if (cosThetaI < 0.0f) {
  		cosThetaI = -cosThetaI;
  		normal = normal.multiplyScalar(-1.0f);
  		eta=new Point3D(1.0f).divideComponents(eta);
  	}

  	Point3D sin2ThetaT = eta.multiplyComponents(eta).multiplyScalar(
						1 - (cosThetaI*cosThetaI));

    Point3D K = new Point3D(1.0f).subtract(sin2ThetaT);

    //componente rossa
		if (K.x<0.0f) {
  		// TIR
			//radice negativa niente rifrazione
  		rays[0].depth=0;
  	} else {
			float cos_theta_t= (float) Math.sqrt(K.x);
			float angle=eta.x*cosThetaI-cos_theta_t;
  		rays[0].d= normal.multiplyScalar(angle);
  		rays[0].d= rays[0].d.subtract(direction.multiplyScalar(eta.x));
  	}

		//componente verde
		if (K.y<0.0f) {
  		rays[1].depth=0;
  	} else {
  		float cos_theta_t= (float) Math.sqrt(K.y);
  		float angle =eta.y*cosThetaI-cos_theta_t;
  		rays[1].d= normal.multiplyScalar(angle);
  		rays[1].d= rays[1].d.subtract(direction.multiplyScalar(eta.y));
  	}

		//componente blu
		if (K.z<0.0f) {
  		rays[2].depth=0;
  	} else {
  		float cos_theta_t= (float) Math.sqrt(K.z);
  		float angle=eta.z*cosThetaI-cos_theta_t;
  		rays[2].d= normal.multiplyScalar(angle);
  		rays[2].d= rays[2].d.subtract(direction.multiplyScalar(eta.z));
		}

		return rays;
  }
    
  //costringe un numero in [0,1]
	public static float clamp(float x) {
		if(x<0)
			return 0;
		else if (x>1)
			return 1;
		else
			return x;
	}

    //costringe le componenti di un vettore in R3 in [0,1]
  public static Point3D clamp3(Point3D f) {
  	float xf=clamp(f.x);
  	float yf=clamp(f.y);
  	float zf=clamp(f.z);
		return new Point3D(xf,yf,zf);
  }
}
