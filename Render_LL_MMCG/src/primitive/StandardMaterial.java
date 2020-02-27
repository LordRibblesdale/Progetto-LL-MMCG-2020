package primitive;

public interface StandardMaterial {
  /* Interfaccia per la selezione di materiali
   * Cruciale per la scelta di un materiale senza doverlo ricreare e nella selezione del materiale dal modellatore
   */
  Material MATERIAL_LIGHT_WHITE = new Material(new Point3D(5.0f), "MATERIAL_LIGHT_WHITE");

  Material MATERIAL_DIFFUSIVE_RED = new Material(
      new Point3D(0.1f,0.0f,0.0f),
      new Point3D(), "MATERIAL_DIFFUSIVE_RED");
  Material MATERIAL_DIFFUSIVE_GREEN = new Material(
      new Point3D(0.05f,0.3f,0.0f),
      new Point3D(), "MATERIAL_DIFFUSIVE_GREEN");
  Material MATERIAL_DIFFUSIVE_BLUE = new Material(
      new Point3D(0.45f,0.45f,1.0f),
      new Point3D(), "MATERIAL_DIFFUSIVE_BLUE");
  Material MATERIAL_DIFFUSIVE_GRAY = new Material(
      new Point3D(0.7f),
      new Point3D(), "MATERIAL_DIFFUSIVE_GRAY");
  Material MATERIAL_DIFFUSIVE_BLACK = new Material(
      new Point3D(),
      new Point3D(), "MATERIAL_DIFFUSIVE_BLACK");
  Material MATERIAL_DIFFUSIVE_PINK = new Material(
      new Point3D(1.0f,0.4f,0.4f),
      new Point3D(), "MATERIAL_DIFFUSIVE_PINK");
  Material MATERIAL_DIFFUSIVE_DEEP_GRAY = new Material(
      new Point3D(0.2f,0.15f,0.15f),
      new Point3D(), "MATERIAL_DIFFUSIVE_DEEP_GRAY");

  Material MATERIAL_REFLECTIVE_GLASS = new Material(
      new Point3D(),
      new Point3D(1.0f), "MATERIAL_REFLECTIVE_GLASS");
  Material MATERIAL_REFLECTIVE_PERFECT_GLASS = new Material(
      new Point3D(),
      new Point3D(),
      new Point3D(1.0f),
      new Point3D(1.55f),
      new Point3D(),
      0.0f,0.0f,false, "MATERIAL_REFLECTIVE_PERFECT_GLASS");
  Material MATERIAL_COOK_TORRANCE_VIOLET = new Material(
      new Point3D(0.6f,0.1f,0.2f),
      new Point3D(5.3f,1.485f,1.485f),
      0.9f, "MATERIAL_COOK_TORRANCE_VIOLET");
  Material MATERIAL_STEEL = new Material(
      new Point3D(),
      new Point3D(1.0f),
      new Point3D(),
      new Point3D(2.485f),
      new Point3D(3.433f),
      0.0f ,0.0f,false, "MATERIAL_STEEL");
  Material MATERIAL_IMPERFECT_STEEL = new Material(
      new Point3D(),
      new Point3D(1.0f),
      new Point3D(),
      new Point3D(1.485f,2.885f,2.885f),
      new Point3D(3.433f,1.433f,1.433f),0.0f,0.01f, false, "MATERIAL_IMPERFECT_STEEL");
  Material MATERIAL_DEEP_RED = new Material(
      new Point3D(0.5f,0.12f,0.2f),
      new Point3D(), "MATERIAL_DEEP_RED");
  Material MATERIAL_TRANSLUCENT_JADE = new Material(
      new Point3D(0.31f,0.65f,0.246f),
      new Point3D(),
      new Point3D(),
      new Point3D(1.3f,1.3f,1.3f),
      new Point3D(),
      0,0,true, "MATERIAL_TRANSLUCENT_JADE");
  Material MATERIAL_DIFFUSIVE_JADE = new Material(
      new Point3D(0.31f,0.65f,0.246f),
      new Point3D(),
      new Point3D(),
      new Point3D(1.3f,1.3f,1.3f),
      new Point3D(),
      0,0,false, "MATERIAL_DIFFUSIVE_JADE");
}
