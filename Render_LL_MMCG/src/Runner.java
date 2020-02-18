import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Runner {
  private Camera cam;

  Runner(Camera cam) {
    this.cam = cam;

    execute();
  }

  private void execute() {
    //ExecutorService pool = Executors.newCachedThreadPool();
    ExecutorService pool = Executors.newWorkStealingPool();

    for (int y = 0; y < RenderAction.h; y++) {
      pool.execute(new ParallelProcessRadiance(y, cam, new Renderer(new Utilities())));
    }

    pool.shutdown();

    while (!pool.isTerminated()) {

    }

    /*
    int y = 0;

    ArrayList<Thread> parallelisation = new ArrayList<>();

    int threads =
        Runtime.getRuntime().availableProcessors() == 1 || Runtime.getRuntime().availableProcessors() == 2
            ? 1
            : Runtime.getRuntime().availableProcessors() - 1;

    System.out.println(threads);

    for (int c = 0; c < threads; c++) {
      parallelisation.add(new Thread(new ParallelProcessRadiance(y++, cam, new Renderer(new Utilities()))));
      parallelisation.get(parallelisation.size()-1).start();
    }

    while (y < RenderAction.h) {
      for (int i = 0; i < parallelisation.size(); i++) {
        if (!parallelisation.get(i).isAlive()) {
          parallelisation.get(i).interrupt();

          try {
            synchronized (Runner.this) {
              parallelisation.set(i, new Thread(new ParallelProcessRadiance(++y, cam, new Renderer(new Utilities()))));
              parallelisation.get(i).start();

            }
          } catch (IllegalThreadStateException e) {
            e.printStackTrace();
          }
        }
      }
    }

    try {
      synchronized (Runner.this) {
        wait(250);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    */
  }
}
