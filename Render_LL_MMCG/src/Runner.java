import java.util.ArrayList;

public class Runner {
  private boolean isRadiance;
  private Camera cam;
  private Renderer renderer;

  Runner(boolean isRadiance, Camera cam, Renderer renderer) {
    this.isRadiance = isRadiance;
    this.cam = cam;
    this.renderer = renderer;

    execute();
  }

  private void execute() {
    int y = 0;
    ArrayList<ParallelProcess> parallelisation = new ArrayList<>();

    int threads = Runtime.getRuntime().availableProcessors();

    for (int c = 0; c < threads; c++) {
      if (isRadiance) {
        parallelisation.add(new ParallelProcessRadiance(y, cam, renderer));
      } else {
        parallelisation.add(new ParallelProcessImage(y));
      }
    }

    while (y < RenderAction.h){
      for (int i = 0; i < parallelisation.size(); i++) {
        if (!parallelisation.get(i).getStatus()) {
          y++;

          try {
            synchronized (Runner.this) {
              if (isRadiance) {
                parallelisation.set(i, new ParallelProcessRadiance(y, cam, renderer));
              } else {
                parallelisation.set(i, new ParallelProcessImage(y));
              }

              parallelisation.get(i).start();
            }
          } catch (IllegalThreadStateException e) {

          }
        }
      }
    }
  }
}
