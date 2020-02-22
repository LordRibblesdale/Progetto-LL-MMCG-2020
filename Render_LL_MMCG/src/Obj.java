import java.util.ArrayList;

//La classe Obj funge da "padre" delle varie geometrie
//presenti nel programma (Triangle e Sphere); grazie a
//questa i vari algoritmi agiscono allo stesso modo su 
//ogni geometria.
//la classe contiene i seguenti metodi:
//calcolo della normale all'oggetto
//calcolo per ottenere un punto random sulla superficie 
//dell'oggetto
//calcolo dell'area dell'oggetto
//verifica dell'intersezione tra un oggetto e un raggio
//calcolo dei valori min e max del bounding box 
//dell'oggetto: i due valori min e max delimitano il
//volume entro cui e' contenuto l'oggetto in questione
public class Obj {
	Sphere s=null;
	Triangle t=null;
	
	//bounding box dell'oggetto:
	Point3D min=new Point3D();
	Point3D max=new Point3D();
	    
	//area dell'oggetto
	float areaObj;
	    
	//potenza emessa dall'oggetto: viene utilizzata 
	//all'interno dell'algoritmo che calcola la radiosita'
	public Point3D P=new Point3D();
	    
	//matId e' l'indice che fara' riferimento alla lista
    //dei materiali, quindi determina il materiale dell'Obj
	int matId;

	//costruttore per Obj sfera
	public Obj(Sphere sp,int nmatId) {
		s=sp;
		t=null;
		matId=nmatId;
	       
		//calcolo l'area dell'oggetto
		areaObj=area();

		//calcolo il bounding box dell'oggetto
		Point3D r= new Point3D(s.rad);
		min=(s.p).subtract(r);
		max=(s.p).add(r);
	}
    
	//costruttore per Obj triangolo
	public Obj(Triangle tr,int nmatId){
		s=null;
		t=tr;
		matId=nmatId;
		//calcolo l'area dell'oggetto
		areaObj=area();

		//calcolo il bounding box dell'oggetto
		max.x=Math.max(t.vertices[0].x, Math.max(t.vertices[1].x, t.vertices[2].x));
		max.y=Math.max(t.vertices[0].y, Math.max(t.vertices[1].y, t.vertices[2].y));
		max.z=Math.max(t.vertices[0].z, Math.max(t.vertices[1].z, t.vertices[2].z));

		min.x=Math.min(t.vertices[0].x, Math.min(t.vertices[1].x, t.vertices[2].x));
		min.y=Math.min(t.vertices[0].y, Math.min(t.vertices[1].y, t.vertices[2].y));
		min.z=Math.min(t.vertices[0].z, Math.min(t.vertices[1].z, t.vertices[2].z));
	}
    
	void setMaterial(int m){
		matId=m;
	}

	//richiamo rispettivamente i metodi di triangle o
	//sphere
	Point3D normal(Point3D iP){
		if(s!=null)
			return s.normal(iP);

		if(t!=null)
			return t.normal();
		else
			return new Point3D();
	}

	//metodo che restituisce un punto random sulla
	//superficie dell'oggetto
	Point3D randomPoint(float rnd1, float rnd2, float rnd3){
		if(s!=null){
			float cos1=(float) Math.cos(rnd1);
			float cos2=(float) Math.cos(rnd2);
			float sin1=(float) Math.sin(rnd1);
			float sin2=(float) Math.sin(rnd2);
			Point3D r=new Point3D(cos1*cos2,cos1*sin2,sin1);
			return (s.p).add(r);
		}
		else if(t!=null){
			float d=rnd1+rnd2+rnd3;
			rnd1 /= d;
			rnd2 /= d;
			rnd3 /= d;
			Point3D ret1=t.vertices[0].multiplyScalar(rnd1);
			Point3D ret2=t.vertices[1].multiplyScalar(rnd2);
			Point3D ret3=t.vertices[2].multiplyScalar(rnd3);
			Point3D ret=ret1.add(ret2);
			ret=ret.add(ret3);

			return ret;
		}
		else
			return new Point3D();
	}
    
	//metodo che calcola l'area dell'oggetto
	float area(){
		if(s!=null){
			//area sfera: 4*pigreco*r
			return 4* Utilities.MATH_PI *s.rad;
		}
		else if(t!=null){
			//prendo i vertici del triangolo
			Point3D[] v= t.vertices;
			//calcolo dell'area del triangolo
			Point3D l1=v[1].subtract(v[0]);
			Point3D l2=(v[2].subtract(v[0]));
			//utilizziamo l'altezza che e' data da il
			//primo lato per il seno dell'angolo compreso
			//tra i due vettori
			float l1_norma=(float) Math.sqrt(l1.x*l1.x+l1.
					y*l1.y+l1.z*l1.z);
			float l2_norma=(float) Math.sqrt(l2.x*l2.x+l2.
					y*l2.y+l2.z*l2.z);
			double cosl1_l2=l1.dotProduct(l2)/(l1_norma*l2_norma);
			float sinl1_l2=(float) Math.sqrt(1-cosl1_l2*
					cosl1_l2);

			return l1_norma*sinl1_l2*l2_norma;
		}
		else
			return 0.0f;
	}
    
	//controlla se il raggio passato come parametro
	//interseca l'oggetto
	double intersect(Ray raggio) {
		//richiamo rispettivamente i metodi di triangle o
		//sphere
		if(s!=null){
			return s.intersect(raggio);
		}
		else if(t!=null){
			return t.intersect(raggio);
		}
		else
			return 0.0f;
	}
    
	//aggiorna (considerando il paramero oldMax) il valore
	//massimo del bound che circonda gli oggetti contenuti
	//nell'array objects
	static Point3D getBoundMax(ArrayList<Obj> objects, Point3D oldMax){
		//per ogni oggetto
		for (Obj object : objects) {
			//se l'oggetto e' un triangolo
			if (object.t != null) {

				Triangle t = object.t;

				//per ogni vertice
				for (int j = 0; j < 3; j++) {
					//si controlla se il vertice e' il massimo
					//tra i vertici controllati finora (se lo
					//e' lo imposto come massimo)
					if (t.vertices[j].x > oldMax.x)
						oldMax.x = t.vertices[j].x;
					if (t.vertices[j].y > oldMax.y)
						oldMax.y = t.vertices[j].y;
					if (t.vertices[j].z > oldMax.z)
						oldMax.z = t.vertices[j].z;
				}
			}

			//se l'oggetto e' una sfera
			if (object.s != null) {
				//carico il centro della sfera
				Point3D sp = object.s.p;
				//carico il raggio della sfera
				float rad = object.s.rad;

				//viene preso in esame il bounding box
				//della sfera quello cioe' il cubo di
				//lato 2*rad centrato sulla sfera
				if (sp.x + rad > oldMax.x) oldMax.x = sp.x + rad;
				if (sp.y + rad > oldMax.y) oldMax.y = sp.y + rad;
				if (sp.z + rad > oldMax.z) oldMax.z = sp.z + rad;
			}
		}

		return oldMax;
	}
    
	//aggiorna (considerando il paramero oldMin) il valore
	//minimo del bound che circonda gli oggetti contenuti
	//nell'array objects
	static Point3D getBoundMin(ArrayList<Obj> objects, Point3D oldMin){
		//per ogni oggetto
		for (Obj object : objects) {

			//se l'oggetto e' un triangolo
			if (object.t != null) {

				Triangle t = object.t;

				//per ogni vertice
				for (int j = 0; j < 3; j++) {
					//si controlla se il vertice e' il minimo
					//tra i vertici controllati finora (se lo
					//e' lo imposto come minimo)
					if (t.vertices[j].x < oldMin.x)
						oldMin.x = t.vertices[j].x;
					if (t.vertices[j].y < oldMin.y)
						oldMin.y = t.vertices[j].y;
					if (t.vertices[j].z < oldMin.z)
						oldMin.z = t.vertices[j].z;
				}
			}
			//se l'oggetto e' una sfera
			if (object.s != null) {

				//carico il centro della sfera
				Point3D sp = object.s.p;
				//carico il raggio della sfera
				float rad = object.s.rad;

				//viene preso in esame il bounding box
				//della sfera quello cioe' il cubo di
				//lato 2*rad centrato sulla sfera
				if (sp.x - rad < oldMin.x)
					oldMin.x = sp.x - rad;
				if (sp.y - rad < oldMin.y)
					oldMin.y = sp.y - rad;
				if (sp.z - rad < oldMin.z)
					oldMin.z = sp.z - rad;
			}
		}

		return oldMin;
	}

	void translate(Point3D direction) {
		if (t != null) {
			t.translate(direction);
		} else {
			s.translate(direction);
		}
	}

	void rotateTriangleOnly(Point3D axis, double phi) {
		if (t != null) {
			t.rotate(axis, phi, true);
		}
	}

	void rotateTriangleInSpace(Point3D axis, double phi) {
		if (t != null) {
			t.rotate(axis, phi, false);
		}
	}
}
