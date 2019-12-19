//classe per definire una mesh: una raccolta di vertici, 
//lati e facce che definiscono la forma, quindi la
//modellazione solida di un oggetto poliedrico.
//Le facce di solito sono costituite da triangoli 
//(maglia triangolare), quadrilateri o altri poligoni 
//convessi semplici, poiche' cio' semplifica il rendering, 
//ma puo' anche essere composto da poligoni concavi piu' 
//generali e sfere. 
//Nel nostro caso considereremo solo sfere o triangoli.
//La classe contiene anche un metodo per settare il 
//materiale di una mesh e un metodo per spezzare in 
//due parti uguali ogni triangolo della mesh (e' ovvio che 
//il metodo non funziona per mesh di sfere) 
public class Mesh {
	//nome della mesh
    String nome;
    
    //array di puntatori ad oggetti
    public Obj[] objects;
    
    //numero di oggetti presenti nella mesh
    public int nObj=0;
       
    
    // costruttore Mesh
    public Mesh(String n, Obj[] o,int nO){
        nome= n;
        //array di oggetti contenuti nella mesh 
        objects=o;
        if(objects.length!=nO)
        	nO=objects.length;
        //numero di oggetti
        nObj=nO;
    }
    
    //metodo per impostare il materiale degli oggetti 
    //presenti nella mesh
    void setMaterial(int m){
        for(int i=0; i<nObj;i++){
            objects[i].setMaterial(m);
        }
    }
    
    //funzione valida solo per Mesh di triangoli
    //il metodo suddivide i triangoli della Mesh in due, 
    //duplicando quindi il numero di triangoli della Mesh
    //nel nostro caso sceneDepth=0, quindi non si usa mai
    //questo metodo
    void suddividi(){
        //nuovo array dei triangoli
        Obj[] objects2 =new Obj[nObj*2];
        int n=0;
        
        for(int i=0; i<nObj; i++){
            //valido solamente su triangoli
            if(objects[i].t!=null){
                //carico i vertici del triangolo
                Point3D[] v=objects[i].t.vertices;
                //creo i lati del triangolo
                Point3D l[]={ v[2].subtract(v[0]),
                		v[0].subtract(v[1]),v[1].
                        subtract(v[2])};
                //cerco il lato piu' lungo del triangolo
                int pos=0;
                if(l[1].normalize()>l[pos].normalize())pos=1;
                if(l[2].normalize()>l[pos].normalize())pos=2;
                
                //cerco il punto a meta' del lato piu' 
                //lungo del triangolo
                float val=(float) (l[pos].normalize()*0.5);
                //normalizzo il lato piu' lungo
                l[pos].getNormalizedPoint();
                //carico i nuovi vertici del triangolo
                Point3D nv[]={v[(pos+1)%3],v[(pos+2)%3],
                		v[pos],l[pos].multiplyScalar(val).
                        add(v[pos])};
                //creo i nuovi due triangoli
                objects2[n]=new Obj(new Triangle(nv[0],
                		nv[1],nv[3]),objects[i].matId);
                n++;
                objects2[n]=new Obj(new Triangle(nv[0],
                		nv[3],nv[2]),objects[i].matId);
                n++;
            }
            
        }
        //aggiorno l'array degli oggetti e il numero di 
        //oggetti della mesh
        objects= objects2;
        nObj=nObj*2;
    }

    public static void main(String[] args) {
    	
	}
 
}
