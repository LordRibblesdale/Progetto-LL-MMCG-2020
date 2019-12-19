//classe che definisce un triangolo attraverso un array di
//float3 contenente i 3 vertici del triangolo, e la sua
//normale.
//La classe ha la funzione che calcola l'intersezione 
//del triangolo con un raggio, e la funzione che 
//restituisce la normale al triangolo
public class Triangle {
	//array dei vertici 
	public Point3D[]vertices;
	//Normale al triangolo
	public Point3D n;
	
	//costruttore di default
	public Triangle() {
		
	}
	
	//costruttore:i parametri in input sono i tre vertici
	//del triangolo
	public Triangle(Point3D v0, Point3D v1, Point3D v2){
       
        // triangolo definito da un array di 3 float3 
		vertices = new Point3D[3];
		vertices[0]=v0;
		vertices[1]=v1;
		vertices[2]=v2;
        
        //calcoliamo la normale con un metodo apposito
		n=tNormalCalc();
	}
	
	//vettore normale e' ottenuto tramite prodotto 
	//vettoriale di a e b
	Point3D tNormalCalc() {
		//calcolo dei vettori appartenenti al piano del 
		//triangolo a e b
		Point3D a=vertices[1].subtract(vertices[0]);
		Point3D b=vertices[2].subtract(vertices[0]);
		Point3D n=(a.crossProduct(b)).getNormalizedPoint();
		return n;
	}
	
	//funzione di intersezione con un raggio: Return 
	//distanza o -1.0f se non c'e' intersezione
	float intersect(Ray r) {
		
		//risolvo il sistema o+Td=a1+ beta(b1-a1) + 
		//gamma(c1-a1) => riscritto come [ beta(a1-b1)+
		//gamma(a-c1)+Td1 = a-o ]  dove T,beta,gamma sono 
		//le incognite e a1 b1 e c1 vertici del triangolo,
	    //d1 e' invece la direzione del raggio
	       
		//Componenti note:
		
	    //componenti X
	        
	    // (a1-b1).x
		double a = vertices[0].x-vertices[1].x;
	    // (a1-c1).x
		double b=vertices[0].x-vertices[2].x;
	    //d1.x
		double c=r.d.x;
	    // (a-o).x
		double d=vertices[0].x-r.o.x;

	    //componenti Y
		double e=vertices[0].y-vertices[1].y;
		double f=vertices[0].y-vertices[2].y;
		double g=r.d.y;
		double h=vertices[0].y-r.o.y;

	    //componenti Z
		double i=vertices[0].z-vertices[1].z;
		double j=vertices[0].z-vertices[2].z;
		double k=r.d.z;
		double l=vertices[0].z-r.o.z;

	    //ora ho tutte le componenti del sistema , lo 
		//risolvo (inizio a calcolare beta, gamma e t)
	    // | a  b   c  ||d|
	    // | e  f   g  ||h|
	    // | i  j   k  ||l|
	            
		double m=f*k-g*j;
		double n=h*k-g*l;
		double p=f*l-h*j;

		double q=g*i-e*k;
		double s=e*j-f*i;

		double inv_denom=1.0/(a*m+b*q+c*s);

		double e1=d*m-b*n-c*p;
		double beta=e1*inv_denom;

		if(beta<0.0){
			return(-1.0f);
		}

		double r1=e*l-h*i;
		double e2=a*n+d*q+c*r1;
		double gamma=e2*inv_denom;

		if(gamma<0.0){
			return(-1.0f);
		}

		if(beta+gamma>1.0){
			return(-1.0f);
		}

		double e3=a*p-b*r1+d*s;
		float t=(float) (e3*inv_denom);

		if(t<Utilities.EPSILON)
			return -1.0f;
		else
			return t;
	}
	
	//funzione che restituisce la normale del triangolo
    Point3D normal(){
        return n;
    }
    
		
	public static void main(String[] args) {
		 
	}
		
}
