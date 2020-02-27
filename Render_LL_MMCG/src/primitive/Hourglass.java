package primitive;

import primitive.Point3D;
import primitive.Ray;

public class Hourglass {
  // raggio
  public float rad;
  // posizione (centro)
  public Point3D p;

  int matId;

  //costruttore
  public Hourglass(float nrad, Point3D np, int matId) {
    rad=nrad;
    p=np;

    this.matId = matId;
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

    Point3D derPhiFormula = new Point3D(cosPhi*cosTheta*multiplier, cosPhi*senTheta*multiplier, -rad*senPhi);
    Point3D derThetaFormula = new Point3D(-senPhi*senTheta*multiplier, senPhi*cosTheta*multiplier, 0);

    return derPhiFormula.crossProduct(derThetaFormula);
  }

  double intersect(Ray r) {
    Point3D op = p.subtract(r.o);
    double t;

    return 0; //TODO ??
  }
}
