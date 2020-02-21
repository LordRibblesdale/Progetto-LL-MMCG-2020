/* Classe per la creazione di un fotone, con una posizione di partenza, l'energia del fotone e la sua direzione.
 * Il fotone verrà poi gestito nella PhotonBox, ovvero nella suddivisione dello spazio secondo KDTree per gestirne
 *  la proiezione e calcolare l'illuminazione
 */
class Photon {
	Point3D position=new Point3D();
	Point3D power=new Point3D();
	Point3D direction=new Point3D();
	    
	Photon(Point3D ip, Point3D d, Point3D p){
		position.copy(ip);
		power.copy(p);
		direction.copy(d);
	}
}
