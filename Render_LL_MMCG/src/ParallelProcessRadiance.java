public class ParallelProcessRadiance extends Thread implements ParallelProcess {
  private boolean status = true;

  private int y;
  private Camera cam;

  ParallelProcessRadiance(int y, Camera cam) {
    this.y = y;
    this.cam = cam;

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
