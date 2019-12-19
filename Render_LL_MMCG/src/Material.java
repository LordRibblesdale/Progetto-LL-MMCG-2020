//classe contenente la definizione del materiale dell'
//oggetto.
//All'interno di questa classe sono definiti anche i 
//metodi per il calcolo del coefficiente di Fresnel
//(che indica la quantita' di energia luminosa che viene 
//riflessa o rifratta quando incontra il materiale) e per
//il calcolo della BRDF (funzione che indica la la frazione 
//della radianza differenziale incidente da una direzione
//psi che viene riflessa in una direzione theta)
public class Material {
	
	// Diffuse color
	public Point3D Kd=new Point3D();
	    
	// Reflection color
	public Point3D Kr=new Point3D();
	    
	// Refraction color
	public Point3D Kg=new Point3D();
	    
	//potenza luce emessa dall'oggetto
	public Point3D Le=new Point3D();
	    
	// IOR (eta): indice di rifrazione rispetto alle 
	//lunghezza d'onda RGB
	public Point3D ior=new Point3D();
	    
	//coefficente di assorbimento per materiali conduttori
	//(anche esso varia in base alla lunghezza d'onda RGB)
	public Point3D k=new Point3D();
	    
	//Cook-Torrance model
	
	//slope: coefficente di rogusita' della superficie: 
	//scarto quadratico medio della pendenza delle 
	//microsfaccettature. Se m e' piccolo allora
	//l'inclinazione delle microsfaccettature varia poco 
	//rispetto alla normale della superficie e quindi la 
	//riflessione e' molto a fuoco sulla direzione 
	//speculare. Se m e' grande l'inclinazione e' elevata 
	//e la superficie e' ruvida
	public static float slope=0;
	    
	//precisione della riflessione/rifrazione (il parametro
	//deve essere minore di 1)
	public float refImperfection=0;
	
	//boolean che specifica se il materiale in questione 
	//e' permeabile o meno: serve per capire se e' 
	//necessario calcolare la BRDF o la BSSRDF
	public boolean translucent=false;
	
	//materiale di default: diffusivo bianco senza 
	//riflessione
	public Material() {
		
		Kd=new Point3D(1.0f);
		Kr=new Point3D(0.0f);
		Kg=new Point3D(0.0f);
		ior=new Point3D(0.0f);
		Le=new Point3D(0.0f);
	}
	
	//costruttore materiale 1: materiali diffusivi e 
	//riflessivi 
	public Material(Point3D Kd_, Point3D Kr_) {
		
		Kd=Kd_;
		Kr=Kr_;
		Kg=new Point3D(0.0f);
		ior=new Point3D(0.0f);
		Le=new Point3D(0.0f);
		refImperfection=0;
	}
	
	//costruttore materiali lucidi (tramite il modello di 
	//Cook-Torrance)
	public Material(Point3D Kd_, Point3D ior_, float slope_) {
		
		Kd=Kd_;
		slope=slope_;
		ior=ior_;
		Le=new Point3D(0.0f);
	}   
	
	//costruttore materiale generico
	public Material(Point3D Kd_, Point3D Kr_, Point3D Kg_,
									Point3D ior_, Point3D k_, float slope_,
									float refImperfection_, boolean translucent_){

        Le=new Point3D(0.0f);
        //normalizzazione: Kd+Kr deve essere <1
        float Ftot= Kd_.max()+Kr_.max();
        if( Ftot>1){
        	Kd=Kd_.divideScalar(Ftot);
        	Kr=Kr_.divideScalar(Ftot);
        }
        else{
        	Kr=Kr_;
        	Kd=Kd_;
        }
        Kg=Kg_;
        ior=ior_;
        k=k_;
        slope=slope_;
        refImperfection=refImperfection_;
        translucent=translucent_;
        
    }
	
	//materiali che emettono luce: Le corrisponde al colore
	//della luce emessa
	public Material( Point3D Be ){
        Kd=new Point3D(0.0f);
        Kr=new Point3D(0.0f);
        Kg=new Point3D(0.0f);
        ior=new Point3D(0.0f);
        Le=Be.multiplyScalar(Utilities.MATH_1_DIV_PI);
    }
    
	//metodo che calcola il coefficente di Fresnel in base  
	//al coseno dell'angolo di incidenza del raggio visuale 
	//con la normale
	public Point3D getFresn(float cos_i){
		Point3D etat=ior;
	    //viene calcolato solamente per materiali con 
		//indice di rifrazione maggiore di 0
	    if(ior.max()>0){
	    	
	    	//calcolo coefficiente di Fresnel: 
	    	//F=(|Rparal|^2+|Rperp|^2)/2
	    	//verifico se il materiale e' un conduttore o 
	    	//un dielettrico tramite il parametro k
	    	if(k.max()<=0){
	    		//formula per materiali dielettrici
	    		
	    		//indice di rifrazione del vuoto
	    		Point3D etai= new Point3D(1.0f);
	        
	    		//si verifica che il coseno tra la normale
	    		//e il raggio entrante nella superficie sia
	    		//maggiore di 0
	    		//se e' minore di 0 si considera il raggio
	    		//come uscente dalla superficie e vengono 
	    		//quindi invertiti gli indici di rifrazione
	    		if(cos_i<0){
	    			etai=etat;
	    			etat=new Point3D(1.0f);
	    			cos_i=-cos_i;
	    		}
	    
	    		//calcolo del coefficienti di Fresnel:
	        
	    		Point3D et=  etai.divideComponents(etat);
	    
	    		Point3D sint2= (et.multiplyComponents(et)).multiplyScalar(
	    				1-cos_i*cos_i);
	    		//se il seno e' maggiore di 1 allora la 
	    		//radice sara' negativa di conseguenza
	    		// si effettuera' una riflessione totale
	    		//ovvero il coefficente di Fresnel deve 
	    		//essere posto uguale ad 1 (solo in questo
	    		//caso infatti tutta la luce viene 
	    		//completamente riflessa) poiche' qui si 
	    		//considerano indici di rifrazioni 
	    		//differenti in base alla lunghezza d'onda
	    		//se la riflessione totale non avviene per
	    		//ogni lunghezza d'onda il metodo continua
	    		//restringendo il seno in tutte le  
	    		//lunghezze d'onda tra 0 e 1. una volta 
	    		//finito il metodo si controllano le 
	    		//lunghezze d'onda per cui c'e' stata 
	    		//riflessione totale e si pone il 
	    		//coefficente di Fresnel per quelle 
	    		//lunghezze d'onda uguale ad 1
	    		if((sint2.x>1)&&(sint2.y>1)&&(sint2.z>1)){
	    			return new Point3D(1.0f) ;
	    		}
	    		// restrizione in [0,1]
	    		Point3D sint2_= Point3D.clamp3(sint2);
	       
	    		//calcolo del coseno
	    		Point3D one=new Point3D(1.0f);
	    		Point3D cos_t= Point3D.getSquareCompPoint(
	    				one.subtract(sint2_));

	    		
	    		//formula per materiali dielettrici
	    		
	    		//Rparal=(eta2cos1-eta1cos2)/(eta2cos1+
	    		//+eta1cos2)
	    		Point3D etatcos_i=etat.multiplyScalar(cos_i);
	    		Point3D Rparal=(etatcos_i.subtract(
	    				etai.multiplyComponents(cos_t))).divideComponents(
	    						etatcos_i.add(
	    								etai.multiplyComponents(cos_t)));
	    		
	    		//Rperp=(eta1cos1-eta2cos2)/(eta1cos1+
	    		//+eta2cos2)
	    		Point3D etaicos_i=etai.multiplyScalar(cos_i);
	    		Point3D Rperp=(etaicos_i.subtract(
	    				etat.multiplyComponents(cos_t))).divideComponents(
	    						etaicos_i.add(
	    								etat.multiplyComponents(cos_t)));
	    		
	    		//F=(|Rparal|^2+|Rperp|^2)/2
	    		Point3D result= (Rparal.multiplyComponents(Rparal).
									add(Rperp.multiplyComponents(Rperp))).
									multiplyScalar(0.5f);
	    		if(result.average()<Utilities.EPSILON)
	    			result=new Point3D(0.0f);
	        
	    		//controllo della riflessione totale su 
	    		//ogni componente RGB
	    		if(sint2.x>1){
	    			result.x=1;
	    		}
	    		if(sint2.y>1){
	    			result.y=1;
	    		}
	    		if(sint2.z>1){
	    			result.z=1;
	    		}
	    		return result;
	          
	    	}
	    	else{
	    		//formula per materiali conduttori
	    		etat=ior;
	    		
	    		//Rparal=((etat^2+k^2)cos1^2-2*etat*cos1+
	    		//+1)/((etat^2+k^2)cos1^2+2*etat*cos1+1)
	    		Point3D tmp= (etat.multiplyComponents(etat)).add(
	    				k.multiplyComponents(k));
	    		Point3D tmp2= tmp.multiplyScalar(cos_i).
									multiplyScalar(cos_i);
	    		Point3D ior2cos_i= etat.multiplyScalar(
	    				2.0f).multiplyScalar(cos_i);
	    		Point3D Rparal_2= (tmp2.subtract(
	    			    ior2cos_i).add(new Point3D(
	    			    1.0f))).divideComponents(tmp2.add(
	    			    ior2cos_i).add(new Point3D(
	    			    1.0f)));
	    		
	    		//Rperp=((etat^2+k^2)-2*etat*cos1+
	    		//+cos1^2)/((etat^2+k^2)+2*etat*cos1+cos1^2)
	    		Point3D cos2i=new Point3D(cos_i*cos_i);
	    		Point3D Rperp_2=(tmp.subtract(
	    				ior2cos_i).add(cos2i)).
									divideComponents(tmp.add(ior2cos_i).
											add(cos2i));
	    		Point3D result= (Rparal_2.add(
	    				Rperp_2)).multiplyScalar(0.5f);
	    		return result;
	        
	    	}
	    }
	    else{
	        //negli altri casi poniamo il coefficente di
	    	//Fresnel uguale a 1
	        return new Point3D(1.0f);
	    }
	        
	}
	
	//BRDF per materiali riflettenti
	Point3D S_BRDF(Point3D Fresn){
		return Fresn.multiplyComponents(Kr);
	}

	//BRDF per materiali trasparenti
	Point3D T_BRDF(Point3D Fresn){
		Point3D one=new Point3D(1.0f);
		return Kg.multiplyComponents(one.subtract(Fresn));
	}
	
	//BRDF di cook e torrance=kd+(F*D*G)/(pigreco*<psi,n>
	//*<teta,n>)
	//psi e' il raggio in ingresso, theta e' il raggio in 
	//uscita, n e' la normale dell'oggetto
    Point3D C_T_BRDF(Ray psi, Ray theta, Point3D n){
    
    	//parte diffusiva
    	Point3D fr= Kd.multiplyScalar(Utilities.MATH_1_DIV_PI);
        
    	//la parte riflettente viene considerata solo se 
    	//lo slope e' stato inizializzato (ovvero e' 
    	//diverso da 0)
    	if(slope!=0){
    		//dati:
            
    		//halfway vector
    		Point3D H= (psi.d.add(theta.d)).getNormalizedPoint();

    		//<psi,H>=<theta,H>
    		float c= psi.d.dotProduct(H);
    		//<n,H>
    		float cNH=n.dotProduct(H);
    		//<psi,n>
    		float cPsiN=psi.d.dotProduct(n);
    		//<teta,n>
    		float cThetaN=theta.d.dotProduct(n);
            
            
    		//calcolo coefficiente di Fresnel
    		// !!! si potrebbe anche fare con il metodo 
    		//getFresn(cos_i) !!!
            
    		Point3D F;
         
    		Point3D ior2=new Point3D(ior.x*ior.x,ior.y*ior.y,
    				ior.z*ior.z);
            
    		Point3D g=ior2.add(new Point3D(c*c-1));
    		g.abs();
            
    		//g+c
    		Point3D c3=new Point3D(c,c,c);
    		Point3D gc= g.add(c3);
    		//g-c
    		Point3D g_c=g.subtract(c3);
        
    		// (g-c)^2/(g+c)^2
    		Point3D a=((g_c).multiplyComponents(g_c)).divideComponents(gc.multiplyComponents(gc));
            
    		//(c*(g+c)-1)/(c*(g+c)+1)
    		Point3D one=new Point3D(1.0f);
    		Point3D b= ((gc.multiplyScalar(c)).subtract(
    				one)).divideComponents((gc.multiplyScalar(c)).
								add(one));
        
    		//F= 1/2 * ((g-c)^2/(g+c)^2) * (1+ ((c*(g+c)-
    		//-1)/(c*(g+c)+1))^2)
    		F = a.multiplyComponents(one.add(b.multiplyComponents(b))).
								multiplyScalar(0.5f);
            
    		
    		//Distribuzione di Beckmann
    		double sNH= Math.sqrt(1-Math.pow(cNH,2));
            
    		double tan= sNH/(cNH*slope);
    		double e= Math.exp(Math.pow(tan,2));
    		double _D = e*(slope*slope*Math.pow(cNH,4));
    		double D= 1/_D;
    		float Df=(float) D;    
    		//fattore geometrico
            
    		double G= 1;
    		if( 2*cPsiN*cNH/c <G){
    			G=2*cPsiN*cNH/c;
    		}
    		if(2*cThetaN*cNH/c<G){
    			G=2*cThetaN*cNH/c;
    		}
    		float Gf=(float) G;
            
    		//modello di Cook-Torrance
    		fr=fr.add(F.multiplyScalar(Df*Gf*
    				Utilities.MATH_1_DIV_PI *1/(cPsiN*cThetaN)));
        }
        
    return fr;
    }
    

    //metodo empirico (non fotorealistico) per avere
    //un valore per la BSSRDF
    //Fpsi e Ftheta sono i coefficente di Fresnel passati 
    //come parametro, calcolati nel main considerando 
    //come psi il raggio in ingresso e theta quello in 
    //uscita
    Point3D BSSRDF(Point3D Fpsi, Point3D Ftheta) {
	
    	Point3D result;
    	Point3D one=new Point3D(1.0f);
    	
    	float zv=0.005f;
    	float dv;//=0.0125f;
    	float zr=0.0025f;
    	float dr;//=0.01030f;
    	dv=(float) (Math.random() * (150-90)+90);
    	dv=dv/10000f;
    	float l2=dv*dv-zv*zv;
    	dr=(float) Math.sqrt(l2+zr*zr);
    	//System.out.println("dv+ "+dv);
    	//pi4=4*3.14
    	Point3D pi4=new Point3D(4*Utilities.MATH_PI);
    	//i valori si sigmas e sigmaa sono specifici per 
    	//la giada
    	Point3D sigmas=new Point3D(0.657f,0.786f,0.9f);
    	Point3D sigmaa=new Point3D(0.2679f,0.3244f,0.1744f);
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
    	
    	result=(Rd.multiplyComponents(Fpsi).multiplyComponents(Ftheta)).
							divideScalar(Utilities.MATH_PI);
    	return result;
    }
    
    public static void main(String[] args) {
    	
	}
	

}
