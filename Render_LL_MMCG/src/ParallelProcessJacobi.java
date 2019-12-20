public class ParallelProcessJacobi extends Thread {
  private boolean status = true;

  private Point3D[] Pr;
  private Point3D[] P;
  private Point3D Prtot;
  private Obj[] objX;
  private int nObj;

  ParallelProcessJacobi(Point3D Prtot, int nObj, Point3D[] Pr, Point3D[] P, Obj[] objX) {
    this.Prtot = Prtot;
    this.nObj = nObj;
    this.Pr = Pr;
    this.objX = objX;
    this.P = P;

    start();
  }

  @Override
  public void run() {
    status = false;
  }

  public boolean getStatus() {
    return status;
  }
}
