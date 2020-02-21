public interface Properties {
  /* Interfaccia che contiene tutte le opzioni scelte nel Main e in EditPanel
   *  per un utilizzo più pratico tra le classi
   */
  int JACOBI_PANEL = 0;
  int FINAL_GATHERING_PANEL = 1;
  int PHOTON_PANEL = 2;

  String TRANSLUCENT_JADE = "Giada Realistica (traslucente)";
  String DIFFUSIVE_JADE = "Giada Diffusiva";
  String GLASS = "Vetro";

  String[] MATERIALS = {
      TRANSLUCENT_JADE,
      DIFFUSIVE_JADE,
      GLASS
  };

  String ALIGNED = "Sfere allineate";
  String OVERLAPPED = "Sfere sovrapposte";

  String[] POSITIONS = {
      OVERLAPPED,
      ALIGNED
  };
}
