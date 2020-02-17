import javax.swing.*;
import java.awt.*;

public class JacobiPanel extends JPanel {
  //sample per lo Stochastic Jacobi, utilizzati per il calcolo con Monte Carlo
  private JSpinner samples;

  //step massimi per le iterazioni di Jacobi Stocastico
  private JSpinner maxSteps;

  //errore massimo nel processo di Jacobi Stocastico
  private JSpinner maxErr;

  JacobiPanel() {
    super(new GridLayout(0, 1));

    samples = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
    maxSteps = new JSpinner(new SpinnerNumberModel(15, 1, 50, 1));
    maxErr = new JSpinner(new SpinnerNumberModel(3, 0, 10, 1));

    JPanel panel = new JPanel();
    panel.add(new JLabel("Campioni per pixel (raggi)"));
    panel.add(samples);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Numero di iterazioni"));
    panel.add(maxSteps);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Errore massimo (potenza negativa di 10)"));
    panel.add(maxErr);
    add(panel);
  }

  int getSamps() {
    return (Integer) samples.getValue();
  }

  int getMaxSteps() {
    return (Integer) maxSteps.getValue();
  }

  double getMaxErr() {
    return Math.pow(10, -(Integer) maxErr.getValue());
  }

  @Override
  public void setEnabled(boolean isEnabled) {
    super.setEnabled(isEnabled);

    samples.setEnabled(isEnabled);
    maxSteps.setEnabled(isEnabled);
    maxErr.setEnabled(isEnabled);
  }
}
