//import java.util.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
//questo codice calcola la radiosita' di una scena in 
//base al metodo iterativo di Jacobi stocastico.
//Indicazioni per l'utente:
//Ci sono due scelte per la visualizzare dell'immagine 
//finale: una di radiosita' pura ed una di radiosita' 
//seguita da illuminazione riflessa indiretta 
//(impostando true il parametro Fg) che permette di
//ottenere un'immagine fotorealistica
//Si puo' scegliere se visualizzare sfere
//di vetro o di giada impostando true o false 
//(solo uno puo' essere true) i parametri 
//jade e glass.
//Si puo' inoltre scegliere se visualizzare allineate
//o sovrapposte impostando true o false il parametro
//aligned (la scelta true e' consigliata per 
//le sfere in vetro, cosi' da poter apprezzare le 
//multiriflessioni).
//Tutti i parametri tra cui scegliere si possono 
//trovare nelle prime righe di codice
//Si specifica che agendo sul codice e' possibile
//comunque apportare le volute modifiche sui 
//materiali, sulla posizione delle sfere, etc.
public class Main {
public Main() {}
	
//metodo: scegliere una delle due flag per 
//visualizzare il rendering con il metodo di Jacobi
//stocastico (jacob) o con il final gathering(Fg)
static boolean jacob=true;
static boolean Fg=false;
//translucentJade=true se si vuole una 
//visualizzazione con BSSRDF
static boolean translucentJade=false;
//diffusiveJade=true se vogliamo una giada 
//"diffusiva"
static boolean diffusiveJade=false;
static boolean glass=false;
static boolean aligned=false;

//nome del file in cui si andra' a salvare l'immagine
//di output
private final static String filename = "image.ppm";
//stringa contenente le informazioni da scrivere nel 
//file immagine image.ppm
private static String matrix="";

//commentare una delle due seguenti grandezze in 
//base alle esigenze di risoluzione dell'output

//larghezza dell'immagine
static int w=1080;
//altezza dell'immagine
static int h=720;

/*
//larghezza dell'immagine
static int w=900;
//altezza dell'immagine
static int h=300;
*/

//distanza dal centro della fotocamera al viewplane
static float distfilm=700.0f;
//apertura diaframma della fotocamera 
static float ap=0;
//parametri della fotocamera con sistema di riferimen
//to centrato nella scena:
//absolutePos e' vero se stiamo guardando proprio al 
//centro della scena
static boolean absolutePos=false;
//punto in cui e' posizionata la fotocamera rispetto al 
//centro della scena (0,0,0)
static Point3D eye=new Point3D(0.0f,2.0f,12.0f);
//punto guardato rispetto al centro della scena (0,0,0)
//inizialmente coincide  con il centro ma poiche' 
//absolutePos=false lo cambieremo in seguito	
static Point3D lookat=new Point3D(0.0f);
//punto messo a fuoco rispetto al centro della scena 
//(0,0,0): inizialmente coincide  con il centro ma lo 
//cambieremo in seguito
static Point3D focusPoint=new Point3D(0.0f);
//campioni del pixel: numero di campioni per ogni pixels.
//Si tratta dei punti nei pixel attraverso cui faremo 
//passare raggi
static int samps=30; 
//campioni (numero di raggi) per l'illuminazione indiretta
//(ricorsivo in global illumination, non ricorsivo in 
//final gathering)
static int aosamps=1;
//campioni (numero di raggi) illuminazione diretta (non 
//ricorsivo)
static int dirsamps=1;
//campioni scelti per le riflessioni e le rifrazioni
static int refSample=100;
//sample per lo Stochastic Jacobi, utilizzati per il 
//calcolo con Monte Carlo
static int Jacobisamps=150000; //150000000
//numero di step raggiunti dal processo Jacobi Stocastico
static int steps;
//stima dell'errore raggiunto dal processo Jacobi 
//Stocastico
static float err;
//variabile globale in cui verranno salvati gli oggetti 
//della scena (in modo da poter essere aggionati nei 
//metodi richiamati)
static Obj[] GlobalObjects;
//variabile globale, utilizzata nel metodo intersect(), 
//in cui viene salvato l'oggetto intersecato da un 
//raggio considerato 
static Obj intersObj;
//step massimi per le iterazioni di Jacobi Stocastico
static int maxsteps=15; 
//errore massimo nel processo di Jacobi Stocastico
static float maxerr=0.001f;
//densita' triangoli nella stanza
static int sceneDepth= 0;
//profondita' dell'octree
static int depth=14;
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

//se true si vede solo il muro frontale. Se si cambia in
//true, decommentare le parti in createScene()
static boolean frontWall=false;

//altezza da aggiungere alla stanza
static float hroom=1.2f;

//parametri luce:

//grandezza della luce ( se lightsize=1 e dilata=0 e' 
//grande come tutta la scena )
static Point3D scaleL=new Point3D(1.5f);
//se true la luce e' frontale. Se si cambia in true, 
//deccommentare le parti in createScene()
static boolean frontL=false;
//parametro per la traslazione delle pareti della stanza
static Point3D translateL=new Point3D();
//indice del materiale della luce della stanza
static int matIdL=0;

//vettore costruttore dei materiali 
static Material material[]={
	    
	    //luce
	    //0: luce bianca
		new Material(new Point3D(5.0f)),
	    //diffusivi:
	    //1: rosso diffusivo
		new Material(new Point3D(0.1f,0.0f,0.0f),
				new Point3D()),
	    //2: verde diffusivo
		new Material(new Point3D(0.05f,0.3f,0.0f),
				new Point3D()),
	    //3: blu diffusivo
		new Material(new Point3D(0.45f,0.45f,1.0f),
				new Point3D()),
	    //4: grigio chiaro diffusivo
		new Material(new Point3D(0.7f), new Point3D()),
	    
	    //5: nero
		new Material(new Point3D(), new Point3D()),
	    
	    //riflettenti:
	    //6: vetro perfetto
		new Material(new Point3D(),new Point3D(1.0f)),
	    //7: glass perfetto
		new Material(new Point3D(), new Point3D(),
				new Point3D(1.0f),new Point3D(1.55f),
				new Point3D(), 0.0f,0.0f,false),
	    
	    //materiali particolari:
	    //8: viola cook-torrance
		new Material(new Point3D(0.6f,0.1f,0.2f),new
						Point3D(5.3f,1.485f,1.485f),0.9f),
	    //9: steel
		new Material(new Point3D(),new Point3D(1.0f),
				new Point3D(),new Point3D(2.485f),new
						Point3D(3.433f), 0.0f ,0.0f,false),
	    //10: rosso scuro
		new Material(new Point3D(0.5f,0.12f,0.2f),new
						Point3D()),
	    //11: steel imperfect
		new Material(new Point3D(),new Point3D(1.0f),new
						Point3D(),new Point3D(1.485f,2.885f,2.885f),
			new Point3D(3.433f,1.433f,1.433f),0.0f,0.01f,
			false),
		//12: translucentJade (Kd=45,65,40) 
		new Material(new Point3D(0.31f,0.65f,0.246f),
				new Point3D(),new Point3D(),new Point3D
				(1.3f,1.3f,1.3f),new Point3D(),0,0,true),
		//13: rosa diffusivo
		new Material(new Point3D(1.0f,0.4f,0.4f),
		 		new Point3D()),
		//14: grigio scuro diffusivo (pavimento) 
		new Material(new Point3D(0.2f,0.15f,0.15f),
				new Point3D()),
		//15: diffusiveJade (Kd=45,65,40) 
				new Material(new Point3D(0.31f,0.65f,0.246f),
				new Point3D(),new Point3D(),new Point3D
				(1.3f,1.3f,1.3f),new Point3D(),0,0,false)
};

//vettore per gli indici dei materiali delle pareti 
//della stanza 
static int matIdRoom[]={
3, //sinistra
4, //inferiore
13, //posteriore
3, //destra
4, //superiore
4  //frontale
};

//metodo che imposta, a seconda della scelta 
//effettuata dall'utente, il materiale  
//appropriato alle prime tre sfere  
static int setMatIdSphere(){
	   
	int tj=12;
	int dj=15;
	int g=7;
	int one=1;
	int ret=0;
    if(translucentJade==true)
    	ret=tj;
    else if(diffusiveJade==true)
    	ret=dj;
    else if(glass==true)
    	ret=g;
    else
    	ret=one;
    return ret;
}

//static int mIS=setMatIdSphere();
// vettore per gli indici dei materiali delle sfere
// si fa corrispondere alla sfera i-esima del vettore
// spheres definito poco sotto, il materiale di
// indice corrispondente nel vettore material
static int matIdSphere[]={
	1,//mIS,
	1,//mIS,
	1,//mIS,
	1,//7,
	1,//7,
	1,//7,
	1,//4,
	1,//4,
	1,//4,
	1,//4
};

//metodo che imposta, a seconda della scelta 
//effettuata dall'utente, la posizione  
//appropriata alle prime tre sfere  
//Prende come parametro l'indice dell'array
//spheres di cui si deve settare la posizione
static Point3D setSpheresPosition(int index){
	   
	Point3D ret;
	if(aligned==true)
    {
    	switch(index) {
	    	case 0:
	    		ret=new Point3D(-1.0f,0.0f,0.0f);
	    		break;
	    	case 1:
	    		ret=new Point3D(-5.0f,0.3f,0.8f);
	    		break;
	    	case 2:
	    		ret=new Point3D(3.5f,0.5f,3.3f);
	    		break;
	    	default:
	    		ret=new Point3D(0.0f);
    	}
    }
    else
    {
    	switch(index) {
    	case 0:
    		ret=new Point3D(-4.0f,0.0f,0.0f);
    		break;
    	case 1:
    		ret=new Point3D(-7.0f,0.0f,0.0f);
    		break;
    	case 2:
    		ret=new Point3D(-4.0f,0.0f,5.3f);
    		break;
    	default:
    		ret=new Point3D(0.0f);
    	}
    }
    return ret;
}

//static float3 sPos0=setSpheresPosition(0);
//static float3 sPos1=setSpheresPosition(1);
//static float3 sPos2=setSpheresPosition(2);
//vettore costruttore delle sfere 
static Sphere spheres[]={
		
		new Sphere(1,new Point3D()),//(1,sPos0),
		new Sphere(1,new Point3D()),//(1,sPos1),
		new Sphere(1,new Point3D()),//(1,sPos2),
		new Sphere(1,new Point3D(4.0f,0.1f,1.4f)),
		new Sphere(1,new Point3D(-3.0f,0.4f,2.3f)),
		new Sphere(1,new Point3D(-4.0f,2.0f,4.0f)),
		new Sphere(1,new Point3D(6.0f,5.3f,2.0f)),
		new Sphere(1,new Point3D(7.0f,3.4f,1.4f)),
		new Sphere(1,new Point3D(-7.0f,4.0f,2.1f)),
		new Sphere(1,new Point3D(-4.0f,0.9f,0.5f))
    
};

//numero delle sfere effettivamente considerate tra 
//quelle definite in spheres[]
static int nSphere=3;

//static boolean drawSphere=true;

// Background: lo impostiamo come nero
static Point3D background=new Point3D(1.0f,1.0f,1.0f);

//distanza massima dal raggio
static float inf=(float) 1e20;

//variabile globale, utilizzata nel metodo intersect(), 
//in cui viene salvato punto di intersezione tra l'oggetto
//e il raggio considerato 
static float inters;//sarebbe t del metodo intersect;

// array di oggetti (in generale puo' contenere sia 
//triangoli che sfere) contenente tutti gli oggetti della
//scena
Obj[] objects;
//array dei triangoli contenente tutti i triangoli della 
//scena
Triangle[] triangles;

/// vettore in cui carichero' le luci (sono dei semplici 
//Obj che hanno pero' come materiale una luce)
static Obj[] luci;
//numero di luci
static int nLight=0;

//primo elemento della lista di Box (usato per la BSP)
static Box Bound;

//array di mesh della scena
static Mesh[] m;

//array che contiene tutti i pixel (rgb) dell'immagine
static Point3D[]image=new Point3D[w*h];

// Radianza della scena
static Point3D r;

//variabile utilizzata per visualizzare lo stato di 
//caricamento dei box durante la partizione spaziale 
//della scena
//rappresenta i box che sono stati caricati
static int s=0;
//massimi box (cioe' massima profondita') nell'albero 
//per la partizione spaziale della scena
static int S=0;
//liv e' il livello di profondita' all'interno dell'albero
//per la partizione spaziale della scena 
static int liv=0;

//campioni per la fotocamera
static int[] samplesX=new int[w*h];
static int[] samplesY=new int[w*h];

//campioni per la luce indiretta
static int[] aoSamplesX=new int[w*h];
static int[] aoSamplesY=new int[w*h];

//campioni per la luce diretta
static int[] dirSamples1=new int[w*h];
static int[] dirSamples2=new int[w*h];
static int[] dirSamples3=new int[w*h];

//campioni riflessioni/rifrazioni
static int[] refSamples1=new int[w*h];
static int[] refSamples2=new int[w*h];

//BSP=Binary Space Partition

//Intersezione con i box della ripartizione spaziale. 
//Controllando in modo ricorsivo, cerso l'intersezione 
//piu' vicina nei 2 figli di ogni box che stiamo 
//considerando, fino a trovare il punto di intersezione
//b: box da intersecare con il raggio, 
//r: raggio considerato
static boolean intersectBSP(Box b,Ray r){
   
    //se interseco il box
    if(b.intersect(r)){
    	//controllo che i figli non siano nulli (ovvero 
    	//che abbia figli)
        if((b.leaf1!=null)&&(b.leaf2!=null)){
            //se non e' nullo ricomincia con i figli
            intersectBSP(b.leaf1,r);
            intersectBSP(b.leaf2,r);
        }
        //altrimenti se non ha figli:
        else{
            //vengono salvati nell'array objects tutti 
        	//gli oggetti contenuti nel box che stiamo 
        	//considerando
            Obj[] objects= b.objects;
            //salviamo nella variabile nO il numero di
            //oggetti contenuti nel box che stiamo 
            //considerando
            int nO= b.nObj;
            
            //inizializziamo il punto di intersezione 
            //con il raggio
            float d=0;
            
            //per ogni oggetto
            for(int i=0; i<nO; i++){
        	 //nell'if si richiama il metodo intersect() 
             //dell'oggetto in questione: ricordiamo che 
             //questo metodo restituisce il valore della
        	 //distanza o -1.0f se non c'e' intersezione,
             //quindi nella prima condizione dell'if 
             //controlliamo che effetticamente ci sia un'
        	 //intersezione e assegnamo questo valore a d.
        	 //nella seconda condizione controlliamo pero'
             //che questo valore di d appena calcolato 
             //sia < del valore gia' memorizzato nella
        	 //variabile inters (dal momento che dobbiamo
             //salvare l'intersezione piu' vicina)
        	 if((d=objects[i].intersect(r))>=0.0f && 
        			 d<inters){
        		//salviamo quindi il valore di d nella 
        		//variabile globale inters
        		inters=d;
        		//e salviamo l'oggetto intersecato nella 
        		//variabile globale intersObj
        		intersObj=objects[i];
        		
        	 }//fine if
        
            }//fine for
            
        }
    }//fine if(b.intersect(r))
    //altrimenti se non si interseca il  box
    else{
    	return false;
    }
    //dobbiamo anche controllare che il valore inters sia 
    //rimasto minore della distanza massima del raggio inf
    if(inters<inf)
    { 
    	return true;
    }
    //altrimenti l'intersezione e' troppo lontana quindi 
    //non la consideriamo
    else 
    	return false;
}

//metodo che serve a calcolare l'intersezione tra un 
//oggetto e un raggio, che richiama la funzione 
//inersectBSP
static boolean intersect(Ray r, Obj oi){
    
    if((oi)==null){
    	//se l'oggetto e' nullo si richiama semplicemente 
    	//il metodo intersectBSP
    	return intersectBSP(Bound,r);
    }
    else{
    	//se l'oggetto non e' nullo, si salva in un'altra
    	//variabile o1 e poi si rende nullo, per poter 
    	//richiamare il metodo intersectBSP
    	Obj o1=oi;
        oi=null;
        if(intersectBSP(Bound,r)){
        	oi=intersObj;
            if((o1)==(oi)){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}

//metodo che genera randomicamente un valore in [0,1]
//utilizzando x come seme
static float generateRandom(int x){ 
	double a=Math.floor(Math.random() * (x-0+1)) + 0;
	//normalizzazione per riportare il valore in [0,1]
	return (float)(a/x);
}

//si crea la stanza con grandezza dipendente da max e min
//per stanza si intende un ambiente parallelepipedico
//avente pareti (composte da due triangoli per ciascuna
//parete) di un materiale scelto tra quelli presenti 
//nell'array dei materiali, una luce nel nostro caso 
//collocata sul soffitto
static Mesh createScene(Point3D max, Point3D min){
    
	//uso le variabili maxx e minn che hanno i valori di 
	//max e min per non aggiornarli dopo l'utilizzo del 
	//metodo
	Point3D maxx=new Point3D();
	maxx.x=max.x;
	maxx.y=max.y;
	maxx.z=max.z;
	Point3D minn=new Point3D();
	minn.x=min.x;
	minn.y=min.y;
	minn.z=min.z;
	
	float maxhroom=maxx.y+hroom;
	maxx.y=max.y;
	
    //definisco un array per gli 8 vertici della stanza
	//a max y aggiungo sempre hroom 
	//vertici: sinistra=min.x, destra=max.x; basso=min.y,
	//alto=max.y; dietro=min.z, davanti=max.z
    Point3D v[]={
    	//sinistra, in basso, dietro
        minn,
        //sinistra, in alto, dietro
        new Point3D(minn.x,maxhroom,minn.z),
        //sinistra, in basso, davanti
        new Point3D(minn.x,minn.y,maxx.z),
        //destra, in basso, dietro
        new Point3D(maxx.x,minn.y,minn.z),
        //destra, in alto, davanti
        new Point3D(maxx.x,maxhroom,maxx.z),
        //sinistra, in alto, davanti
        new Point3D(minn.x,maxhroom,maxx.z),
        //destra, in alto, dietro
        new Point3D(maxx.x,maxhroom,minn.z),
        //destra, in basso, davanti
        new Point3D(maxx.x,minn.y,maxx.z),
    };
    
    //Dal momento che frontWall=false, si creano 12 
    //triangoli per comporre la stanza: 10 per la stanza
    //(a cui manca appunto la faccia frontale per 
    //permettere di vedere all'interno) e 2 per la luce 
    //parallela al soffitto (perche' frontL=false)
     ArrayList<Triangle> Troom=new ArrayList<Triangle>();
    //array per gli oggetti che compongono la stanza
    Obj[] Oroom=new Obj[12];
        
    //inseriamo la luce
    //c e' la posizione centrale della stanza
    Point3D c=((maxx.add(minn)).multiplyScalar(
    		0.5f));
    //dichiaro l'array Lv che serve per i vertici della 
    //luce e lo inizializzo a (0,0,0)
    Point3D[] Lv=new Point3D[8];
    for(int i=0; i<8; i++) 
    {
    	Lv[i]=new Point3D();
    }
    //dilatazione della luce
    for(int i=0; i<8; i++){
	    //copio in Lv i vertici della stanza 
	    Lv[i].copy(v[i]);
	    //faccio poi le dovute dilatazioni nella x
	    //Lv[i].x=Lv[i].x+(Lv[i].x-c.x)*scaleL.x;
	    
	    //frontL e' false quindi entra in questo if (la 
	    //luce non e' frontale): si fanno quindi le 
	    //dovute dilatazioni nella z  
	    if(!frontL){
	    	//Lv[i].z=Lv[i].z+(Lv[i].z-c.z)*scaleL.z;
	    }
	    /*else{
	        if( (i==4 )|| (i==5) ){
	            Lv[i].y=Lv[i].y+hroom*scaleL.y;
	        }
	    }*/
    }
    
    //anche in questo caso copio i valori in delle 
    //variabili che non si aggiornano
    Point3D translateLL=new Point3D();
    Point3D Lv1=new Point3D();
    Lv1.copy(Lv[1]);
    Point3D Lv4=new Point3D();
    Lv4.copy(Lv[4]);
    Point3D Lv5=new Point3D();
    Lv5.copy(Lv[5]);
    Point3D Lv6=new Point3D();
    Lv6.copy(Lv[6]);
    
    if(!frontL){
    	//definisco i vertici dei triagoli della stanza, 
    	//traslandoli sulla y del valore 
    	//Utilities.EPS=0.01f per evitare di avere 
    	//problemi di aliasing dati dal linee 
    	//perfettamente dritte nella fase di rendering
        translateLL=new Point3D(0,-Utilities.EPSILON,0);
    	Point3D Lv1tr=new Point3D();
    	Lv1tr=(Lv1).add(translateLL);
    	Point3D Lv4tr=new Point3D();
    	Lv4tr=(Lv4).add(translateLL);
    	Point3D Lv5tr=new Point3D();
    	Lv5tr=(Lv5).add(translateLL);
    	Point3D Lv6tr=new Point3D();
    	Lv6tr=(Lv6).add(translateLL);
    	//si creano e si aggiungono i triangoli per la 
    	//luce
    	Triangle Tr0=new Triangle(Lv4tr,Lv5tr,Lv6tr);
        Troom.add(0,Tr0);
        Triangle Tr1=new Triangle(Lv1tr,Lv6tr,Lv5tr);
        Troom.add(1,Tr1);
        
    }
    
    //dilatazione delle pareti
    for(int i=0; i<8; i++){
    	v[i].x=v[i].x+(v[i].x-c.x)*scaleX;
        v[i].z=v[i].z+(v[i].z-c.z)*(scaleZ);
    }
       
    //si creano e si aggiungono i triangoli per le 
    //pareti
    //faccia laterale sinistra
    Triangle Tr2=new Triangle(v[0],v[1],v[2]);
    Troom.add(2,Tr2);
    Triangle Tr3=new Triangle(v[5],v[2],v[1]);
    Troom.add(3,Tr3);
    //faccia inferiore
    Triangle Tr4=new Triangle(v[0],v[2],v[3]);
    Troom.add(4,Tr4);
    Triangle Tr5=new Triangle(v[7],v[3],v[2]);
    Troom.add(5,Tr5);
    //faccia posteriore
    Triangle Tr6=new Triangle(v[0],v[3],v[1]);
    Troom.add(6,Tr6);
    Triangle Tr7=new Triangle(v[6],v[1],v[3]);
    Troom.add(7,Tr7);
    //faccia laterale destra
    Triangle Tr8=new Triangle(v[4],v[6],v[7]);
    Troom.add(8,Tr8);
    Triangle Tr9=new Triangle(v[7],v[6],v[3]);
    Troom.add(9,Tr9);
    //faccia superiore
    Triangle Tr10=new Triangle(v[4],v[5],v[6]);
    Troom.add(10,Tr10);
    Triangle Tr11=new Triangle(v[1],v[6],v[5]);
    Troom.add(11,Tr11);
    
    //imposto i materiali della stanza
    Oroom[0]=new Obj(Troom.get(0),matIdL);
    Oroom[1]=new Obj(Troom.get(1),matIdL);
    Oroom[2]=new Obj(Troom.get(2),matIdRoom[0]);
    Oroom[3]=new Obj(Troom.get(3),matIdRoom[0]);
    Oroom[4]=new Obj(Troom.get(4),matIdRoom[1]);
    Oroom[5]=new Obj(Troom.get(5),matIdRoom[1]);
    Oroom[6]=new Obj(Troom.get(6),matIdRoom[2]);
    Oroom[7]=new Obj(Troom.get(7),matIdRoom[2]);
    Oroom[8]=new Obj(Troom.get(8),matIdRoom[3]);
    Oroom[9]=new Obj(Troom.get(9),matIdRoom[3]);
    Oroom[10]=new Obj(Troom.get(10),matIdRoom[4]);
    Oroom[11]=new Obj(Troom.get(11),matIdRoom[4]);
  
    String str="room";
    //viene resituita una mesh che contiene i triangoli 
    //che costituiscono la stanza e la luce
    return new Mesh(str,Oroom,12);
    
}

//crea una mesh costituita da n sfere 
static Mesh caricaSphere(int n){
    
	//creo un array di Obj di n elementi
    Obj[] oSphere= new Obj[n];
    
    //per ogni elemento creo un oggetto che ha l'elemento
    //i-esimo dell'array di Sphere[] spheres e l'elemento
    //i-esimo di int[] matIdSphere (che considerera'
    //l'i-esimo materiale)
    for(int i=0; i<n; i++){
        oSphere[i]=new Obj(spheres[i],matIdSphere[i]);
    }
    String str= "sfere";
    //viene restituita una mesh delle sfere create 
    return new Mesh(str,oSphere,n);
}

//BSP (binary space partition): dato un box lo ripartisce
//in 2 box identici, tagliando il box di partenza con un
//piano indicato dal parametro "lato" della classe Box
//restituisce lo stesso box di partenza, a cui assegna
//pero' i due sottobox generati ai parametri leaf1 e
//leaf2, che in partenza erano settati a NULL
static Box makeChild(Box B){

    Point3D min= B.V[0];
    Point3D max= B.V[1];
    //l indica il piano con cui si vuole tagliare a 
    //meta' il box (vedere la classe Box per la 
    //definizione di lato)
    int l= B.lato;
    
    //taglio con il piano z=(min.z+max.z)/2 a meta' del 
    //Bound
    if(l==0){
    	//vengono costruiti i box leaf1 e leaf2 del box
        B.leaf1= new Box(min,new Point3D(max.x,max.y,(
        		min.z+max.z)/2),(short)1);
        B.leaf2= new Box(new Point3D(min.x,min.y,(
        		min.z+max.z)/2),max,(short)1);
    }
    //taglio con il piano x=(min.x+max.x)/2 a meta' del
    //Bound
    if(l==1){
    	//vengono costruiti i box leaf1 e leaf2 del box
        B.leaf1= new Box(min,new Point3D((min.x+max.x)/2,
        		max.y,max.z),(short)2);;
        B.leaf2= new Box(new Point3D((min.x+max.x)/2,min.y,
        		min.z),max,(short)2);
    }
    //taglio con il piano y=(min.y+min.y)/2 a meta' del 
    //bound
    if(l==2){
    	//vengono costruiti i box leaf1 e leaf2 del box
        B.leaf1= new Box(min,new Point3D(max.x,(min.y+
        		max.y)/2,max.z),(short)0);;
        B.leaf2= new Box(new Point3D(min.x,(min.y+max.y)/2,
        		min.z),max,(short)0);
    }
    //restituisce il Box di partenza, ma con le leaf 
    //aggiornate
    return B;
}

//metodo iterativo che crea una partizione spaziale della
//scena e divide gli oggetti di un box padre tra i suoi 
//due box figli.
//il metodo continua fino a quando non si raggiunge il 
//valore depth
//notiamo che la variabile liv (livello di profondita' 
//all'interno dell'albero) e' globale e viene 
//continuamente aggiornata nei passaggi
static Box setPartition(Box b){
    
    //procede solo se il box non e' nullo, il livello di
	//profondita' non ha superato il parametro depth e se
	//ci sono almeno un numero di oggetti maggiore del
	//valore di sogliaBox dentro al box
    if((liv<depth)&&(b!=null)&&(b.nObj>sogliaBox)){
    	//aumentiamo il livello di profondita' dell'albero
        liv++;
        
        //crea i figli del box padre
        b=makeChild(b);
        //assegna ai figli (foglie dell'albero) gli 
        //oggetti appartenti al box padre 
        b.setLeafObj();
        
        //continua la partizione iterando il procedimento
        ///finche' non si raggiunge il livello di massima
        //profondita'
        setPartition(b.leaf1);
        setPartition(b.leaf2);
        
        //variabile utilizzata per visualizzare lo stato 
        //di caricamento dei box
        s++;
        System.out.println("box caricati:  "+s
        		+"  su un massimo di  "+S
        		+"  (ogni box contenente minimo  "
        		+sogliaBox+"  oggetti)");
        //una volta finita la partizione dei figli 
        //ritorna al livello iniziale
        liv--;
    }
    //Il box di ritorno e' aggiornato con le partizioni 
    //richieste
    return b;
}

//metodo che restituisce la radianza emessa dall'oggetto 
//o in direzione r:
static Point3D Le(Ray r, Obj o) {
	//carico l'indice del material
	int mId = o.matId;
	
	//dichiato e inizializzo a 0 il valore in uscita
	Point3D radianceOutput = new Point3D();

	//con il seguente if si controlla se il materiale 
	//emette effettivamente luce
	if (material[mId].Le.max() > 0) {
		float Ler = material[mId].Le.max();
		Point3D Ler3=new Point3D(Ler);
		//con il metodo clamp3 si evita che la radianza in 
		//uscita superi il valore massimo di radianza: 1	
		Point3D.clamp3(Ler3);
		radianceOutput = Ler3;
	}
	return radianceOutput;
}

//Metodo per il calcolo dell'illuminazione diretta, cioe'
//il contributo che arriva all'oggetto direttamente dalla
//fonte di luce
//I parametri in input sono:
//r: raggio di entrata
//o: oggetto dal quale partira' il nuovo raggio
//x e y indici del seme iniziale per la generazione
//di numeri randomici
static Point3D directIllumination(Ray r, Obj o, int x, int y) {

	//inizializzo le variabili aleatorie comprese tra 0 e 
	//1 che utilizzeremo per campionare un punto all'
	//interno dell'oggetto o in maniera equidistribuita
	float rnd1 = 0;
	float rnd2 = 0;
	float rnd3 = 0;

	//si inizializza a 0 il valore in uscita dal processo
	Point3D radianceOutput = new Point3D();

	//si carica la normale all'oggetto nel punto r.o
	Point3D n1 = o.normal(r.o);

	//si carica l'identificativo del materiale
	int mId = o.matId;

	//definizione e inizializzoazione a null dell'oggetto 
    //che si andra' ad intersecare
	Obj objX;
    objX=null;

	//per ogni luce 
	for (int i = 0; i < nLight; i++) {

		//carico l'area della luce in esame
		float area = luci[i].areaObj;

		//per ogni s campione della luce tra i dirsamps 
		//campioni totali per l'illuminazione diretta
		for (int s = 0; s < dirsamps; s++) {

		  //controllo per decidere se calcolare la
		  //BRDF o la BSSRDF, quindi la calcoliamo
	      //nel punto osservato
		  //inizializziamo B che e' la variabile in 
		  //cui salviamo il valore di BRDF o la 
		  //BSSRDF
		  Point3D B;
		  //Caso BSSRDF
		  if(material[mId].translucent)
		  {
			//zv e zr sono scelti arbitrariamente in
			//modo tale da essere sufficientemente 
			//vicini alla superficie
			float zv=0.005f;
		    float dv;
		    float zr=0.0025f;
		    float dr;
		    dv=(float) (Math.random() * (150-90)+90);
		    //lo divido per non avere un numero troppo
		    //grande
		    dv=dv/10000f;
		    //rispetto la triangolarita'
		    float l2=dv*dv-zv*zv;
		    float l=(float) Math.sqrt(l2);
		    dr=(float) Math.sqrt(l2+zr*zr);
		    //r.o e' il punto in cui arriva il raggio: 
		    //per rispettare il modello di Jensen e' 
		    //necessario spostarlo di l per un
		    //angolo casuale
		    
			//utilizzo questa variabile tt perche' non 
			//posso usare il valore x+y*w nell'array
			//dirSamples1[], altrimenti l'ultimo indice
			//sarebbe fuori dal range (ricordo che la
			//misura e' w*h ma gli indici vanno da 0 a 
			//w*h-1)
			int tt =x+y*w;
			//allora faccio l'if per tt<w*h cosi' da 
			//accertarmi che non sia considerato l'indice 
			//w*h-esimo 
			if(tt<w*h) 
			{
				rnd1 = generateRandom(dirSamples1[tt]);
				rnd2 = generateRandom(dirSamples2[tt]);
				rnd3 = generateRandom(dirSamples3[tt]);
			}
			//genero due angoli casuali
			float rndPhi = 2 * Utilities.MATH_PI *(rnd1);
			float rndTeta = (float)Math.acos((float)Math.
					sqrt(rnd2));
			float cosP=(float)Math.cos(rndPhi);
			float cosT=(float)Math.cos(rndTeta);
			float sinP=(float)Math.sin(rndPhi);
			float sinT=(float)Math.sin(rndTeta);
			float px=r.o.x+l*cosP*cosT;
			float py=r.o.y+l*cosP*sinT;
			float pz=r.o.z+l*sinT;
			Point3D newPoint=new Point3D(px,py,pz);
			
			
			//si carica il punto campionato sulla luce
			Point3D p =luci[i].randomPoint(rnd1,rnd2,rnd3);

			//la direzione e' quella che congiunge il punto
			//r.o al punto campionato
			Point3D dir = (p.subtract(newPoint));
			//salviamo la distanza tra i due punti
			float norma = dir.normalize();
			dir = dir.getNormalizedPoint();
			//creazione del raggio d'ombra diretto verso 
			//la luce
			float cosTheta=r.d.dotProduct(n1);
			Point3D Ftheta=material[mId].getFresn(
					cosTheta);
			
			//peso la direzione in base al fattore di Fresnel
			//ma verifico che la direzione formi un angolo
            //di massimo 90 gradi con la normale 
			boolean okdir=true;
			while(!okdir) {
	         dir.x=dir.x*Ftheta.x;
	         dir.y=dir.y*Ftheta.y;
	         dir.z=dir.z*Ftheta.z;
	         
	         if(dir.dotProduct(n1)>0)
	            	okdir=false;
	         //altrimenti si cerca una nuova direzione
	        }//fine while(okdir)
	        
			Ray directRay= new Ray(newPoint, dir);
			
			//viene inizializzato l'oggetto che il raggio 
			//intersechera' con l'oggetto che il raggio 
			//punta
			objX = luci[i];
			//si inizializza la massima distanza a cui il
			//raggio puo' arrivare
			float t = inf;

			//verifica del fattore di visibilita' 
			if (intersect(directRay, objX)) {

				t=inters;
	        	inters=inf;
	        	objX=intersObj;
	        	intersObj=null;
				//vengono caricati i dati della luce:
				//normale nel punto p
				Point3D n2 = luci[i].normal(p);
				//identificativo del materiale della luce
				int lid = luci[i].matId;
				//calcoliamo la BSSRDF
				float cosPsi=directRay.d.dotProduct(n1);
				Point3D Fpsi=material[mId].getFresn(
						cosPsi);
				//float cosTheta=r.d.dot(n1);
				//float3 Ftheta=material[mId].getFresn(
				//		cosTheta);
				Point3D one=new Point3D(1.0f);
		    	
		    	
		    	//pi4=4*3.14
		    	Point3D pi4=new Point3D(4*Utilities.MATH_PI);
		    	//i valori si sigmas e sigmaa sono specifici per 
		    	//la giada
		    	Point3D sigmas=new Point3D(0.657f,0.786f,0.9f);
		    	Point3D sigmaa=new Point3D(0.2679f,0.3244f,0.1744f);
		    	
		    	//float3 sigmas=new float3(6.57f,7.86f,9f);
		    	//float3 sigmaa=new float3(2.679f,3.244f,1.744f);
		    	
		    	Point3D sigmat=sigmaa.add(sigmas);
		    	Point3D sigmatr=(sigmaa.multiplyComponents(sigmat)).
									multiplyScalar(3.0f);
		    	sigmatr= Point3D.getSquareCompPoint(sigmatr);
		    	Point3D alpha=sigmas.divideComponents(sigmat);
		    	
		    	Point3D expdr=sigmatr.multiplyScalar(dr*-1.0f);
		    	float dr3=dr*dr*dr;
		    	Point3D edivdr=(Point3D.exponent(expdr)).
		    			operatorDividedBy(dr3);
		    	
		    	Point3D expdv=sigmatr.multiplyScalar(dv*-1.0f);
		    	float dv3=dv*dv*dv;
		    	Point3D edivdv=(Point3D.exponent(expdv)).
		    			operatorDividedBy(dv3);
		    	Point3D rPart=(((sigmatr.multiplyScalar(dr)).
									add(one)).multiplyComponents(edivdr)).
									multiplyScalar(zr);
		    	Point3D vPart=(((sigmatr.multiplyScalar(dv)).
									add(one)).multiplyComponents(edivdv)).
									multiplyScalar(zv);
		    	
		    	Point3D Rd=(alpha.divideComponents(pi4)).multiplyComponents(rPart.
									add(vPart));
		    	
		    	B=(Rd.multiplyComponents(Fpsi).multiplyComponents(Ftheta)).
									divideScalar(Utilities.MATH_PI);
		    	
					
				//enfatizzo il colore verde
				B.x=B.x*0.31f;
			    B.y=B.y*0.65f;
			    B.z=B.z*0.246f;
			    	
				//calcolo dell'illuminazione diretta
				//vengono definiti i seguenti float3 per
				//leggibilita' del risultato
				float dirN1N2=(-dir.dotProduct(n1))*(dir.dotProduct(n2));
				float norma2=(float) Math.pow(norma, 2);
				//radianceOutput = radianceOutput + 
				//+ material[lid].Le.mult(BRDF)*(area)*
				//*(-dir.dot(n1)*dir.dot(n2)) / (pow(
				//(norma, 2));
				radianceOutput = radianceOutput.
								add(material[lid].Le.
												multiplyComponents(B).multiplyScalar(area).
												multiplyScalar(dirN1N2)).
								divideScalar(norma2);
				
			}
		  }
		  //Caso BRDF
		  else 
		  {

				int tt =x+y*w;
				if(tt<w*h) 
				{
					rnd1 = generateRandom(dirSamples1[tt]);
					rnd2 = generateRandom(dirSamples2[tt]);
					rnd3 = generateRandom(dirSamples3[tt]);
				}
				
				//si carica il punto campionato sulla luce
				Point3D p =luci[i].randomPoint(rnd1,rnd2,rnd3);

				//la direzione e' quella che congiunge il punto
				//r.o al punto campionato
				Point3D dir = (p.subtract(r.o));
				//salviamo la distanza tra i due punti
				float norma = dir.normalize();
				dir = dir.getNormalizedPoint();

				//creazione del raggio d'ombra diretto verso 
				//la luce
				Ray directRay= new Ray(r.o, dir);

				//viene inizializzato l'oggetto che il raggio 
				//intersechera' con l'oggetto che il raggio 
				//punta
				objX = luci[i];
				//si inizializza la massima distanza a cui il
				//raggio puo' arrivare
				float t = inf;

				//verifica del fattore di visibilita' 
				if (intersect(directRay, objX)) {

					t=inters;
		        	inters=inf;
		        	objX=intersObj;
		        	intersObj=null;
					//vengono caricati i dati della luce:
					//normale nel punto p
					Point3D n2 = luci[i].normal(p);
					//identificativo del materiale della luce
					int lid = luci[i].matId;
					//calcoliamo la BRDF
					B = material[mId].C_T_BRDF(
							directRay, r, n1);
					
					//calcolo dell'illuminazione diretta
					//vengono definiti i seguenti float3 per
					//leggibilita' del risultato
					float dirN1N2=(-dir.dotProduct(n1))*(dir.dotProduct(n2));
					float norma2=(float) Math.pow(norma, 2);
					//radianceOutput = radianceOutput + 
					//+ material[lid].Le.mult(BRDF)*(area)*
					//*(-dir.dot(n1)*dir.dot(n2)) / (pow(
					//(norma, 2));
					radianceOutput = radianceOutput.
									add(material[lid].Le.
													multiplyComponents(B).multiplyScalar(area).
													multiplyScalar(dirN1N2)).
									divideScalar(norma2);
					
				}
			  
		  }
			  
		  }

	}
	
	//dividiamo per tutti i sample utilizzati nell'
	//estimatore di Monte Carlo
	radianceOutput = radianceOutput.divideScalar
			(dirsamps*nLight);
	return radianceOutput;
}

//metodo per l'illuminazione indiretta 
//I parametri in input sono:
//r: raggio di entrata
//o: oggetto dal quale partira' il nuovo raggio
//x e y indici del seme iniziale per la generazione
//di numeri randomici
static Point3D FinalIndirect(Ray r, Obj o, int x, int y) {

	//inizializzo a 0 il valore che resitituiro' alla fine
	//del processo
	Point3D radianceOutput = new Point3D();

	//normale dell'oggetto in esame
	Point3D n1 = o.normal(r.o);

	int mId = o.matId;

	//per ogni s campione della luce tra gli aosamps
	//campioni totali per l'illuminazione indiretta
	for (int s = 0; s < aosamps; s++) {

		//quindi distribuito uniformemente sull'emisfero
		Point3D dir;
		float rndX = 0.0f;
		float rndY = 0.0f;

		//utilizzo questa variabile tt perche' non 
		//posso usare il valore x+y*w nell'array
		//dirSamples1[], altrimenti l'ultimo indice
		//sarebbe fuori dal range (ricordo che la
		//misura e' w*h ma gli indici vanno da 0 a 
		//w*h-1)
		int tt =x+y*w;
		//allora faccio l'if per tt<w*h cosi' da 
		//accertarmi che non sia considerato l'indice 
		//w*h-esimo 
		if(tt<w*h) 
		{
			rndX = generateRandom(aoSamplesX[tt]);
			rndY = generateRandom(aoSamplesY[tt]);
		}
		
	

		//distribuisco i numeri random sull'emisfero
		float rndPhi = 2 * Utilities.MATH_PI *(rndX);
		float rndTeta = (float)Math.acos((float)Math.
				sqrt(rndY));

		// Create onb (ortho normal basis) on iP punto di 
		//intersezione
		Point3D u, v, w;
		w = n1;
		//vettore up (simile a (0,1,0))
		Point3D up=new Point3D(0.0015f, 1.0f, 0.021f);
		v = w.crossProduct(up);
		v= v.getNormalizedPoint();
		u = v.crossProduct(w);

		float cosPhi=(float) Math.cos(rndPhi);
        float sinTeta=(float) Math.sin(rndTeta);
        float sinPhi=(float) Math.sin(rndPhi);
        float cosTeta=(float) Math.cos(rndTeta);
		dir = (u.multiplyScalar(cosPhi*sinTeta)).
						add(v.multiplyScalar(sinPhi*
				sinTeta)).add(w.multiplyScalar(
				cosTeta));
		dir=dir.getNormalizedPoint();

		//creo il raggio dal punto di intersezione ad un 
		//punto a caso sull'emisfero
		Ray reflRay=new Ray(r.o, dir);
		float t = inf;
		Obj objX;
        objX=null;
		// il metodo va' avanti solo se interseca un 
        //oggetto e se questo oggetto non e' una luce 
        //(poiche' la luminosita' diretta l'abbiamo gia'
        //considerata)
		if (intersect(reflRay, objX)) {
			
			t=inters;
        	inters=inf;
        	objX=intersObj;
        	intersObj=null;
        	if(material[(objX).matId].Le.max() == 0)
        	{
        		float _area = 1 / (objX).area();
        		radianceOutput = radianceOutput.
										add(((objX).P).multiplyComponents(
        			material[mId].Kd).multiplyScalar
        			(_area));
        	}
		}
		
	}

	radianceOutput = radianceOutput.divideScalar(
			aosamps);
	return radianceOutput;


}

//metodo per il Final Gathering, che si serve di una sola
//iterazione del raytracing stocastico, nella quale si
//raccolgono (gathering) le informazioni ottenute dalla 
//soluzione precalcolata di radiosita', attraverso il
//metodo jacobi stocastico)
//il valore restituito tiene conto dell'illuminazione
//generata dall'oggetto, del contributo dell'illuminazione
//diretta e di quello dell'illuminazione indiretta
static Point3D FinalGathering(Ray viewRay, int x, int y, Obj o)
{
	//inizializzo a 0 il valore che resitituiro' alla fine
	//del processo
	Point3D radianceOutput = new Point3D();

	//illuminazione generata
	Point3D le= Le(viewRay, o);
	radianceOutput = radianceOutput.add(le);
	
	//illuminazione diretta
	Point3D di= directIllumination(viewRay, o, x, y);
	radianceOutput = radianceOutput.add(di);
	
	//illuminazione indiretta
	Point3D fi=FinalIndirect(viewRay, o, x, y);
    radianceOutput = radianceOutput.add(fi);

	
	return radianceOutput;

}

//metodo che serve a calcolare la radianza nel punto 
//dell'oggetto che stiamo considerando
//r e' il raggio che parte dal punto dell'oggetto 
//intersecato (dal raggio della fotocamera considerato) 
//e arriva all'osservatore
//o e' l'oggetto in cui ci troviamo (quello intersecato 
//dal raggio della fotocamera che stavamo considerando)
//x indica la colonna in cui ci troviamo, y indica la 
//riga in cui ci troviamo
public static Point3D radiance(Ray r, Obj o, int x, int y){
		   
//definisco e inizializzo a (0,0,0) il float3 per la 
//radianza riflessa 
Point3D radianceRefl=new Point3D();
//definisco e inizializzo a (0,0,0) il float3 per la 
//radianza rifratta
Point3D radianceRefr=new Point3D();
    
//salvo in una variabile l'indice del materiale dell'
//oggetto considerato
int mId=o.matId;
//normale dell'oggetto nel punto osservato
Point3D n1;
n1= o.normal(r.o);
    
//coseno tra il raggio entrante e la normale
float cos_i= r.d.dotProduct(n1);
    
//fattore di Fresnel
Point3D Fresn = material[mId].getFresn(cos_i);
    
//si verifica se il materiale ha una componente speculare
//e che non sia stato superato il numero massimo di 
//riflessioni
if((material[mId].Kr.max()>0)&&
		(nRay<Utilities.MAX_DEPTH+1)){
	
 //con questo controllo si evitano le riflessioni interne
 //al materiale 
 if(cos_i>0){
 // riflessione del raggio in entrata rispetto alla 
 //normale n1
 Point3D refl= Point3D.reflect(r.d,n1);
 //si verifica che il materiale sia perfettamente 
 //speculare o che non sia la prima riflessione (si evita
 //in questo modo l'aumento esponenziale dei raggi nel 
 //caso in cui ci siano riflessioni multiple tra specchi 
 //imperfetti)
 if((material[mId].refImperfection==0)||(nRay>0)){
	//definisco e inizializzo a (0,0,0) il raggio riflesso
    Ray reflRay=new Ray();
    //l'origine e' la stessa del raggio passato come 
    //parametro
    reflRay.o=r.o;
    //la direzione e' la riflessa di quella del raggio
    //passato come parametro
    reflRay.d=refl;
    //dichiaro e inizializzo la variabile t in cui 
    //salveremo il punto di intersezione fra l'oggetto
    //considerato e reflRay
    float t=inf;
    //definizione e inizializzoazione a null dell'oggetto 
    //che si andra' ad intersecare
    Obj objX;
    objX=null;
    //intersezione del raggio con gli elementi della scena
    if(intersect(reflRay,objX)){
    	//pongo t uguale al valore di intersezione 
    	//memorizzato nella variabile globale inters
    	t=inters;
    	//resetto inters uguale a inf in modo da avere 
    	//il giusto valore di partenza la prossima volta 
    	//che si utilizzera' il metodo intersect()
    	inters=inf;
    	//salvo nell'elemento i-esimo dell'array objX 
    	//l'elemento intersecato dal raggio ffRay 
    	objX=intersObj;
    	//resetto intersObj=null in modo da avere il 
    	//giusto valore di partenza la prossima volta che 
    	//si utilizzera' il metodo intersect()
    	intersObj=null;
    	//si calcola il punto di intersezione
    	Point3D iP=(reflRay.o).add((reflRay.d).
							multiplyScalar(t));
    	//si crea il nuovo raggio
    	Ray r2=new Ray(iP,refl.multiplyScalar(-1));
    	//si aumentano il numero di riflessioni per il 
    	//raytracing
    	nRay++;
    	//si calcola la radianza riflessa utilizzando 
    	//ricorsivamente la funzione radiance
    	radianceRefl=radiance(r2,objX,x,y).multiplyComponents(material
    			[mId].S_BRDF(Fresn));
    	//si diminuiscono il numero di riflessioni
    	nRay--;
    }
            
 }//fine if per specchi imperfetti
 else
 {
	 //si aumenta il numero di riflessioni una volta per 
	 //tutti i campioni
	 nRay++;
     //per ogni campione 
     for(int s=0; s<refSample; s++)
     {
    	 //dichiaro due variabili aleatorie
    	 float random1;
         float random2;
                
         //flag per la verifica dell'orientazione del 
         //raggio
         boolean okdir=true;
         //direzione del nuovo raggio
         Point3D dir=new Point3D();
         //finche' non abbiamo un raggio con giusta 
         //orientazione
         while(okdir){
            //creazione delle variabili aleatorie 
        	//uniformi usando come semi gli elementi dell'
        	//array refSamples1
            random1= generateRandom(refSamples1[x+y*w]);
            random2= generateRandom(refSamples2[x+y*w]);
            
            //Inverse Cumulative Distribution Function 
            //del coseno modificata
            //creiamo la base ortonormale per generare 
            //la direzione del raggio riflesso reflRay
            float rndPhi=2*Utilities.MATH_PI *(random1);
            float rndTeta=(float) Math.acos((float) 
            		Math.pow(random2,material[mId].
            		refImperfection));
            
            // creazione della base ortonormale rispetto 
            //alla direzione di riflessione
            Point3D u,v,w;
            //inizializzo w uguale alla riflessione del 
            //raggio in entrata
            w=refl;
            //vettore up (simile a (0,1,0))
            Point3D up=new Point3D(0.0015f,1.0f,0.021f);
            //il prodotto vettoriale tra w e up mi 
            //genera il vettore v normale a entambi, che 
            //normalizzo
            v=w.crossProduct(up);
            v=v.getNormalizedPoint();
            //il prodotto vettoriale tra v e w mi genera 
            //il vettore u normale a entambi dal momento
            //che i vettori v e w sono gia' normali, non 
            //c'e' bisogno di noemalizzare il vettoe u)
            u=v.crossProduct(w);
            //ora che abbiamo la base ortonormale, 
            //possiamo calcolare la direzione dir
            //salvo in delle cariabili i calori di seno 
            //e coseno necessari per il calcolo di dir
            float cosPhi=(float) Math.cos(rndPhi);
            float sinTeta=(float) Math.sin(rndTeta);
            float sinPhi=(float) Math.sin(rndPhi);
            float cosTeta=(float) Math.cos(rndTeta);
            //dir=(u*(cosPhi*sinTeta))+(v*(sinPhi*sinTeta)
            //)+(w*(cosTeta)) poi normalizzato
            dir=(u.multiplyScalar(cosPhi*sinTeta)).
										add(v.multiplyScalar(sinPhi*
            		sinTeta)).add(w.
										multiplyScalar(cosTeta));
            
            //si verifica che la direzione formi un angolo
            //di massimo 90 gradi con la normale 
            if(dir.dotProduct(n1)>0)
            	okdir=false;
            //altrimenti si cerca una nuova direzione
         }//fine while(okdir)
         
         //creazione del raggio riflesso
         Ray reflRay=new Ray();
         reflRay.o=r.o;
         reflRay.d=dir;
         //massima distanza del raggio
         float t=inf;
         Obj objX;
         objX=null;
         //intersezione con gli oggetti della scena
         if(intersect(reflRay, objX)){
        	t=inters;
        	inters=inf;
        	objX=intersObj;
        	intersObj=null;
        	//punto di intersezione del raggio con 
        	//l'oggetto objX:
            Point3D iP=(reflRay.o).add(
            		(reflRay.d).multiplyScalar(t));
            //nuovo raggio:
            Ray r2=new Ray(iP,refl.multiplyScalar(-1));

            //calcolo della radianza riflessa
            radianceRefl=radianceRefl.add(
            		radiance(r2,objX,x,y).multiplyComponents(
            		material[mId].S_BRDF(Fresn)));
         }
     }
     nRay--;
     //divido per il numero di raggi che sono stati creati
     radianceRefl=radianceRefl.divideScalar(
    		 (float)refSample);
           
  }
 }
}
    

//rifrazione
//si verifica che l'oggetto abbia Kg>0, non sia un 
//metallo e non si siano superato il numero massimo di 
//riflessioni del ray tracer
if((material[mId].Kg.max()>0)&&(nRay<Utilities.MAX_DEPTH
		+1)&&(material[mId].k.max()==0))
{
 //si verifica che l'indice di rifrazione sia uguale per 
 //tutte le componenti RGB
 //in questo caso il calcolo per la rifrazione sara' 
 //semplificato
 if((material[mId].ior.x==material[mId].ior.y)&&
		 (material[mId].ior.x==material[mId].ior.z))
 {	
	//direzione del raggio rifratto:
	Point3D dir=new Point3D();
	//calcolo della direzione per il raggio rifratto:
	dir= Point3D.getRefraction(r.d,n1,
			material[mId].ior.x);
	//se c'e' effettivamente una rifrazione non interna
	if(dir!=new Point3D(-1.0f)){
	    
	    //viene creato il raggio rigratto
	    Ray refrRay=new Ray(r.o,dir);
	    
	    //si verifica il parametro di imperfezione del 
	    //materiale
	    if((material[mId].refImperfection==0)||(nRay>0))
	    {
	        float t=inf;
	        Obj objX;
	        objX=null;
	        
	        if(intersect(refrRay, objX))
	        {
	        	t=inters;
		        inters=inf;
		        objX=intersObj;
		        intersObj=null;
		        Point3D iP=(refrRay.o).add(
		        		 refrRay.d.multiplyScalar(t));
		        Ray r2=new Ray(iP,dir.multiplyScalar(-1));
		
		        nRay++;
		        //calcolo della radianza rifratta
		        radianceRefr=radiance(r2,objX,x,y).multiplyComponents(
		        		 material[mId].T_BRDF(Fresn));
		        nRay--;
	        }
	       
	    }
	    else{
	        
	    	nRay++;
	        //per ogni sample
	        for(int s=0; s<refSample; s++){
	            
	            float random1;
	            float random2;
	            //flag di controllo sulla direzione creata
	            boolean okdir=true;
	            //float3 dir;
	            
	            while(okdir){
	                
	             //variabili aleatorie uniformi in 
	             //[0,1]
	             random1= generateRandom(
	            		refSamples1[x+y*w]);
	             random2= generateRandom(
	            		refSamples1[x+y*w]);
	                
	             //distribuisco i numeri random sull'
	             //emisfero
	             float rndPhi=2*Utilities.MATH_PI *(
	            		random1);
	             float rndTeta=(float) Math.acos(
	            		(float) Math.pow(random2,
	            		material[mId].refImperfection));
	            
	             // creazione della base ortonormale 
	             //creata a partire dal raggio rifratto
	             Point3D u,v,w;
	             w=refrRay.d;
	             //vettore up (simile a (0,1,0))
	             Point3D up=new Point3D(0.0015f,1.0f,0.021f);
	             v=w.crossProduct(up);
	             v=v.getNormalizedPoint();
	             u=v.crossProduct(w);
	             //si calcola la direzione del raggio
	            
	             float cosPhi=(float) Math.cos(rndPhi);
	             float sinTeta=(float) Math.sin(rndTeta);
	             float sinPhi=(float) Math.sin(rndPhi);
	             float cosTeta=(float) Math.cos(rndTeta);
	             dir=(u.multiplyScalar(cosPhi*sinTeta)).
											 add(v.multiplyScalar(
	            		sinPhi*sinTeta)).add(
	            		w.multiplyScalar(cosTeta));
	             //si verifica che l'angolo tra la 
	             //normale e la nuova direzione sia 
	             //maggiore di 90 gradi
	             if(dir.dotProduct(n1.multiplyScalar(-1))>0)
	            	 okdir=false;
	            }//fine while(okdir)
	            //nuovo raggio:
	            Ray rRay=new Ray();
	            rRay.o=r.o;
	            rRay.d=dir;
	            
	            float t=inf;
	            Obj objX;
	            objX=null;
	            
	            if(intersect(refrRay, objX)){
	            	t=inters;
	            	inters=inf;
	            	objX=intersObj;
	            	intersObj=null;
	                //punto di intersezione
	                Point3D iP=(refrRay.o).add((
	                		refrRay.d).multiplyScalar(t));
	                Ray r2=new Ray(iP,dir.multiplyScalar(
	                		-1));
	
	                //calcolo della radianca rifratta
	                radianceRefr=radianceRefr.add
	                		(radiance(r2,objX,x,y).multiplyComponents(
	                		material[mId].T_BRDF(Fresn)));
	            }
	        }
	        nRay--;
	        //si mediano i contributi di tutti i raggi 
	        //usati
	        radianceRefr=radianceRefr.divideScalar(
	        		(float)refSample);
	        
	    }
  }
 }
 //se l'indice di rifrazione e' diverso nelle 3 
 //componeneti RGB allora si devono calcolare 3 raggi 
 //uno per ogni componente. In questo caso pero' per
 //facilitare il calcolo non viene considerato l'indice 
 //di imperfezione del materiale
 else{
        
    // rifrazone del raggio basata sulla normale
    // Raggio utilizzato per la rifrazione
    Ray[] refrRay= new Ray[3];
    refrRay[0]=new Ray();
    refrRay[1]=new Ray();
    refrRay[2]=new Ray();
    refrRay[0].o=r.o;
    refrRay[1].o=r.o;
    refrRay[2].o=r.o;
    float K[]={0,0,0};
    
    //calcolo dei 3 raggi rifratti
    refrRay= Point3D.getRefraction(refrRay,r.d,n1,material[mId].
    		ior);
    
    //carichiamo la brdf sul vettore g[] di 3 elementi 
    //cosi da accedervi piu' facilmente
    Point3D brdf= material[mId].T_BRDF(Fresn);
    float g[]={brdf.x,brdf.y,brdf.z};
    nRay++;
    //per ogni componente RGB
    for(int i=0;i<3;i++){
        //si verifica che non ci sia stata riflessione
    	//totale
        if(refrRay[i].depth!=0){
            
            float t=inf;
            Obj objX;
            objX=null;
            
            if(intersect(refrRay[i], objX)){
            	t=inters;
            	inters=inf;
            	objX=intersObj;
            	intersObj=null;
            	nRay++;
                Point3D iP=(r.o).add((refrRay[i].
                		d).multiplyScalar(t));
                Ray r2=new Ray(iP,refrRay[i].d.
												multiplyScalar(-1));

                //viene calcolato il valore di radianza 
                //del raggio
                Point3D pr= radiance(r2,objX,x,y);
                //si deve ora estrapolare la componente 
                //i di tale radianza
                float[] rg=new float[3];
            	rg[0]=pr.x;
                rg[1]=pr.y;
                rg[2]=pr.z;
                //si moltiplica infine per la componente 
                //della brdf corrispondente
                    K[i]+=rg[i]*g[i];
               }
            }
        }
        radianceRefr=new Point3D(K[0],K[1],K[2]);
        nRay--;
 }
}

//algoritmi per il calcolo dell'illuminazione globale:
//si controlla che il materiale abbia componente Kd>0 in 
//almeno una delle 3 componenti o che il coefficiente 
//slope per la rugosita' della superficie sia >0 o che
//il materiale emetta luce
if((material[mId].Kd.max()>0)||(material[mId].slope>0)||
		(material[mId].Le.max()>0)){
    
    
    //metodo di Jacobi stocastico e final gathering:
    if (jacob && Fg) {

    	float areaInverse= (1.0f)/((o).areaObj);
		Point3D L=((o).P).multiplyScalar(areaInverse);
		Point3D f=FinalGathering(r, x, y, o);
		return f.add(radianceRefr).
						add(radianceRefl);

	}
	//metodo di Jacobi stocastico:
	if(jacob){
		float areaInverse= (1.0f)/((o).areaObj);
		Point3D L=((o).P).multiplyScalar(areaInverse);
		
		//si somma alla radianza emessa dalla patch 
		//(pesata per l'area), la radianza data dalla 
		//riflessione e dalla rifrazione, quindi si 
		//restituisce il valore di radianza calcolato in 
		//questo modo
        return L.add(radianceRefr).add(
        		radianceRefl);
  
	}
    
    //se non e' stato impostato nessuno di tali metodi 
    //allora viene restituito il colore nero
    return new Point3D();
        
   }//fine if
   else{
       //se il materiale e' riflettente o trasparente
       return radianceRefl.add(radianceRefr);
   }
}
	
//funzione per il calcolo della radiosita' della scena:
//in questa funzione si utilizza il metodo di Jacobi 
//stocastico per calcolare il valore di potenza di ogni 
//patch della scena
//si usa la variabile globale GlobalObject in modo da 
//conservare i valori aggiornati delle potenze dei vari 
//oggetti
static void JacobiStoc(int nObj){	    
	//definizco la potenza residua totale delle patch:
    Point3D Prtot=new Point3D();
    //definisco la potenza di ogni patch della scena:
    Point3D[] P=new Point3D[nObj];
   
    //definisco la potenza residua di ogni patch della 
    //scena:
    Point3D[] Pr=new Point3D[nObj];
    
    //inizializzo a (0,0,0) P e Pr
    for(int i=0; i<nObj;i++) {
    	P[i]=new Point3D();
    	Pr[i]=new Point3D();
    }
    
    //inizializzo il numero di step raggiunti dal processo
    steps=0;
    //inizializzo la stima dell'errore raggiunto dal 
    //processo
    err=0;
    //inizializzo l'array objX in cui inizialmente gli 
    //oggetti sono nulli, ma poi vengono settati con 
    //l'oggetto intersecato dal raggio che parte da una
    //patch i-esima presa in consideraazione:l'oggetto 
    //intersecato e' proprio la patch j su cui sara' 
    //rilasciata la potenza totale delle 3 componenti rgb 
    Obj[] objX=new Obj[nObj];
    
    //Vengono caricati i valori iniziali di Luminosita' 
    //Emessa per ogni patch della scena (Pe)
    for(int i=0;i<nObj;i++){
        
        //viene caricata l'area dell'oggetto i-esimo
        float area=GlobalObjects[i].areaObj;
        //se l'area e' piu' piccola della precisione di 
        //calcolo allora impostiamo l'area a 0
        if(area<Utilities.EPSILON) {area=0;}
        
        //potenza della luce
        //Potenza emessa dalla patch i: 
        //(potenza emessa)*(pi greco)*(area)
        Point3D LP=material[GlobalObjects[i].matId].Le.
								multiplyScalar(Utilities.MATH_PI).
								multiplyScalar(area);
        
        //viene calcolata la potenza totale iniziale: 
        //(potenza residua della patch)+
        //+(potenza emessa dalla patch)
        Prtot=Prtot.add(LP);
        
        //viene salvata nell'array P la potenza dell'
        //elemento i
        P[i].copy(LP);
      
        //viene salvata nell'array Pr la potenza residua 
        //dell'elemento i
        Pr[i].copy(LP);
        
        //viene calcolato l'errore di approssimazione 
        //(con cui e' possibile fermare il metodo)
        err=(float) (err+Math.pow(LP.average(),2));
         
    }//fine for
    
    //dopo aver aggiunto dei quadrati dobbiamo fare la 
    //radice del risultato finale
    err=(float) Math.sqrt(err);
    
    
    //iterazioni del metodo di Jacobi:
    //Si continuano le iterazioni finche' l'energia 
    //rilasciata non diventa minore di un certo valore 
    ///(verificato dalla variabile err) 
    //o gli steps superano gli steps massimi (verificato
    //dalla variabile maxsteps). 
    //Devono essere vere entrambe 
    while((err>maxerr)&&(steps<maxsteps)){
    	//percentuale di completamento
    	System.out.println("completamento jacobi "+(
    			steps/(float)maxsteps)*100);
    	//viene inizializzato un seme iniziale casuale
        //da 0 a 30000
    	int s;
        s=(int) Math.floor(Math.random()*(30001));
        //inizializzo per le tre componenti una variabile 
        //per il numero di sample utilizzati per la patch
        //(N*) e una per il numero di sample utilizzati 
        //finora (Nprev*)
        int NprevX=0;
        int NX=0;
        int NprevY=0;
        int NY=0;
        int NprevZ=0;
        int NZ=0;
        
        //potenza residua totale nelle tre componenti
        float Prt= Prtot.x+Prtot.y+Prtot.z;
        //campioni distribuiti in base alla potenza 
        //totale di ciascuna componente RGB che chiamiamo 
        //qui (x,y,z)
        
        Point3D samps=new Point3D(Jacobisamps*Prtot.x,
        		Jacobisamps*Prtot.y,Jacobisamps*Prtot.z);
        samps=samps.divideScalar(Prt);
        //parametro che ci permette di contare, quindi 
        //utilizzare tutti i campioni che erano stati 
        //previsti
        Point3D q=new Point3D();
        //per ogni patch della scena
        for(int i=0;i<nObj;i++)
        {
         //salviamo nella variabile locale o l'oggetto 
         //i-esimo dell'array GlobalObjects
         Obj o=GlobalObjects[i];
        
         //probabilita' con cui viene scelta la patch i
         Point3D pi=(Pr[i].divideComponents(Prtot));
        
         q=q.add(pi);
        
         //componente rossa:
        
         //calcoliamo il numero di campioni utilizzati 
         //per la patch i in base alla sua potenza 
         //e anche in base a quanti campioni sono gia' 
         //stati usati
         NX=Math.round((q.x*samps.x)+NprevX*(-1));
         //per ogni campione dell'elemento i
         for (int j=0; j<NX; j++){
           
            //creo una direzione randomica uniformemente 
        	//distribuita sull'emisfero frontale alla 
        	//patch
            //definisco la variabile che rappresentera'
        	//la direzione
        	Point3D dir;
        	//creo 3 variabili randomiche che si basano 
        	//sul seme iniziale casuale s definito 
        	//randomicamente
            float rndX=0.0f;
            float rndY=0.0f;
            float rndZ=0.0f;
            rndX=generateRandom(s);
            rndY=generateRandom(s);
            rndZ=generateRandom(s);
            
            //Inverse Cumulative Distribuction Function
            //creiamo la base ortonormale per generare
            //la direzione del raggio ffRay
            float rndPhi=2*Utilities.MATH_PI *(rndX);
            float rndTeta=(float) Math.acos(Math.sqrt(
            		rndY));
            
            //dichiaro e inizializzo un punto scelto 
            //uniformemente sulla patch i
            Point3D rndPoint=null;
            rndPoint= o.randomPoint(rndX, rndY,rndZ);
            
            //Si crea ora la base ortonormale
            Point3D u;
            Point3D v;
            Point3D w=null;
            //settiamo w come la normale all'oggetto nel 
            //punto rndPoint
            w=o.normal(rndPoint);
           
            //vettore up (simile a (0,1,0)) 
            Point3D up=new Point3D(0.0015f,1.0f,0.021f);
            //il prodotto vettoriale tra w e up mi genera
            //il vettore v normale a entambi, che 
            //normalizzo
            v=w.crossProduct(up);
            v=v.getNormalizedPoint();
            //il prodotto vettoriale tra v e w mi genera 
            //il vettore u normale a entambi
            //dal momento che i vettori v e w sono gia' 
            //normali, non c'e' bisogno di noemalizzare 
            //il vettore u)
            u=v.crossProduct(w);
              
            //ora che abbiamo la base ortonormale, 
            //possiamo calcolare la direzione dir
            //salvo in delle cariabili i calori di seno 
            //e coseno necessari per il calcolo di dir
            float cosRndPhi=(float) Math.cos(rndPhi);
            float sinRndTeta=(float) Math.sin(rndTeta);
            float sinRndPhi=(float) Math.sin(rndPhi);
            float cosRndTeta=(float) Math.cos(rndTeta);
            //dir=(u*(cosRndPhi*sinRndTeta))+(v*
            //*(sinRndPhi*sinRndTeta))+(w*(cosRndTeta)) 
            //poi normalizzato
            dir=u.multiplyScalar(cosRndPhi*sinRndTeta).
										add(v.multiplyScalar(
            		sinRndPhi*sinRndTeta)).add(
            		w.multiplyScalar(cosRndTeta));
            dir=dir.getNormalizedPoint();
                
            //creo il raggio per scegliere la patch j 
            //con probabilita' uguale al fattore di forma
            //tra la patch i e quella j
            Ray ffRay=new Ray(rndPoint,dir);
            //dichiaro e inizializzo la variabile t in cui
            //salveremo il punto di intersezione fra 
            //l'oggetto considerato  e ffRay
            float t=inf;
            //inizializzo a null l'oggetto intersecato
            objX[i]=null;
            
            //l'oggetto intersecato e' proprio la patch j
            //su cui sara' rilasciata la potenza totale 
            //della componente rossa
            if(intersect(ffRay, objX[i])){
        	  //pongo t uguale al valore di intersezione
        	  //memorizzato nella variabile globale inters
        	  t=inters;
        	  //resetto inters uguale a inf in modo da 
        	  //avere il giusto valore di partenza la 
        	  //prossima volta che si utilizzera' il
        	  //metodo intersect()
        	  inters=inf;
        	  //salvo nell'elemento i-esimo dell'array 
        	  //objX l'elemento intersecato dal raggio 
        	  //ffRay 
        	  objX[i]=intersObj;
        	  //resetto intersObj=null in modo da avere 
        	  //il giusto valore di partenza la prossima 
        	  //volta che si utilizzera' il
        	  //metodo intersect()
        	  intersObj=null;
        	  //salviamo la potenza rilasciata all'interno
        	  //della struttura dell'oggetto: essa si 
              //sommera' con la potenza residua parziale
        	  //che l'oggetto ha raggiunto finora; solo 
              //alla fine del processo infatti avremo 
        	  //la potenza residua totale della patch
              objX[i].P.x=objX[i].P.x+material[objX[i].
                              matId].Kd.x*(Prtot.x)/(
                              (float)samps.x);
        	                                      
            }
         }//fine for per la compoente rossa
         //aggiorniamo il numero di campioni usati per 
         //questa componente
         NprevX=NprevX+NX;
    
         //per le componenti verde e blu si utilizzeranno 
         //gli stessi procedimenti della componente rossa,
         //quindi fare riferimento ai commenti sovrastanti
         //per maggiori dettagli
        
         //componente verde:
        
         //campioni per la patch i
         NY=Math.round((q.y*samps.y)+NprevY*(-1));
    
         //per ogni campione sull'elemento i
         for (int j=0; j<NY; j++){
        
        	Point3D dir;
        	float rndX=0.0f;
        	float rndY=0.0f;
        	float rndZ=0;
        
            rndX=generateRandom(s);
            rndY=generateRandom(s);
            rndZ=generateRandom(s);
        
            float rndPhi=2*Utilities.MATH_PI *(rndX);
            float rndTeta=(float) Math.acos(Math.sqrt(
            		rndY));
        
            Point3D rndPoint=null;
            //punto scelto uniformemente nella patch i:
            rndPoint= o.randomPoint(rndX, rndY,rndZ);
            
            //base ortonormale
            Point3D u,v;
            Point3D w=null;
            
            w=o.normal(rndPoint);
        
            //vettore up (simile a (0,1,0))
            Point3D up=new Point3D(0.0015f,1.0f,0.021f);
            v=w.crossProduct(up);
            v=v.getNormalizedPoint();
            u=v.crossProduct(w);
        
            float cosRndPhi=(float) Math.cos(rndPhi);
            float sinRndTeta=(float) Math.sin(rndTeta);
            float sinRndPhi=(float) Math.sin(rndPhi);
            float cosRndTeta=(float) Math.cos(rndTeta);	                
            dir=u.multiplyScalar(cosRndPhi*sinRndTeta).
										add(v.multiplyScalar(
            		sinRndPhi*sinRndTeta)).add(
            		w.multiplyScalar(cosRndTeta));
            dir=dir.getNormalizedPoint();
        
            //creo il raggio per scegliere la patch j 
            //con probabilita' uguale al fattore di forma
            //tra la patch i e quella j
            Ray ffRay=new Ray(rndPoint,dir);
            float t=inf;
            objX[i]=null;
        
            if(intersect(ffRay, objX[i])){
            	t=inters;
            	inters=inf;
            	objX[i]=intersObj;
            	intersObj=null;
            	objX[i].P.y=objX[i].P.y+material[objX[i].
            	                matId].Kd.y*(Prtot.y)/(
            	                (float)samps.y);
            }
            
         }//fine for per la compoente verde
         NprevY=NprevY+NY;

         //componente blu:
        
         //campioni per la patch i
         NZ=Math.round((q.z*samps.z)+NprevZ*(-1));
        
         //per ogni campione sull'elemento i
	     for (int j=0; j<NZ; j++){
	    
	    	Point3D dir;
	        float rndX=0.0f;
	        float rndY=0.0f;
	        float rndZ=0;
	            
	        rndX=generateRandom(s);
	        rndY=generateRandom(s);
	        rndZ=generateRandom(s);
	        
	        //direzione casuale
	        float rndPhi=2*Utilities.MATH_PI *(rndX);
	        float rndTeta=(float) Math.acos(Math.sqrt(
	        		rndY));
	        
	        //punto scelto uniformemente nella patch i
	        Point3D rndPoint=null;
	        rndPoint= o.randomPoint(rndX, rndY,rndZ);
	        
	        //base ortonormale
	        Point3D u,v;
	        Point3D w=null;
	        
	        w=o.normal(rndPoint);
	
	        //vettore up (simile a (0,1,0))
	        Point3D up=new Point3D(0.0015f,1.0f,0.021f);
	        v=w.crossProduct(up);
	        v=v.getNormalizedPoint();
	        u=v.crossProduct(w);
	        
	        float cosRndPhi=(float) Math.cos(rndPhi);
	        float sinRndTeta=(float) Math.sin(rndTeta);
	        float sinRndPhi=(float) Math.sin(rndPhi);
	        float cosRndTeta=(float) Math.cos(rndTeta);
	        dir=u.multiplyScalar(cosRndPhi*sinRndTeta).
									add(v.multiplyScalar(
	        		sinRndPhi*sinRndTeta)).
									add(w.multiplyScalar(
	        		cosRndTeta));
	        dir=dir.getNormalizedPoint();
	        
	        //creo il raggio d'ombra dal punto di 
	        //intersezione ad un punto a caso sull'
	        //emisfero
	        Ray ffRay=new Ray(rndPoint,dir);
	        float t=inf;
	        objX[i]=null;
	        
	        if(intersect(ffRay,objX[i])){
	        	t=inters;
	        	inters=inf;
	        	objX[i]=intersObj;
	        	intersObj=null;
	        	objX[i].P.z=objX[i].P.z+material[objX[i].
	        	              matId].Kd.z*(Prtot.z)/(
	        	              (float)samps.z);
	        }
	     }//fine for per la componente blu
	     NprevZ=NprevZ+NZ;
	     }//fine for per le patch della scena
	    
         //una volta terminata la fase di shooting si 
         //riaggiornano le componenti di Potenza residua,
         //Pr, Potenza P, e si azzerano le potenze 
         //residue parziali salvate negli oggetti
         //azzeramento della potenza totale:
         Prtot=new Point3D(0);
         
         for(int i=0;i<nObj;i++)
         {
        	//aggiornamento delle Potenze (vengono  
		    //aggiunte le potenze residue totali 
        	//immagazzinate dalle patch durante 
		    //il processo)
		    P[i]=P[i].add(GlobalObjects[i].P);
		
		    //aggiornamento delle potenze residue totali 
		    Pr[i].copy(GlobalObjects[i].P);
		    
		    //calcolo dell'errore
		    err=(float) (err+Math.pow(Pr[i].average(),2));
		    
		    //calcolo dell'energia residua totale
		    Prtot=Prtot.add(Pr[i]);
		    
		    //azzeramento della potenza residua parziale 
		    //contenuta nella patch
		    GlobalObjects[i].P.copy(new Point3D());
        
         }
       
         //nel for abbiamo elevato al quadrato le 
         //componenti aggiunte all'errore, ora ne 
         //facciamo la radice
         err=(float) Math.sqrt(err);
         //viene aumentato il numero di step
         steps++;
   
    } //fine while

    //I valori ottenuti vengono salvati su ciascuna 
    //patch nella variabile P (in cui durante il processo
    //veniva salvata la potenza residua parziale)
    for(int i=0; i<nObj; i++)
    {
    	(GlobalObjects[i].P).copy(P[i]);
    }
    /*
    //questi valori sono copiati dai valori finali dei 
    //calcoli di Forti: sono stati utilizzati durante il 
    //debug per il vetro
    GlobalObjects[0].P.copy(new  float3());
    GlobalObjects[1].P.copy(new  float3());
    GlobalObjects[2].P.copy(new  float3());
    GlobalObjects[3].P.copy(new  float3(894.375f));
    GlobalObjects[4].P.copy(new  float3(894.375f));
    GlobalObjects[5].P.copy(new  float3(
    					15.129f,17.509f,56.0f));
    GlobalObjects[6].P.copy(new  float3(
    					17.49f,25.657f,53.919f));
    GlobalObjects[7].P.copy(new  float3(
    					484.33f,452.43f,474.02f));
    GlobalObjects[8].P.copy(new  float3(
    					443.096f,435.775f,479.05f));
    GlobalObjects[9].P.copy(new  float3(
    					96.33f,87.035f,113.20f));
    GlobalObjects[10].P.copy(new  float3(	
    					72.9f,54.21f,59.98f));
    GlobalObjects[11].P.copy(new  float3(
    					29.63f,27.28f,113.30f));
    GlobalObjects[12].P.copy(new  float3(
    					9.74f,15.04f,40.54f));
    GlobalObjects[13].P.copy(new  float3(
    					0.0f,0.0f,0.00046f));
    GlobalObjects[14].P.copy(new  float3(
    					0.0f,0.0f,0.00268f));
    */
    
    /*
    //questi valori sono copiati dai valori finali dei 
    //calcoli di Forti: sono stati utilizzati durante il 
    //debug per la giada
    GlobalObjects[0].P.copy(new  float3(
    					2.394f,6.786f,4.094f));
    GlobalObjects[1].P.copy(new  float3(
    					8.121f,23.189f,13.258f));
    GlobalObjects[2].P.copy(new  float3(
    					9.984f,19.5f,14.736f));
    GlobalObjects[3].P.copy(new  float3(894.375f));
    GlobalObjects[4].P.copy(new  float3(894.375f));
    GlobalObjects[5].P.copy(new  float3(
    					12.409f,12.303f,46.359f));
    GlobalObjects[6].P.copy(new  float3(
    					17.121f,22.949f,62.68f));
    GlobalObjects[7].P.copy(new  float3(
    					451.02f,426.07f,433.29f));
    GlobalObjects[8].P.copy(new  float3(
    					439.42f,433.56f,475.34f));
    GlobalObjects[9].P.copy(new  float3(
    					12.647f,0.0f,0.0f));
    GlobalObjects[10].P.copy(new  float3(	
    					9.531f,0.0f,0.0f));
    GlobalObjects[11].P.copy(new  float3(
    					28.64f,27.68f,113.48f));
    GlobalObjects[12].P.copy(new  float3(
    					7.67f,13.56f,40.7f));
    GlobalObjects[13].P.copy(new  float3(
    					0.0f,0.00037f,0.0f));
    GlobalObjects[14].P.copy(new  float3(
    					0.0f,0.0f,0.00005f));
    
    */
}

private static void addItemsToMethodMenu(
		JMenu method_menu,int[] bool)
{
	JRadioButtonMenuItem menuItem;
	ButtonGroup bg = new ButtonGroup(); 
		
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Solo Jacobi");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[0]=0;//Fg
    	        }  
    	    });
    // Aggiungo il bottone al gruppo
    bg.add(menuItem); 
    // Infine lo aggiungo al menu
    method_menu.add(menuItem); 
    
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Jacobi + final gathering");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[0]=1;//Fg  
    	        }  
    	    });
    bg.add(menuItem); 
    method_menu.add(menuItem);     
    
}

private static void addItemsToMaterialMenu(
		JMenu material_menu,int[] bool)
{
	JRadioButtonMenuItem menuItem;
	ButtonGroup bg = new ButtonGroup(); 
	
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Giada Realistica (traslucente)");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[1]=1;//translucentJade=true;  
    				bool[2]=0;//diffusiveJade=false;
    				bool[3]=0;//glass=false;
    	        }  
    	    });
    bg.add(menuItem); 
    material_menu.add(menuItem); 
    
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Giada Diffusiva");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[1]=0;//translucentJade=false;  
    				bool[2]=1;//diffusiveJade=true;
    				bool[3]=0;//glass=false;
    	        }  
    	    });
    bg.add(menuItem); 
    material_menu.add(menuItem); 
    
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Cristallo");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[1]=0;//FgtranslucentJade=false;  
    				bool[2]=0;//FgdiffusiveJade=false;
    				bool[3]=1;//glass=true;
    	        }  
    	    });
    bg.add(menuItem);
    material_menu.add(menuItem); 
}

private static void addItemsToPositionMenu(
		JMenu position_menu,int[] bool)
{
	JRadioButtonMenuItem menuItem;
	ButtonGroup bg = new ButtonGroup(); 
	
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Sfere allineate");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[4]=1;//aligned=true;  
    	        }  
    	    });
    bg.add(menuItem); 
    position_menu.add(menuItem);
    
    menuItem = new JRadioButtonMenuItem(); 
    menuItem.setText("Sfere sovrapposte");	
    menuItem.addActionListener(new ActionListener(){  
    	public void actionPerformed(ActionEvent e){  
    				bool[4]=0;//aligned=false;  
    	        }  
    	    });
    bg.add(menuItem); 
    position_menu.add(menuItem);
    
    
}

//metodo per creare il menu box
private static void createMenu(JFrame f, final 
		JTextField tf, final JButton ok_button, 
		int[] bool)
{
	// componenti del jFrame
	JMenuBar menu_bar;
    JMenu method_menu;
    JMenu material_menu;
    JMenu position_menu;
    
    // creo la menu bar
	menu_bar = new JMenuBar(); 
	menu_bar.setBounds(15,15,300,40);

    // creo i menu
    method_menu = new JMenu("Metodo");
    material_menu = new JMenu("Materiale");
    position_menu = new JMenu("Posizione");
    
    ok_button.setBounds(250,15,100,40);
    tf.setText("Scegliere i parametri");
    tf.setBounds(50,15, 200,100);
    
    addItemsToMethodMenu(method_menu,bool);
    addItemsToMaterialMenu(material_menu,bool);
    addItemsToPositionMenu(position_menu,bool);
    
    // aggiungo alla menu bar i menu
    menu_bar.add(method_menu);
    menu_bar.add(material_menu);
    menu_bar.add(position_menu);
    f.add(ok_button, -1);
    f.add(tf, -1);
    f.setJMenuBar(menu_bar); 
    
    // imposto le dimensioni della finestra
    f.setSize(400,200);  
    
    f.setLayout(null);  
    
    f.setVisible(true);
    
    // centro la finestra nello schermo
    f.setLocationRelativeTo(null);
    
    // scelgo cosa succedera'  quando si chiudera'
    //  la finestra
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

}



//la classe main costruisce una stanza rettangolare con 
//dentro 3 sfere e opportune luci, e un osservatore che 
//guarda in una direzione appropriata: il generico raggio
//di visuale attraversa un appropriato pixel del 
//viewplane. Su quel pixel vengono calcolati, mediante 
//metodi di radiosita' stocastica, colore e luminosita' 
//di cio' che l'osservatore vede.

public static void main(String[] args)  
		throws FileNotFoundException, IOException {
	
  //inizialmente imposto la finestra con le varie
  //scelte per l'utente
  JFrame f=new JFrame("Image creator");
  final JTextField tf=new JTextField();
  final JButton ok_button=new JButton("Conferma");
  int[] bool=new int[5];
  createMenu(f,tf,ok_button, bool);
  ok_button.addActionListener(new ActionListener(){  
    public void actionPerformed(ActionEvent e){
    	
        //Metodo
    	if(bool[0]==1) 
            Fg=true; 
    	else if(bool[0]==0)
    		Fg=false; 
    	
    	//Materiale
    	if(bool[1]==1) 
            translucentJade=true; 
    	else if(bool[1]==0) 
    		translucentJade=false;
    	if(bool[2]==1)
    		diffusiveJade=true;
    	else if(bool[2]==0)
    		diffusiveJade=false;
    	if(bool[3]==1)
    		glass=true; 
    	else if(bool[3]==0)
    		glass=false;
    	
    	//Posizione
    	if(bool[4]==1) 
                aligned=true; 
    	else if(bool[4]==0) 
    			aligned=false; 
    		
    	tf.setText("Creazione immagine in corso");
    	
    	//jacob=true;
    	//Fg=true;
    	//glass=true;
    	//capire perche se li lascio funziona e se li tolgo fa tutto nero (non ha senso)
    	int mIS=setMatIdSphere();
    	for(int sph=0; sph<nSphere; sph++)
    		matIdSphere[sph]=mIS;
    	
    	Point3D sPos0=setSpheresPosition(0);
    	Point3D sPos1=setSpheresPosition(1);
    	Point3D sPos2=setSpheresPosition(2);
    	//vettore costruttore delle sfere 
    	spheres[0]=new Sphere(1,sPos0);
    	spheres[1]=new Sphere(1,sPos1);
    	spheres[2]=new Sphere(1,sPos2);
    

    ok_button.setEnabled(false);
    //Qua inizia il processo di creazione dell'immagine 
	//inizializzo il numero di mesh
	int nMesh = 0;
      
	//dovendo disegnare 3 sfere e la stanza definisco un 
	//array di 2 Mesh: nella prima delle due mesh (per 
	//m[0]) aggiungiamo le 3 
	//sfere richiamando il metodo caricaSphere
    //if(drawSphere){
    	m=new Mesh[2];
        nMesh=1;
        m[0]= caricaSphere(nSphere);
    //}
    
    //inizializzo massimo e minimo punto della scena
    Point3D max = null;
    Point3D min = null;
    //inizializzo dei fittizi massimo e minimo, che mi 
    //serviranno per definire i valori di max e min
    Point3D oldMin=new Point3D(Float.POSITIVE_INFINITY);
    Point3D oldMax=new Point3D(Float.NEGATIVE_INFINITY);
    
    //trovo le dimensioni della scena (tralasciando la 
    //stanza in cui gli oggetti sono contenuti)
    for(int i=0; i<nMesh;i++)
    {    
      max=Obj.getBoundMax(m[i].objects,oldMax,m[i].nObj);
      min=Obj.getBoundMin(m[i].objects,oldMin,m[i].nObj);
    }
    //ingrandisco un po' la stanza
    //max.operatorTimes(2.4f);
    //min.operatorTimes(2.4f);
    //definisco e calcolo il punto in cui guarda 
    //l'osservatore: il centro della scena
    Point3D center= (max.add(min)).
						multiplyScalar(0.5f);
    if(!absolutePos)
    {
    	//lookat= (0,0,0)+center
    	lookat=lookat.add(center);
    }
    //l'osservatore si trova nel punto camPosition
    Point3D camPosition= center.add(eye);
    //calcolo del punto di messa a fuoco pf
    Point3D pf = new Point3D();
    //pf= center+(0,0,0)
    pf.copy(center.add(focusPoint));
    
    //costruttore della fotocamera
    //imposto la fotocamera che guarda il centro 
    //dell'oggetto ed e' posizionata davanti
    camera cam=new camera(camPosition,lookat,new
						Point3D(0.00015f,1.00021f,0.0f),w,h,distfilm);

    //Abbiamo ora a disposizione tutti gli elementi 
    //necessari per costruire la stanza
    //Creo la stanza in cui mettere l'oggetto (per 
    //visualizzare l'illuminazione globale)
    //La carico come ultima mesh
    m[nMesh]=createScene(max,min);
    
    //nel nostro caso sceneDepth=0, quindi non si entra 
    //mai in questo ciclo
    for(int q=0;q<sceneDepth;q++){
        m[nMesh].suddividi();
    }
    
    //A questo punto consideriamo l'intero array di mesh,
    //ora composto da oggetti+stanza e aggiorno i valori 
    //della grandezza della stanza, usando di nuovo i 
    //metodi getBoundMin e getBoundMax. 
    oldMax=max;
    oldMin=min;
    max=Obj.getBoundMax(m[nMesh].objects,oldMax,m[nMesh].
    		nObj);
    min=Obj.getBoundMin(m[nMesh].objects,oldMin,m[nMesh].
    		nObj);
 
    //numero di oggetti nella scena
    int nO=0;
     
    //ciclo per la mesh delle sfere + la mesh della 
    //stanza, per aggiornare il numero di oggetti e di 
    //luci. 
    for(int i=0; i<nMesh+1;i++){
        //aggiungiamo al contatore n0 il numero di 
    	//oggetti nella mesh m[i];
        nO+=m[i].nObj;
        
        //per ogni oggetto della mesh
        for(int j=0;j<m[i].nObj;j++){
            
          //se l'oggetto e' una luce aggiorno il 
          //valore del contatore delle luci nLight
          if(material[m[i].objects[j].matId].Le.max()>0)
          {
        	 nLight++;
          }
        }
    }
    
    //vettore che conterra' gli oggetti della scena
    Obj[] objects= new Obj[nO];
    Obj[] SceneObjects= new Obj[nO];
    //vettore che contiene solo le luci della scena
    luci= new Obj[nLight];
    nO=0;
    nLight=0;
    
    for(int i=0; i<nMesh+1;i++){
        
        //e carico tutto nella lista globale degli oggetti
        for(int j=0;j<m[i].nObj;j++){
            objects[nO]=m[i].objects[j];
            SceneObjects[nO]=m[i].objects[j];
            nO++;
            //se l'oggetto e' una luce la carico dentro 
            //l'array delle luci
            if(material[m[i].objects[j].matId].Le.max()>0)
            {
                luci[nLight]=m[i].objects[j];
                nLight++;
            }
            
        }
    }
    
    //costruzione del Kd-tree che ripartisce la scena
    
    //l=0: iniziamo a partizionare col piano xy
    int l=0;
    //liv e' il livello di profondita' all'interno 
    //dell'albero	    
    liv=0;
    
    //creo il Bounding Box
    //Bound e' il primo elemento dell'albero che contiene
    //tutti gli oggetti della scena
    Bound=new Box(min, max, (short) l);
        
    Bound.setObjects(objects,nO);
        
    //inizializzo la variabile S che e' la massima 
    //profondita' dell'albero 
    for(int i=1; i<depth; i++){
        S+=Math.pow(2,i);
    }
        
    //crea il tree: si richiama il metodo setPartition()
    //per dividere gli oggetti del box padre nei box figli
    Bound=setPartition(Bound);
    
    //salviamo gli oggetti della scena nella variabile 
    //globale GlobalObjects in modo da poterli aggiornare 
    //in JacobiStoc()
    GlobalObjects=new Obj[nO];
    for(int i=0; i<nO; i++) {
    	GlobalObjects[i]=SceneObjects[i];
    }
    
    //la definizione di inters mi serve per il metodo 
    //intersectBPS che utilizza questa variabile
    inters=inf;
    
    //richiamo la funzione per il calcolo della radiosita'
    //della scena attraverso il metodo di Jacobi 
    //stocastico
    if(Fg!=true)
    	JacobiStoc(nO);
    
    //aggiorno la variabile locale SceneObjects con i 
    //valori ora contenuti in globalObjects
    for(int i=0; i<nO; i++) {
    	SceneObjects[i]=GlobalObjects[i];
    }
    
    //nota: rand() in C++ e' un numero random tra 0 e 
    //RAND_MAX=2147483647: qua usero' 
    //Math.random() * (fine-iniz+1)) + iniz 
    //cioe' Math.random()* (2147483648)
    for (int i = 0; i < (w*h); i++) {
    	//creiamo i campioni necessari per:
    	
    	//la fotocamera
		samplesX[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));
		samplesY[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));

		//la luce indiretta
		aoSamplesX[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));
		aoSamplesY[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));

		//la luce diretta
		dirSamples1[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));
		dirSamples2[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));
		dirSamples3[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));

		//riflessioni/rifrazioni
		refSamples1[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));
		refSamples1[i] = (int) (Math.random()*(Integer.
				MAX_VALUE+1));
		
		//inizializzo l'immagine nera
		image[i]=new Point3D();
	}

    //Stampiamo le varie informazioni
	System.out.println("Samples: "+samps+" "+aosamps);		
    System.out.println("Origin: x "+cam.eye.x+" y "+
    			cam.eye.y+" z "+cam.eye.z);
    System.out.println("Direction: x "+cam.lookAt.x+" y "+
    			cam.lookAt.y+" z "+cam.lookAt.z);
    System.out.println("Up: x "+cam.up.x+" y "+cam.up.y+
    			" z "+cam.up.z);
    System.out.println("D: "+cam.d+" W: "+cam.width+
    			" H: "+cam.height);
    
	cam.aperturaDiaframma = ap;// =0

	//l'istruzione seguente calcola il fattore di scala 
	//con una trasformazione perospettica che porta 
	//l'origine della posizione della fotocamera e il
	//vettore dell'osservatore al vettore della fotocamera
	cam.fuoco = (pf.z - cam.eye.z) / (cam.W.z*(-cam.d));

	System.out.println("Fuoco: "+cam.fuoco);
	System.out.println("Apertura diaframma: "+cam.
			aperturaDiaframma);
    
	
	//Ora viene creata l'immagine
	
	//iniziamo la stringa matrix con valori di settaggio
	//richiesti
	matrix +="P3\n" + w + "\n" + h + "\n255\n";
    
    //per tutte le righe
	for(int y = 0; y <= h; y++)
	{ 
	
	  //stampiamo la percentuale di completamento per 
	  //monitorare l'avanzamento del rendering
	  double percentY = ((float)y / (float)h) * 100;
	  System.out.println("percentuale di completamento "
	  		+ "radianza:	 "+percentY);
	
	  //per tutte le colonne
      for(int x = 0; x <= w; x++)
      { 
    	// Ora siamo nel pixel
		// r e' la radianza: in questo caso e' tutto nero
		r = new Point3D(0.0f);

		// Loop per ogni campione
		for (int s = 0; s < samps; s++) {

			//inizializiamo un raggio per la camera
			Ray cameraRay;

			//transformazione delle variabili x e y in 
			//float corrispondono alla posizione che
			//cameraRay deve raggiungere 
			float raster_x = (float)x;
			float raster_y = (float)y;

			//origine del raggio della fotocamera 
			Point3D origin=new Point3D();
			origin.copy(cam.eye);

			//se ho piu' di un campione allora 
			//distribuisco gli altri campioni in modo 
			//casuale
			// !!! COMPLETARE I COMMENTI DI QUESTA 
			//PARTE CON AIUTO DI FORTI!!!
			if (s > 0) {

			  float rndX=0;
			  float rndY=0;

			//utilizzo questa variabile tt perche' non 
				//posso usare il valore x+y*w nell'array
				//samplesX[], altrimenti l'ultimo indice
				//sarebbe fuori dal range (ricordo che la
				//misura e' w*h ma gli indici vanno da 0 a 
				//w*h-1)
				int tt =x+y*w;
				//allora faccio l'if per tt<w*h cosi' da 
				//accertarmi che non sia considerato l'indice 
				//w*h-esimo 
				if(tt<w*h) 
				{
					// gli passo il numero random da cui 
					  //siamo partiti all'interno del pixel 
					  rndX = generateRandom(samplesX[tt]);
					  rndY = generateRandom(samplesY[tt]);

				}
			
			  //if (cam.aperturaDiaframma > 0) {
				//prendiamo un punto a caso su un disco di
				raster_x += Math.cos(2 * Utilities.MATH_PI *
						rndX)*cam.aperturaDiaframma*rndY;
				raster_y += Math.sin(2 * Utilities.MATH_PI *
						rndX)*cam.aperturaDiaframma*rndY;
				Point3D camUFuoco=cam.U.multiplyScalar(cam.
						fuoco*(x - raster_x));
				Point3D camVFuoco=cam.V.multiplyScalar(cam.
						fuoco*(y - raster_y));
				
				origin = origin.add(camUFuoco).
								add(camVFuoco);

			  //}
			  /*else{//diaframma=0
			
				// in this case i take another
				//quindi aggiungo alla posizione del 
				//pixel (presa all'angolo in basso a 
				//sinistra) le quantita' random comprese 
				//in [0,1]
				raster_x += rndX;
				raster_y += rndY;
			  }*/
			}

			// prediamo la direzione della fotocamera 
			Point3D ray_direction;
			//ray_direction e' calcolato con l'ONB(base 
			//ortonormale) della fotocamera
			//il raggio dalla fotocamera al campione sara'
			//data dalla combinazione lineare dell'ONB 
			//della fotocamera
			//centro il piano rispetto alla fotocamera 
			//sottraendo w/2 alla componente in x e h/2 
			//alla componente in y infine la distanza z
			//tra la fotocamera e il piano e' cam.d

			//ray_direction=U*(raster_x-w/2)+
			//+V*(raster_y-h/2)+W*(-cam.d)
			ray_direction = (cam.U.multiplyScalar(
					raster_x - 0.5f*w)).add(
					cam.V.multiplyScalar(raster_y -
					0.5f*h)).add(cam.W.
							multiplyScalar(-cam.d));
			ray_direction=ray_direction.getNormalizedPoint();

			//Ora si crea il raggio della fotocamera 
			cameraRay = new Ray(origin, ray_direction);

			//dichiaro e inizializzo la variabile t in cui
			//salveremo il punto di intersezione fra 
			//l'oggetto considerato  e cameraRay
            float t = inf;
            //inizializzo a null l'oggetto intersecato 
            //dal raggio
            Obj o=null;
			//intersezione del raggio con gli elementi 
            //della scena:
			if(intersect(cameraRay, o)) {
			  //pongo t uguale al valore di intersezione
			  //memorizzato nella variabile globale inters
        	  t=inters;
        	  //resetto inters uguale a inf in modo da 
        	  //avere il giusto valore di partenza la 
        	  //prossima volta che si utilizzera' 
        	  //il metodo intersect()
        	  inters=inf;
        	  //salvo nella variabile o objX l'elemento 
        	  //intersecato dal raggio cameraRay 
        	  o=intersObj;
        	  //resetto intersObj=null in modo da avere 
        	  //il giusto valore di partenza la prossima 
        	  //volta che si utilizzera' il metodo
        	  //intersect()
        	  intersObj=null;
        	  //si calcola il punto di intersezione
			  Point3D iP = (cameraRay.o).add(
					  cameraRay.d.multiplyScalar(t));
			  //viene creato il primo raggio per il 
			  //calcolo della radianza
			  //questo raggio parte dal punto ed e' 
			  //diretto verso l'osservatore
			  Ray first=new Ray(iP, (cameraRay.d).
								multiplyScalar(-1));
			  //si aggiunge alla variabile r il contributo
			  //di radianza del punto considerato
			  r=r.add(radiance(first, o, x, y));
			 }
			//se non si interseca nessun oggetto si 
			//aggiunge alla variabile r il colore di
			//background (nero)
			else 
			{
				r = r.add(background);
			}
		}
		//divido per il numero di campioni del pixel
		r = r.divideScalar((float)samps);
		r.multiplyScalar(0.3f);
		// A questo punto si crea un'immagine basata sui 
		//valori di radianza r

		//le componenti RGB del vettore r vengono tagliate
		//se non comprese in [0,1] dopodiche' vengono 
		//caricate nel vettore image
		//nota: per ogni y che aumenta abbiamo gia'
		//caricato w pixel
		
		//utilizzo questa variabile tt perche' non posso
		//usare il valore x+y*w nell'array image[w*y], 
		//altrimenti l'ultimo indice sarebbe fuori dal
		//range (ricordo che la misura e' w*h ma gli 
		//indici vanno da 0 a w*h-1)
		int tt =x+y*w;
		//allora faccio l'if per tt<w*h cosi' da 
		//accertarmi che non sia considerato l'indice 
		//w*h-esimo 
		if(tt<w*h)
		{
			image[x+y*w].x = Point3D.clamp(r.x);
			image[x+y*w].y = Point3D.clamp(r.y);
			image[x+y*w].z = Point3D.clamp(r.z);
		}
	  }
 	}
    
    //Ora si disegna l'immagine: si procede aggiungendo 
	//alla stringa matrix le informazioni contenute 
	//nell'array image in cui abbiamo precedentemente 
    //salvato tutti i valori di radianza 
    for(int i = 0; i <w*h; i++){
    	
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
    	  		
    	
    	//i valori di radianza devono essere trasformati 
    	//nell'intervallo [0,255] per rappresentare la 
    	//gamma cromatica in valori RGB
        matrix += ""+ Utilities.toInt(image[i].x) + " " 
        		+ Utilities.toInt(image[i].y) + " " + 
        		Utilities.toInt(image[i].z) + "  " ;
        
        
 	   
    } tf.setText("Immagine completata!");
    
    //Per finire viene scritto il file con con permesso
    //di scrittura e chiuso.
    FileOutputStream fos;
	try {
		fos = new FileOutputStream(filename);
		fos.write(new String(matrix).getBytes());
		fos.close();
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	} catch (IOException e1) {
		e1.printStackTrace();
	}
    
    
    }  
  });  
  }

}
