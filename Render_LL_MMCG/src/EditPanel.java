import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class EditPanel extends JPanel {
  private JButton renderButton;

  private JPanel centralPanel;
  private JPanel bottomPanel;
  private JTabbedPane tabs = null;
  private JComboBox<String> materials;
  private JComboBox<String> positions;

  // Sample per il rendering dell'immagine
  private JSpinner samples;

  JacobiPanel jacobiPanel;
  FinalGatheringPanel finalGatheringPanel;
  PhotonPanel photonPanel;

  /* EditPanel è un pannello che contiene un JTabbedPane in cui sono presenti le 3 tecniche di rendering
   *  implementate. Per ognuna è presente il suo pannello con i suoi parametri.
   * RenderAction, con accesso statico, controllerà quale tab è scelto e quali valori dai vari
   *  JSpinner (e successivamente JSlider) richiedere per impostare il rendering effettivo
   */

  EditPanel() {
    super();
    setLayout(new BorderLayout());

    bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomPanel.add(new JLabel("Materiali: "));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(materials = new JComboBox<>(Properties.MATERIALS));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(new JLabel("Posizione sfere: "));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(positions = new JComboBox<>(Properties.POSITIONS));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(renderButton = new JButton("Render!"));

    initialise();

    renderButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            new RenderAction(true);
          }
        }).start();
      }
    });

    add(centralPanel);
    add(bottomPanel, BorderLayout.PAGE_END);
  }

  private void initialise() {
    centralPanel = new JPanel(new BorderLayout());

    tabs = new JTabbedPane();

    tabs.add(jacobiPanel = new JacobiPanel(), "Jacobi");
    tabs.add(finalGatheringPanel = new FinalGatheringPanel(), "Final Gathering");
    tabs.add(photonPanel = new PhotonPanel(), "Photon Mapping");

    centralPanel.add(tabs);

    samples = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));

    JPanel panel = new JPanel();
    panel.add(new JLabel("Campioni per pixel (raggi)"));
    panel.add(samples);
    centralPanel.add(panel, BorderLayout.PAGE_END);
  }

  int getMethod() {
    return tabs.getSelectedIndex();
  }

  String getMaterial() {
    return materials.getItemAt(materials.getSelectedIndex());
  }

  String getPosition() {
    return positions.getItemAt(positions.getSelectedIndex());
  }

  void setUI(boolean isEnabled) {
    this.setEnabled(isEnabled);

    samples.setEnabled(isEnabled);
    jacobiPanel.setEnabled(isEnabled);
    finalGatheringPanel.setEnabled(isEnabled);
    photonPanel.setEnabled(isEnabled);
  }

  @Override
  public void setEnabled(boolean isEnabled) {
    super.setEnabled(isEnabled);

    materials.setEnabled(isEnabled);
    positions.setEnabled(isEnabled);
    tabs.setEnabled(isEnabled);
    renderButton.setEnabled(isEnabled);
  }

  int getSamps() {
    return (Integer) samples.getValue();
  }
}
