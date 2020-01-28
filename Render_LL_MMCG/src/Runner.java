import java.util.ArrayList;

public class Runner {
  private Camera cam;
  private Renderer renderer;

  Runner(Camera cam, Renderer renderer) {
    this.cam = cam;
    this.renderer = renderer;

    execute();
  }

  private void execute() {
    int y = 0;
    ArrayList<Thread> parallelisation = new ArrayList<>();

    int threads = Runtime.getRuntime().availableProcessors();

    for (int c = 0; c < threads; c++) {
      parallelisation.add(new Thread(new ParallelProcessRadiance(y, cam, renderer, new Utilities())));
      parallelisation.get(parallelisation.size()-1).start();

      synchronized (Runner.this) {
        try {
          wait(50);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    while (y < RenderAction.h){
      for (int i = 0; i < parallelisation.size(); i++) {
        if (!parallelisation.get(i).isAlive()) {
          parallelisation.get(i).interrupt();
          y++;

          try {
            synchronized (Runner.this) {
              parallelisation.set(i, new Thread(new ParallelProcessRadiance(y, cam, renderer, new Utilities())));
              parallelisation.get(i).start();

              wait(50);
            }
          } catch (IllegalThreadStateException | InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
