public class Hourglass {
  // raggio
  public float rad;
  // posizione (centro)
  public Point3D p;

  //costruttore
  public Hourglass(float nrad, Point3D np) {
    rad=nrad;
    p=np;
  }

  Point3D normal(Point3D iP) {
    double phi = Math.acos(iP.x);

    double senPhi = Math.sin(phi);
    double cosPhi = Math.cos(phi);

    double rad10 = rad/(double) 10;
    double multiplier = (rad10 + cosPhi*(rad - rad10));

    double theta = Math.acos(iP.y / (senPhi*multiplier));

    double senTheta = Math.sin(theta);
    double cosTheta = Math.cos(theta);

    Point3D derPhiFormula = new Point3D(-rad*senPhi, cosPhi*cosTheta*multiplier, cosPhi*senTheta*multiplier);
    Point3D derThetaFormula = new Point3D(0, -senPhi*senTheta*multiplier, senPhi*cosTheta*multiplier);

    return derPhiFormula.crossProduct(derThetaFormula);
  }

  double intersect(Ray r) {
    Point3D op = p.subtract(r.o);
    double t;

    return 0; //TODO ??
  }
}
