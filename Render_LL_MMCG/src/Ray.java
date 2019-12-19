//classe che definisce un raggio attraverso i parametri
//origine, direzione e profondita'
public class Ray {

	// Origine del raggio
	public Point3D o;
	// Direzione del raggio
	public Point3D d;
	// Profondita' del raggio
	public float depth;
		
	public Ray() {
		o = new Point3D();
		d = new Point3D();
		depth = 0.0f;
	}
	
	public Ray(Point3D or, Point3D di) {
		o = or;
		d = di;
		depth = 0.0f;
	}
	
	public void setDepth(float newDepth)
	{
		depth=newDepth;
	}


}
