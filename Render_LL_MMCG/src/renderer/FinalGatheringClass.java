package renderer;

import primitive.Obj;
import primitive.Point3D;
import primitive.Ray;

import static renderer.DirectIlluminationClass.directIllumination;
import static renderer.EmittedObjRadianceClass.emittedObjRadiance;
import static renderer.FinalIndirectClass.finalIndirect;

public class FinalGatheringClass {
  //metodo per il Final Gathering, che si serve di una sola
  //iterazione del raytracing stocastico, nella quale si
  //raccolgono (gathering) le informazioni ottenute dalla
  //soluzione precalcolata di radiosita', attraverso il
  //metodo jacobi stocastico)
  //il valore restituito tiene conto dell'illuminazione
  //generata dall'oggetto, del contributo dell'illuminazione
  //diretta e di quello dell'illuminazione indiretta
  static Point3D finalGathering(Ray viewRay, int x, int y, Obj o) {
    // Variabile per la radianza
    Point3D radianceOutput = new Point3D();

    // Illuminazione emessa
    Point3D le= emittedObjRadiance(o);
    radianceOutput = radianceOutput.add(le);

    // Illuminazione diretta
    Point3D di= directIllumination(viewRay, o, x, y);
    radianceOutput = radianceOutput.add(di);

    // Illuminazione indiretta
    // Nota: l'illuminazione non è ricorsiva quindi solo un raggio con più sample saranno inviati
    Point3D fi= finalIndirect(viewRay, o, x, y);
    radianceOutput = radianceOutput.add(fi);

    return radianceOutput;
  }
}
