public class ParallelProcessImage extends Thread implements ParallelProcess {
  private boolean status = true;

  private int y;

  ParallelProcessImage(int y) {
    this.y = y;

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
