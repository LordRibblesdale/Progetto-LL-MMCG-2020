package ui;

import javax.swing.*;
import java.awt.*;

public class FinalGatheringPanel extends JPanel {
  //campioni (numero di raggi) per l'illuminazione indiretta
  //(ricorsivo in global illumination, non ricorsivo in
  //final gathering)
  private JSpinner aoSamples;

  //campioni (numero di raggi) illuminazione diretta (non ricorsivo)
  private JSpinner dirSamples;

  //campioni scelti per le riflessioni e le rifrazioni
  private JSpinner refSamples;

  private JCheckBox jacobiCheck;

  FinalGatheringPanel() {
    super(new GridLayout(0, 1));

    aoSamples = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    dirSamples = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    refSamples = new JSpinner(new SpinnerNumberModel(75, 1, 1000, 1));
    jacobiCheck = new JCheckBox("Sperimentale: aggiunta di Jacobi al processo");
    jacobiCheck.setSelected(false);

    JPanel panel = new JPanel();
    panel.add(new JLabel("Campioni per pixel, illuminazione diretta"));
    panel.add(dirSamples);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Campioni per pixel, illuminazione indiretta"));
    panel.add(aoSamples);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Campioni per pixel, riflessioni e rifrazioni"));
    panel.add(refSamples);
    add(panel);

    add(jacobiCheck);
  }

  int getAOSamples() {
    return (Integer) aoSamples.getValue();
  }

  int getDirSamples() {
    return (Integer) dirSamples.getValue();
  }

  int getRefSamples() {
    return (Integer) refSamples.getValue();
  }

  boolean getJacobiCheck() {
    return jacobiCheck.isSelected();
  }

  @Override
  public void setEnabled(boolean isEnabled) {
    super.setEnabled(isEnabled);

    jacobiCheck.setSelected(isEnabled);
    aoSamples.setEnabled(isEnabled);
    dirSamples.setEnabled(isEnabled);
    refSamples.setEnabled(isEnabled);
  }
}
