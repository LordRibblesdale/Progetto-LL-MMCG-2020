import java.util.ArrayList;
//sistemare i commenti
public class Octree {

	//vertici che definiscono il box dell'octree:
    Point3D[] V=new Point3D[2];
    
    //centro pesato in base alla posizione degli oggetti presenti nel box dell'octree:
    Point3D center=new Point3D();//=new float3();
    
    //oggetti che il box dell'octree contiene:
    int nObj=0;
    Obj[] objects;
    
    // puntatori a Box
    //questi corrispondono agli 8 figli del box dell'octree
    Octree[] leaf= null;
    
	public Octree(Point3D min, Point3D max) {
		V[0]=min;
        V[1]=max;
	}
	
	void setObjects(Obj[] o,int nO){
        objects=o;
        nObj=nO;
        
        //calcolo del punto centrale del box
        for(int i=0; i<nObj; i++){
        	
            if(objects[i].t!=null){
                for(int j=0; j<3; j++){ 
                	center=center.add(objects[i].t.vertices[j].divideScalar(3*nObj));
                }
            }
            if(objects[i].s!=null){
                center=center.add(objects[i].s.p.multiplyScalar(1.0f).divideScalar(nObj));
            }
        }
    }
	
	boolean intersect(Ray r){
        //si inizializza il parametro di intersezione del raggio
        
        //parametro di entrata dal box
        double Tnear=Float.NEGATIVE_INFINITY;
        //parametro di uscita dal box
        double Tfar=Float.POSITIVE_INFINITY;
        
        //si carica il raggio r dentro due array di 3 dimensioni
        double[] d={r.d.x,r.d.y,r.d.z};
        double[] o={r.o.x,r.o.y,r.o.z};
        
        //si carica il minimo e massimo del box dentro un array di 3 dimensioni
        double[] min={V[0].x,V[0].y,V[0].z};
        double[] max={V[1].x,V[1].y,V[1].z};
        
        //corrispondenze 
        //i=0 asse x
        //i=1 asse y
        //i=2 asse z
        
        //per ogni coppia di piani
        for(int i=0;i<3;i++){
            
            //si controlla se il raggio e' ortogonale all'asse i
            if(d[i]==0){
                //se e' ortogonale mi basta controllare che l'origine del raggio non sia all'interno del box
                if((o[i]<min[i])||(o[i]>max[i])){
                    return false;
                }
            }else{
                
                //altrimenti mi ricavo le intersezioni del raggio con i piani i nelle posizioni min[i] e max[i]
                double T1= (min[i] -o[i])/d[i];
                double T2= (max[i] -o[i])/d[i];
                
                //si ordinano dal piu' piccolo al piu' grande le intersezioni
                if(T1>T2) {//swap(T1,T2);
                	double app=T2;
                    T2=T1;
                    T1=app;
                }
                //si salva il t piu' vicino piu' grande
                if(T1>Tnear) Tnear=T1;
                //si salva il t piu' lontano piu' piccolo
                if(T2<Tfar) Tfar=T2;
                
                //se non c'e' intersezione tra i due segmenti allora non c'e' intersezione col box
                if(Tnear>Tfar){return false;}
                
                // se il raggio interseca nella direzione sbagliata allo stesso modo non c'e' intersezione col box
                if(Tfar<0){return false;}
                
            }
        }
        
        //se si superano tutti i controlli vuol dire che il box e' stato intersecato
        return true;
    }
	
	// divide gli oggetti tra i leaf del box padre basandosi sugli oggetti che questo contiene
    void setLeafObj(){
        
        //creo 8 std::vector che conterranno gli array di oggetti appartenenti ai leaf del box
    	ArrayList<Obj> leaf0_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf1_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf2_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf3_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf4_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf5_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf6_Objects = new ArrayList<Obj>();
    	ArrayList<Obj> leaf7_Objects = new ArrayList<Obj>();
    	
    	ArrayList<ArrayList<Obj>> leaf_Objects = new ArrayList<ArrayList<Obj>>();
    	leaf_Objects.add(0, leaf0_Objects);
    	leaf_Objects.add(1, leaf1_Objects);
    	leaf_Objects.add(2, leaf2_Objects);
    	leaf_Objects.add(3, leaf3_Objects);
    	leaf_Objects.add(4, leaf4_Objects);
    	leaf_Objects.add(5, leaf5_Objects);
    	leaf_Objects.add(6, leaf6_Objects);
    	leaf_Objects.add(7, leaf7_Objects);
    	
    	//ArrayList<Obj>[] leaf_Objectss = new ArrayList<Obj>();
    	//ArrayList<Obj> leaf2_Objects = new ArrayList<Obj>();
    	//std::vector<Obj*> leaf_Objects[8];
        
        for(int i=0;i<nObj;i++){
            
            //vogliamo scoprire in quale leaf e' l'oggetto
            boolean inleaf[]={false,false,false,false,false,false,false,false};
            
            //se l'oggetto e' un triangolo
            if(objects[i].t!=null){
                
                //carico il triangolo
                Triangle t = objects[i].t;
                
                //per ogni vertice
                for (int j=0; j<3; j++){
                    //asse X
                    if(t.vertices[j].getX() < center.getX()){
                        
                        //asse Y
                        if(t.vertices[j].getY() < center.getY()){
                            
                            //asse Z
                            if(t.vertices[j].getZ() < center.getZ()){
                            	inleaf[0]=true;
                            }
                            else{
                            	inleaf[1]=true;
                            }
                            
                        //asseY
                        }
                        else{
                        	//asse Z
                            if(t.vertices[j].getZ() < center.getZ()){
                            	inleaf[2]=true;
                            }
                            else{
                            	inleaf[3]=true;
                            }
                        }
                        
                    //asse X
                    }
                    else{
                    	//asse Y
                        if(t.vertices[j].getY() < center.getY()){
                        	
                            //asse Z
                            if(t.vertices[j].getZ() < center.getZ()){
                                inleaf[4]=true;
                            }
                            else{
                            	inleaf[5]=true;
                            }
                            
                            //asseY
                        }
                        else{
                        	//asse Z
                            if(t.vertices[j].getZ() < center.getZ()){
                            	inleaf[6]=true;
                            }
                            else{
                            	inleaf[7]=true;
                            }    
                        }
                    }
                }
            }
            
            // altrimenti per qualsiasi altro tipo di oggetto utilizziamo il bounding box dell'oggetto per verificare in quale box l'oggetto e' presente
            else{

            	Point3D minObj= objects[i].min;
                Point3D maxObj= objects[i].max;

                if(minObj.getX() < center.getX()){
                	if(minObj.getY() < center.getY()){
                		if(minObj.getZ() < center.getZ()){
                			inleaf[0]=true;
                		}
                        if(maxObj.getZ() > center.getZ()){
                        	inleaf[1]=true;
                        }
                    }
                    if(maxObj.getY() > center.getY()){
                    	if(minObj.z<center.z){
                            inleaf[2]=true;
                        }
                        if(maxObj.z>center.z){
                        	inleaf[3]=true;
                        }
                    }
                }
                if(maxObj.x>center.x){
                	if(minObj.y<center.y){
                        if(minObj.z<center.z){
                            inleaf[4]=true;
                        }
                        if(maxObj.z>center.z){
                            inleaf[5]=true;
                        }
                    }
                    if(maxObj.y>center.y){
                        if(minObj.z<center.z){
                            inleaf[6]=true;
                        }
                        if(maxObj.z>center.z){
                            inleaf[7]=true;
                        }
                    }
                }
            }
            
            //carico l'oggetto nel vettore relativo
            for(int k=0; k<8; k++){
            	if(inleaf[k]) { 
                	leaf_Objects.get(k).add(leaf_Objects.get(k).size(), objects[i]);
                }
            }
            
        }
        
        
        
        //ora che so' la grandezza degli array posso crearli nella giusta dimensione
        for(int k=0; k<8;k++){
            int nO=(int)leaf_Objects.get(k).size();
            leaf[k].nObj=nO;
            leaf[k].objects=new Obj[nO];
            
            for(int i=0; i<nO;i++){
                leaf[k].objects[i]=leaf_Objects.get(k).get(i);
                
                //calcolo del centro dell
                for(int i1=0; i1<nObj; i1++){
                    if(objects[i1].t!=null){
                        for(int j=0; j<3; j++){
                            center=center.add(objects[i1].t.vertices[j].multiplyScalar(1.0f).divideScalar(3*nObj));
                        }
                    }
                    if(objects[i1].s!=null){
                        center=center.add(objects[i1].s.p.multiplyScalar(1.0f).divideScalar(nObj));
                    }
                }
            }
        }
    }
}
