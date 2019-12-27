//classe per la generazione di numero randomici
//Nel progetto di Forti i numeri possono essere generati 
//con i metodi random RND, LCG o con la libreria sobol SOB
//(scaricabile online), in questo progetto si considera
//sempre e solo RND
public class Rand_t {

	public char type;

	public Rand_t() {
		//Nel progetto di Forti puo' essere r per RND, 
		//l per LCG, s per SOB (in questo progetto si 
		//considera sempre e solo RND)
		type='r';
	}
	
	public void setType(char r)	{ type=r; }
	
	public char getType() { return type; }
	
	//trasforma dei numeri random X e Y che in partenza  
	//sono in un quadrato, numeri random in un disco
	//rndXY[] contiene i float rndX e rndY da costringere 
	//nel disco
	static float[] square_to_disk(float[] rndXY){
		float rndX=rndXY[0];
		float rndY=rndXY[1];
		
		float phi=0;
		float r;

	    //numero casuali all'interno del quadrato centrato
		//nell'origine di lunghezza 2
		float a=2.0f*rndX-1.0f; //coordinata x
		float b=2.0f*rndY-1.0f; //coordinata y
	    
	    //dividiamo il quadrato in quattro quadranti 
		//basati sulle rette che formano 45 gradi con gli 
		//assi su ogni quadrante
		if(a>-b){
			if(a>b){
	            //primo quadrante x>y x>-y
	            //  -a<b<a
	            //scelgo per raggio il valore pia' grande 
				//tra a e b (che sicuramente e' <1) , in 
				//questo quadrante e' a 
				r=a;
	            //in questo modo quelli lontani dal centro 
				//tenderanno a restare lontani , difatti i 
				//problemi sorgono all'avvicinarsi dei 
				//punti alle rette a 45� poiche' la loro
	            //posizione viene variata di molto 
				//(compressa) infatti passiamo da 
				//r^2= a^2+b^2 a r^2= a^2
	            //scelgo l'angolo di conseguenza
	            //scalo l'angolo iniziale pi/4 in base al 
				//rapporto y/x se siamo sulla retta avremo
				//proprio pi/4 altrimenti l'angolo 
				//diventera' pia' piccolo man mano che ci
	            // avviciniamo all'asse x in cui y=0 e 
				//quindi phi=0
				phi=(float)0.785398163397448309616f*(b/a);
			}
			else{
	            // secondo quadrante x>-y x<y
	            // -b<a<b
				r=b;
				phi=0.785398163397448309616f*(2.0f-(a/b));
			}
		}
		else{
			if(a<b){
	            //terzo quadrante x<-y x<y
	            // a<b<-a
				r=-a;
				phi=0.785398163397448309616f*(4.0f+(b/a));
			}
			else{
	            //quarto quadrante x<-y x>y
	            // b<a<-b
				r=-b;
				if(phi==b)
				 phi=0.785398163397448309616f*(6.0f-(a/b));
				else
				 phi=0.0f;
			}
		}

	    // nuove coordinate dei punti distribuiti sul disco
		rndX=(float) (r*Math.cos(phi));
		rndY=(float) (r*Math.sin(phi));
		
		float[] ret= {rndX,rndY};
		return ret;
	}
	
	// Linear congruential generator LCG
	//si definisce il metodo per completezza, anche se 
	//comunque non viene mai utilizzato nel progetto
	static int lcg(int x){
		long a;
		a=(1664525*(x)+1013904223)%4294967296L;
		return (int) a;
		
	}
	
	// Genera un numero random in [0,1] basandosi su x 
	//che dovra' essere scelto randomicamente
	//A differenza del progetto di Forti, qua si sceglie 
	//sempre RND
	float generateRandom(int x){
		double a=Math.floor(Math.random() * (x-0+1)) + 0;
		return (float)(a/x);
	}
}
