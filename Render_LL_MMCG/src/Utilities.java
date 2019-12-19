//questa classe "di appoggio" contiene alcune variabili 
//utilizzate nel main (da non modificare) e alcune funzioni 
//matematiche
public class Utilities {

	//DEFINIZIONE DI VARIABILI
	
	static final float EPSILON = 1.e-2f;
	static final float MATH_PI = 3.14159265358979323846f;
	//1 su pi greco
	static final float MATH_1_DIV_PI = 0.318309886183790671538f;
	static final float MATH_1_DIV_180 = 0.005555555690079927f;

	//massima ricorsivita' del ray tracing 
	static int MAX_DEPTH=100;

	//massima ricorsivita' del photon mapping
	static int MAX_DEPTH_PHOTON=100000;

	//massima ricorsivita' del ray tracing
	static int MAX_DEPTH_CAUSTIC=100;

	//DEFINIZIONE DI FUNZIONI MATEMATICHE
	
	//funzione da gradi a radianti
	public float degreesToRadiants(float deg) {
		return deg* MATH_PI * MATH_1_DIV_180;
	}

	//funzione da radianti a gradi 
	public float radiantsToDegrees(float rad) {
		return rad*180.0f* MATH_1_DIV_PI;
	}

	//costringe un numero in [0,1]
	static float clamp(float x){
		return x<0 ? 0 : x>1 ? 1 : x;
	}

	//Transforma un float in [0,1], in un intero in [0,255]
	//Si utilizza con una gamma di 2.2, ovvero il numero 
	//viene portato a [0,1] dopodiche' viene elevato alla 
	//2.2: in questo modo il valore 0,218 si avvicina allo
	//0,5 ovvero viene portato a meta' della gamma dando 
	//piu' spazio ai colori scuri, i quali creano sempre 
	//piu' problemmi. Moltiplicando infine per 255 si 
	//estende per il range di bit preso in esame e la 
	//funzione ritorna l'approssimazione ad intero
	static int toInt(float x){
		return (int) (Math.pow(clamp(x), 1/2.2)*255);
	}
}
