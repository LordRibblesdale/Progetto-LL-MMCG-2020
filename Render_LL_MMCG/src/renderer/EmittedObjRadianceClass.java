package renderer;

import primitive.Obj;
import primitive.Point3D;
import ui.RenderAction;

public class EmittedObjRadianceClass {
  // Metodo che restituisce la radianza emessa dall'oggetto o in direzione r:
  static Point3D emittedObjRadiance(Obj o) {
    //carico l'indice del material
    int mId = o.matId;

    //dichiato e inizializzo a 0 il valore in uscita
    Point3D radianceOutput = new Point3D();

    //con il seguente if si controlla se il materiale
    //emette effettivamente luce
    if (RenderAction.material[mId].emittedLight.max() > 0) {
      double Ler = RenderAction.material[mId].emittedLight.max();
      Point3D Ler3 = new Point3D(Ler);
      //con il metodo clamp3 si evita che la radianza in
      //uscita superi il valore massimo di radianza: 1
      Point3D.clamp3(Ler3);
      radianceOutput = Ler3;
    }
    return radianceOutput;
  }
}
