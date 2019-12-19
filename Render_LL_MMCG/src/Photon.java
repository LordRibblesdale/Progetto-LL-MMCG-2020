//sistemare i commenti
public class Photon {

	public Point3D position=new Point3D();
	public Point3D power=new Point3D();
	public Point3D direction=new Point3D();
	    
	public Photon(Point3D ip, Point3D d, Point3D p){
		position.copy(ip);
	    power.copy(p);
	    direction.copy(d);
	}

}
