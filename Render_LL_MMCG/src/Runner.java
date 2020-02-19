import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Runner {
  private Camera cam;

  Runner(Camera cam) {
    this.cam = cam;

    execute();
  }

  private void execute() {
    ExecutorService pool = Executors.newWorkStealingPool();

    for (int y = 0; y < RenderAction.h; y++) {
      pool.execute(new ParallelProcessRadiance(y, cam, new Renderer(new Utilities())));
    }

    pool.shutdown();

    try {
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
