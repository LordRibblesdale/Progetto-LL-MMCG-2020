package renderer;

import primitive.Camera;
import ui.RenderAction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* La classe renderer.Runner è adibita alla gestione
 *  del multithreading del rendering
 * Il rendering viene suddiviso per le righe dell'immagine,
 *  creando un Runnable per ogni riga, nel quale per ogn
 *  pixel della riga avviene il rendering.
 * Il tutto è gestito tramite le pools, dalle librerie di Java
 * Creando i threads, si fa avviare ogni thread con .execute()
 *  e si attende il termine del rendering con
 *  .awaitTermination().
 * .newWorkStealingPool() crea i thread in base ai threads
 *  disponibili determinati dal sistema operativo
 */

class Runner {
  private Camera cam;

  Runner(Camera cam) {
    this.cam = cam;

    execute();
  }

  private void execute() {
    ExecutorService pool = Executors.newWorkStealingPool();

    for (int y = 0; y < RenderAction.height; y++) {
      pool.execute(
          new ParallelProcessRadiance(y, cam));
    }

    pool.shutdown();

    try {
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
