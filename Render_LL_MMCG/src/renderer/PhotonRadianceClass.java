package renderer;

import partition.PhotonBox;
import primitive.Obj;
import primitive.Photon;
import primitive.Point3D;
import primitive.Ray;
import ui.RenderAction;

import java.util.Hashtable;
import java.util.Map;

public class PhotonRadianceClass {
  /* Metodo che gestisce l'energia uscente dalla superficie tramite il Photon Mapping
   */
  static Point3D photonRadiance(Ray r, Obj objX, PhotonBox[] Tree, double photonSearchDisc, int nph) {
    Utilities utilities = new Utilities();

    Point3D radianceOutput = new Point3D();

    //carico l'ID del materiale
    int matId= objX.matId;

    //carico la normale dell'oggetto
    Point3D n1= objX.normal(r.o);

    //fotoni trovati nelle vicinanze del punto in esame

    Hashtable<Double, Photon> nearPh = new Hashtable<>();

    utilities.locatePhotons(nearPh,r.o,1,objX,Tree, photonSearchDisc,nph);

    //per ogni fotone trovato
    for (Map.Entry<Double, Photon> entry : nearPh.entrySet()) {
      //raggio di entrata del fotone
      Ray psi= new Ray(r.o, entry.getValue().direction);

      //calcolo della BRDF
      Point3D BRDF = RenderAction.material[matId].C_T_BRDF(psi,r,n1);

      //distanza del fotone dal punto
      double dist = entry.getKey();

      double W=1-(Math.sqrt(dist)/((1.1)*Math.sqrt(photonSearchDisc)));
      //stima della radianza nel punto
      radianceOutput = radianceOutput.add(BRDF.multiplyComponents(entry.getValue().power).multiplyScalar(Utilities.MATH_PI).multiplyScalar(1/ photonSearchDisc).multiplyScalar(W));
    }

    return radianceOutput;
  }
}
